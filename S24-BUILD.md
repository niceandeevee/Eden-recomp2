# Eden: Galaxy S24+ build

This tree is Eden master with changes for the Galaxy S24 / S24+ / S24 Ultra
(Snapdragon 8 Gen 3, Adreno 750). It builds a new `s24` flavor; the stock flavors are unchanged.

## What's different

**Build (native code)**
- New `YUZU_BUILD_PRESET=sd8gen3`: `-march=armv8.7-a+aes+sha2+fp16+fp16fml+dotprod+i8mm+bf16+rcpc+lse -mtune=cortex-x4`,
  `-O3` for release (AGP otherwise builds RelWithDebInfo at `-O2 -g`). SVE/SVE2 are left out on purpose.
- Intra-library calls bound directly instead of through PLT stubs (`-fno-semantic-interposition`,
  `-Wl,-Bsymbolic-functions`), no errno bookkeeping in libm calls (`-fno-math-errno`, so e.g. sqrt is a
  single instruction), `-fno-plt`, and linker dead-code removal + safe identical-code folding
  (`--gc-sections --icf=safe -O2`) for better instruction-cache use. No `-ffast-math`.
- Link-time optimization (`ENABLE_LTO=ON`, ThinLTO) in the CI build, with an automatic non-LTO fallback.
- Same `com.miHoYo.Yuanshen` package spoof as the upstream "Optimized" build, so Samsung's
  Game Optimizing Service and the Qualcomm driver apply their game performance profiles.

**Defaults (`EDEN_S24_TUNING`)**, also what "Reset to default" restores:
- Asynchronous shaders: on
- Force max GPU clock: on
- VRAM usage: Aggressive (the S24+ has 12 GB)
- NVDEC video decoding: GPU
- Shader pipeline worker threads: 4

**Runtime**
- The emulation screen requests the panel's highest refresh rate (120 Hz) at the current resolution.

**New menus**
- Settings → *Advanced & hidden options*: 55 core settings that had no Android menu
  (multicore, frame pacing, async presentation, driver pipeline cache, ASTC recompression,
  video frame-rate sync, barrier feedback loops, compute pipelines, all Dynarmic unsafe and debug
  JIT options, GPU macro/shader debugging, time zone, sound mode, RNG seed, virtual SD,
  DLC/updates from game folders, kiosk mode, letterbox background color, controller emulation
  options and core debugging/dump options).
- Settings → Applets: the 14 other applets (each can be built-in, real applet, or disabled).
- Memory layout gains 10 GB and 12 GB; resolution gains 5x to 8x.

**Fixes**
- Player-select applet setting used a key the core doesn't know (`player_select_applet`); fixed to
  `player_select_applet_mode`.
- GPU fence behavior listed a fifth value the core doesn't have.

Desktop-only settings (mouse/keyboard, raw input, Joy-Con/Pro Controller drivers, UDP/TAS,
GDB stub, RenderDoc, Nsight, fullscreen mode, Vulkan device index) are intentionally not exposed.

## Building it for free with GitHub Actions

1. Create a **public** repository on GitHub (Actions minutes are free and unlimited for public repos).
2. Push this whole folder to it (keep the `.github` folder).
3. Open the repo's **Actions** tab. The *Build Eden S24+ APK* workflow starts on push; you can also
   start it with **Run workflow**.
4. When it finishes (roughly 1.5–3 hours), open the run and download **Eden-S24plus-APK**.
   Unzip it and install the `.apk` on the phone.

The APK is signed with the project's public debug key, so later builds install over earlier ones.
It uses the same package name as the upstream "Optimized" build, so uninstall that first
(export your saves from Eden before you do).

## Building locally

```
.ci/android/build.sh -t s24 -b Release -DENABLE_LTO=ON
```

Needs JDK 17, Android SDK platform 36, NDK 28.2.13676358, CMake 3.31.5 and `glslangValidator`.
The APK lands in `artifacts/`.
