package com.elise.countdown

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Comptes à rebours"

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val add = Button(this).apply {
            text = "+ Ajouter un compte à rebours"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, EditActivity::class.java))
            }
        }
        root.addView(add, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            setMargins(dp(16), dp(16), dp(16), 0)
        })

        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(24))
        }
        root.addView(container)

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        refresh()
        CountdownWidgetProvider.updateAll(this)
    }

    private fun refresh() {
        container.removeAllViews()
        val list = Store.load(this)

        if (list.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "Aucun compte à rebours pour l'instant.\n\n" +
                    "Pour l'afficher sur l'écran d'accueil : appui long sur un espace vide, " +
                    "puis Widgets, puis \"Comptes à rebours\". Tu peux en ajouter plusieurs, " +
                    "chacun lié à un compte à rebours différent."
                textSize = 15f
                setPadding(0, dp(16), 0, 0)
            })
            return
        }

        for (c in list) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
                background = GradientDrawable().apply {
                    setColor(0x14000000)
                    cornerRadius = dp(12).toFloat()
                }
                isClickable = true
                setOnClickListener {
                    startActivity(
                        Intent(this@MainActivity, EditActivity::class.java).putExtra("id", c.id)
                    )
                }
            }

            row.addView(TextView(this).apply {
                text = c.emoji
                textSize = 30f
                setPadding(0, 0, dp(14), 0)
            })

            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            col.addView(TextView(this).apply {
                text = c.name
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            col.addView(TextView(this).apply {
                text = Fmt.remaining(c.target)
                textSize = 16f
            })
            col.addView(TextView(this).apply {
                text = Fmt.date(c.target)
                textSize = 12f
                alpha = 0.7f
            })
            row.addView(col)

            container.addView(row, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                setMargins(0, dp(8), 0, 0)
            })
        }
    }
}
