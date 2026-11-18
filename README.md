# Eden S24+ symbol maps

Files are named <run id>-<commit>-<ELF build id>. A tombstone's build id for
libyuzu-android.so identifies the exact file. Resolve addresses with:

    xz -dk libs/<name>.so.xz && llvm-symbolizer --obj=libs/<name>.so --inlines -C 0x...

| run | commit | build id |
|---|---|---|
| 44407166573 | 305e944 | e27f5a7db4289c4075396c56ffe6907efa58b25a |
| 44346288539 | b83c4f7 | 76f2b0747b10b9ba2fdd37b4fbcf9c122d04166a |
