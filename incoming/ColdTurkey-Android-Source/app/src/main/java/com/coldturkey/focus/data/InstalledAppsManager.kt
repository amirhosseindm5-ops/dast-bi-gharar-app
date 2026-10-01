package com.coldturkey.focus.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppModel(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val isSystemApp: Boolean
)

/**
 * مدیر واکشی برنامه‌های نصب‌شده کاربر به همراه آیکون و عنوان واقعی
 */
class InstalledAppsManager(private val context: Context) {

    suspend fun getInstalledApps(): List<AppModel> = withContext(Dispatchers.IO) {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // واکشی تمام اکتیویتی‌های لانچر قابل اجرا توسط کاربر
        val resolveInfoList = packageManager.queryIntentActivities(mainIntent, 0)
        val selfPackage = context.packageName

        val appList = mutableListOf<AppModel>()

        for (resolveInfo in resolveInfoList) {
            val pkgName = resolveInfo.activityInfo.packageName
            // برنامه خودش یا لانچرهای سیستمی پایه را مستثنی کن
            if (pkgName == selfPackage) continue

            try {
                val appLabel = resolveInfo.loadLabel(packageManager).toString()
                val iconDrawable = resolveInfo.loadIcon(packageManager)
                val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

                appList.add(
                    AppModel(
                        packageName = pkgName,
                        appName = appLabel,
                        icon = iconDrawable,
                        isSystemApp = isSystem
                    )
                )
            } catch (e: Exception) {
                // برنامه‌های حذف‌شده یا غیرقابل‌دسترس
            }
        }

        // مرتب‌سازی الفبایی بر اساس نام برنامه
        appList.sortedBy { it.appName.lowercase() }
    }
}
