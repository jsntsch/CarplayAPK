# -*- coding: utf-8 -*-
"""修正 AirPlaySession 注释里对方法名的指代（实际是 t0/s0/D1，不是 sendSetup）。

历史说明：曾尝试把 jadx 的 `XxxVar` 变量名统一改成新类名的 camelCase，
但 `f` / `d` / `m` 这类单字母类在两个包里含义不同，
全局替换会让「变量名」和「类型名」对不上（出现 `f avcEncoder` 这种），
所以撤销了。变量名保留 jadx 原样，可读性靠类名 + 注释承担。
"""
import os

DST = os.environ.get("CARPLAY_SRC",
    os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "java"))

REPL = [
    ("{@code sendSetup} 附近 —— 构造 SETUP 的 9 个键 + V1.7.28 加的 features 数组",
     "{@code t0(...)} —— 构造并发送 SETUP：9 个基础键 + V1.7.28 追加的 features 字符串数组"),
    ('{@code eventPort} 读取点 —— 事件通道端口来自 SETUP <b>应答</b>，不是请求',
     '{@code D1(map, "eventPort")} —— 事件通道端口，取自 SETUP <b>应答</b>，不是请求'),
]


def main():
    n = 0
    for dirpath, _d, fns in os.walk(DST):
        for fn in fns:
            if not fn.endswith('.java'):
                continue
            p = os.path.join(dirpath, fn)
            text = open(p, encoding='utf-8', errors='replace').read()
            orig = text
            for a, b in REPL:
                text = text.replace(a, b)
            if text != orig:
                open(p, 'w', encoding='utf-8', newline='\n').write(text)
                n += 1
    print('comment fixes:', n)


if __name__ == '__main__':
    main()
