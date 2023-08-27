package `fun`.lifeupapp.calmanager.datasource.data

import `fun`.lifeupapp.calmanager.common.Selectable

/**
 * CalendarModel define class
 *
 * MIT License
 * Copyright (c) 2023 AyagiKei
 */
data class CalendarModel(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val ownerName: String,
    override val isSelected: Boolean = false
) : Selectable