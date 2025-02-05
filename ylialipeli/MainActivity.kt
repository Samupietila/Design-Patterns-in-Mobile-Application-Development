package com.example.ylialipeli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ylialipeli.ui.theme.YliAliPeliTheme
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YliAliPeliTheme {

                pelaa()
                }
            }
        }
    }

enum class Pelitulos {
    ali, osuma, yli
}
class YliAli(var alaraja: Int = 1, var ylaraja: Int = 10, var numero: Int = (alaraja..ylaraja).random(), var arvaukset: Int = 0) {
    fun arvaa(arvaus: Int): Pelitulos {
        arvaukset++
        return if (arvaus < this.numero) Pelitulos.ali
        else if (arvaus > this.numero) Pelitulos.yli
        else Pelitulos.osuma
    }
}

@Composable
fun pelaa() {
    var peli = remember { YliAli() }
    var resultText by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    Column(Modifier.padding(vertical = 60.dp)) {
        Text("Tervetuloa pelaamaan YliAliPeliä")

        TextField(
            value = text,
            label = { Text("Arvaa")},
            onValueChange = { uusiArvo ->
                val numero = uusiArvo.toIntOrNull()
                text = if (numero == null) {
                    ""
                }
                else if (numero < peli.alaraja) {
                    "Liian pieni arvaus!"
                }
                else if (numero > peli.ylaraja) {
                    "Liian suuri arvaus!"
                } else {
                    uusiArvo
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(onClick = {
            val guess = text.toIntOrNull()
            if (guess != null) {
                 val result= peli.arvaa(guess)
                resultText = when (result) {
                    Pelitulos.ali -> "Ali!"
                    Pelitulos.yli -> "Yli!"
                    Pelitulos.osuma -> "Oikein! Arvausten määrä: ${peli.arvaukset}"
                }
            }
        }) { Text("click!") }
        Text(resultText)
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun pelaaPeliPreview() {
    Column {
        pelaa()
    }
}