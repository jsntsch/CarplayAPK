import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编译器级 Java 类改名工具。
 *
 * 为什么不用正则改引用：jadx 产物里混淆类名（a / b / b0 ...）与字段名、局部变量名
 * 大量同名，纯文本替换必然误伤（历史教训：`this.b.a(...)` 被改成
 * `this.b.VerifyResult(...)`）。这里改用 javac 语义分析：
 *   - 对每个 IdentifierTree / MemberSelectTree 调 Trees.getElement()，
 *     只有「解析出来确实是目标 ClassSymbol」的那一个标识符才改写；
 *   - 类声明名（ClassTree 的 simple name 不是独立 Tree 节点）用
 *     findDeclNamePos() 在声明头部定位，并校验取到的就是目标名。
 *
 * 用法：
 *   java RenameTool \
 *     --cp <classpath>            # 必填，如 android.jar
 *     --map <map.tsv>             # 必填，每行 `oldFqKey<TAB>NewSimpleName`
 *                                 # oldFqKey 形如 io.carplay.app.secured.i0$b
 *     --files <files.txt>         # 必填，每行一个 .java 绝对路径
 *     [--apply]                   # 不给则只 dry-run 统计
 *     [--report <report.tsv>]     # 每个目标的命中数明细
 *
 * 语义名字符集只允许 [A-Za-z_$][A-Za-z0-9_$]*，避免注入。
 */
public final class RenameTool {

    private static final Pattern IDENT =
            Pattern.compile("\\b(class|interface|enum|record)\\s+([A-Za-z_$][A-Za-z0-9_$]*)");
    private static final Pattern VALID_NEW = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

    /** oldFqKey -> new simple name */
    private final Map<String, String> renameMap = new LinkedHashMap<>();
    /** 统计：newName -> 引用改写次数 */
    private final Map<String, Integer> refHits = new LinkedHashMap<>();
    /** 统计：newName -> 声明改写次数 */
    private final Map<String, Integer> declHits = new LinkedHashMap<>();
    /** 未能解析成符号、但文本里出现的完全限定名残留 */
    private final Map<String, Integer> unresolved = new LinkedHashMap<>();
    /** --fix-ctors：把「无返回类型但名字≠类名」的破损构造器改回类名 */
    private boolean fixCtors = false;

