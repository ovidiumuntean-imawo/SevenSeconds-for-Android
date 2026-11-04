package com.imawo.sevenseconds.ui.fx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

internal const val K_SPARK = 0   // dâră luminoasă cu cap incandescent
internal const val K_SHARD = 1   // cioburi / confetti (segment gros rotitor)
internal const val K_EMBER = 2   // jar (cometa cronometrului)

/**
 * Sistem de particule fără alocări (array-uri primitive, ștergere „swap with last").
 * Coordonate în pixeli, în spațiul ferestrei.
 */
class ParticleSystem(private val cap: Int) {
    val x = FloatArray(cap)
    val y = FloatArray(cap)
    val vx = FloatArray(cap)
    val vy = FloatArray(cap)
    val life = FloatArray(cap)      // 1 → 0
    val decay = FloatArray(cap)     // 1 / durată
    val size = FloatArray(cap)
    val rot = FloatArray(cap)
    val spin = FloatArray(cap)
    val drag = FloatArray(cap)
    val grav = FloatArray(cap)
    val color = IntArray(cap)       // ARGB
    val kind = IntArray(cap)

    var count = 0
        private set

    fun add(
        kd: Int, px: Float, py: Float, pvx: Float, pvy: Float, sz: Float,
        lifeSec: Float, argb: Int,
        dragK: Float = 1.5f, gravity: Float = 0f, rotation: Float = 0f, spinRate: Float = 0f,
    ) {
        if (count >= cap) return
        val i = count++
        kind[i] = kd; x[i] = px; y[i] = py; vx[i] = pvx; vy[i] = pvy; size[i] = sz
        life[i] = 1f; decay[i] = 1f / lifeSec; color[i] = argb
        drag[i] = dragK; grav[i] = gravity; rot[i] = rotation; spin[i] = spinRate
    }

    fun clear() {
        count = 0
    }

    fun step(dt: Float) {
        var i = 0
        while (i < count) {
            life[i] -= decay[i] * dt
            if (life[i] <= 0f) {
                removeAt(i)
                continue
            }
            val k = 1f - drag[i] * dt
            val kk = if (k < 0f) 0f else k
            vx[i] *= kk
            vy[i] = vy[i] * kk + grav[i] * dt
            x[i] += vx[i] * dt
            y[i] += vy[i] * dt
            rot[i] += spin[i] * dt
            i++
        }
    }

    private fun removeAt(i: Int) {
        val l = count - 1
        if (i != l) {
            kind[i] = kind[l]; x[i] = x[l]; y[i] = y[l]; vx[i] = vx[l]; vy[i] = vy[l]
            life[i] = life[l]; decay[i] = decay[l]; size[i] = size[l]; rot[i] = rot[l]
            spin[i] = spin[l]; drag[i] = drag[l]; grav[i] = grav[l]; color[i] = color[l]
        }
        count = l
    }
}

internal fun DrawScope.drawParticles(ps: ParticleSystem) {
    for (i in 0 until ps.count) {
        val a = ps.life[i].coerceIn(0f, 1f)
        val c = Color(ps.color[i])
        val px = ps.x[i]
        val py = ps.y[i]
        val head = Offset(px, py)
        when (ps.kind[i]) {
            K_SPARK -> {
                val trail = 0.045f * (0.45f + a)
                val tail = Offset(px - ps.vx[i] * trail, py - ps.vy[i] * trail)
                val s = ps.size[i]
                drawLine(
                    color = c.copy(alpha = a * 0.9f), start = tail, end = head,
                    strokeWidth = s * (0.35f + 0.65f * a), cap = StrokeCap.Round, blendMode = BlendMode.Plus,
                )
                drawCircle(c.copy(alpha = a * 0.28f), radius = s * 1.9f, center = head, blendMode = BlendMode.Plus)
                drawCircle(
                    Color.White.copy(alpha = a), radius = s * 0.42f * (0.5f + 0.5f * a),
                    center = head, blendMode = BlendMode.Plus,
                )
            }
            K_SHARD -> {
                val h = ps.size[i]
                val dx = cos(ps.rot[i]) * h
                val dy = sin(ps.rot[i]) * h
                drawLine(
                    color = c.copy(alpha = (a * 1.4f).coerceAtMost(1f)),
                    start = Offset(px - dx, py - dy), end = Offset(px + dx, py + dy),
                    strokeWidth = h * 0.7f, cap = StrokeCap.Butt, blendMode = BlendMode.Plus,
                )
            }
            else -> {
                val r = ps.size[i] * (0.45f + 0.55f * a)
                drawCircle(c.copy(alpha = a * 0.22f), radius = r * 3f, center = head, blendMode = BlendMode.Plus)
                drawCircle(c.copy(alpha = a * 0.9f), radius = r, center = head, blendMode = BlendMode.Plus)
            }
        }
    }
}

