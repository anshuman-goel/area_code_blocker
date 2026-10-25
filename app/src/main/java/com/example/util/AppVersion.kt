package com.example.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object AppVersion {
    fun name(context: Context): String =
        packageInfo(context.packageManager, context.packageName).versionName.orEmpty()

    @Suppress("NewApi")
    internal fun packageInfo(
        packageManager: PackageManager,
        packageName: String,
        sdkInt: Int = Build.VERSION.SDK_INT,
    ): PackageInfo = if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }
}
