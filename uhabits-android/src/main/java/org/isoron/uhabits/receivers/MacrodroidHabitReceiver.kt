package org.isoron.uhabits.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.utils.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import kotlin.math.roundToInt

class MacrodroidHabitReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitList: HabitList

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("HABITS_TEST", "Intent Received!")
        if (intent.action != ACTION_QUERY_HABITS) return

        val app = context.applicationContext as HabitsApplication
        app.component.inject(this)

        val today = DateUtils.getTodayWithOffset()
        val habitsArray = JSONArray()

        for (habit in habitList) {
            if(habit.isArchived)
                continue
            val habitJson = JSONObject()
            habitJson.put("id", habit.id)
            habitJson.put("name", habit.name)
            habitJson.put("description", habit.description)
            habitJson.put("completed_today", habit.isCompletedToday())
            
            // Score
            val score = habit.scores[today]
            habitJson.put("score", (score.value * 100).roundToInt())

            // Streak
            val currentStreak = habit.streaks.getCurrentStreak(today)
            val streakLength = currentStreak?.length ?: 0
            habitJson.put("streak", streakLength)

            habitsArray.put(habitJson)
        }

        val resultIntent = Intent(ACTION_HABITS_DATA)
        resultIntent.putExtra(EXTRA_HABITS_JSON, habitsArray.toString())
        resultIntent.setPackage("com.arlosoft.macrodroid")
        
        context.sendBroadcast(resultIntent)
    }

    companion object {
        const val ACTION_QUERY_HABITS = "org.isoron.uhabits.ACTION_QUERY_HABITS"
        const val ACTION_HABITS_DATA = "org.isoron.uhabits.ACTION_HABITS_DATA"
        const val EXTRA_HABITS_JSON = "habits_json"
    }
}
