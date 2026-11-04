package com.imawo.sevenseconds.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imawo.sevenseconds.R
import com.imawo.sevenseconds.SevenSecondsApp
import com.imawo.sevenseconds.core.Platform
import com.imawo.sevenseconds.core.findActivity
import com.imawo.sevenseconds.game.ChallengeOutcome
import com.imawo.sevenseconds.game.GameViewModel
import com.imawo.sevenseconds.game.Rank
import com.imawo.sevenseconds.ui.fx.FxEngine
import com.imawo.sevenseconds.ui.fx.NeonText
import com.imawo.sevenseconds.ui.fx.ShareIcon
import com.imawo.sevenseconds.ui.fx.TrophyIcon
import com.imawo.sevenseconds.ui.fx.WarningIcon
import com.imawo.sevenseconds.ui.theme.DeepSpace
import com.imawo.sevenseconds.ui.theme.NeonBlue
import com.imawo.sevenseconds.ui.theme.NeonCyan
import com.imawo.sevenseconds.ui.theme.NeonOrange
import com.imawo.sevenseconds.ui.theme.NeonPurple
import com.imawo.sevenseconds.ui.theme.NeonRed
import com.imawo.sevenseconds.ui.theme.SysGreen
import com.imawo.sevenseconds.ui.theme.SysRed
import com.imawo.sevenseconds.ui.theme.SysYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Rezultatul rundei, „înghețat" când apare ecranul (ViewModel-ul se resetează la ieșire, iar fade-out-ul nu trebuie să clipească). */
private data class RunResult(
    val score: Int,
    val best: Int,
    val isNewBest: Boolean,
    val rank: Rank?,
    val outcome: ChallengeOutcome?,
    val target: Int?,
)

@Composable
fun GameOverScreen(
    vm: GameViewModel,
    fx: FxEngine,
    onDismiss: () -> Unit,
    onRanks: () -> Unit,
) {
    val result = remember {
        RunResult(vm.score, vm.best, vm.isNewBest, vm.rank, vm.outcome, vm.runChallengeTarget)
    }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f))) {
        if (result.outcome != null) {
            ChallengeResult(result, fx, onDismiss)
        } else {
            NormalResult(result, fx, onDismiss, onRanks)
        }
    }
}

// ───────────────────────── rezultat obișnuit ─────────────────────────

@Composable
private fun NormalResult(r: RunResult, fx: FxEngine, onRetry: () -> Unit, onRanks: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as SevenSecondsApp }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val titleScale = remember { Animatable(0.5f) }
    val titleAlpha = remember { Animatable(0f) }
    val scoreScale = remember { Animatable(0.8f) }
    val counter = remember { Animatable(0f) }
    val buttonsIn = remember { Animatable(0f) }
    var recordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch { titleAlpha.animateTo(1f, tween(300)) }
        scope.launch { titleScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 110f)) }
        delay(200)
        scope.launch { scoreScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 158f)) }
        scope.launch {
            delay(50)
            counter.animateTo(r.score.toFloat(), tween(durationMillis = 700, easing = FastOutSlowInEasing))
        }
        delay(200)
        scope.launch { buttonsIn.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
        if (r.isNewBest) {
            delay(400)
            fx.celebrate()
            app.haptics.rankUp()
            delay(500)
            recordDialog = true
        }
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        NeonText(
            text = stringResource(R.string.game_over),
            color = Color.White, glow = NeonRed, fontSize = 52.sp, glowRadius = 18f,
            modifier = Modifier.graphicsLayer {
                scaleX = titleScale.value
                scaleY = titleScale.value
                alpha = titleAlpha.value
            },
        )

        Spacer(Modifier.height(20.dp))

        Box(
            Modifier.graphicsLayer {
                scaleX = scoreScale.value
                scaleY = scoreScale.value
            },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(300.dp)) {
                drawCircle(
                    Brush.radialGradient(listOf(NeonBlue.copy(alpha = 0.32f), Color.Transparent), center = center, radius = size.minDimension / 2f),
                    radius = size.minDimension / 2f,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.you_scored),
                    color = NeonCyan.copy(alpha = 0.7f), fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 4.sp,
                )
                NeonText(
                    text = counter.value.toInt().toString(),
                    color = Color.White, glow = NeonCyan, fontSize = 90.sp, glowRadius = 16f,
                )
                Text(
                    text = stringResource(R.string.taps),
                    color = Color.White.copy(alpha = 0.5f), fontSize = 20.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 6.sp,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = if (r.isNewBest) stringResource(R.string.new_best) else stringResource(R.string.best_over, r.best),
            color = if (r.isNewBest) SysYellow else Color.White.copy(alpha = 0.55f),
            fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp,
        )

        r.rank?.let {
            Spacer(Modifier.height(14.dp))
            RankCard(it, Modifier.graphicsLayer { alpha = buttonsIn.value })
        }

        Spacer(Modifier.weight(1f))

        Column(
            Modifier
                .widthIn(max = 420.dp)
                .graphicsLayer {
                    alpha = buttonsIn.value
                    translationY = (1f - buttonsIn.value) * 50f * density
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BigButton(
                label = stringResource(R.string.retry_mission), fill = NeonBlue.copy(alpha = 0.6f),
                borderColor = NeonCyan, shape = 16, textSize = 20, onClick = onRetry,
            )
            BigButton(
                label = stringResource(R.string.challenge_friend), fill = NeonPurple.copy(alpha = 0.4f),
                borderColor = NeonPurple.copy(alpha = 0.5f), shape = 12, textSize = 16,
                icon = { ShareIcon(Color.White, size = 20.dp) },
                onClick = { Platform.shareChallenge(context, r.score) },
            )
            BigButton(
                label = stringResource(R.string.ranks), fill = SysYellow.copy(alpha = 0.2f),
                borderColor = SysYellow.copy(alpha = 0.3f), shape = 12, textSize = 13,
                icon = { TrophyIcon(Color.White, size = 18.dp) }, onClick = onRanks,
            )
        }
        Spacer(Modifier.height(24.dp))
    }

    if (recordDialog) {
        NeonDialog(
            title = stringResource(R.string.record_title),
            message = stringResource(R.string.record_message, r.score),
            button = stringResource(R.string.lets_go),
            onDismiss = {
                recordDialog = false
                context.findActivity()?.let { Platform.requestReview(it) }
            },
        )
    }
}

