# SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
# SPDX-License-Identifier: GPL-3.0-or-later

# SPDX-FileCopyrightText: 2023 yuzu Emulator Project
# SPDX-License-Identifier: GPL-3.0-or-later

add_library(yuzu-android SHARED
    emu_window/emu_window.cpp
    emu_window/emu_window.h
    native.cpp
    native.h
    native_config.cpp
    native_freedreno.cpp
    android_settings.cpp
    game_metadata.cpp
    native_log.cpp
    android_config.cpp
    android_config.h
    native_input.cpp
    native_post_processing.cpp
)

set_property(TARGET yuzu-android PROPERTY IMPORTED_LOCATION ${FFmpeg_LIBRARY_DIR})

target_link_libraries(yuzu-android PRIVATE audio_core common core input_common frontend_common video_core)
# Vulkan only on Android: no OpenGL loader (glad), EGL or GLES needed.
target_link_libraries(yuzu-android PRIVATE android camera2ndk jnigraphics log)
if (ARCHITECTURE_arm64)
    target_link_libraries(yuzu-android PRIVATE adrenotools)
endif()

target_link_libraries(yuzu-android PRIVATE ${FFmpeg_LIBRARIES} OpenSSL::SSL)
if (TARGET cpp-jwt::cpp-jwt)
    target_link_libraries(yuzu-android PRIVATE cpp-jwt::cpp-jwt)
endif()
if (ENABLE_UPDATE_CHECKER)
    target_compile_definitions(yuzu-android PUBLIC ENABLE_UPDATE_CHECKER)
endif()

set(CPACK_PACKAGE_EXECUTABLES ${CPACK_PACKAGE_EXECUTABLES} yuzu-android)

target_link_options(yuzu-android PRIVATE "-Wl,-Bsymbolic")

if (EDEN_S24_TUNING)
    # Export only the JNI entry points; everything else is internal (smaller, better LTO).
    set(_yuzu_android_map "${CMAKE_CURRENT_SOURCE_DIR}/yuzu-android.map")
    target_link_options(yuzu-android PRIVATE "-Wl,--version-script=${_yuzu_android_map}")
    set_property(TARGET yuzu-android APPEND PROPERTY LINK_DEPENDS "${_yuzu_android_map}")
    # Android 11+ (the S24 build needs Android 14): compact RELR relocation tables.
    target_link_options(yuzu-android PRIVATE "-Wl,--pack-dyn-relocs=android+relr")
    # Keep .text.hot / .text.unlikely etc. as separate groups so PGO-hot code is packed together.
    target_link_options(yuzu-android PRIVATE "-Wl,-z,keep-text-section-prefix")
    # JNI entry points must stay visible to Java.
    set_target_properties(yuzu-android PROPERTIES
        C_VISIBILITY_PRESET default CXX_VISIBILITY_PRESET default VISIBILITY_INLINES_HIDDEN OFF)
endif()