    public static void main(String[] args) throws Exception {
        Map<String, String> opts = parseArgs(args);
        RenameTool tool = new RenameTool();
        tool.loadMap(new File(opts.get("--map")));
        List<File> files = loadFileList(new File(opts.get("--files")));
        boolean apply = opts.containsKey("--apply");
        tool.fixCtors = opts.containsKey("--fix-ctors");
        String cp = opts.get("--cp");

        int changed = tool.run(files, cp, apply);

        // 报告
        PrintWriter out = new PrintWriter(System.out, true);
        out.println("files_scanned=" + files.size());
        out.println("targets=" + tool.renameMap.size());
        int totalRef = 0, totalDecl = 0;
        for (String newName : tool.renameMap.values()) {
            int r = tool.refHits.getOrDefault(newName, 0);
            int d = tool.declHits.getOrDefault(newName, 0);
            totalRef += r;
            totalDecl += d;
            out.println(String.format("  %-32s refs=%-5d decl=%d", newName, r, d));
        }
        out.println("total_ref_edits=" + totalRef);
        out.println("total_decl_edits=" + totalDecl);
        out.println("files_changed=" + changed);
        out.println("mode=" + (apply ? "APPLY" : "DRY-RUN"));
        if (opts.containsKey("--report")) {
            try (PrintWriter rp = new PrintWriter(opts.get("--report"), "UTF-8")) {
                rp.println("new_name\trefs\tdecls");
                for (String newName : tool.renameMap.values()) {
                    rp.println(newName + "\t" + tool.refHits.getOrDefault(newName, 0)
                            + "\t" + tool.declHits.getOrDefault(newName, 0));
                }
            }
        }
        // 硬失败：有目标一次都没命中，说明名字表写错了
        List<String> zero = new ArrayList<>();
        for (String newName : tool.renameMap.values()) {
            if (tool.refHits.getOrDefault(newName, 0) == 0
                    && tool.declHits.getOrDefault(newName, 0) == 0) {
                zero.add(newName);
            }
        }
        if (!zero.isEmpty()) {
            out.println("WARN_zero_hit=" + zero);
        }
        if (!apply && changed > 0) {
            out.println("(dry-run: 未写盘)");
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("--apply") || a.equals("--fix-ctors")) {
                m.put(a, "1");          // 布尔开关，不吞下一个参数
            } else if (a.startsWith("--")) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("缺少参数值: " + a);
                }
                m.put(a, args[++i]);
            }
        }
        for (String req : new String[]{"--cp", "--map", "--files"}) {
            if (!m.containsKey(req)) {
                throw new IllegalArgumentException("必填参数缺失: " + req);
            }
        }
        return m;
    }

    private void loadMap(File f) throws IOException {
        Charset cs = StandardCharsets.UTF_8;
        for (String line : Files.readAllLines(f.toPath(), cs)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int tab = line.indexOf('\t');
            if (tab < 0) {
                throw new IllegalArgumentException("map 行格式错误(需 TAB 分隔): " + line);
            }
            String key = line.substring(0, tab).trim();
            String newName = line.substring(tab + 1).trim();
            if (!VALID_NEW.matcher(newName).matches()) {
                throw new IllegalArgumentException("非法 Java 标识符: " + newName);
            }
            renameMap.put(key, newName);
            refHits.put(newName, 0);
            declHits.put(newName, 0);
        }
    }

    private static List<File> loadFileList(File f) throws IOException {
        List<File> list = new ArrayList<>();
        for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8)) {
            line = line.trim();
            if (!line.isEmpty()) {
                list.add(new File(line));
            }
        }
        return list;
    }

    /** 返回被修改的文件数。 */
    private int run(List<File> files, String cp, boolean apply) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("没有 javac，请用 JDK 而不是 JRE 运行");
        }
        StandardJavaFileManager fm =
                compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        Iterable<? extends JavaFileObject> units = fm.getJavaFileObjectsFromFiles(files);

        List<String> options = Arrays.asList(
                "-proc:none", "-nowarn", "-encoding", "UTF-8", "-cp", cp, "-Xmaxerrs", "10000");
        // 用 DiagnosticCollector 吞掉 javac 的告警（AccessController 过时之类），
        // 避免污染工具输出
        JavacTask task = (JavacTask) compiler.getTask(null, fm,
                new javax.tools.DiagnosticCollector<JavaFileObject>(), options, null, units);

        List<CompilationUnitTree> cus = new ArrayList<>();
        for (CompilationUnitTree cu : task.parse()) {
            cus.add(cu);
        }
        task.analyze();

        Trees trees = Trees.instance(task);
        SourcePositions sp = trees.getSourcePositions();

        int changedFiles = 0;
        for (CompilationUnitTree cu : cus) {
            JavaFileObject src = cu.getSourceFile();
            String text = readText(src);
            if (text == null) {
                continue;
            }
            List<int[]> spans = new ArrayList<>();   // {start, end, newNameIdx}
            List<String> newNames = new ArrayList<>();

            new Scanner(trees, sp, cu, spans, newNames, text).scan(cu, null);
            collectDecls(trees, cu, sp, text, spans, newNames);

            if (spans.isEmpty()) {
                continue;
            }
            // 去重 + 按位置倒序
            List<int[]> uniq = dedup(spans);
            uniq.sort((x, y) -> Integer.compare(y[0], x[0]));
            StringBuilder sb = new StringBuilder(text);
            for (int[] e : uniq) {
                String nn = newNames.get(e[2]);
                sb.replace(e[0], e[1], nn);
                // 统计
                if (e[3] == 1) {
                    refHits.merge(nn, 1, Integer::sum);
                } else {
                    declHits.merge(nn, 1, Integer::sum);
                }
            }
            changedFiles++;
            if (apply) {
                Path p = Paths.get(src.toUri());
                Files.write(p, sb.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        fm.close();
        return changedFiles;
    }

    private static List<int[]> dedup(List<int[]> spans) {
        // spans 元素: {start, end, newNameIdx, kind(0=decl,1=ref)}
        List<int[]> out = new ArrayList<>();
        List<int[]> sorted = new ArrayList<>(spans);
        sorted.sort((a, b) -> a[0] != b[0] ? Integer.compare(a[0], b[0])
                : Integer.compare(b[1], a[1]));
        int lastStart = -1, lastEnd = -1;
        for (int[] e : sorted) {
            if (e[0] == lastStart && e[1] == lastEnd) {
                continue; // 同一区间重复命中（嵌套访问链），保留先出现的
            }
            out.add(e);
            lastStart = e[0];
            lastEnd = e[1];
        }
        // 若出现包含关系（外层 select 覆盖内层 identifier 的情况不会发生，
        // 因为 select 只替换尾部；防御性丢弃被完全包含的区间）
        List<int[]> res = new ArrayList<>();
        for (int[] e : out) {
            boolean covered = false;
            for (int[] o : res) {
                if (o[0] <= e[0] && o[1] >= e[1] && !(o[0] == e[0] && o[1] == e[1])) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                res.add(e);
            }
        }
        return res;
    }

    private static String readText(JavaFileObject src) throws IOException {
        CharSequence cs = src.getCharContent(true);
        if (cs == null) {
            return null;
        }
        String s = cs.toString();
        // 检查解码是否产生了替换字符（说明不是 UTF-8）
        if (s.indexOf('\uFFFD') >= 0) {
            // 回退：按 GBK 重新读原始字节
            byte[] raw = Files.readAllBytes(Paths.get(src.toUri()));
            try {
                s = StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(raw)).toString();
            } catch (CharacterCodingException ex) {
                s = new String(raw, Charset.forName("GBK"));
            }
        }
        return s;
    }

    /** 定位类声明名（ClassTree 的 simple name 不是独立 Tree 节点）。 */
    private void collectDecls(Trees trees, CompilationUnitTree cu, SourcePositions sp, String text,
                              List<int[]> spans, List<String> newNames) {
        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitClass(ClassTree node, Void unused) {
                TreePath path = getCurrentPath();
                Element e = trees.getElement(path);
                String newName = null;
                if (e instanceof TypeElement) {
                    newName = renameMap.get(canonical((TypeElement) e));
                }
                String oldSimple = node.getSimpleName().toString();
                if (newName != null) {
                    long start = sp.getStartPosition(cu, node);
                    int pos = findDeclNamePos(text, (int) start, oldSimple);
                    if (pos >= 0) {
                        spans.add(new int[]{pos, pos + oldSimple.length(),
                                newNames.size(), 0});
                        newNames.add(newName);
                    }
                }
                // 构造器：MethodTree 返回类型为 null 即构造器，名字必须等于类名。
                // 类刚改过名时，jadx 留下的旧构造器名（`public b(...)`）会变成
                // 「invalid method declaration; return type required」——这里一并修。
                String targetCtor = newName != null ? newName : oldSimple;
                for (Tree member : node.getMembers()) {
                    if (!(member instanceof com.sun.source.tree.MethodTree)) {
                        continue;
                    }
                    com.sun.source.tree.MethodTree mt = (com.sun.source.tree.MethodTree) member;
                    if (mt.getReturnType() != null) {
                        continue;
                    }
                    String ctorNow = mt.getName().toString();
                    if (ctorNow.equals(targetCtor)) {
                        continue;
                    }
                    if (newName == null && !fixCtors) {
                        continue;   // 没改名也没开修复模式，不动
                    }
                    long ms = sp.getStartPosition(cu, mt);
                    int mp = findCtorNamePos(text, (int) ms, ctorNow);
                    if (mp >= 0) {
                        spans.add(new int[]{mp, mp + ctorNow.length(), newNames.size(), 0});
                        newNames.add(targetCtor);
                    }
                }
                return super.visitClass(node, unused);
            }
        }.scan(cu, null);
    }

    /** 在声明起始位置之后找第一个 `class|interface|enum|record NAME` 的 NAME 位置。 */    private static int findDeclNamePos(String text, int from, String simpleName) {
        if (from < 0 || from >= text.length()) {
            return -1;
        }
        int windowEnd = Math.min(text.length(), from + 4000);
        Matcher m = IDENT.matcher(text);
        m.region(from, windowEnd);
        if (m.find() && simpleName.equals(m.group(2))) {
            return m.start(2);
        }
        // 兜底：从 from 起逐行找，允许前面有注解
        int idx = text.indexOf(simpleName, from);
        return (idx >= 0 && idx < windowEnd) ? idx : -1;
    }

    /** 构造器名位置：声明头里第一个 `Name(` 或 `Name<...>(`。 */
    private static int findCtorNamePos(String text, int from, String simpleName) {
        if (from < 0 || from >= text.length()) {
            return -1;
        }
        int windowEnd = Math.min(text.length(), from + 3000);
        Matcher m = Pattern.compile("(?<![\\w$.])" + Pattern.quote(simpleName) + "\\s*[<(]")
                .matcher(text);
        m.region(from, windowEnd);
        return m.find() ? m.start() : -1;
    }

    /** 引用扫描：只改「解析出来确实是目标类」的标识符。 */
    private final class Scanner extends TreePathScanner<Void, Void> {
        private final Trees trees;
        private final SourcePositions sp;
        private final CompilationUnitTree cu;
        private final List<int[]> spans;
        private final List<String> newNames;
        private final String text;

        Scanner(Trees trees, SourcePositions sp, CompilationUnitTree cu,
                List<int[]> spans, List<String> newNames, String text) {
            this.trees = trees;
            this.sp = sp;
            this.cu = cu;
            this.spans = spans;
            this.newNames = newNames;
            this.text = text;
        }

        @Override
        public Void visitIdentifier(IdentifierTree node, Void unused) {
            record(node, null, 0);
            return super.visitIdentifier(node, unused);
        }

        @Override
        public Void visitMemberSelect(MemberSelectTree node, Void unused) {
            record(node, node.getIdentifier().toString(), 1);
            return super.visitMemberSelect(node, unused);
        }

        private void record(Tree node, String selectName, int kind) {
            // `X.this` / `X.class` / `X.super`：整个 select 的 element 可能是类本身，
            // 但真正要改的是限定符 X（它自己会作为 IdentifierTree 被访问），
            // 这里必须跳过，否则会把 `this` / `class` 关键字换掉。
            if (kind == 1 && ("this".equals(selectName) || "class".equals(selectName)
                    || "super".equals(selectName))) {
                return;
            }
            Element e;
            try {
                e = trees.getElement(getCurrentPath());
            } catch (RuntimeException ex) {
                return;
            }
            if (!(e instanceof TypeElement)) {
                return;
            }
            String newName = renameMap.get(canonical((TypeElement) e));
            if (newName == null) {
                return;
            }
            long s, t;
            try {
                s = sp.getStartPosition(cu, node);
                t = sp.getEndPosition(cu, node);
            } catch (RuntimeException ex) {
                return;
            }
            if (s < 0 || t < 0) {
                return;
            }
            String simple = ((TypeElement) e).getSimpleName().toString();
            int st = (int) s;
            int en = (int) t;
            if (kind == 1) {
                // MemberSelectTree：只替换尾部的标识符
                st = en - selectName.length();
                if (st < s) {
                    return;
                }
            } else if (!text.regionMatches(st, simple, 0, simple.length())) {
                // javac 已知怪癖：`new X()` 里 clazz 标识符的 pos 指向 `new` 关键字，
                // 直接按 [pos,end) 替换会把 `new` 吃掉。这里以文本为准校正。
                int idx = text.lastIndexOf(simple, en - simple.length());
                if (idx >= st && idx + simple.length() == en) {
                    st = idx;
                } else {
                    return;     // 对不上就不改，宁漏不错
                }
            }
            spans.add(new int[]{st, en, newNames.size(), 1});
            newNames.add(newName);
        }
    }

    /** 包名 + 各级外层类型简单名（用 $ 连接），与 rename_map 的 key 格式一致。 */
    private static String canonical(TypeElement te) {
        Deque<String> names = new ArrayDeque<>();
        Element e = te;
        while (e instanceof TypeElement) {
            names.addFirst(e.getSimpleName().toString());
            e = e.getEnclosingElement();
        }
        String pkg = (e instanceof PackageElement)
                ? ((PackageElement) e).getQualifiedName().toString() : "";
        String joined = String.join("$", names);
        return pkg.isEmpty() ? joined : pkg + "." + joined;
    }
}
