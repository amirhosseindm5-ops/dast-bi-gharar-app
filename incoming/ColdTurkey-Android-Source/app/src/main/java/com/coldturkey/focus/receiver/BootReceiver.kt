package com.coldturkey.focus.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coldturkey.focus.data.SessionManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val sessionManager = SessionManager(context)
            // اگر قفل قبل از خاموش شدن فعال بوده، با روشن شدن مجدد خودکار ادامه می‌یابد
            if (sessionManager.isSessionActive()) {
                // سرویس Accessibility توسط خود اندروید مجدداً راه‌اندازی می‌شود
            }
        }
    }
}
