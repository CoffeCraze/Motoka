package com.autoaceite.motoka

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AutoAceiteService : AccessibilityService() {

    private var ultimoClique = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val prefs = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(MainActivity.KEY_ATIVO, false)) return

        val pacoteAlvo = prefs.getString(MainActivity.KEY_PACOTE, "")?.trim().orEmpty()
        val textoBotao = prefs.getString(MainActivity.KEY_TEXTO, MainActivity.TEXTO_PADRAO)
            ?.trim().orEmpty().ifEmpty { MainActivity.TEXTO_PADRAO }

        // Evita cliques repetidos em sequência
        val agora = System.currentTimeMillis()
        if (agora - ultimoClique < 1500) return

        val raizes = mutableListOf<AccessibilityNodeInfo>()
        rootInActiveWindow?.let { raizes.add(it) }
        try {
            windows?.forEach { w -> w.root?.let { raizes.add(it) } }
        } catch (_: Exception) {
        }

        for (raiz in raizes) {
            if (pacoteAlvo.isNotEmpty() && raiz.packageName?.toString() != pacoteAlvo) continue
            val encontrados = raiz.findAccessibilityNodeInfosByText(textoBotao)
            for (no in encontrados) {
                if (clicar(no)) {
                    ultimoClique = System.currentTimeMillis()
                    return
                }
            }
        }
    }

    private fun clicar(no: AccessibilityNodeInfo): Boolean {
        // 1) tenta o clique "normal" no próprio nó ou em um pai clicável
        var alvo: AccessibilityNodeInfo? = no
        while (alvo != null && !alvo.isClickable) alvo = alvo.parent
        if (alvo != null && alvo.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true

        // 2) plano B: toque por gesto no centro do botão
        val r = Rect()
        no.getBoundsInScreen(r)
        if (r.width() <= 0 || r.height() <= 0) return false
        val path = Path().apply { moveTo(r.exactCenterX(), r.exactCenterY()) }
        val gesto = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 60))
            .build()
        return dispatchGesture(gesto, null, null)
    }

    override fun onInterrupt() {}
}
