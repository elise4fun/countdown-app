package com.elise.countdown

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) update(context, manager, id)
        scheduleTick(context)
    }

    override fun onEnabled(context: Context) {
        scheduleTick(context)
    }

    override fun onDisabled(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(tickIntent(context))
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        for (id in ids) Store.removeWidget(context, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TICK) updateAll(context)
    }

    companion object {
        const val ACTION_TICK = "com.elise.countdown.TICK"

        private fun tickIntent(context: Context): PendingIntent {
            val i = Intent(context, CountdownWidgetProvider::class.java).setAction(ACTION_TICK)
            return PendingIntent.getBroadcast(
                context, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // Rafraîchissement toutes les ~15 minutes (Android peut l'espacer en économie d'énergie)
        private fun scheduleTick(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                tickIntent(context)
            )
        }

        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, CountdownWidgetProvider::class.java))
            for (id in ids) update(context, mgr, id)
        }

        fun update(context: Context, mgr: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_countdown)
            val c = Store.widgetCountdown(context, widgetId)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

            if (c == null) {
                views.setTextViewText(R.id.w_emoji, "⏳")
                views.setTextViewText(R.id.w_name, "Touche pour choisir un compte à rebours")
                views.setTextViewText(R.id.w_remaining, "")
                views.setTextViewText(R.id.w_date, "")
                val cfg = Intent(context, WidgetConfigActivity::class.java)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                views.setOnClickPendingIntent(
                    R.id.w_root,
                    PendingIntent.getActivity(context, widgetId, cfg, flags)
                )
            } else {
                views.setTextViewText(R.id.w_emoji, c.emoji)
                views.setTextViewText(R.id.w_name, c.name)
                views.setTextViewText(R.id.w_remaining, Fmt.remaining(c.target))
                views.setTextViewText(R.id.w_date, Fmt.date(c.target))
                val open = Intent(context, MainActivity::class.java)
                views.setOnClickPendingIntent(
                    R.id.w_root,
                    PendingIntent.getActivity(context, 0, open, flags)
                )
            }
            mgr.updateAppWidget(widgetId, views)
        }
    }
}
