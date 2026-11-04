package com.elise.countdown

import android.app.AlertDialog
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

class EditActivity : Activity() {

    private var existing: Countdown? = null
    private val cal: Calendar = Calendar.getInstance()

    private lateinit var emojiField: EditText
    private lateinit var nameField: EditText
    private lateinit var dateBtn: Button
    private lateinit var timeBtn: Button

    private val presets = listOf(
        "🎂", "🎉", "✈️", "🏖️", "🎄", "💍", "🎓", "💼",
        "🏠", "🚀", "🛰️", "🔋", "📅", "❤️", "⚽", "⏳"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ex = Store.find(this, intent.getStringExtra("id"))
        existing = ex
        title = if (ex == null) "Nouveau compte à rebours" else "Modifier"

        if (ex != null) {
            cal.timeInMillis = ex.target
        } else {
            cal.add(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }

        root.addView(label("Icône (emoji)"))
        emojiField = EditText(this).apply {
            setText(ex?.emoji ?: "⏳")
            textSize = 28f
            filters = arrayOf(InputFilter.LengthFilter(8))
            hint = "⏳"
        }
        root.addView(emojiField)

        val presetRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (p in presets) {
            presetRow.addView(TextView(this).apply {
                text = p
                textSize = 26f
                setPadding(dp(8), dp(4), dp(8), dp(4))
                setOnClickListener { emojiField.setText(p) }
            })
        }
        root.addView(HorizontalScrollView(this).apply { addView(presetRow) })

        root.addView(label("Nom"))
        nameField = EditText(this).apply {
            setText(ex?.name ?: "")
            hint = "Ex : Vacances"
            setSingleLine(true)
        }
        root.addView(nameField)

        root.addView(label("Date d'échéance"))
        dateBtn = Button(this).apply {
            setOnClickListener {
                DatePickerDialog(
                    this@EditActivity,
                    { _, y, m, d ->
                        cal.set(y, m, d)
                        refreshButtons()
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        root.addView(dateBtn)

        root.addView(label("Heure"))
        timeBtn = Button(this).apply {
            setOnClickListener {
                TimePickerDialog(
                    this@EditActivity,
                    { _, h, mi ->
                        cal.set(Calendar.HOUR_OF_DAY, h)
                        cal.set(Calendar.MINUTE, mi)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        refreshButtons()
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true
                ).show()
            }
        }
        root.addView(timeBtn)

        val save = Button(this).apply {
            text = "Enregistrer"
            setOnClickListener { save() }
        }
        root.addView(save, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            topMargin = dp(24)
        })

        if (ex != null) {
            val del = Button(this).apply {
                text = "Supprimer"
                setOnClickListener { confirmDelete(ex) }
            }
            root.addView(del)
        }

        setContentView(ScrollView(this).apply { addView(root) })
        refreshButtons()
    }

    private fun label(t: String) = TextView(this).apply {
        text = t
        textSize = 13f
        alpha = 0.7f
        setPadding(0, dp(16), 0, 0)
    }

    private fun refreshButtons() {
        val f = java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG)
        dateBtn.text = f.format(cal.time)
        timeBtn.text = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    private fun save() {
        val name = nameField.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Donne un nom au compte à rebours", Toast.LENGTH_SHORT).show()
            return
        }
        val emoji = emojiField.text.toString().trim().ifEmpty { "⏳" }
        val c = Countdown(existing?.id ?: Store.newId(), name, emoji, cal.timeInMillis)
        Store.upsert(this, c)
        CountdownWidgetProvider.updateAll(this)
        finish()
    }

    private fun confirmDelete(c: Countdown) {
        AlertDialog.Builder(this)
            .setMessage("Supprimer \"${c.name}\" ?")
            .setPositiveButton("Supprimer") { _, _ ->
                Store.delete(this, c.id)
                CountdownWidgetProvider.updateAll(this)
                finish()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
