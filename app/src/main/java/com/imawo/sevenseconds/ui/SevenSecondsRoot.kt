package com.imawo.sevenseconds.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.imawo.sevenseconds.SevenSecondsApp
import com.imawo.sevenseconds.core.GameConfig
import com.imawo.sevenseconds.core.Platform
import com.imawo.sevenseconds.core.PlayGamesBridge
import com.imawo.sevenseconds.game.GameViewModel
import com.imawo.sevenseconds.game.Phase
import com.imawo.sevenseconds.ui.fx.Backdrop
import com.imawo.sevenseconds.ui.fx.FxEngine
import com.imawo.sevenseconds.ui.fx.FxOverlay
import com.imawo.sevenseconds.ui.screens.GameOverScreen
import com.imawo.sevenseconds.ui.screens.GameScreen
import com.imawo.sevenseconds.ui.screens.SettingsPanel
import com.imawo.sevenseconds.ui.theme.DeepSpace
import kotlin.math.min
import androidx.compose.runtime.withFrameNanos

/**
 * Rădăcina UI-ului.
 *
 * Scalare adaptivă: tot UI-ul e proiectat într-un spațiu de ~390×780 dp și scalat uniform (prin LocalDensity),
 * deci arată la fel pe telefon mic, telefon mare, tabletă sau pliabil, în portret sau peisaj.
 * Efectele (fundal, stardust, unde de șoc) umplu întreaga fereastră.
 */
@Composable
fun SevenSecondsRoot(vm: GameViewModel) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as SevenSecondsApp }
    val fx = remember { FxEngine() }

    var settingsOpen by remember { mutableStateOf(false) }
    var showOver by remember { mutableStateOf(false) }
    var reduceUser by remember { mutableStateOf(app.prefs.reduceFx) }
    val systemAnimationsOff = remember { Platform.systemAnimationsOff(context) }

    SideEffect { fx.reduce = reduceUser ?: systemAnimationsOff }

    BoxWithConstraints(Modifier.fillMaxSize().background(DeepSpace)) {
        val ui = min(maxWidth.value / 390f, maxHeight.value / 780f).coerceIn(0.72f, 2.2f)
        val base = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(base.density * ui, min(base.fontScale, 1.15f))
        ) {
            val density = LocalDensity.current.density
            SideEffect { fx.density = density }

            // UN SINGUR ceas pentru toate efectele
            LaunchedEffect(fx) {
                while (true) {
                    withFrameNanos { now -> fx.frame(now, vm.phase, vm.startElapsed) }
                }
            }

            // finalul rundei: explozie → pauză scurtă → ecranul Game Over
            LaunchedEffect(vm.phase) {
                when (vm.phase) {
                    Phase.Over -> {
                        fx.onFinish()
                        app.haptics.finish()
                        kotlinx.coroutines.delay(if (fx.reduce) 150L else GameConfig.FINALE_DELAY_MS)
                        showOver = true
                    }
                    Phase.Idle -> {
                        showOver = false
                        fx.resetForIdle()
                    }
                    Phase.Running -> showOver = false
                }
            }

            Box(
                Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        fx.width = it.width.toFloat()
                        fx.height = it.height.toFloat()
                    }
            ) {
                Backdrop(fx)

                GameScreen(
                    vm = vm, fx = fx, hidden = showOver,
                    onOpenSettings = { settingsOpen = true },
                    onRanks = { PlayGamesBridge.showLeaderboard() },
                )

                AnimatedVisibility(
                    visible = showOver,
                    enter = fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.96f),
                    exit = fadeOut(tween(200)),
                ) {
                    GameOverScreen(
                        vm = vm, fx = fx,
                        onDismiss = { vm.dismissOver() },
                        onRanks = { PlayGamesBridge.showLeaderboard() },
                    )
                }

                FxOverlay(fx)

                AnimatedVisibility(
                    visible = settingsOpen,
                    enter = fadeIn(tween(200)),
                    exit = fadeOut(tween(150)),
                ) {
                    SettingsPanel(
                        reduceFx = fx.reduce,
                        onReduceFxChange = {
                            reduceUser = it
                            app.prefs.reduceFx = it
                        },
                        onClose = { settingsOpen = false },
                    )
                }
            }

            // Back: ignorat în timpul rundei; închide setările / ecranul Game Over
            BackHandler(enabled = vm.phase == Phase.Running) { }
            BackHandler(enabled = showOver && vm.phase == Phase.Over) { vm.dismissOver() }
            BackHandler(enabled = settingsOpen) { settingsOpen = false }
        }
    }
}
