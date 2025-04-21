package com.example.spendwise

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PasscodeActivity : AppCompatActivity() {
    private val CORRECT_PASSCODE = "1234"
    private var enteredPasscode = StringBuilder()
    private lateinit var tvPasscode: TextView
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passcode)

        tvPasscode = findViewById(R.id.tvPasscode)
        tvError = findViewById(R.id.tvError)

        setupKeypad()
    }

    private fun setupKeypad() {
        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                if (enteredPasscode.length < 4) {
                    enteredPasscode.append((it as Button).text)
                    updatePasscodeDisplay()
                }
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            enteredPasscode.clear()
            updatePasscodeDisplay()
            tvError.visibility = View.GONE
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            if (enteredPasscode.isNotEmpty()) {
                enteredPasscode.deleteCharAt(enteredPasscode.length - 1)
                updatePasscodeDisplay()
                tvError.visibility = View.GONE
            }
        }
    }

    private fun updatePasscodeDisplay() {
        val display = StringBuilder()
        for (i in 0 until 4) {
            if (i < enteredPasscode.length) {
                display.append("●")
            } else {
                display.append("_")
            }
        }
        tvPasscode.text = display.toString()

        if (enteredPasscode.length == 4) {
            validatePasscode()
        }
    }

    private fun validatePasscode() {
        if (enteredPasscode.toString() == CORRECT_PASSCODE) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            tvError.text = getString(R.string.wrong_passcode)
            tvError.visibility = View.VISIBLE
            enteredPasscode.clear()
            updatePasscodeDisplay()
        }
    }
} 