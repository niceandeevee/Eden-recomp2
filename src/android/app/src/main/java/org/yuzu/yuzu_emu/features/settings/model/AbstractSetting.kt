// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package org.yuzu.yuzu_emu.features.settings.model

import org.yuzu.yuzu_emu.utils.NativeConfig

interface AbstractSetting {
    val key: String
    val defaultValue: Any

    // Settings that exist only in the Android UI (theme, controller page, vibration, headers...)
    // have no native counterpart; answer for them locally instead of asking the core about a
    // name it doesn't know.
    val isRuntimeModifiable: Boolean
        get() = if (NativeConfig.isNativeSetting(key)) {
            NativeConfig.getIsRuntimeModifiable(key)
        } else {
            true
        }

    val pairedSettingKey: String
        get() = if (NativeConfig.isNativeSetting(key)) NativeConfig.getPairedSettingKey(key) else ""

    val isSwitchable: Boolean
        get() = NativeConfig.isNativeSetting(key) && NativeConfig.getIsSwitchable(key)

    var global: Boolean
        get() = !NativeConfig.isNativeSetting(key) || NativeConfig.usingGlobal(key)
        set(value) {
            if (NativeConfig.isNativeSetting(key)) {
                NativeConfig.setGlobal(key, value)
            }
        }

    val isSaveable: Boolean
        get() = NativeConfig.isNativeSetting(key) && NativeConfig.getIsSaveable(key)

    fun getValueAsString(needsGlobal: Boolean = false): String

    fun reset()
}
