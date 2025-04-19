package com.example.ylialipeliviews

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var peli = YliAliPeli(1,50)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val inputField = findViewById<EditText>(R.id.guessInput)
        val guessButton = findViewById<Button>(R.id.button)
        val outputField = findViewById<TextView>(R.id.textView3)
        val guessAmount = findViewById<TextView>(R.id.textView2)
        guessButton.setOnClickListener { pelaaPeli(inputField,guessButton,outputField,guessAmount)}

    }
    fun resetoiPeli(inputField:EditText,guessButton: Button, outputField: TextView, guessAmount: TextView) {
        peli = YliAliPeli(1,50)
        guessButton.setOnClickListener { pelaaPeli(inputField, guessButton,outputField,guessAmount) }
        guessButton.text = "Paina arvataksesi!"
        outputField.text = ""
        guessAmount.text = "Arvaa 1—50"

    }
    fun pelaaPeli(inputField: EditText, guessButton: Button, outputField: TextView, guessAmount: TextView) {
        val textInput = inputField.text.toString()
        val guess = textInput.toIntOrNull()
        inputField.text?.clear()
        guess?.let {
            if (it in peli.low..peli.high) {
                val result = peli.arvaa(guess)
                when (result) {
                    YliAliPeli.Arvaustulos.Low -> outputField.text = "($guess)Liian pieni!"
                    YliAliPeli.Arvaustulos.Hit -> {
                        outputField.text = "OSUMA!"
                        guessAmount.text = "Arvausten määrä: ${peli.guesses}"
                        guessButton.text = "PELAA UUDESTAAN!"
                        guessButton.setOnClickListener { resetoiPeli(inputField,guessButton,outputField,guessAmount) }
                    }

                    YliAliPeli.Arvaustulos.High -> outputField.text = "($guess)Liian suuri!"
                }
            } else {
                outputField.text = "$guess ≠ 1 — 50"
            }
        } ?: run {
            outputField.text = "!!VIRHE!!"
        }

    }
}


class YliAliPeli(val low: Int, val high:
Int) {
    val secret = (low..high).random()
    var guesses = 0
    fun arvaa(arvaus: Int): Arvaustulos {
        guesses++
        return if (arvaus > secret)
            Arvaustulos.High
        else if (arvaus < secret)
            Arvaustulos.Low
        else Arvaustulos.Hit
    }
    enum class Arvaustulos {
        Low, Hit, High
    }

}


