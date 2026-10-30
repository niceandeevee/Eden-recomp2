// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package org.yuzu.yuzu_emu.features.settings.model

import org.yuzu.yuzu_emu.utils.NativeConfig

enum class BooleanSetting(override val key: String) : AbstractBooleanSetting {
    AUDIO_MUTED("audio_muted"),
    FASTMEM("cpuopt_fastmem"),
    FASTMEM_EXCLUSIVES("cpuopt_fastmem_exclusives"),
    CORE_SYNC_CORE_SPEED("sync_core_speed"),
    ENABLE_CHEATS_BY_DEFAULT("enable_cheats_by_default"),
    RENDERER_USE_SPEED_LIMIT("use_speed_limit"),
    USE_CUSTOM_CPU_TICKS("use_custom_cpu_ticks"),
    SKIP_CPU_INNER_INVALIDATION("skip_cpu_inner_invalidation"),
    FIX_BLOOM_EFFECTS("fix_bloom_effects"),
    EMULATE_BGR565("emulate_bgr565"),
    RESCALE_HACK("rescale_hack"),
    CPUOPT_UNSAFE_HOST_MMU("cpuopt_unsafe_host_mmu"),
    USE_DOCKED_MODE("use_docked_mode"),
    USE_AUTO_STUB("use_auto_stub"),
    RENDERER_USE_DISK_SHADER_CACHE("use_disk_shader_cache"),
    RENDERER_FORCE_MAX_CLOCK("force_max_clock"),
    RENDERER_ASYNCHRONOUS_GPU_EMULATION("use_asynchronous_gpu_emulation"),
    RENDERER_ASYNCHRONOUS_SHADERS("use_asynchronous_shaders"),
    RENDERER_REACTIVE_FLUSHING("use_reactive_flushing"),
    ENABLE_BUFFER_HISTORY("enable_buffer_history"),
    USE_OPTIMIZED_VERTEX_BUFFERS("use_optimized_vertex_buffers"),
    SYNC_MEMORY_OPERATIONS("sync_memory_operations"),
    BUFFER_REORDER_DISABLE("disable_buffer_reorder"),
    RENDERER_DEBUG("debug"),
    RENDERER_PATCH_OLD_QCOM_DRIVERS("patch_old_qcom_drivers"),
    RENDERER_VERTEX_INPUT_DYNAMIC_STATE("vertex_input_dynamic_state"),
    RENDERER_SAMPLE_SHADING("sample_shading"),
    RENDERER_FRAME_GEN("frame_gen"),
    RENDERER_FRAME_GEN_FLOW_SCALE_AUTO("frame_gen_flow_scale_auto"),
    GPU_UNSWIZZLE_ENABLED("gpu_unswizzle_enabled"),
    PICTURE_IN_PICTURE("picture_in_picture"),
    USE_CUSTOM_RTC("custom_rtc_enabled"),
    BLACK_BACKGROUNDS("black_backgrounds"),
    INVERT_CONFIRM_BACK_CONTROLLER_BUTTONS("invert_confirm_back_controller_buttons"),

    ENABLE_FOLDER_BUTTON("enable_folder_button"),
    ENABLE_QLAUNCH_BUTTON("enable_qlaunch_button"),

    ENABLE_UPDATE_CHECKS("enable_update_checks"),
    JOYSTICK_REL_CENTER("joystick_rel_center"),
    DPAD_SLIDE("dpad_slide"),
    HAPTIC_FEEDBACK("haptic_feedback"),
    SHOW_INPUT_OVERLAY("show_input_overlay"),
    OVERLAY_SNAP_TO_GRID("overlay_snap_to_grid"),
    TOUCHSCREEN("touchscreen"),
    AIRPLANE_MODE("airplane_mode"),

    SHOW_SOC_OVERLAY("show_soc_overlay"),
    SHOW_BUILD_ID("show_build_id"),
    SHOW_DRIVER_VERSION("show_driver_version"),
    SHOW_DEVICE_MODEL("show_device_model"),
    SHOW_GPU_MODEL("show_gpu_model"),
    SHOW_SOC_MODEL("show_soc_model"),
    SHOW_FW_VERSION("show_firmware_version"),

    SOC_OVERLAY_BACKGROUND("soc_overlay_background"),
    ENABLE_INPUT_OVERLAY_AUTO_HIDE("enable_input_overlay_auto_hide"),
    HIDE_OVERLAY_ON_CONTROLLER_INPUT("hide_overlay_on_controller_input"),

    PERF_OVERLAY_BACKGROUND("perf_overlay_background"),
    SHOW_PERFORMANCE_OVERLAY("show_performance_overlay"),

    SHOW_FPS("show_fps"),
    SHOW_FRAMETIME("show_frame_time"),
    SHOW_APP_RAM_USAGE("show_app_ram_usage"),
    SHOW_SYSTEM_RAM_USAGE("show_system_ram_usage"),
    SHOW_BAT_TEMPERATURE("show_bat_temperature"),
    SHOW_POWER_INFO("show_power_info"),
    SHOW_SHADERS_BUILDING("show_shaders_building"),

    DEBUG_FLUSH_BY_LINE("flush_line"),
    EXTENDED_LOGGING("extended_logging"),
    DONT_SHOW_DRIVER_SHADER_WARNING("dont_show_driver_shader_warning"),
    ENABLE_OVERLAY("enable_overlay"),