/**
 * „Stardust" — echivalentul ParticleView-ului iOS (CAEmitterLayer), dar cu adâncime:
 * 3 straturi de paralaxă, licărire, derivă laterală și viteză care crește cu „heat".
 * Coordonate normalizate 0..1.
 */
class StarField(private val n: Int) {
    val sx = FloatArray(n)
    val sy = FloatArray(n)
    val sz = FloatArray(n)       // adâncime 0.25..1
    val sph = FloatArray(n)      // fază licărire
    val tint = IntArray(n)

    init {
        val r = Random(7)
        for (i in 0 until n) {
            sx[i] = r.nextFloat()
            sy[i] = r.nextFloat()
            sz[i] = 0.25f + r.nextFloat() * 0.75f
            sph[i] = r.nextFloat() * TAU
            tint[i] = r.nextInt(3)
        }
    }

    fun step(dt: Float, speedMul: Float, rng: Random) {
        for (i in 0 until n) {
            sy[i] += (0.010f + 0.030f * sz[i]) * speedMul * dt
            if (sy[i] > 1.02f) {
                sy[i] -= 1.04f
                sx[i] = rng.nextFloat()
            }
        }
    }
}

private val STAR_TINTS = arrayOf(Color.White, Color(0xFFBFE9FF), Color(0xFFE2C8FF))

internal fun DrawScope.drawStars(stars: StarField, t: Float, heat: Float) {
    val w = size.width
    val h = size.height
    val u = density
    for (i in 0 until stars.sx.size) {
        val z = stars.sz[i]
        val sway = sin(t * 0.35f + stars.sph[i]) * 0.012f * w * z
        val c = Offset(stars.sx[i] * w + sway, stars.sy[i] * h)
        val twinkle = 0.55f + 0.45f * sin(t * (1.2f + 2f * z) + stars.sph[i] * 3f)
        val a = ((0.25f + 0.75f * z) * twinkle * (0.8f + 0.5f * heat)).coerceIn(0f, 1f)
        val r = (0.7f + 2.1f * z) * u * (1f + 0.4f * heat)
        val col = STAR_TINTS[stars.tint[i]]
        drawCircle(col.copy(alpha = a), radius = r, center = c)
        if (z > 0.7f) drawCircle(col.copy(alpha = a * 0.18f), radius = r * 3.2f, center = c)
    }
}

// ───────────────────────── text plutitor („+1", bannere de rank) ─────────────────────────
internal const val F_PLUS = 0
internal const val F_BANNER = 1

class FloaterSystem(private val cap: Int) {
    val x = FloatArray(cap)
    val y0 = FloatArray(cap)
    val y1 = FloatArray(cap)
    val age = FloatArray(cap)
    val dur = FloatArray(cap)
    val kind = IntArray(cap)
    val heat = FloatArray(cap)
    val text = arrayOfNulls<String>(cap)

    var count = 0
        private set

    fun add(kd: Int, px: Float, startY: Float, endY: Float, durSec: Float, heatAt: Float, label: String) {
        if (count >= cap) removeAt(0)
        val i = count++
        kind[i] = kd; x[i] = px; y0[i] = startY; y1[i] = endY; dur[i] = durSec
        age[i] = 0f; heat[i] = heatAt; text[i] = label
    }

    fun step(dt: Float) {
        var i = 0
        while (i < count) {
            age[i] += dt / dur[i]
            if (age[i] >= 1f) {
                removeAt(i)
                continue
            }
            i++
        }
    }

    fun clear() {
        count = 0
    }

    private fun removeAt(i: Int) {
        val l = count - 1
        if (i != l) {
            kind[i] = kind[l]; x[i] = x[l]; y0[i] = y0[l]; y1[i] = y1[l]; age[i] = age[l]
            dur[i] = dur[l]; heat[i] = heat[l]; text[i] = text[l]
        }
        text[l] = null
        count = l
    }
}
