package `fun`.lifeupapp.calmanager.datasource

import `fun`.lifeupapp.calmanager.datasource.data.CalendarModel
import `fun`.lifeupapp.calmanager.utils.logE
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract.Calendars
import androidx.core.database.getStringOrNull

/**
 * Utils for Calendar Reminders
 *
 * MIT License
 * Copyright (c) 2021 AyagiKei
 */

object CalendarDataSource {

    private val EVENT_PROJECTION = arrayOf(
        Calendars._ID,  // 0
        Calendars.ACCOUNT_NAME,  // 1
        Calendars.CALENDAR_DISPLAY_NAME,  // 2
        Calendars.OWNER_ACCOUNT // 3
    )

    private const val PROJECTION_ID_INDEX = 0
    private const val PROJECTION_ACCOUNT_NAME_INDEX = 1
    private const val PROJECTION_DISPLAY_NAME_INDEX = 2
    private const val PROJECTION_OWNER_ACCOUNT_INDEX = 3
    private const val CALENDER_URL = "content://com.android.calendar/calendars"

    /**
     * list accounts
     *
     * @param context Context
     * @return the id or -1L
     */
    fun listAccounts(context: Context): Result<List<CalendarModel>> {
        val list = ArrayList<CalendarModel>()
        try {
            context.contentResolver
                .query(Calendars.CONTENT_URI, EVENT_PROJECTION, null, null, null)
                .use { userCursor ->
                    userCursor ?: return Result.success(list)
                    userCursor.moveToFirst()
                    while (!userCursor.isAfterLast) {
                        try {
                            val calID: Long = userCursor.getLong(PROJECTION_ID_INDEX)
                            val displayName = userCursor.getStringOrNull(PROJECTION_DISPLAY_NAME_INDEX) ?: ""
                            val accountName: String = userCursor.getStringOrNull(PROJECTION_ACCOUNT_NAME_INDEX) ?: ""
                            val ownerName: String = userCursor.getStringOrNull(PROJECTION_OWNER_ACCOUNT_INDEX) ?: ""

                            list.add(CalendarModel(calID, displayName, accountName, ownerName))
                        } catch (e: Exception) {
                            logE(e)
                        }
                        userCursor.moveToNext()
                    }
                }
        } catch (e: Exception) {
            logE(e)
            return Result.failure(e)
        }
        return Result.success(list)
    }

    suspend fun deleteTheAccount(context: Context, id: Long): Result<Unit> {
        if (id >= 0) {
            kotlin.runCatching {
                val selection = "((${Calendars._ID} = ?))"
                val selectionArgs: Array<String> = arrayOf(id.toString())
                val calendarUri = Uri.parse(CALENDER_URL)
                context.contentResolver.delete(calendarUri, selection, selectionArgs)
            }.onSuccess {
                return Result.success(Unit)
            }.onFailure {
                logE(it)
                return Result.failure(it)
            }
        } else {
            // account is not existed, the delete action can be seen as success
            return Result.success(Unit)
        }
        return Result.success(Unit)
    }
}