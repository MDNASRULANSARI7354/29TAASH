package com.example.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PreferencesRepository
import com.example.model.GameSettings
import com.example.ui.theme.FeltDark
import com.example.ui.theme.FeltLight
import com.example.ui.theme.FeltMedium
import com.example.ui.theme.GoldPrimary

@Composable
fun SettingsScreen(
    prefsRepo: PreferencesRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialSettings = remember { prefsRepo.loadSettings() }
    var minBid by remember { mutableStateOf(initialSettings.minBid) }
    var practiceMode by remember { mutableStateOf(initialSettings.practiceMode) }
    var hiddenTrump by remember { mutableStateOf(initialSettings.hiddenTrumpEnabled) }
    var pairBonus by remember { mutableStateOf(initialSettings.pairBonusEnabled) }
    var soundEffects by remember { mutableStateOf(initialSettings.soundEffects) }
    var vibration by remember { mutableStateOf(initialSettings.vibration) }
    var targetScore by remember { mutableStateOf(initialSettings.targetScore) }

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(FeltLight, FeltMedium, FeltDark)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Settings & Rule Options (सेटिंग्स)",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD0E1A11)),
                    border = BorderStroke(1.dp, Color(0x44FFD54F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Practice Mode Toggle
                        SettingToggleRow(
                            title = "Practice Mode (अभ्यास मोड)",
                            subtitle = "Enables tactical Hints and Undo for played cards",
                            checked = practiceMode,
                            onCheckedChange = { practiceMode = it },
                            testTag = "setting_practice_toggle"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Hidden Trump Toggle
                        SettingToggleRow(
                            title = "Hidden Trump (छुपा हुकुम)",
                            subtitle = "Trump remains secret until a player asks to reveal it",
                            checked = hiddenTrump,
                            onCheckedChange = { hiddenTrump = it },
                            testTag = "setting_hidden_trump_toggle"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Royal Pair (Jodi)
                        SettingToggleRow(
                            title = "Royal Pair / Jodi Bonus (जोड़ी)",
                            subtitle = "Holding K & Q of trump adjusts target by 4 points",
                            checked = pairBonus,
                            onCheckedChange = { pairBonus = it },
                            testTag = "setting_pair_bonus_toggle"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sound Effects
                        SettingToggleRow(
                            title = "Sound Effects (ध्वनि)",
                            subtitle = "Audio feedback for card snaps and trick wins",
                            checked = soundEffects,
                            onCheckedChange = { soundEffects = it },
                            testTag = "setting_sound_toggle"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Vibration
                        SettingToggleRow(
                            title = "Haptic Vibration (कंपन)",
                            subtitle = "Haptic pulse when your turn arrives",
                            checked = vibration,
                            onCheckedChange = { vibration = it },
                            testTag = "setting_vibration_toggle"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SAVE BUTTON
                Button(
                    onClick = {
                        val newSettings = GameSettings(
                            minBid = minBid,
                            practiceMode = practiceMode,
                            hiddenTrumpEnabled = hiddenTrump,
                            pairBonusEnabled = pairBonus,
                            soundEffects = soundEffects,
                            vibration = vibration,
                            targetScore = targetScore
                        )
                        prefsRepo.saveSettings(newSettings)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_settings_btn")
                ) {
                    Text("Save & Apply Settings", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFFA0B3A5),
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GoldPrimary,
                checkedTrackColor = Color(0xFF1B5E20)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
