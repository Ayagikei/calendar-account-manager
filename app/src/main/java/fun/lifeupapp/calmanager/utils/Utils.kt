package `fun`.lifeupapp.calmanager.utils

import `fun`.lifeupapp.calmanager.R.string
import android.content.Context
import android.content.Intent
import android.net.Uri
import splitties.toast.toast

/**
 * some utils
 *
 *  MIT License
 * Copyright (c) 2022 AyagiKei
 */
fun launchStorePage(context: Context, packageName: String) {
    try {
        val uri = Uri.parse("market://details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        toast(string.about_not_found_android_store)
        logE(e)
    }
}