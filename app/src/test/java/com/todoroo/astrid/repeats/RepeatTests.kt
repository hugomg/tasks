package com.todoroo.astrid.repeats

import com.natpryce.makeiteasy.MakeItEasy
import com.natpryce.makeiteasy.PropertyValue
import com.todoroo.astrid.alarms.AlarmService
import com.todoroo.astrid.dao.TaskDao
import com.todoroo.astrid.data.Task
import com.todoroo.astrid.gcal.GCalHelper
import com.todoroo.astrid.service.TaskCompleter
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.tasks.LocalBroadcastManager
import org.tasks.makers.TaskMaker
import org.tasks.time.DateTime

abstract class RepeatTests {
    private val alarmService = Mockito.mock(AlarmService::class.java)
    private val helper = RepeatTaskHelper(
            Mockito.mock(GCalHelper::class.java),
            alarmService,
            Mockito.mock(TaskDao::class.java),
            Mockito.mock(LocalBroadcastManager::class.java),
            Mockito.mock(TaskCompleter::class.java)
    )

    @Before
    fun before() {
        runBlocking {
            `when`(alarmService.getAlarms(anyLong())).thenReturn(emptyList())
            `when`(alarmService.synchronizeAlarms(anyLong(), anySet())).thenReturn(false)
        }
    }

    protected fun newDay(year: Int, month: Int, day: Int) =
            DateTime(
                    Task.createDueDate(
                            Task.URGENCY_SPECIFIC_DAY,
                            DateTime(year, month, day).millis
                    )
            )

    protected fun newDayTime(year: Int, month: Int, day: Int, hour: Int, minute: Int) =
            DateTime(
                    Task.createDueDate(
                            Task.URGENCY_SPECIFIC_DAY_TIME,
                            DateTime(year, month, day, hour, minute).millis
                    )
            )

    protected fun calculateNextDueDate(task: Task): DateTime = runBlocking {
        helper.handleRepeat(task)
        DateTime(task.dueDate)
    }

    protected fun newFromDue(
            recur: String,
            due: DateTime,
            vararg properties: PropertyValue<in Task?, *>,
            afterComplete: Boolean = false
    ) = TaskMaker.newTask(
            MakeItEasy.with(TaskMaker.RECUR, recur),
            MakeItEasy.with(TaskMaker.AFTER_COMPLETE, afterComplete),
            MakeItEasy.with(TaskMaker.DUE_TIME, due),
            *properties
    )
}