    // GPU Logging
    GPU_LOG_VULKAN_CALLS("gpu_log_vulkan_calls"),
    GPU_LOG_SHADER_DUMPS("gpu_log_shader_dumps"),
    DUMP_GUEST_SHADERS("dump_guest_shaders"),
    DUMP_MACROS("dump_macros"),
    GPU_LOG_MEMORY_TRACKING("gpu_log_memory_tracking"),
    GPU_LOG_DRIVER_DEBUG("gpu_log_driver_debug"),

    ENABLE_QUICK_SETTINGS("enable_quick_settings"),

    // Native settings that previously had no Android UI
    MULTI_CORE("use_multi_core"),
    ASYNC_PRESENTATION("async_presentation"),
    VULKAN_DRIVER_PIPELINE_CACHE("use_vulkan_driver_pipeline_cache"),
    USE_VIDEO_FRAMERATE("use_video_framerate"),
    BARRIER_FEEDBACK_LOOPS("barrier_feedback_loops"),
    ENABLE_COMPUTE_PIPELINES("enable_compute_pipelines"),
    CPUOPT_UNSAFE_UNFUSE_FMA("cpuopt_unsafe_unfuse_fma"),
    CPUOPT_UNSAFE_REDUCE_FP_ERROR("cpuopt_unsafe_reduce_fp_error"),
    CPUOPT_UNSAFE_IGNORE_STANDARD_FPCR("cpuopt_unsafe_ignore_standard_fpcr"),
    CPUOPT_UNSAFE_INACCURATE_NAN("cpuopt_unsafe_inaccurate_nan"),
    CPUOPT_UNSAFE_FASTMEM_CHECK("cpuopt_unsafe_fastmem_check"),
    CPUOPT_UNSAFE_IGNORE_GLOBAL_MONITOR("cpuopt_unsafe_ignore_global_monitor"),
    CPUOPT_PAGE_TABLES("cpuopt_page_tables"),
    CPUOPT_BLOCK_LINKING("cpuopt_block_linking"),
    CPUOPT_RETURN_STACK_BUFFER("cpuopt_return_stack_buffer"),
    CPUOPT_FAST_DISPATCHER("cpuopt_fast_dispatcher"),
    CPUOPT_CONTEXT_ELIMINATION("cpuopt_context_elimination"),
    CPUOPT_CONST_PROP("cpuopt_const_prop"),
    CPUOPT_MISC_IR("cpuopt_misc_ir"),
    CPUOPT_REDUCE_MISALIGN_CHECKS("cpuopt_reduce_misalign_checks"),
    CPUOPT_RECOMPILE_EXCLUSIVES("cpuopt_recompile_exclusives"),
    CPUOPT_IGNORE_MEMORY_ABORTS("cpuopt_ignore_memory_aborts"),
    DISABLE_SHADER_LOOP_SAFETY_CHECKS("disable_shader_loop_safety_checks"),
    SHADER_FEEDBACK("shader_feedback"),
    DISABLE_MACRO_JIT("disable_macro_jit"),
    DISABLE_MACRO_HLE("disable_macro_hle"),
    FRAME_GEN_DUMP_FLOW("frame_gen_dump_flow"),
    RNG_SEED_ENABLED("rng_seed_enabled"),
    USE_VIRTUAL_SD("use_virtual_sd"),
    EXT_CONTENT_FROM_GAME_DIRS("ext_content_from_game_dirs"),
    QUEST_FLAG("quest_flag"),
    VIBRATION_ENABLED("vibration_enabled"),
    ENABLE_ACCURATE_VIBRATIONS("enable_accurate_vibrations"),
    MOTION_ENABLED("motion_enabled"),
    ENABLE_RING_CONTROLLER("enable_ring_controller"),
    ENABLE_IR_SENSOR("enable_ir_sensor"),
    RANDOM_AMIIBO_ID("random_amiibo_id"),
    ENABLE_ALL_CONTROLLERS("enable_all_controllers"),
    PERFORM_VULKAN_CHECK("perform_vulkan_check"),
    USE_DEBUG_ASSERTS("use_debug_asserts"),
    ENABLE_FS_ACCESS_LOG("enable_fs_access_log"),
    REPORTING_SERVICES("reporting_services"),
    DUMP_EXEFS("dump_exefs"),
    DUMP_NSO("dump_nso"),
    DUMP_AUDIO_COMMANDS("dump_audio_commands"),
    USE_DEV_KEYS("use_dev_keys"),
    DISABLE_WEB_APPLET("disable_web_applet");

    //external fun isFrameSkippingEnabled(): Boolean
    external fun isFrameInterpolationEnabled(): Boolean

    override fun getBoolean(needsGlobal: Boolean): Boolean =
        NativeConfig.getBoolean(key, needsGlobal)

    override fun setBoolean(value: Boolean) {
        if (NativeConfig.isPerGameConfigLoaded()) {
            global = false
        }
        NativeConfig.setBoolean(key, value)
    }

    override val defaultValue: Boolean by lazy { NativeConfig.getDefaultToString(key).toBoolean() }

    override fun getValueAsString(needsGlobal: Boolean): String = getBoolean(needsGlobal).toString()

    override fun reset() = NativeConfig.setBoolean(key, defaultValue)
}
