package com.elise.countdown

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date
import java.util.UUID

data class Countdown(val id: String, val name: String, val emoji: String, val target: Long)

object Store {
    private const val PREFS = "countdown_store"
    private const val KEY_LIST = "list"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(ctx: Context): List<Countdown> {
        val raw = prefs(ctx).getString(KEY_LIST, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val out = ArrayList<Countdown>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Countdown(
                    o.getString("id"),
                    o.getString("name"),
                    o.optString("emoji", "⏳"),
                    o.getLong("target")
                )
            )
        }
        return out.sortedBy { it.target }
    }

    private fun save(ctx: Context, list: List<Countdown>) {
        val arr = JSONArray()
        for (c in list) {
            arr.put(
                JSONObject()
                    .put("id", c.id)
                    .put("name", c.name)
                    .put("emoji", c.emoji)
                    .put("target", c.target)
            )
        }
        prefs(ctx).edit().putString(KEY_LIST, arr.toString()).apply()
    }

    fun find(ctx: Context, id: String?): Countdown? =
        if (id == null) null else load(ctx).firstOrNull { it.id == id }

    fun upsert(ctx: Context, c: Countdown) {
        val list = load(ctx).toMutableList()
        val i = list.indexOfFirst { it.id == c.id }
        if (i >= 0) list[i] = c else list.add(c)
        save(ctx, list)
    }

    fun delete(ctx: Context, id: String) {
        save(ctx, load(ctx).filter { it.id != id })
    }

    fun newId(): String = UUID.randomUUID().toString()

    // Association widget -> compte à rebours
    fun setWidget(ctx: Context, widgetId: Int, countdownId: String) {
        prefs(ctx).edit().putString("w$widgetId", countdownId).apply()
    }

    fun widgetCountdown(ctx: Context, widgetId: Int): Countdown? =
        find(ctx, prefs(ctx).getString("w$widgetId", null))

    fun removeWidget(ctx: Context, widgetId: Int) {
        prefs(ctx).edit().remove("w$widgetId").apply()
    }
}

object Fmt {
    fun remaining(target: Long, now: Long = System.currentTimeMillis()): String {
        val diff = target - now
        if (diff <= 0) return "Échéance passée"
        val mins = diff / 60000
        val days = mins / 1440
        val hours = (mins % 1440) / 60
        val m = mins % 60
        return when {
            days > 0 -> "$days j $hours h"
            hours > 0 -> "$hours h $m min"
            else -> "${if (m < 1) 1 else m} min"
        }
    }

    fun date(target: Long): String =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(target))
}

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