@Composable
private fun RankCard(rank: Rank, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .widthIn(max = 420.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = 0.07f))
            .border(1.dp, NeonCyan.copy(alpha = 0.25f), shape)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(rank.title),
            color = NeonCyan, fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(rank.message),
            color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, lineHeight = 17.sp,
            textAlign = TextAlign.Center,
        )
    }
}

// ───────────────────────── rezultat provocare ─────────────────────────

@Composable
private fun ChallengeResult(r: RunResult, fx: FxEngine, onReturn: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as SevenSecondsApp }
    val win = r.outcome == ChallengeOutcome.Win
    val tone = if (win) SysGreen else SysRed

    val iconScale = remember { Animatable(0f) }
    val textIn = remember { Animatable(0f) }
    val cardIn = remember { Animatable(0f) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch { iconScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 140f)) }
        if (win) {
            fx.celebrate()
            app.haptics.rankUp()
        }
        delay(250)
        scope.launch { textIn.animateTo(1f, tween(400)) }
        delay(250)
        scope.launch { cardIn.animateTo(1f, tween(450, easing = FastOutSlowInEasing)) }
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            Modifier.graphicsLayer {
                scaleX = iconScale.value
                scaleY = iconScale.value
            },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(190.dp)) {
                drawCircle(
                    Brush.radialGradient(listOf(tone.copy(alpha = 0.45f), Color.Transparent), center = center, radius = size.minDimension / 2f),
                    radius = size.minDimension / 2f,
                )
            }
            if (win) TrophyIcon(tone, size = 84.dp) else WarningIcon(tone, DeepSpace, size = 84.dp)
        }

        Spacer(Modifier.height(18.dp))

        NeonText(
            text = stringResource(if (win) R.string.mission_accomplished else R.string.mission_failed),
            color = tone, glow = tone, fontSize = 40.sp, glowRadius = 14f, textAlign = TextAlign.Center,
            lineHeight = 46.sp,
            modifier = Modifier.graphicsLayer { alpha = textIn.value },
        )

        Spacer(Modifier.height(24.dp))

        val shape = RoundedCornerShape(20.dp)
        Column(
            Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = cardIn.value
                    translationY = (1f - cardIn.value) * 40f * density
                }
                .clip(shape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.5.dp, tone.copy(alpha = 0.5f), shape)
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatRow(stringResource(R.string.target_colon), (r.target ?: 0).toString(), SysYellow)
            StatRow(stringResource(R.string.you_colon), r.score.toString(), if (win) SysGreen else NeonOrange)
        }

        Spacer(Modifier.weight(1f))

        Box(
            Modifier
                .widthIn(max = 420.dp)
                .graphicsLayer { alpha = cardIn.value }
        ) {
            BigButton(
                label = stringResource(R.string.return_to_base), fill = tone.copy(alpha = 0.35f),
                borderColor = tone, shape = 16, textSize = 18, onClick = onReturn,
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        NeonText(text = value, color = color, glow = color, fontSize = 34.sp, glowRadius = 8f)
    }
}

// ───────────────────────── componente comune ─────────────────────────

@Composable
internal fun BigButton(
    label: String,
    fill: Color,
    borderColor: Color,
    shape: Int,
    textSize: Int,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
) {
    val s = RoundedCornerShape(shape.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(s)
            .background(fill)
            .border(if (shape >= 16) 2.dp else 1.dp, borderColor, s)
            .clickable(onClick = onClick)
            .padding(vertical = if (shape >= 16) 18.dp else 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = label, color = Color.White, fontSize = textSize.sp,
            fontWeight = FontWeight.Black, letterSpacing = 1.sp,
        )
    }
}

@Composable
internal fun NeonDialog(title: String, message: String, button: String, onDismiss: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val scale = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 300f)) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(32.dp)
                .widthIn(max = 380.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
                .clip(shape)
                .background(DeepSpace.copy(alpha = 0.96f))
                .border(2.dp, SysYellow.copy(alpha = 0.7f), shape)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NeonText(text = title, color = SysYellow, glow = SysYellow, fontSize = 26.sp, glowRadius = 10f, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Text(
                text = message, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp,
                lineHeight = 22.sp, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            BigButton(
                label = button, fill = SysYellow.copy(alpha = 0.25f), borderColor = SysYellow,
                shape = 16, textSize = 17, onClick = onDismiss,
            )
        }
    }
}
