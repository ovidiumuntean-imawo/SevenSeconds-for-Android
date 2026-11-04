package com.imawo.sevenseconds.ui.fx

import android.os.SystemClock
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import com.imawo.sevenseconds.core.GameConfig
import com.imawo.sevenseconds.game.Phase
import com.imawo.sevenseconds.ui.theme.NeonBlue
import com.imawo.sevenseconds.ui.theme.NeonCyan
import com.imawo.sevenseconds.ui.theme.NeonOrange
import com.imawo.sevenseconds.ui.theme.NeonPurple
import com.imawo.sevenseconds.ui.theme.NeonRed
import com.imawo.sevenseconds.ui.theme.SysYellow
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

private val WHITE = 0xFFFFFFFF.toInt()
private val COOL = intArrayOf(NeonCyan.toArgb(), WHITE, NeonBlue.toArgb(), 0xFF7DF9FF.toInt())
private val HOT = intArrayOf(NeonOrange.toArgb(), SysYellow.toArgb(), WHITE, NeonRed.toArgb())
private val SHARDS = intArrayOf(NeonCyan.toArgb(), WHITE, NeonBlue.toArgb(), NeonPurple.toArgb())
private val EMBERS = intArrayOf(NeonOrange.toArgb(), SysYellow.toArgb(), NeonRed.toArgb(), WHITE)
private val GOLD = intArrayOf(SysYellow.toArgb(), WHITE, NeonOrange.toArgb(), 0xFFFF4DA6.toInt())

/**
 * Motorul de efecte: UN SINGUR ceas pentru tot ce se mișcă (particule, inele rotitoare, glitch, shake, unde de șoc).
 * Compozabilele de desen citesc `subscribe()` în faza de desenare ⇒ se redesenează la fiecare cadru FĂRĂ recompoziție.
 *
 * „heat" (0..1) = cât de repede tastezi. Controlează culoarea scânteilor, viteza inelelor,
 * intensitatea fulgerelor din buton, viteza stardust-ului, tăria haptics-urilor și a shake-ului.
 */
@Stable
class FxEngine {
    private val tickState = mutableLongStateOf(0L)

    /** Apelează în blocuri de desen / graphicsLayer ca să te abonezi la cadre. */
    fun subscribe(): Long = tickState.longValue

    /** Zecimi de secundă rămase — se schimbă doar la 10 Hz (pentru text). */
    val timeLeftTenths = mutableIntStateOf((GameConfig.DURATION_MS / 100L).toInt())

    var reduce by mutableStateOf(false)

    var timeSec = 0f
        private set
    var running = false
        private set
    var timeLeftMs = GameConfig.DURATION_MS
        private set
    var heat = 0f
        private set

    /** 0 → 1 în ultimele 3 secunde. */
    val urgency: Float
        get() = if (!running) 0f else saturate((3000f - timeLeftMs) / 3000f)

    // ── layout (px, coordonate rădăcină) ──
    var density = 1f
    var width = 0f
    var height = 0f
    var buttonCenter = Offset.Zero
    var timerCenter = Offset.Zero
    var timerRadius = 0f

    // ── subsisteme ──
    val sparks = ParticleSystem(520)
    val stars = StarField(110)
    val shock = ShockwaveState()
    val floaters = FloaterSystem(24)

    // ── scalari de efect ──
    var shakeX = 0f
        private set
    var shakeY = 0f
        private set
    var flash = 0f
        private set
    var pulse = 0f
        private set
    var scorePop = 0f
        private set
    var ring1 = 0f
        private set
    var ring2 = 0f
        private set
    var glitch = 0f
        private set
    var glitchOffset = 0f
        private set
    val gbY = FloatArray(3)
    val gbH = FloatArray(3)
    val gbDx = FloatArray(3)

    private var shakeAmp = 0f
    private var glitchAcc = 0f
    private var emberAcc = 0f
    private var lastShockT = -10f
    private var lastNanos = 0L
    private val rng = Random(System.nanoTime())

