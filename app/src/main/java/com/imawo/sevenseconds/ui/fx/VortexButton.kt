package com.imawo.sevenseconds.ui.fx

import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imawo.sevenseconds.R
import com.imawo.sevenseconds.core.GameConfig
import com.imawo.sevenseconds.ui.theme.NeonBlue
import com.imawo.sevenseconds.ui.theme.NeonCyan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.launch

/** Geometria și stilurile care depind doar de densitate — create o singură dată, nu în fiecare cadru. */
internal class VortexStyle(u: Float) {
    val ringThin = Stroke(
        1.5f * u, cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f * u, 40f * u, 10f * u, 30f * u)),
    )
    val ringThick = Stroke(
        3f * u, cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f * u, 50f * u, 20f * u, 40f * u)),
    )
    val arcWide = Stroke(8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val arcMid = Stroke(3.2f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val arcThin = Stroke(1.3f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val rim = Stroke(2f * u)
    val boltGlow = Stroke(7f * u, join = StrokeJoin.Round)
    val arcPath = Path()

    /** Fulgerul, centrat pe (0,0), 60dp înălțime. */
    val bolt = Path().apply {
        val pts = floatArrayOf(
            0.55f, 0.00f, 0.10f, 0.58f, 0.40f, 0.58f, 0.30f, 1.00f, 0.90f, 0.36f, 0.58f, 0.36f, 0.76f, 0.00f,
        )
        val h = 60f * u
        var i = 0
        while (i < pts.size) {
            val x = (pts[i] - 0.5f) * h
            val y = (pts[i + 1] - 0.5f) * h
            if (i == 0) moveTo(x, y) else lineTo(x, y)
            i += 2
        }
        close()
    }
}

/**
 * Butonul „Vortex Electric" — portul lui ArcButton din iOS, cu adaosuri:
 *  - fulgere procedurale care se regenerează ~16–26 Hz (mai multe și mai groase cu cât tastezi mai repede);
 *  - inelele se rotesc mai repede cu „heat";
 *  - bloom real (blur) pe Android 12+;
 *  - tap-ul se înregistrează la ATINGERE (nu la ridicarea degetului) și suportă mai multe degete.
 */
@Composable
fun VortexButton(
    fx: FxEngine,
    deployed: Boolean,
    enabled: Boolean,
    onPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val u = LocalDensity.current.density
    val style = remember(u) { VortexStyle(u) }
    val deploy = remember { Animatable(0f) }
    val deployFlash = remember { Animatable(0f) }

    LaunchedEffect(deployed) {
        if (deployed) {
            deployFlash.snapTo(1f)
            launch { deployFlash.animateTo(0f, tween(600)) }
            deploy.animateTo(1f, spring(dampingRatio = 0.85f, stiffness = 50f))
        } else {
            deploy.snapTo(0f)
        }
    }

    var pressed by remember { mutableStateOf(false) }
    val press = animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
        label = "vortexPress",
    )
    val onPressState = rememberUpdatedState(onPress)
    val description = stringResource(R.string.cd_tap_button)
    val tapLabel = stringResource(R.string.tap_label)

    Box(
        modifier
            .size(300.dp)
            .graphicsLayer {
                val d = deploy.value
                val s = (0.01f + 0.99f * d) * (1f - 0.07f * press.value)
                scaleX = s
                scaleY = s
                alpha = d.coerceIn(0f, 1f)
                translationY = -60f * (1f - d) * density
            }
            .semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
                onClick {
                    onPressState.value()
                    true
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        var down = false
                        val othersDown = event.changes.any { it.pressed && it.previousPressed }
                        for (ch in event.changes) {
                            if (ch.changedToDown()) {
                                if (GameConfig.ALLOW_MULTITOUCH || !othersDown) down = true
                                ch.consume()
                            }
                        }
                        pressed = event.changes.any { it.pressed }
                        if (down) onPressState.value()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (Build.VERSION.SDK_INT >= 31 && !fx.reduce) {
            Canvas(Modifier.fillMaxSize().blur(18.dp, BlurredEdgeTreatment.Unbounded)) {
                fx.subscribe()
                drawVortexBloom(fx, press.value, style)
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            fx.subscribe()
            drawVortexMain(fx, press.value, style)
            if (deployFlash.value > 0.01f) {
                drawCircle(
                    Color.White.copy(alpha = deployFlash.value * 0.9f), radius = 100f * density,
                    center = center, blendMode = BlendMode.Plus,
                )
            }
        }
        Text(
            text = tapLabel,
            color = Color.White.copy(alpha = if (pressed) 0.45f else 0.8f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 3.sp,
            modifier = Modifier.offset(y = 58.dp),
        )
    }
}

// ───────────────────────── desen ─────────────────────────

private fun DrawScope.drawVortexMain(fx: FxEngine, press: Float, st: VortexStyle) {
    val u = density
    val c = center
    val t = fx.timeSec
    val heat = fx.heat
    val kick = fx.pulse

    // 1. aura exterioară
    val glowR = 140f * u * (1f + 0.2f * press + 0.12f * kick)
    drawCircle(
        Brush.radialGradient(
            listOf(NeonCyan.copy(alpha = 0.30f + 0.14f * heat + 0.16f * kick), Color.Transparent),
            center = c, radius = glowR,
        ),
        radius = glowR, center = c,
    )

    // 2. inele punctate contra-rotative
    rotate(fx.ring1, c) {
        drawCircle(
            Color.White.copy(alpha = 0.6f), radius = 102.5f * u, center = c,
            style = st.ringThin, blendMode = BlendMode.Plus,
        )
    }
    rotate(-fx.ring2, c) {
        drawCircle(
            NeonCyan, radius = 102.5f * u, center = c,
            style = st.ringThick, blendMode = BlendMode.Plus,
        )
    }

    // 3. nucleul
    val coreR = 90f * u * (1f + 0.014f * sin(t * 3.1f) + 0.07f * kick - 0.06f * press)
    drawCircle(NeonCyan.copy(alpha = 0.10f), radius = coreR + 14f * u, center = c)
    drawCircle(NeonCyan.copy(alpha = 0.16f), radius = coreR + 7f * u, center = c)
    drawCircle(
        Brush.radialGradient(
            listOf(Color.White, NeonCyan, NeonBlue.copy(alpha = 0.8f), Color.Black),
            center = c, radius = coreR,
        ),
        radius = coreR, center = c,
    )
    drawCircle(Color.White.copy(alpha = 0.5f), radius = coreR, center = c, style = st.rim)
    if (press > 0.01f) {
        drawCircle(Color.White.copy(alpha = 0.22f * press), radius = coreR * 0.92f, center = c, blendMode = BlendMode.Plus)
    }
    if (kick > 0.02f) {
        drawCircle(
            Color.White.copy(alpha = 0.6f * kick), radius = (90f + 70f * (1f - kick)) * u,
            center = c, style = st.rim, blendMode = BlendMode.Plus,
        )
    }

    // 4. fulgere
    drawArcs(fx, st, c, u, bloom = false)

    // 5. simbolul bolt
    val jitter = if (fx.running) 0.04f * sin(t * 80f) else 0f
    val bs = (1f - 0.2f * press) * (1f + jitter)
    withTransform({
        translate(c.x, c.y - 6f * u)
        scale(bs, bs, Offset.Zero)
    }) {
        drawPath(st.bolt, Color.White.copy(alpha = 0.18f), style = st.boltGlow, blendMode = BlendMode.Plus)
        drawPath(st.bolt, Brush.verticalGradient(listOf(Color.White, NeonCyan), startY = -30f * u, endY = 30f * u))
    }
}

/** Stratul care se estompează (blur) și se adună peste desenul clar ⇒ „bloom". */
private fun DrawScope.drawVortexBloom(fx: FxEngine, press: Float, st: VortexStyle) {
    val u = density
    val c = center
    val kick = fx.pulse
    val coreR = 90f * u * (1f + 0.07f * kick - 0.06f * press)
    drawCircle(NeonCyan.copy(alpha = 0.50f + 0.25f * fx.heat + 0.20f * kick), radius = coreR * 0.92f, center = c)
    drawArcs(fx, st, c, u, bloom = true)
    val bs = 1f - 0.2f * press
    withTransform({
        translate(c.x, c.y - 6f * u)
        scale(bs, bs, Offset.Zero)
    }) {
        drawPath(st.bolt, Color.White.copy(alpha = 0.9f))
    }
}

private fun DrawScope.drawArcs(fx: FxEngine, st: VortexStyle, c: Offset, u: Float, bloom: Boolean) {
    if (fx.reduce) return
    val heat = fx.heat
    val seed = (fx.timeSec * (16f + 10f * heat)).toInt()
    val rnd = Random(seed * 7919 + 13)
    val nRadial = 2 + (heat * 4f).toInt()
    val nRing = 1 + (heat * 2.5f).toInt()
    val k = 0.75f + 0.25f * heat
    val path = st.arcPath

    for (i in 0 until nRadial) {
        val a0 = rnd.nextFloat() * TAU
        val r0 = 84f * u
        val r1 = (118f + rnd.nextFloat() * 42f) * u
        path.rewind()
        path.moveTo(c.x + cos(a0) * r0, c.y + sin(a0) * r0)
        val segs = 7
        for (s in 1..segs) {
            val f = s / segs.toFloat()
            val r = mix(r0, r1, f)
            val a = a0 + (rnd.nextFloat() - 0.5f) * 0.5f
            path.lineTo(c.x + cos(a) * r, c.y + sin(a) * r)
        }
        strokeBolt(path, st, k, bloom)
    }
    for (i in 0 until nRing) {
        val a0 = rnd.nextFloat() * TAU
        val span = 0.45f + rnd.nextFloat() * 0.8f
        val segs = 10
        path.rewind()
        for (s in 0..segs) {
            val a = a0 + span * s / segs
            val r = (100f + (rnd.nextFloat() - 0.5f) * 16f) * u
            val x = c.x + cos(a) * r
            val y = c.y + sin(a) * r
            if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        strokeBolt(path, st, k, bloom)
    }
}

private fun DrawScope.strokeBolt(path: Path, st: VortexStyle, k: Float, bloom: Boolean) {
    if (bloom) {
        drawPath(path, NeonCyan.copy(alpha = 0.6f * k), style = st.arcWide, blendMode = BlendMode.Plus)
        return
    }
    drawPath(path, NeonCyan.copy(alpha = 0.14f * k), style = st.arcWide, blendMode = BlendMode.Plus)
    drawPath(path, NeonCyan.copy(alpha = 0.55f * k), style = st.arcMid, blendMode = BlendMode.Plus)
    drawPath(path, Color.White.copy(alpha = 0.95f * k), style = st.arcThin, blendMode = BlendMode.Plus)
}
