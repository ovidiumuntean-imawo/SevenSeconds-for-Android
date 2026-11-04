package com.imawo.sevenseconds.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imawo.sevenseconds.R
import com.imawo.sevenseconds.SevenSecondsApp
import com.imawo.sevenseconds.game.GameViewModel
import com.imawo.sevenseconds.game.Phase
import com.imawo.sevenseconds.game.Ranks
import com.imawo.sevenseconds.game.TapResult
import com.imawo.sevenseconds.ui.fx.FxEngine
import com.imawo.sevenseconds.ui.fx.GearIcon
import com.imawo.sevenseconds.ui.fx.GlitchScore
import com.imawo.sevenseconds.ui.fx.NeonText
import com.imawo.sevenseconds.ui.fx.RadialTimer
import com.imawo.sevenseconds.ui.fx.TrophyIcon
import com.imawo.sevenseconds.ui.fx.VortexButton
import com.imawo.sevenseconds.ui.theme.NeonCyan
import com.imawo.sevenseconds.ui.theme.NeonOrange
import com.imawo.sevenseconds.ui.theme.NeonRed
import com.imawo.sevenseconds.ui.theme.SysYellow
import java.util.Locale
import kotlinx.coroutines.delay

/** Ecranul principal de joc (HUD): timer radial, scor, butonul Vortex, RANKS. `hidden` = Game Over e deasupra. */
@Composable
fun GameScreen(
    vm: GameViewModel,
    fx: FxEngine,
    hidden: Boolean,
    onOpenSettings: () -> Unit,
    onRanks: () -> Unit,
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as SevenSecondsApp }

    var deployed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(500)
        deployed = true
    }

    val hudAlpha = animateFloatAsState(if (hidden) 0f else 1f, tween(300), label = "hudAlpha")
    val phase = vm.phase

    val onPress: () -> Unit = {
        when (vm.onTap()) {
            TapResult.Started -> {
                app.haptics.start()
                fx.onStart()
            }
            TapResult.Counted -> {
                val s = vm.score
                fx.onTap()
                app.sound.playTap(fx.heat)
                app.haptics.tap(fx.heat)
                Ranks.milestoneAt(s)?.let { r ->
                    fx.milestone(context.getString(r.title))
                    app.haptics.rankUp()
                }
            }
            TapResult.Ignored -> Unit
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = hudAlpha.value }
            .safeDrawingPadding()
    ) {
        // setări — doar când ești pe ecranul de start
        val settingsAlpha = animateFloatAsState(if (phase == Phase.Idle && !hidden) 0.75f else 0f, tween(250), label = "gear")
        val settingsDescription = stringResource(R.string.cd_settings)
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 6.dp, end = 10.dp)
                .size(48.dp)
                .graphicsLayer { alpha = settingsAlpha.value }
                .clip(RoundedCornerShape(24.dp))
                .semantics { contentDescription = settingsDescription }
                .clickable(enabled = phase == Phase.Idle && !hidden, onClick = onOpenSettings),
            contentAlignment = Alignment.Center,
        ) {
            GearIcon(Color.White)
        }

        Column(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    fx.subscribe()
                    translationX = fx.shakeX
                    translationY = fx.shakeY
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(20.dp))

            vm.pendingChallenge?.let { target ->
                NeonText(
                    text = stringResource(R.string.target_hud, target),
                    color = SysYellow, glow = NeonOrange, fontSize = 42.sp, glowRadius = 10f,
                )
                Spacer(Modifier.height(4.dp))
            }

            TimerLabel(fx, phase)
            Spacer(Modifier.height(10.dp))

            Box(Modifier.size(width = 340.dp, height = 196.dp), contentAlignment = Alignment.BottomCenter) {
                RadialTimer(fx)
                ScoreBlock(vm, fx)
            }

            if (phase == Phase.Idle && vm.best > 0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.best_hud, vm.best),
                    color = Color.White.copy(alpha = 0.55f), fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp,
                )
            }

            Spacer(Modifier.weight(1f))

            // wrapper NETRANSFORMAT: de aici citim centrul real al butonului (animația de „deploy" nu îl mută)
            Box(
                Modifier
                    .size(300.dp)
                    .onGloballyPositioned { c ->
                        val o = c.positionInRoot()
                        fx.buttonCenter = Offset(o.x + c.size.width / 2f, o.y + c.size.height / 2f)
                    },
                contentAlignment = Alignment.Center,
            ) {
                VortexButton(fx = fx, deployed = deployed, enabled = !hidden, onPress = onPress)
            }

            Spacer(Modifier.weight(1f))

            RanksButton(visible = phase == Phase.Idle && vm.pendingChallenge == null && !hidden, onClick = onRanks)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun TimerLabel(fx: FxEngine, phase: Phase) {
    val tenths = fx.timeLeftTenths.intValue
    val urgent = phase == Phase.Running && tenths <= 30
    val text = if (phase == Phase.Idle) stringResource(R.string.time_idle)
    else String.format(Locale.US, "%.1f s", tenths / 10f)
    NeonText(
        text = text,
        color = if (urgent) NeonRed else Color.White,
        glow = if (urgent) NeonRed else NeonCyan,
        fontSize = 28.sp,
        glowRadius = if (urgent) 12f else 9f,
    )
}

@Composable
private fun ScoreBlock(vm: GameViewModel, fx: FxEngine) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 4.dp)) {
        Text(
            text = stringResource(R.string.score_label),
            color = NeonCyan.copy(alpha = 0.8f), fontSize = 18.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 8.sp,
        )
        GlitchScore(vm.score, fx)
    }
}

@Composable
private fun RanksButton(visible: Boolean, onClick: () -> Unit) {
    val a = animateFloatAsState(if (visible) 1f else 0f, tween(250), label = "ranksAlpha")
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .padding(horizontal = 40.dp)
            .widthIn(max = 320.dp)
            .fillMaxWidth()
            .graphicsLayer { alpha = a.value }
            .clip(shape)
            .background(SysYellow.copy(alpha = 0.2f))
            .border(1.dp, SysYellow.copy(alpha = 0.3f), shape)
            .clickable(enabled = visible, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrophyIcon(Color.White, size = 18.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.ranks),
            color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp,
        )
    }
}
