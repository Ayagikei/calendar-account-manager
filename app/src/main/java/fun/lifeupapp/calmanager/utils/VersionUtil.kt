package `fun`.lifeupapp.calmanager.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES

/**
 * version util
 *
 * MIT License
 * Copyright (c) 2022 AyagiKei
 */
object VersionUtil {
    /**
     * get version code of the app
     *
     * @param ctx context
     *
     * @return int version code
     */
    fun getLocalVersion(ctx: Context): Long {
        return try {
            val packageInfo = getPackageInfo(ctx)
            if (VERSION.SDK_INT >= VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            logE(e)
            0
        }
    }

    /**
     * get version name of the app
     *
     * @param ctx context
     *
     * @return string version code
     */
    fun getLocalVersionName(ctx: Context): String {
        return try {
            val packageInfo = getPackageInfo(ctx)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            logE(e)
            ""
        }
    }

    private fun getPackageInfo(ctx: Context): PackageInfo {
        return (ctx.applicationContext ?: ctx)
            .packageManager
            .getPackageInfo(ctx.packageName, 0)
    }
}