    // ───────────────────────── cadru ─────────────────────────
    fun frame(nowNanos: Long, phase: Phase, startElapsedMs: Long) {
        val dt = if (lastNanos == 0L) 0.016f
        else ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastNanos = nowNanos
        val slow = if (reduce) 0.6f else 1f
        timeSec += dt * slow
        running = phase == Phase.Running

        timeLeftMs = when (phase) {
            Phase.Running ->
                (GameConfig.DURATION_MS - (SystemClock.elapsedRealtime() - startElapsedMs))
                    .coerceIn(0L, GameConfig.DURATION_MS)
            Phase.Idle -> GameConfig.DURATION_MS
            Phase.Over -> 0L
        }
        val tenths = (timeLeftMs / 100L).toInt()
        if (tenths != timeLeftTenths.intValue) timeLeftTenths.intValue = tenths

        heat *= exp(-dt / 0.7f)
        pulse *= exp(-dt * 9f)
        scorePop *= exp(-dt * 11f)
        flash *= exp(-dt * 4.5f)
        shakeAmp *= exp(-dt * 16f)
        if (reduce) {
            shakeX = 0f; shakeY = 0f
        } else {
            shakeX = (rng.nextFloat() * 2f - 1f) * shakeAmp
            shakeY = (rng.nextFloat() * 2f - 1f) * shakeAmp
        }
        ring1 = (ring1 + (120f + 260f * heat) * dt * slow) % 360f
        ring2 = (ring2 + (80f + 210f * heat) * dt * slow) % 360f

        // glitch: 20 Hz, la fel ca GlitchScoreView de pe iOS, dar cu benzi reale și mai agresiv când „e foc"
        glitchAcc += dt
        while (glitchAcc >= 0.05f) {
            glitchAcc -= 0.05f
            if (running && !reduce && rng.nextFloat() > 0.8f - 0.25f * heat) {
                glitch = 0.5f + rng.nextFloat() * 0.5f
                glitchOffset = (rng.nextFloat() * 2f - 1f) * 15f
                for (b in 0 until 3) {
                    gbY[b] = (rng.nextFloat() - 0.5f) * 0.8f
                    gbH[b] = 5f + rng.nextFloat() * 10f
                    gbDx[b] = (rng.nextFloat() * 2f - 1f) * 20f
                }
            } else {
                glitch *= 0.5f
                glitchOffset *= 0.5f
            }
        }

        // jar din capul cometei cronometrului
        if (running && timerRadius > 0f) {
            emberAcc += dt * ((if (reduce) 8f else 26f) + 40f * urgency)
            while (emberAcc >= 1f) {
                emberAcc -= 1f
                emitEmber()
            }
        }

        sparks.step(dt)
        stars.step(dt, (1f + heat * 2.2f) * (if (reduce) 0.4f else 1f), rng)
        floaters.step(dt)
        tickState.longValue = nowNanos
    }

    private fun emitEmber() {
        val p = saturate(timeLeftMs / GameConfig.DURATION_MS.toFloat())
        val ang = Math.toRadians((180f + 180f * p).toDouble()).toFloat()
        val hx = timerCenter.x + timerRadius * cos(ang)
        val hy = timerCenter.y + timerRadius * sin(ang)
        val u = density
        val a = ang + (rng.nextFloat() - 0.5f) * 1.6f
        val sp = (20f + rng.nextFloat() * 70f) * u
        sparks.add(
            K_EMBER, hx, hy, cos(a) * sp, sin(a) * sp - 25f * u, (1.4f + rng.nextFloat() * 2.6f) * u,
            0.5f + rng.nextFloat() * 0.7f, EMBERS[rng.nextInt(EMBERS.size)], dragK = 1.2f, gravity = -30f * u,
        )
    }

    // ───────────────────────── evenimente ─────────────────────────
    fun onStart() {
        heat = 0.35f
        pulse = 1f
        if (!reduce) flash = max(flash, 0.18f)
        val c = buttonCenter
        val u = density
        shock.trigger(c.x, c.y, timeSec, 0.9f)
        val n = if (reduce) 10 else 28
        for (i in 0 until n) {
            val a = i * TAU / n
            sparks.add(
                K_SPARK, c.x + cos(a) * 90f * u, c.y + sin(a) * 90f * u, cos(a) * 560f * u, sin(a) * 560f * u,
                4f * u, 0.55f, COOL[i % COOL.size], dragK = 3f,
            )
        }
    }

