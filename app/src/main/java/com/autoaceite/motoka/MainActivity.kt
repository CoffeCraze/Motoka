package com.autoaceite.motoka

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    companion object {
        const val PREFS = "auto_aceite"
        const val KEY_ATIVO = "ativo"
        const val KEY_PACOTE = "pacote"
        const val KEY_TEXTO = "texto"
        const val TEXTO_PADRAO = "Aceitar entrega"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val pad = (16 * resources.displayMetrics.density).toInt()

        val coluna = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }

        fun texto(t: String, tamanho: Float = 15f) = TextView(this).apply {
            text = t
            textSize = tamanho
            setPadding(0, pad / 2, 0, pad / 2)
        }

        coluna.addView(texto("Auto Aceite Motoka", 24f))
        coluna.addView(texto(
            "1) Toque em \"Ativar serviço de acessibilidade\" e ligue \"Auto Aceite Motoka\".\n" +
            "2) Volte aqui e ligue a chave abaixo.\n" +
            "3) Abra o app de entregas e fique online."
        ))

        val btnAcess = Button(this).apply {
            text = "Ativar serviço de acessibilidade"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        coluna.addView(btnAcess)

        val chave = Switch(this).apply {
            text = "Aceitar entregas automaticamente"
            textSize = 17f
            isChecked = prefs.getBoolean(KEY_ATIVO, false)
            setPadding(0, pad, 0, pad)
            setOnCheckedChangeListener { _, marcado ->
                prefs.edit().putBoolean(KEY_ATIVO, marcado).apply()
            }
        }
        coluna.addView(chave)

        coluna.addView(texto("Texto do botão a procurar:"))
        val campoTexto = EditText(this).apply {
            setText(prefs.getString(KEY_TEXTO, TEXTO_PADRAO))
            inputType = InputType.TYPE_CLASS_TEXT
        }
        coluna.addView(campoTexto)

        coluna.addView(texto("Pacote do app de entregas (opcional, deixa o app restrito só a ele):"))
        val campoPacote = EditText(this).apply {
            setText(prefs.getString(KEY_PACOTE, ""))
            hint = "ex.: com.empresa.motoka"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        coluna.addView(campoPacote)

        val salvar = Button(this).apply {
            text = "Salvar"
            setOnClickListener {
                prefs.edit()
                    .putString(KEY_TEXTO, campoTexto.text.toString().trim())
                    .putString(KEY_PACOTE, campoPacote.text.toString().trim())
                    .apply()
                text = "Salvo!"
                postDelayed({ text = "Salvar" }, 1200)
            }
        }
        coluna.addView(salvar)

        val scroll = ScrollView(this)
        scroll.addView(coluna, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setContentView(scroll)
    }
}
