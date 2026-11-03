// SPDX-FileCopyrightText: Copyright 2026 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

// SPDX-FileCopyrightText: 2023 yuzu Emulator Project
// SPDX-License-Identifier: GPL-2.0-or-later

package org.yuzu.yuzu_emu

import android.app.ActivityManager
import android.app.Application
import android.app.ApplicationExitInfo
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import org.yuzu.yuzu_emu.features.input.NativeInput
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import android.content.res.Configuration
import android.os.LocaleList
import org.yuzu.yuzu_emu.features.settings.model.IntSetting
import org.yuzu.yuzu_emu.utils.DirectoryInitialization
import org.yuzu.yuzu_emu.utils.DocumentsTree
import org.yuzu.yuzu_emu.utils.GpuDriverHelper
import org.yuzu.yuzu_emu.utils.Log
import org.yuzu.yuzu_emu.utils.PowerStateUpdater
import org.yuzu.yuzu_emu.utils.ControllerNavigationGlobalHook
import java.util.Locale

fun Context.getPublicFilesDir(): File = getExternalFilesDir(null) ?: filesDir

class YuzuApplication : Application() {
    private fun createNotificationChannels() {
        val name: CharSequence = getString(R.string.app_notification_channel_name)
        val description = getString(R.string.app_notification_channel_description)
        val foregroundService = NotificationChannel(
            getString(R.string.app_notification_channel_id),
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        foregroundService.description = description
        foregroundService.setSound(null, null)
        foregroundService.vibrationPattern = null

        val noticeChannel = NotificationChannel(
            getString(R.string.notice_notification_channel_id),
            getString(R.string.notice_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        noticeChannel.description = getString(R.string.notice_notification_channel_description)
        noticeChannel.setSound(null, null)

        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(noticeChannel)
        notificationManager.createNotificationChannel(foregroundService)
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        recordPreviousExit()
        documentsTree = DocumentsTree()
        DirectoryInitialization.start()

        // Initialize Freedreno config BEFORE loading native library
        // This ensures GPU driver environment variables are set before adrenotools initializes
        GpuDriverHelper.initializeFreedrenoConfigEarly()

        NativeLibrary.playTimeManagerInit()
        GpuDriverHelper.initializeDriverParameters()
        NativeInput.reloadInputDevices()
        NativeLibrary.logDeviceInfo()
        PowerStateUpdater.start()
        Log.logDeviceInfo()
        ControllerNavigationGlobalHook.install(this)

        createNotificationChannels()
    }

    /**
     * Saves why Eden's previous processes ended (crash, low-memory kill, normal exit...) to
     * last-exit.txt in the user folder, plus the system's native crash report for the most recent
     * native crash as last-native-crash.pb. Crashes that kill the process outright (signals,
     * Android's low-memory killer) leave nothing in Eden's own log.
     */
    private fun recordPreviousExit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return
        }
        try {
            val dir = getExternalFilesDir(null) ?: return
            val am = getSystemService(ActivityManager::class.java) ?: return
            val exits = am.getHistoricalProcessExitReasons(packageName, 0, 5)
            if (exits.isEmpty()) {
                return
            }
            val reasons = mapOf(
                ApplicationExitInfo.REASON_EXIT_SELF to "EXIT_SELF",
                ApplicationExitInfo.REASON_SIGNALED to "SIGNALED",
                ApplicationExitInfo.REASON_LOW_MEMORY to "LOW_MEMORY",
                ApplicationExitInfo.REASON_CRASH to "CRASH (Java)",
                ApplicationExitInfo.REASON_CRASH_NATIVE to "CRASH_NATIVE",
                ApplicationExitInfo.REASON_ANR to "ANR",
                ApplicationExitInfo.REASON_INITIALIZATION_FAILURE to "INITIALIZATION_FAILURE",
                ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE to "EXCESSIVE_RESOURCE_USAGE",
                ApplicationExitInfo.REASON_USER_REQUESTED to "USER_REQUESTED",
                ApplicationExitInfo.REASON_USER_STOPPED to "USER_STOPPED",
                ApplicationExitInfo.REASON_OTHER to "OTHER"
            )
            val text = StringBuilder()
            for (info in exits) {
                text.append("time=").append(java.util.Date(info.timestamp))
                    .append(" reason=").append(reasons[info.reason] ?: info.reason.toString())
                    .append(" status=").append(info.status)
                    .append(" pss_kb=").append(info.pss)
                    .append(" rss_kb=").append(info.rss)
                    .append(" description=").append(info.description ?: "")
                    .append('\n')
            }
            File(dir, "last-exit.txt").writeText(text.toString())

            val nativeCrash = exits.firstOrNull {
                it.reason == ApplicationExitInfo.REASON_CRASH_NATIVE ||
                    it.reason == ApplicationExitInfo.REASON_ANR
            }
            nativeCrash?.traceInputStream?.use { input ->
                val name = if (nativeCrash.reason == ApplicationExitInfo.REASON_ANR) {
                    "last-anr.txt"
                } else {
                    "last-native-crash.pb"
                }
                FileOutputStream(File(dir, name)).use { output -> input.copyTo(output) }
            }
        } catch (e: Exception) {
            // Diagnostics only; never block startup.
        }
    }

    companion object {
        var documentsTree: DocumentsTree? = null
        lateinit var application: YuzuApplication

        val appContext: Context
            get() = application.applicationContext

        private val LANGUAGE_CODES = arrayOf(
            "system", "en", "es", "fr", "de", "it", "pt", "pt-BR", "ru", "ja", "ko",
            "zh-CN", "zh-TW", "pl", "cs", "nb", "hu", "uk", "vi", "id", "ar", "ckb", "fa", "he", "sr"
        )

        fun applyLanguage(context: Context): Context {
            val languageIndex = IntSetting.APP_LANGUAGE.getInt()
            val langCode = if (languageIndex in LANGUAGE_CODES.indices) {
                LANGUAGE_CODES[languageIndex]
            } else {
                "system"
            }

            if (langCode == "system") {
                return context
            }

            val locale = when {
                langCode.contains("-") -> {
                    val parts = langCode.split("-")
                    Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
                }
                else -> Locale.Builder().setLanguage(langCode).build()
            }

            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            config.setLocales(LocaleList(locale))

            return context.createConfigurationContext(config)
        }
    }
}
