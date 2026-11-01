# -*- coding: utf-8 -*-
"""把 jadx 产物重命名成可读工程。

三条铁律：
1. 只替换「类型位置」，用白名单正则，绝不全局 replace。
2. 简单名替换必须限定包，且前面不能是点 —— 否则 `io.carplay.app.secured.h`
   里那个 `h` 会被当成 `prototype.h` 一起改掉（本项目踩过）。
3. 构造函数只在「文件主类」上改：`y5.java` 里的 `public y5(` 才改成
   `public EventChannel(`；嵌套类 `h(...)` 的构造名绝不能动。
"""
import os, re, sys
import rename_map as RM

SRC = r"C:\Users\SCH\WorkBuddy\2026-10-24-16-47-09\_jadx_v1729\sources"
DST = r"C:\Users\SCH\WorkBuddy\2026-10-24-16-47-09\OcarPlay-Java-V1729\src\main\java"

RE_PKG = re.compile(r'^package\s+([\w.]+);', re.M)
RE_IMPORT = re.compile(r'^import\s+(?:static\s+)?([\w.]+);', re.M)
NOT_QUALIFIED = r'(?<![\w.])'


def type_patterns(old, is_self):
    o = re.escape(old)
    pre = NOT_QUALIFIED + o
    pats = []
    if is_self:
        pats.append((re.compile(r'^(\s*(?:(?:public|private|protected)\s+)?)' + o + r'(\s*\()', re.M), r'\1{NEW}\2'))
    pats += [
        (re.compile(r'\b(class|interface|enum)\s+' + o + r'\b'), r'\1 {NEW}'),
        (re.compile(r'\bnew\s+' + o + r'\b'), 'new {NEW}'),
        (re.compile(r'\bextends\s+' + o + r'\b'), 'extends {NEW}'),
        (re.compile(r'\bimplements\s+' + o + r'\b'), 'implements {NEW}'),
        (re.compile(r'\binstanceof\s+' + o + r'\b'), 'instanceof {NEW}'),
        (re.compile(r'\bthrows\s+' + o + r'\b'), 'throws {NEW}'),
        (re.compile(r'(\bcatch\s*\(\s*)' + o + r'\b'), r'\1{NEW}'),
        (re.compile(r'(\(\s*)' + o + r'(\s*\))'), r'\1{NEW}\2'),
        (re.compile(r'(<\s*)' + o + r'(?=\s*[,>])'), r'\1{NEW}'),
        (re.compile(pre + r'(\s*\[\s*\])'), '{NEW}' + r'\1'),
        (re.compile(pre + r'(\s*\.)'), '{NEW}' + r'\1'),
        (re.compile(pre + r'(\s+\w+\s*\()'), '{NEW}' + r'\1'),
        (re.compile(pre + r'(\s+\w+\s*[;=,):])'), '{NEW}' + r'\1'),
    ]
    return pats


def local_class_names(text):
    """文件里出现过的所有 class 声明名（含嵌套类）。"""
    return set(re.findall(r'\bclass\s+(\w+)', text))


def rewrite_file(text, pkg, imports, core, self_old):
    locals_ = local_class_names(text)
    for fqcn, new, _ in core:
        old_pkg, old_simple = fqcn.rsplit('.', 1)
        if fqcn in text:
            text = text.replace(fqcn, old_pkg + '.' + new)
        # 嵌套类同名屏蔽：`f.java` 里的 `class h` 是嵌套类，
        # 不能把它当成同包的顶层 prototype.h 一起改名。
        if old_simple != self_old and old_simple in locals_:
            continue
        if old_pkg == pkg or fqcn in imports:
            for pat, rep in type_patterns(old_simple, old_simple == self_old):
                text = pat.sub(rep.replace('{NEW}', new), text)
    return text


def main():
    core = sorted(RM.CORE, key=lambda x: -len(x[0].rsplit('.', 1)[1]))
    rename_index = {fq: new for fq, new, _ in core}
    files = renamed = changed = 0
    for dirpath, _d, fns in os.walk(SRC):
        for fn in fns:
            if not fn.endswith('.java'):
                continue
            sp = os.path.join(dirpath, fn)
            rel = os.path.relpath(sp, SRC)
            text = open(sp, encoding='utf-8', errors='replace').read()
            m = RE_PKG.search(text)
            pkg = m.group(1) if m else ''
            imports = set(RE_IMPORT.findall(text))
            fqcn = pkg + '.' + fn[:-5]
            new_text = rewrite_file(text, pkg, imports, core, fn[:-5])
            new_base = rename_index.get(fqcn, fn[:-5])
            files += 1
            renamed += (new_base != fn[:-5])
            changed += (new_text != text)
            dp = os.path.join(DST, os.path.dirname(rel), new_base + '.java')
            os.makedirs(os.path.dirname(dp), exist_ok=True)
            open(dp, 'w', encoding='utf-8', newline='\n').write(new_text)
    print('files=%d renamed=%d content_changed=%d' % (files, renamed, changed))


if __name__ == '__main__':
    main()
