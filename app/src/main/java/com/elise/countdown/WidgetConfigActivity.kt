package com.elise.countdown

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class WidgetConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Choisir un compte à rebours"

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Si l'utilisateur quitte sans choisir, le widget n'est pas ajouté
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }
        root.addView(TextView(this).apply {
            text = "Quel compte à rebours afficher dans ce widget ?"
            textSize = 16f
        })
        container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(container)
        root.addView(Button(this).apply {
            text = "+ Nouveau compte à rebours"
            setOnClickListener {
                startActivity(Intent(this@WidgetConfigActivity, EditActivity::class.java))
            }
        }, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(16) })

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) refresh()
    }

    private fun refresh() {
        container.removeAllViews()
        for (c in Store.load(this)) {
            container.addView(Button(this).apply {
                text = "${c.emoji}  ${c.name}  (${Fmt.remaining(c.target)})"
                isAllCaps = false
                setOnClickListener { choose(c) }
            }, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(8) })
        }
    }

    private fun choose(c: Countdown) {
        Store.setWidget(this, widgetId, c.id)
        CountdownWidgetProvider.update(this, AppWidgetManager.getInstance(this), widgetId)
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
    }
}
