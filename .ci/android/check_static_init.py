#!/usr/bin/env python3
"""Fail if any C++ static initializer in the library is never run.

Every __cxx_global_var_init* function must be reachable from .init_array, either
directly or through a call/branch from a function that is. A full-LTO build once
dropped the .init_array entry for a template static member (the input factory
registry), leaving it zeroed and crashing the app at startup.
"""
import re
import subprocess
import sys

objdump, lib = sys.argv[1], sys.argv[2]

init_array = subprocess.run([objdump, "-s", "-j", ".init_array", lib],
                            capture_output=True, text=True, check=True).stdout
entries = set()
for line in init_array.splitlines():
    m = re.match(r"^\s*[0-9a-f]+((?:\s+[0-9a-f]{8}){1,4})", line)
    if not m:
        continue
    words = m.group(1).split()
    for lo, hi in zip(words[0::2], words[1::2]):
        le = lambda w: int.from_bytes(bytes.fromhex(w), "little")
        entries.add(le(lo) | (le(hi) << 32))

disasm = subprocess.run([objdump, "-d", "--no-show-raw-insn", lib],
                        capture_output=True, text=True, check=True).stdout
func_re = re.compile(r"^([0-9a-f]+) <(.+)>:$")
call_re = re.compile(r"\b(?:bl|b)\s+0x([0-9a-f]+)\b")
funcs = {}       # addr -> name
calls = {}       # addr -> set(target addrs)
cur = None
for line in disasm.splitlines():
    m = func_re.match(line)
    if m:
        cur = int(m.group(1), 16)
        funcs[cur] = m.group(2)
        calls[cur] = set()
        continue
    if cur is not None:
        c = call_re.search(line)
        if c:
            calls[cur].add(int(c.group(1), 16))

reached, todo = set(), [a for a in entries if a in funcs]
while todo:
    a = todo.pop()
    if a in reached:
        continue
    reached.add(a)
    todo.extend(t for t in calls.get(a, ()) if t in funcs and t not in reached)

inits = {a: n for a, n in funcs.items() if n.startswith("__cxx_global_var_init")}
missing = sorted(a for a in inits if a not in reached)
print(f"init_array entries: {len(entries)}, static initializers: {len(inits)}, "
      f"unreachable: {len(missing)}")
for a in missing:
    print(f"  never run: {a:#x} {inits[a]}")
sys.exit(1 if missing else 0)
