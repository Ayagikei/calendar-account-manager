package `fun`.lifeupapp.calmanager.utils

import android.util.Log

private const val COMMON_TAG = "CalManager"


/**
 * log utils
 *
 * MIT License
 * Copyright (c) 2022 AyagiKei
 */
fun logI(msg: String) {
    Log.i(COMMON_TAG, msg)
}


fun logD(msg: String) {
    Log.d(COMMON_TAG, msg)
}


fun logE(msg: String) {
    Log.e(COMMON_TAG, msg)
}

fun logE(throwable: Throwable) =
    logE("", throwable)

fun logE(msg: String, throwable: Throwable) {
    Log.e(COMMON_TAG, msg, throwable)
}

fun logI(tag: String, msg: String) = logI("$tag: $msg")

fun logD(tag: String, msg: String) = logD("$tag: $msg")

fun logE(tag: String, msg: String) = logE("$tag: $msg")

fun logE(tag: String, msg: String = "", throwable: Throwable) =
    logE("$tag: $msg", throwable)

fun logW(msg: String) {
    logW(COMMON_TAG, msg)
}

fun logW(tag: String, msg: String) {
    logW("$tag: $msg")
}