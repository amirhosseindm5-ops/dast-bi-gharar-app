package com.coldturkey.focus.data

import android.content.Context
import android.content.SharedPreferences

/**
 * ذخیره و مدیریت ۱۰۰٪ آفلاین و محلی وضعیت قفل، پکیج‌های مسدود و زمان پایان
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("cold_turkey_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_ACTIVE = "key_is_active"
        private const val KEY_IS_STRICT = "key_is_strict"
        private const val KEY_END_TIMESTAMP = "key_end_timestamp"
        private const val KEY_BLOCKED_PACKAGES = "key_blocked_packages"
        private const val KEY_WHITELISTED_PACKAGES = "key_whitelisted_packages"
        private const val KEY_ATTEMPTS_COUNT = "key_attempts_count"
    }

    fun startSession(durationMinutes: Int, blockedPackages: Set<String>, isStrict: Boolean) {
        val now = System.currentTimeMillis()
        val endTime = now + (durationMinutes * 60 * 1000L)

        prefs.edit()
            .putBoolean(KEY_IS_ACTIVE, true)
            .putBoolean(KEY_IS_STRICT, isStrict)
            .putLong(KEY_END_TIMESTAMP, endTime)
            .putStringSet(KEY_BLOCKED_PACKAGES, blockedPackages)
            .putInt(KEY_ATTEMPTS_COUNT, 0)
            .apply()
    }

    fun isSessionActive(): Boolean {
        val isActive = prefs.getBoolean(KEY_IS_ACTIVE, false)
        if (!isActive) return false

        val endTime = prefs.getLong(KEY_END_TIMESTAMP, 0L)
        if (System.currentTimeMillis() >= endTime) {
            // منقضی شدن خودکار جلسه
            stopSession()
            return false
        }
        return true
    }

    fun getRemainingSeconds(): Long {
        if (!isSessionActive()) return 0L
        val endTime = prefs.getLong(KEY_END_TIMESTAMP, 0L)
        val diff = (endTime - System.currentTimeMillis()) / 1000
        return if (diff > 0) diff else 0L
    }

    fun getBlockedApps(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun getWhitelistedApps(): Set<String> {
        return prefs.getStringSet(KEY_WHITELISTED_PACKAGES, setOf(
            "com.google.android.dialer",
            "com.google.android.apps.messaging",
            "com.google.android.apps.maps",
            "com.google.android.calculator"
        )) ?: emptySet()
    }

    fun setWhitelistedApps(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_WHITELISTED_PACKAGES, packages).apply()
    }

    fun isStrictMode(): Boolean {
        return prefs.getBoolean(KEY_IS_STRICT, false)
    }

    fun stopSession() {
        prefs.edit()
            .putBoolean(KEY_IS_ACTIVE, false)
            .putLong(KEY_END_TIMESTAMP, 0L)
            .apply()
    }

    fun recordBlockedAttempt(packageName: String) {
        val current = prefs.getInt(KEY_ATTEMPTS_COUNT, 0)
        prefs.edit().putInt(KEY_ATTEMPTS_COUNT, current + 1).apply()
    }

    fun getBlockedAttemptsCount(): Int {
        return prefs.getInt(KEY_ATTEMPTS_COUNT, 0)
    }
}
