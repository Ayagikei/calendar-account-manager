package `fun`.lifeupapp.calmanager.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

object VersionUtil {
    /**
     * get version code of the app
     *
     * @param ctx context
     *
     * @return int version code
     */
    fun getLocalVersion(ctx: Context): Int {
        return try {
            val packageInfo = ctx.applicationContext
                .packageManager
                .getPackageInfo(ctx.packageName, 0)
            packageInfo.versionCode
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
            val packageInfo = ctx.applicationContext
                .packageManager
                .getPackageInfo(ctx.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            logE(e)
            ""
        }
    }
}