    fun onTap() {
        val u = density
        val c = buttonCenter
        heat += (1f - heat) * 0.4f
        pulse = 1f
        scorePop = 1f

        val hot = smoothstep(0.55f, 0.9f, heat)
        val n = if (reduce) 4 else 7 + (heat * 9f).toInt()
        for (k in 0 until n) {
            val a = rng.nextFloat() * TAU
            val sp = (170f + rng.nextFloat() * 430f) * u * (0.85f + 0.6f * heat)
            val pal = if (rng.nextFloat() < hot) HOT else COOL
            sparks.add(
                K_SPARK, c.x + cos(a) * 78f * u, c.y + sin(a) * 78f * u, cos(a) * sp, sin(a) * sp,
                (2.2f + rng.nextFloat() * 3.6f) * u, 0.32f + rng.nextFloat() * 0.45f,
                pal[rng.nextInt(pal.size)], dragK = 3.2f,
            )
        }
        if (!reduce) shakeAmp = max(shakeAmp, (2.2f + 5f * heat) * u)
        if (timeSec - lastShockT > 0.09f) {
            lastShockT = timeSec
            shock.trigger(c.x, c.y, timeSec, 0.55f + 0.45f * heat)
        }
        floaters.add(
            F_PLUS, c.x + (rng.nextFloat() * 60f - 30f) * u, c.y - (50f + rng.nextFloat() * 20f) * u,
            c.y - 320f * u, 0.8f, heat, "+1",
        )
    }

    /** Ai atins un rank nou în timpul rundei. */
    fun milestone(title: String) {
        val u = density
        floaters.add(F_BANNER, width / 2f, height * 0.29f, height * 0.29f, 1.5f, 1f, title)
        shock.trigger(width / 2f, height * 0.4f, timeSec, 1.2f)
        if (!reduce) flash = max(flash, 0.28f)
        val c = buttonCenter
        for (i in 0 until (if (reduce) 16 else 44)) {
            val a = rng.nextFloat() * TAU
            val sp = (260f + rng.nextFloat() * 620f) * u
            sparks.add(
                K_SPARK, c.x, c.y, cos(a) * sp, sin(a) * sp, (3f + rng.nextFloat() * 4f) * u,
                0.5f + rng.nextFloat() * 0.5f, GOLD[rng.nextInt(GOLD.size)], dragK = 2.6f,
            )
        }
    }

    /** Explozia de la final — ecoul pictogramei (buton care explodează în cioburi). */
    fun onFinish() {
        val u = density
        val c = buttonCenter
        for (i in 0 until (if (reduce) 24 else 72)) {
            val a = rng.nextFloat() * TAU
            val sp = (260f + rng.nextFloat() * 720f) * u
            sparks.add(
                K_SHARD, c.x, c.y, cos(a) * sp, sin(a) * sp - 120f * u, (4f + rng.nextFloat() * 7f) * u,
                0.9f + rng.nextFloat() * 0.8f, SHARDS[rng.nextInt(SHARDS.size)],
                dragK = 0.7f, gravity = 900f * u, rotation = rng.nextFloat() * TAU,
                spinRate = (rng.nextFloat() * 2f - 1f) * 14f,
            )
        }
        for (i in 0 until (if (reduce) 20 else 64)) {
            val a = rng.nextFloat() * TAU
            val sp = (300f + rng.nextFloat() * 800f) * u
            sparks.add(
                K_SPARK, c.x, c.y, cos(a) * sp, sin(a) * sp, (3f + rng.nextFloat() * 5f) * u,
                0.5f + rng.nextFloat() * 0.6f, COOL[rng.nextInt(COOL.size)], dragK = 2.8f,
            )
        }
        shock.trigger(c.x, c.y, timeSec, if (reduce) 0.8f else 1.7f)
        flash = if (reduce) 0.15f else 0.9f
        if (!reduce) shakeAmp = 15f * u
        pulse = 1f
    }

    /** NEW RECORD — confetti auriu din partea de sus a ecranului. */
    fun celebrate() {
        val u = density
        for (i in 0 until (if (reduce) 30 else 90)) {
            val a = (rng.nextFloat() * 1.2f + 0.2f) * 3.1415927f   // în jos, evantai
            val sp = (180f + rng.nextFloat() * 520f) * u
            sparks.add(
                K_SHARD, width * (0.25f + rng.nextFloat() * 0.5f), height * 0.12f,
                cos(a) * sp, sin(a) * sp, (3f + rng.nextFloat() * 5f) * u,
                1.2f + rng.nextFloat() * 1.0f, GOLD[rng.nextInt(GOLD.size)],
                dragK = 1.1f, gravity = 520f * u, rotation = rng.nextFloat() * TAU,
                spinRate = (rng.nextFloat() * 2f - 1f) * 10f,
            )
        }
        shock.trigger(width / 2f, height * 0.3f, timeSec, 1.0f)
    }

    fun resetForIdle() {
        sparks.clear()
        floaters.clear()
        heat = 0f
        flash = 0f
        shakeAmp = 0f
    }
}
