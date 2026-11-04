package com.imawo.sevenseconds.ui.fx

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.RenderEffect as ComposeRenderEffect

/** Până la 4 unde de șoc simultane. Timpul e în secunde „FxEngine.timeSec". */
class ShockwaveState {
    val ox = FloatArray(4)
    val oy = FloatArray(4)
    val t0 = FloatArray(4) { -1000f }
    val power = FloatArray(4)
    private var next = 0

    fun trigger(x: Float, y: Float, nowSec: Float, p: Float) {
        val i = next
        next = (next + 1) and 3
        ox[i] = x; oy[i] = y; t0[i] = nowSec; power[i] = p
    }

    fun anyActive(nowSec: Float): Boolean {
        for (i in 0 until 4) {
            val age = nowSec - t0[i]
            if (age >= 0f && age <= 1.3f) return true
        }
        return false
    }
}

/** Distorsiune reală a fundalului (AGSL, Android 13+). Pe versiuni mai vechi rămân doar inelele desenate. */
interface ShockwaveEffectProvider {
    fun effect(state: ShockwaveState, nowSec: Float, pxPerDp: Float): ComposeRenderEffect?
}

fun createShockwaveProvider(): ShockwaveEffectProvider? =
    if (Build.VERSION.SDK_INT >= 33) {
        try {
            AgslShockwave()
        } catch (ignored: Throwable) {
            null // shaderul nu s-a compilat pe acest dispozitiv ⇒ fallback la inele
        }
    } else {
        null
    }

private const val SHOCK_AGSL = """
uniform shader content;
uniform float k;
uniform float4 w0;
uniform float4 w1;
uniform float4 w2;
uniform float4 w3;

float2 shock(float2 p, float4 w) {
    if (w.z < 0.0) {
        return float2(0.0, 0.0);
    }
    float2 d = p - w.xy;
    float dist = max(length(d), 0.001);
    float r = w.z * 620.0 * k;
    float x = (dist - r) / (46.0 * k);
    float env = exp(-x * x) * exp(-w.z * 2.4) * w.w;
    return (d / dist) * (-sin(x * 2.0) * env * 16.0 * k);
}

half4 main(float2 fc) {
    float2 off = shock(fc, w0) + shock(fc, w1) + shock(fc, w2) + shock(fc, w3);
    return content.eval(fc + off);
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private class AgslShockwave : ShockwaveEffectProvider {
    private val shader = RuntimeShader(SHOCK_AGSL)
    private val names = arrayOf("w0", "w1", "w2", "w3")
    private var broken = false

    override fun effect(state: ShockwaveState, nowSec: Float, pxPerDp: Float): ComposeRenderEffect? {
        if (broken || !state.anyActive(nowSec)) return null
        return try {
            for (i in 0 until 4) {
                val age = nowSec - state.t0[i]
                val active = age >= 0f && age <= 1.3f
                shader.setFloatUniform(
                    names[i], state.ox[i], state.oy[i],
                    if (active) age else -1f, if (active) state.power[i] else 0f,
                )
            }
            shader.setFloatUniform("k", pxPerDp)
            RenderEffect.createRuntimeShaderEffect(shader, "content").asComposeRenderEffect()
        } catch (ignored: Throwable) {
            broken = true
            null
        }
    }
}
