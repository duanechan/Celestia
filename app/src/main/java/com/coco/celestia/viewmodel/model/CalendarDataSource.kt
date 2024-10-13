package com.coco.celestia.viewmodel.model

import com.coco.celestia.util.getDaysOfMonthStartingFromMonday
import java.time.LocalDate
import java.time.YearMonth

class CalendarDataSource {
    fun getDates(yearMonth: YearMonth): List<CalendarUIState.Date> {
        return yearMonth.getDaysOfMonthStartingFromMonday()
            .map { date ->
                CalendarUIState.Date(
                    dayOfMonth = if (date.monthValue == yearMonth.monthValue) {
                        "${date.dayOfMonth}"
                    } else {
                        ""
                    },
                    isSelected = date.isEqual(LocalDate.now()) && date.monthValue == yearMonth.monthValue
                )
            }
    }
}