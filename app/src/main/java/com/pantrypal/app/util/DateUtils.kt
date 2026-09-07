package com.pantrypal.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private val dayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

fun LocalDate.formatShort(): String = format(dayFormatter)
