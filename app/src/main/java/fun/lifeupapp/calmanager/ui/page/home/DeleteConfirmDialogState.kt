package `fun`.lifeupapp.calmanager.ui.page.home

import `fun`.lifeupapp.calmanager.datasource.data.CalendarModel

data class DeleteConfirmDialogState(
    val calendars: List<CalendarModel>,
    val countdown: Int = 3
)
