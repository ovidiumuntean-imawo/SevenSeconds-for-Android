package com.imawo.sevenseconds.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.imawo.sevenseconds.R
import com.imawo.sevenseconds.SevenSecondsApp
import com.imawo.sevenseconds.core.GameConfig
import com.imawo.sevenseconds.core.Platform
import com.imawo.sevenseconds.ui.fx.NeonText
import com.imawo.sevenseconds.ui.theme.DeepSpace
import com.imawo.sevenseconds.ui.theme.NeonBlue
import com.imawo.sevenseconds.ui.theme.NeonCyan

@Composable
fun SettingsPanel(reduceFx: Boolean, onReduceFxChange: (Boolean) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as SevenSecondsApp }
    var soundOn by remember { mutableStateOf(app.prefs.soundOn) }
    var hapticsOn by remember { mutableStateOf(app.prefs.hapticsOn) }
    val shape = RoundedCornerShape(24.dp)

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(28.dp)
                .widthIn(max = 380.dp)
                .clip(shape)
                .background(DeepSpace.copy(alpha = 0.97f))
                .border(1.5.dp, NeonCyan.copy(alpha = 0.5f), shape)
                // consumă atingerile ca să nu închidă panoul
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .padding(22.dp),
        ) {
            NeonText(
                text = stringResource(R.string.settings_title), color = Color.White, glow = NeonCyan,
                fontSize = 24.sp, glowRadius = 10f,
            )
            Spacer(Modifier.height(14.dp))

            ToggleRow(stringResource(R.string.settings_sound), null, soundOn) {
                soundOn = it
                app.prefs.soundOn = it
                app.sound.enabled = it
            }
            ToggleRow(stringResource(R.string.settings_haptics), null, hapticsOn) {
                hapticsOn = it
                app.prefs.hapticsOn = it
                app.haptics.enabled = it
            }
            ToggleRow(stringResource(R.string.settings_reduce_fx), stringResource(R.string.settings_reduce_fx_hint), reduceFx, onReduceFxChange)

            Spacer(Modifier.height(10.dp))
            BigButton(
                label = stringResource(R.string.settings_community), fill = NeonBlue.copy(alpha = 0.35f),
                borderColor = NeonBlue, shape = 12, textSize = 14,
                onClick = { Platform.openUrl(context, GameConfig.COMMUNITY_URL) },
            )
            Spacer(Modifier.height(10.dp))
            BigButton(
                label = stringResource(R.string.settings_close), fill = Color.White.copy(alpha = 0.08f),
                borderColor = Color.White.copy(alpha = 0.25f), shape = 12, textSize = 14, onClick = onClose,
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, hint: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (hint != null) Text(hint, color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
        }
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan,
                uncheckedThumbColor = Color.White.copy(alpha = 0.7f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.15f),
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}
