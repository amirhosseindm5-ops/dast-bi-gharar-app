package com.coldturkey.focus.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.coldturkey.focus.data.SessionManager
import com.coldturkey.focus.ui.BlockOverlayActivity

/**
 * سرویس دسترسی‌پذیری اصلی برای مسدودسازی آنی برنامه‌ها.
 * این سرویس رویداد TYPE_WINDOW_STATE_CHANGED را پایش کرده
 * و به محض باز شدن پکیج هدف در زمان فعال بودن تایمر،
 * سریعاً دستور Home صادر کرده و صفحه قفل فارسی را نمایش می‌دهد.
 */
class AppBlockerAccessibilityService : AccessibilityService() {

    private lateinit var sessionManager: SessionManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        sessionManager = SessionManager(applicationContext)

        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 50
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceInfo = info
        Log.i("ColdTurkey", "Accessibility Service Connected & Active")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val foregroundPackage = event.packageName?.toString() ?: return

        // بستن خود سرویس یا صفحه اوورلی را نادیده بگیر
        if (foregroundPackage == packageName) return
        if (foregroundPackage == "com.android.systemui") return

        // بررسی اینکه آیا جلسه قفل فعال است و آیا پکیج جزو برنامه‌های مسدودشده است
        if (sessionManager.isSessionActive()) {
            // ۱. اولویت اول: بررسی لیست سفید (برنامه‌های ضروری همیشه مجاز مانند تماس و پیامک)
            val whitelistedApps = sessionManager.getWhitelistedApps()
            if (whitelistedApps.contains(foregroundPackage)) return

            val blockedApps = sessionManager.getBlockedApps()

            // اگر حالت سفت‌وسخت فعال باشد، ورود به بخش تنظیمات گوشی برای متوقف کردن برنامه را نیز بلاک می‌کنیم
            if (sessionManager.isStrictMode() && isSettingsPackage(foregroundPackage)) {
                triggerBlockAction(foregroundPackage, "دسترسی به تنظیمات در زمان قفل سفت‌وسخت مسدود است")
                return
            }

            if (blockedApps.contains(foregroundPackage)) {
                triggerBlockAction(foregroundPackage, null)
            }
        }
    }

    private fun triggerBlockAction(targetPackage: String, customMessage: String?) {
        // ۱. ابتدا دستور سخت‌افزاری بازگشت به صفحه اصلی Home برای قطع اجرای برنامه
        performGlobalAction(GLOBAL_ACTION_HOME)

        // ۲. باز کردن اکتیویتی اوورلی تمام‌صفحه با پیام فارسی و ثانیه‌شمار
        val overlayIntent = Intent(applicationContext, BlockOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(BlockOverlayActivity.EXTRA_PACKAGE_NAME, targetPackage)
            if (customMessage != null) {
                putExtra(BlockOverlayActivity.EXTRA_CUSTOM_MESSAGE, customMessage)
            }
        }
        startActivity(overlayIntent)
        sessionManager.recordBlockedAttempt(targetPackage)
    }

    private fun isSettingsPackage(pkg: String): Boolean {
        return pkg == "com.android.settings" || pkg.contains("settings")
    }

    override fun onInterrupt() {
        Log.w("ColdTurkey", "Accessibility Service Interrupted")
    }
}
