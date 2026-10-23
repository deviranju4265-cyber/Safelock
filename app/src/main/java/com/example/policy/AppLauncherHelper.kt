package com.example.policy

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.example.data.model.InstalledAppInfo

class AppLauncherHelper(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    fun getInstalledLauncherApps(approvedPackages: Set<String>): List<InstalledAppInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )

        val ownPackage = context.packageName

        return resolveInfos
            .filter { it.activityInfo.packageName != ownPackage }
            .map { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(packageManager).toString()
                val isSystem = isEssentialSystemApp(pkgName)
                InstalledAppInfo(
                    packageName = pkgName,
                    appName = label,
                    isApproved = approvedPackages.contains(pkgName) || isSystem,
                    isSystemEssential = isSystem
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    fun isEssentialSystemApp(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return lower.contains("dialer") ||
                lower.contains("phone") ||
                lower.contains("telecom") ||
                lower.contains("contacts") ||
                lower.contains("emergency")
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                Log.w("AppLauncher", "No launch intent found for $packageName")
                false
            }
        } catch (e: Exception) {
            Log.e("AppLauncher", "Error launching $packageName", e)
            false
        }
    }

    fun launchEmergencyDialer() {
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Log.e("AppLauncher", "Failed to launch dialer", e)
        }
    }
}
