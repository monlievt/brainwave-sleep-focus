package com.monliev.brainwave.ui.main

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.monliev.brainwave.CategoryDetail
import com.monliev.brainwave.Player
import com.monliev.brainwave.Statistics
import com.monliev.brainwave.audio.preset.Preset
import com.monliev.brainwave.data.local.CustomPresetEntity
import com.monliev.brainwave.data.local.SessionLogEntity
import com.monliev.brainwave.data.local.SessionAlarmEntity
import kotlinx.coroutines.launch

// Category Color Tokens as per design-system.md
val ColorAccentStudy = Color(0xFFFF6B6B)
val ColorAccentSpirit = Color(0xFFFFA451)
val ColorAccentSleep = Color(0xFF6DD98C)
val ColorAccentBody = Color(0xFF5DBEEA)
val ColorAccentBrain = Color(0xFF8C7CF0)

fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "STUDY" -> ColorAccentStudy
        "SPIRIT" -> ColorAccentSpirit
        "SLEEP" -> ColorAccentSleep
        "BODY" -> ColorAccentBody
        "BRAIN" -> ColorAccentBrain
        else -> Color.White
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.uppercase()) {
        "STUDY" -> Icons.Default.Book
        "SPIRIT" -> Icons.Default.Spa
        "SLEEP" -> Icons.Default.NightsStay
        "BODY" -> Icons.Default.Favorite
        "BRAIN" -> Icons.Default.Psychology
        else -> Icons.Default.Info
    }
}

// ----------------------------------------------------
// ThemeColors & rememberThemeColors dynamic system
// ----------------------------------------------------
data class ThemeColors(
    val background: Color,
    val card: Color,
    val text: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val border: Color
)

@Composable
fun rememberThemeColors(isDarkMode: Boolean): ThemeColors {
    return remember(isDarkMode) {
        if (isDarkMode) {
            ThemeColors(
                background = Color(0xFF121212),
                card = Color(0xFF1E1E1E),
                text = Color.White,
                textSecondary = Color.White.copy(alpha = 0.6f),
                textTertiary = Color.White.copy(alpha = 0.3f),
                border = Color.White.copy(alpha = 0.1f)
            )
        } else {
            ThemeColors(
                background = Color(0xFFF5F5F5),
                card = Color(0xFFFFFFFF),
                text = Color(0xFF1C1B1F),
                textSecondary = Color(0xFF1C1B1F).copy(alpha = 0.6f),
                textTertiary = Color(0xFF1C1B1F).copy(alpha = 0.3f),
                border = Color(0xFF1C1B1F).copy(alpha = 0.1f)
            )
        }
    }
}

// ----------------------------------------------------
// 1. Onboarding & Disclaimer 3-Slide Pager Screen
// ----------------------------------------------------
@Composable
fun OnboardingScreen(
    onAccept: () -> Unit,
    viewModel: MainScreenViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val colors = rememberThemeColors(isDarkMode)

    var currentSlide by remember { mutableIntStateOf(0) }
    var isChecked by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Skip Button (Slide 0 and 1 only)
        if (currentSlide < 2) {
            Text(
                text = "Skip",
                color = colors.textSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable { currentSlide = 2 }
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "Brainwave",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorAccentBrain
                )
                Text(
                    text = "Sleep & Focus",
                    fontSize = 16.sp,
                    color = colors.textSecondary
                )
            }

            // Pager Card Content
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    when (currentSlide) {
                        0 -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = ColorAccentBrain,
                                    modifier = Modifier.size(96.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Entrain Your Mind",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Experience scientific, real-time synthesized binaural beats configured to boost focus, sleep depth, and relaxation.",
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        1 -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = ColorAccentSleep,
                                    modifier = Modifier.size(96.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Sleep & Focus Helpers",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Custom background noise generators (white, pink, brown) and fading sleep timers ensure a seamless sleep transition.",
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        2 -> {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "Medical Disclaimer",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                LazyColumn(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    item {
                                        Text(
                                            text = "Information about brainwave frequencies and their uses is educational in nature.\n\nThe effects of brainwave entrainment vary between individuals and are not intended to diagnose, treat, cure, or prevent any disease or medical condition.\n\nConsult a medical professional before using audio entrainment tools, especially if you have a specific medical condition (including epilepsy) or are pregnant.\n\nDo not use while driving or operating machinery.",
                                            fontSize = 13.sp,
                                            lineHeight = 20.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Nav Controls (Indicators & Pager buttons)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    for (i in 0..2) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (currentSlide == i) ColorAccentBrain else colors.textTertiary,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (currentSlide < 2) {
                    Button(
                        onClick = { currentSlide++ },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(text = "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    // Final Acceptance Slide Controls
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isChecked = !isChecked }
                                .padding(8.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { isChecked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = ColorAccentBrain,
                                    uncheckedColor = colors.textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "I understand and agree to the disclaimer",
                                color = colors.text,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onAccept,
                            enabled = isChecked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorAccentBrain,
                                disabledContainerColor = ColorAccentBrain.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Get Started",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChecked) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. Main Tabs Screen (Drawer integration & Navigation)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    onNavigate: (NavKey) -> Unit,
    viewModel: MainScreenViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPaywallDialog by remember { mutableStateOf(false) }

    val currentPreset by viewModel.currentPreset.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val colors = rememberThemeColors(isDarkMode)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.card,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(24.dp)
                ) {
                    // Header Section with Premium Status Badge
                    val isPremiumUnlocked by viewModel.isPremium.collectAsState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Brainwave",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.text
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        if (isPremiumUnlocked) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(ColorAccentSpirit.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = ColorAccentSpirit,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Premium Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorAccentSpirit
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(colors.border.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Free Version",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = colors.border, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Premium Promo Banner (hidden if premium is unlocked)
                    if (!isPremiumUnlocked) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ColorAccentBrain),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    showPaywallDialog = true
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Unlock Premium Features",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Get access to library custom presets, continuous soundscapes, and multi-journey sequences.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Options List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Share Application Row
                        DrawerItemRow(
                            icon = Icons.Default.Share,
                            title = "Share Application",
                            colors = colors,
                            onClick = {
                                scope.launch { drawerState.close() }
                                com.monliev.brainwave.audio.playback.AdMobManager.isNavigatingExternally = true
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Brainwave — Sleep & Focus")
                                    putExtra(Intent.EXTRA_TEXT, "Listen to binaural beats and sleep better: https://play.google.com/store/apps/details?id=com.monliev.brainwave")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                            }
                        )

                        // Rate Application Row
                        DrawerItemRow(
                            icon = Icons.Default.Star,
                            title = "Rate Application",
                            colors = colors,
                            onClick = {
                                scope.launch { drawerState.close() }
                                com.monliev.brainwave.audio.playback.AdMobManager.isNavigatingExternally = true
                                val rateIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=com.monliev.brainwave"))
                                try {
                                    context.startActivity(rateIntent)
                                } catch (e: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.monliev.brainwave")))
                                }
                            }
                        )

                        // Contact Support Row
                        DrawerItemRow(
                            icon = Icons.Default.Email,
                            title = "Contact Support",
                            colors = colors,
                            onClick = {
                                scope.launch { drawerState.close() }
                                com.monliev.brainwave.audio.playback.AdMobManager.isNavigatingExternally = true
                                val emailIntent = Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:support@monliev.com")).apply {
                                    putExtra(Intent.EXTRA_SUBJECT, "Brainwave Feedback")
                                }
                                try {
                                    context.startActivity(emailIntent)
                                } catch (e: Exception) {
                                    // Fallback if no email client
                                }
                            }
                        )

                        // About Screen Row
                        DrawerItemRow(
                            icon = Icons.Default.Info,
                            title = "About Screen",
                            colors = colors,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showAboutDialog = true
                            }
                        )
                    }

                    // Night Mode Toggle Switch Row (Moved to bottom!)
                    DrawerItemRow(
                        icon = Icons.Default.NightsStay,
                        title = "Night Mode",
                        colors = colors,
                        action = {
                            androidx.compose.material3.Switch(
                                checked = isDarkMode,
                                onCheckedChange = { viewModel.toggleDarkMode(it) },
                                colors = androidx.compose.material3.SwitchDefaults.colors(
                                    checkedThumbColor = ColorAccentBrain,
                                    checkedTrackColor = ColorAccentBrain.copy(alpha = 0.4f),
                                    uncheckedThumbColor = colors.textSecondary,
                                    uncheckedTrackColor = colors.border
                                )
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode indicator
                    Text(
                        text = "Version 1.0.0 (MVP)",
                        fontSize = 11.sp,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = colors.background,
            topBar = {
                TopAppBar(
                    title = {
                        val titleText = when (selectedTab) {
                            0 -> "Home"
                            1 -> "Library"
                            else -> "Sessions"
                        }
                        Text(
                            text = titleText,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Side Menu",
                                tint = colors.text
                            )
                        }
                    },
                    actions = {
                        // Statistics Chart Navigation Icon
                        IconButton(onClick = { onNavigate(Statistics) }) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "Statistics",
                                tint = colors.textSecondary
                            )
                        }

                        if (isPlaying && currentPreset != null) {
                            IconButton(onClick = { onNavigate(Player(currentPreset!!.preset_id)) }) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Active Player",
                                    tint = getCategoryColor(currentPreset!!.category)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.background
                    )
                )
            },
            bottomBar = {
                Column {
                    // Sticky Mini Player Bar sitting flat directly on top of navigation bar
                    if (currentPreset != null) {
                        MiniPlayerBar(
                            viewModel = viewModel,
                            onNavigateToPlayer = { presetId -> onNavigate(Player(presetId)) },
                            colors = colors,
                            isFloating = false
                        )
                    }
                    NavigationBar(
                        containerColor = colors.card
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ColorAccentBrain,
                                selectedTextColor = ColorAccentBrain,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = ColorAccentBrain.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Star, contentDescription = "Library") },
                            label = { Text("Library") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ColorAccentBrain,
                                selectedTextColor = ColorAccentBrain,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = ColorAccentBrain.copy(alpha = 0.1f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.List, contentDescription = "Sessions") },
                            label = { Text("Sessions") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ColorAccentBrain,
                                selectedTextColor = ColorAccentBrain,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary,
                                indicatorColor = ColorAccentBrain.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(onNavigate = onNavigate, viewModel = viewModel, colors = colors)
                    1 -> LibraryScreen(onNavigate = onNavigate, colors = colors, viewModel = viewModel)
                    2 -> SessionsScreen(colors = colors, viewModel = viewModel, onShowPaywall = { showPaywallDialog = true })
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutScreenDialog(onDismiss = { showAboutDialog = false }, colors = colors)
    }

    if (showPaywallDialog) {
        PaywallDialog(
            colors = colors,
            viewModel = viewModel,
            onDismiss = { showPaywallDialog = false }
        )
    }
}

// ----------------------------------------------------
// 2.1 Tab Contents: Home
// ----------------------------------------------------
@Composable
fun HomeScreen(
    onNavigate: (NavKey) -> Unit,
    viewModel: MainScreenViewModel,
    colors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val isPremiumUnlocked by viewModel.isPremium.collectAsState()
    val categories = listOf(
        Pair("STUDY", ColorAccentStudy),
        Pair("SPIRIT", ColorAccentSpirit),
        Pair("SLEEP", ColorAccentSleep),
        Pair("BODY", ColorAccentBody),
        Pair("BRAIN", ColorAccentBrain)
    )

    // Fixed: Split the categories into two parts to insert the AdMob card as a standalone item block.
    // This ensures Jetpack Compose applies spacedBy(16.dp) correctly above and below the ad card.
    val firstPart = remember { categories.take(3) } // STUDY, SPIRIT, SLEEP
    val secondPart = remember { categories.drop(3) } // BODY, BRAIN

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(firstPart) { (categoryName, color) ->
            CategoryCard(
                name = categoryName,
                color = color,
                colors = colors,
                onClick = { onNavigate(CategoryDetail(categoryName)) }
            )
        }
        if (!isPremiumUnlocked) {
            item {
                AdMobNativeAd(
                    colors = colors,
                    isSquare = false,
                    modifier = Modifier.fillMaxWidth() // Height enforced inside AdMobNativeAd card container to 115.dp!
                )
            }
        }
        items(secondPart) { (categoryName, color) ->
            CategoryCard(
                name = categoryName,
                color = color,
                colors = colors,
                onClick = { onNavigate(CategoryDetail(categoryName)) }
            )
        }
    }
}

@Composable
fun CategoryCard(
    name: String,
    color: Color,
    colors: ThemeColors,
    onClick: () -> Unit
) {
    // Exact static titles matching the user screenshot 5
    val categorySubtitles = mapOf(
        "STUDY" to "Memory | Focus | Aid",
        "SPIRIT" to "Trance | Astral travel | Solfeggio | Chanting | 3rd Eye",
        "SLEEP" to "Sleep | Deep Sleep | Lucid Dream",
        "BODY" to "Healing | Addictions | Energizer | Inflammation",
        "BRAIN" to "Intelligence | Creativity | Relaxation | Euphoria | Intuition"
    )
    val subtitle = categorySubtitles[name.uppercase()] ?: "Binaural entrainment beats"

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable(onClick = onClick)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Accent strip on left
                Box(
                    modifier = Modifier
                        .width(12.dp)
                        .fillMaxHeight()
                        .background(color)
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 20.dp, end = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            // Category Icon on Right
            Icon(
                imageVector = getCategoryIcon(name),
                contentDescription = null,
                tint = color.copy(alpha = 0.4f),
                modifier = Modifier
                    .padding(end = 24.dp)
                    .size(52.dp)
            )
        }
    }
}

// ----------------------------------------------------
// 2.2 Tab Contents: Library (Empty State)
// ----------------------------------------------------
@Composable
fun LibraryScreen(
    onNavigate: (NavKey) -> Unit,
    colors: ThemeColors,
    viewModel: MainScreenViewModel
) {
    val customPresets by viewModel.customPresets.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Create Custom Preset Button (PRD: lebar, warna aksen utama)
        Button(
            onClick = { showCreateDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Custom Preset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (customPresets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Oops! Your library is empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your own custom presets or favorite the default ones to see them here.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(customPresets) { presetEntity ->
                    LibraryPresetItem(
                        presetEntity = presetEntity,
                        colors = colors,
                        onPlayClick = {
                            onPlayClickPreset(presetEntity.presetId, viewModel, onNavigate)
                        },
                        onDeleteClick = {
                            viewModel.deleteCustomPreset(presetEntity.presetId)
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreatePresetDialog(
            colors = colors,
            onDismiss = { showCreateDialog = false },
            onSave = { title, category, carrierHz, beatHz, noiseType, noiseVolume, timerMins ->
                viewModel.createCustomPreset(title, category, carrierHz, beatHz, noiseType, noiseVolume, timerMins)
                showCreateDialog = false
            }
        )
    }
}

private fun onPlayClickPreset(presetId: String, viewModel: MainScreenViewModel, onNavigate: (NavKey) -> Unit) {
    // If it is a custom preset, make sure it is loaded and started automatically
    val custom = viewModel.customPresets.value.find { it.presetId == presetId }
    if (custom != null) {
        viewModel.startPlayback(custom.toPreset())
    }
    onNavigate(Player(presetId))
}

@Composable
fun LibraryPresetItem(
    presetEntity: CustomPresetEntity,
    colors: ThemeColors,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val categoryColor = getCategoryColor(presetEntity.category)
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category accent strip (Left)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(categoryColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = presetEntity.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Badge label custom vs favorite
                    Box(
                        modifier = Modifier
                            .background(
                                if (presetEntity.isCustom) ColorAccentBrain.copy(alpha = 0.1f)
                                else categoryColor.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (presetEntity.isCustom) "CUSTOM" else "FAVORITE",
                            color = if (presetEntity.isCustom) ColorAccentBrain else categoryColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${presetEntity.carrierFrequencyHz.toInt()}Hz Carrier • ${presetEntity.beatFrequencyHz}Hz Beat",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                val timerText = if (presetEntity.timerMinutes > 0) "${presetEntity.timerMinutes} mins" else "Continuous"
                Text(
                    text = "Timer: $timerText • Noise: ${presetEntity.noiseType.uppercase()}",
                    fontSize = 12.sp,
                    color = colors.textTertiary
                )
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(categoryColor, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.border.copy(alpha = 0.3f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePresetDialog(
    colors: ThemeColors,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, String, Float, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("STUDY") }
    var carrierHz by remember { mutableStateOf(200.0) }
    var beatHz by remember { mutableStateOf(10.0) }
    var noiseType by remember { mutableStateOf("none") }
    var noiseVolume by remember { mutableStateOf(0.15f) }
    var timerMins by remember { mutableIntStateOf(30) }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.background),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Create Custom Preset",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                // Title Input
                Column {
                    Text(text = "Title", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("e.g. Memory Booster", fontSize = 13.sp) },
                        singleLine = true,
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedTextColor = colors.text,
                            unfocusedTextColor = colors.text,
                            focusedContainerColor = colors.card,
                            unfocusedContainerColor = colors.card,
                            focusedIndicatorColor = ColorAccentBrain,
                            unfocusedIndicatorColor = colors.border
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // Category Selection
                Column {
                    Text(text = "Category", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("STUDY", "SPIRIT", "SLEEP", "BODY", "BRAIN").forEach { cat ->
                            val isSelected = category == cat
                            val catColor = getCategoryColor(cat)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) catColor.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) catColor else colors.border,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { category = cat }
                                    .padding(vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) catColor else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Carrier Frequency Slider (100 - 400 Hz)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Carrier Frequency", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                        Text(text = "${carrierHz.toInt()} Hz", fontSize = 12.sp, color = colors.text, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = carrierHz.toFloat(),
                        onValueChange = { carrierHz = it.toDouble() },
                        valueRange = 100f..400f,
                        colors = SliderDefaults.colors(
                            thumbColor = ColorAccentBrain,
                            activeTrackColor = ColorAccentBrain,
                            inactiveTrackColor = colors.border
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Beat Frequency Slider (1.0 - 30.0 Hz)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Beat Frequency (Binaural Tone)", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                        Text(text = String.format("%.1f Hz", beatHz), fontSize = 12.sp, color = colors.text, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = beatHz.toFloat(),
                        onValueChange = { beatHz = it.toDouble() },
                        valueRange = 1f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = ColorAccentBrain,
                            activeTrackColor = ColorAccentBrain,
                            inactiveTrackColor = colors.border
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Noise Type Selection
                Column {
                    Text(text = "Background Noise Type", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("none", "white", "pink", "brown").forEach { type ->
                            val isSelected = noiseType == type
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) ColorAccentBrain.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) ColorAccentBrain else colors.border,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { noiseType = type }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = type.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ColorAccentBrain else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                // Noise Volume Slider (only enabled if noise is selected)
                val isNoiseEnabled = noiseType != "none"
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Noise Volume", fontSize = 12.sp, color = if (isNoiseEnabled) colors.textSecondary else colors.textTertiary, fontWeight = FontWeight.Bold)
                        Text(text = if (isNoiseEnabled) "${(noiseVolume * 100).toInt()}%" else "Disabled", fontSize = 12.sp, color = if (isNoiseEnabled) colors.text else colors.textTertiary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = noiseVolume,
                        onValueChange = { noiseVolume = it },
                        enabled = isNoiseEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isNoiseEnabled) ColorAccentBrain else colors.textTertiary,
                            activeTrackColor = if (isNoiseEnabled) ColorAccentBrain else colors.border,
                            inactiveTrackColor = colors.border,
                            disabledThumbColor = colors.textTertiary.copy(alpha = 0.5f),
                            disabledActiveTrackColor = colors.border.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Sleep Timer Duration (0 for Continuous, 15, 30, 45, 60 mins)
                Column {
                    Text(text = "Sleep Timer Duration", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0, 15, 30, 45, 60).forEach { mins ->
                            val isSelected = timerMins == mins
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) ColorAccentBrain.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) ColorAccentBrain else colors.border,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { timerMins = mins }
                                    .padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (mins == 0) "NONE" else "${mins}m",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ColorAccentBrain else colors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = colors.textSecondary, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val finalTitle = title.trim().ifEmpty { "My Custom Preset" }
                            onSave(finalTitle, category, carrierHz, beatHz, noiseType, noiseVolume, timerMins)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Save", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2.3 Tab Contents: Sessions (Empty State)
// ----------------------------------------------------
@Composable
fun SessionsScreen(
    colors: ThemeColors,
    viewModel: MainScreenViewModel,
    onShowPaywall: () -> Unit
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val sessionLogs by viewModel.sessionLogs.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    var showAddAlarmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sub-Tabs Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            listOf("History", "Bedtime Scheduler").forEachIndexed { index, title ->
                val isSelected = selectedSubTab == index
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) ColorAccentBrain else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedSubTab = index }
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedSubTab) {
            0 -> {
                // History List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Playback History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.text)
                    if (sessionLogs.isNotEmpty()) {
                        androidx.compose.material3.TextButton(onClick = { viewModel.clearHistory() }) {
                            Text(text = "Clear All", color = ColorAccentSpirit, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (sessionLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "You have no sessions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.text)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Completed sessions will be logged here.", fontSize = 13.sp, color = colors.textSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(sessionLogs) { log ->
                            SessionLogItem(log = log, colors = colors)
                        }
                    }
                }
            }
            1 -> {
                // Bedtime Scheduler Tab (Premium or temporarily unlocked via Rewarded Ad)
                val isPremiumUnlocked by viewModel.isPremium.collectAsState()
                val isSchedulerUnlockedTemporarily by viewModel.isSchedulerUnlockedTemporarily.collectAsState()

                if (isPremiumUnlocked || isSchedulerUnlockedTemporarily) {
                    Button(
                        onClick = { showAddAlarmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Bedtime Alarm",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (alarms.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(text = "No alarms scheduled", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.text)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Setup an alarm to automatically start playing a wellness preset at your preferred bedtime.",
                                    fontSize = 13.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(alarms) { alarm ->
                                SessionAlarmItem(
                                    alarm = alarm,
                                    colors = colors,
                                    onActiveToggle = { viewModel.toggleAlarmActive(alarm) },
                                    onDeleteClick = { viewModel.deleteAlarm(alarm) }
                                )
                            }
                        }
                    }
                } else {
                    // Locked Bedtime Scheduler State Screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "Premium Feature Required",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.text,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Scheduling automatic bedtime alarms is a Premium feature. Watch a video ad to unlock temporary access for 24 hours, or upgrade to Premium.",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            // Watch Ad Button
                            Button(
                                onClick = {
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        com.monliev.brainwave.audio.playback.AdMobManager.showRewardedAd(
                                            activity = activity,
                                            onUserEarnedReward = {
                                                viewModel.unlockSchedulerTemporarily()
                                            },
                                            onAdClosed = {
                                                // Failed or closed without reward
                                            }
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorAccentSleep),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(text = "Watch Ad to Unlock for 24 Hours", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            // Buy Premium Button
                            androidx.compose.material3.OutlinedButton(
                                onClick = onShowPaywall,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(text = "Unlock Premium Lifetime", color = colors.text, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAlarmDialog) {
        AddAlarmDialog(
            colors = colors,
            viewModel = viewModel,
            onDismiss = { showAddAlarmDialog = false },
            onSave = { presetId, presetTitle, category, hour, minute ->
                viewModel.addAlarm(presetId, presetTitle, category, hour, minute)
                showAddAlarmDialog = false
            }
        )
    }
}

@Composable
fun SessionLogItem(
    log: SessionLogEntity,
    colors: ThemeColors
) {
    val categoryColor = getCategoryColor(log.category)
    val formattedDate = remember(log.timestamp) {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(log.timestamp))
    }
    val formattedDuration = remember(log.durationSeconds) {
        val mins = log.durationSeconds / 60
        val secs = log.durationSeconds % 60
        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(categoryColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.presetTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Text(text = "${log.category} • Listened: $formattedDuration", fontSize = 12.sp, color = colors.textSecondary)
            }
            Text(text = formattedDate, fontSize = 11.sp, color = colors.textTertiary)
        }
    }
}

@Composable
fun SessionAlarmItem(
    alarm: SessionAlarmEntity,
    colors: ThemeColors,
    onActiveToggle: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val categoryColor = getCategoryColor(alarm.presetCategory)
    val timeText = remember(alarm.hour, alarm.minute) {
        val amPm = if (alarm.hour >= 12) "PM" else "AM"
        val displayHour = when {
            alarm.hour == 0 -> 12
            alarm.hour > 12 -> alarm.hour - 12
            else -> alarm.hour
        }
        String.format("%02d:%02d %s", displayHour, alarm.minute, amPm)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = if (alarm.isActive) categoryColor else colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isActive) colors.text else colors.textSecondary
                )
                Text(
                    text = "Plays: ${alarm.presetTitle} (${alarm.presetCategory})",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Switch(
                    checked = alarm.isActive,
                    onCheckedChange = { onActiveToggle() },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = categoryColor,
                        uncheckedThumbColor = colors.textTertiary,
                        uncheckedTrackColor = colors.border
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.border.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddAlarmDialog(
    colors: ThemeColors,
    viewModel: MainScreenViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Int) -> Unit
) {
    // Collect all presets: default + custom
    val customPresets by viewModel.customPresets.collectAsState()
    val allSelectablePresets = remember(viewModel.allPresets, customPresets) {
        viewModel.allPresets + customPresets.map { it.toPreset() }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var hour by remember { mutableIntStateOf(22) } // default 10 PM
    var minute by remember { mutableIntStateOf(0) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.background),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, colors.border, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add Bedtime Alarm",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )

                // Time Pickers (Hour and Minute sliders for ease)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Hour", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                        val amPm = if (hour >= 12) "PM" else "AM"
                        val displayHour = when {
                            hour == 0 -> 12
                            hour > 12 -> hour - 12
                            else -> hour
                        }
                        Text(text = String.format("%02d %s", displayHour, amPm), fontSize = 13.sp, color = colors.text, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = hour.toFloat(),
                        onValueChange = { hour = it.toInt() },
                        valueRange = 0f..23f,
                        colors = SliderDefaults.colors(
                            thumbColor = ColorAccentBrain,
                            activeTrackColor = ColorAccentBrain,
                            inactiveTrackColor = colors.border
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Minute", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                        Text(text = String.format("%02d", minute), fontSize = 13.sp, color = colors.text, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = minute.toFloat(),
                        onValueChange = { minute = it.toInt() },
                        valueRange = 0f..59f,
                        colors = SliderDefaults.colors(
                            thumbColor = ColorAccentBrain,
                            activeTrackColor = ColorAccentBrain,
                            inactiveTrackColor = colors.border
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Preset selector
                Column {
                    Text(text = "Select Preset to Play", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(colors.card, RoundedCornerShape(8.dp))
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(4.dp)
                        ) {
                            itemsIndexed(allSelectablePresets) { idx, preset ->
                                val isSelected = selectedIndex == idx
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) ColorAccentBrain.copy(alpha = 0.15f) else Color.Transparent,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .clickable { selectedIndex = idx }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(getCategoryColor(preset.category), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = preset.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ColorAccentBrain else colors.text
                                        )
                                        Text(
                                            text = preset.category,
                                            fontSize = 10.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = colors.textSecondary, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val preset = allSelectablePresets.getOrNull(selectedIndex)
                            if (preset != null) {
                                onSave(preset.preset_id, preset.title, preset.category, hour, minute)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Save", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 3. Category Detail Screen (with Large Name and Icon)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryName: String,
    onNavigate: (NavKey) -> Unit,
    onBackClick: () -> Unit,
    viewModel: MainScreenViewModel = viewModel()
) {
    val color = getCategoryColor(categoryName)
    val presets = remember(categoryName) { viewModel.getPresetsByCategory(categoryName) }
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val colors = rememberThemeColors(isDarkMode)
    val currentPreset by viewModel.currentPreset.collectAsState()
    val isPremiumUnlocked by viewModel.isPremium.collectAsState()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = {
            // Floating Mini Player Bar: Elevated cleanly above the system navbar to prevent overlap!
            if (currentPreset != null) {
                MiniPlayerBar(
                    viewModel = viewModel,
                    onNavigateToPlayer = { presetId -> onNavigate(Player(presetId)) },
                    colors = colors,
                    isFloating = true, // Enabled floating mode (fully rounded card)
                    modifier = Modifier
                        .navigationBarsPadding() // Safely pads above the navbar
                        .padding(horizontal = 16.dp, vertical = 8.dp) // Floats with clean margins
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // Large Category Name and Outline Icon Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName.uppercase(),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Icon(
                    imageVector = getCategoryIcon(categoryName),
                    contentDescription = null,
                    tint = color.copy(alpha = 0.8f),
                    modifier = Modifier.size(64.dp)
                )
            }

            // Fixed: Split presets list to insert AdMob card as a standalone item block.
            // This ensures spacedBy spacing is applied correctly above and below the ad card on Category Detail Screen.
            if (presets.size >= 3) {
                val firstPart = remember(presets) { presets.take(2) }
                val secondPart = remember(presets) { presets.drop(2) }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(firstPart) { preset ->
                        PresetCard(
                            preset = preset,
                            color = color,
                            colors = colors,
                            onClick = { onNavigate(Player(preset.preset_id)) }
                        )
                    }
                    if (!isPremiumUnlocked) {
                        item {
                            AdMobNativeAd(
                                colors = colors,
                                isSquare = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    items(secondPart) { preset ->
                        PresetCard(
                            preset = preset,
                            color = color,
                            colors = colors,
                            onClick = { onNavigate(Player(preset.preset_id)) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(presets) { preset ->
                        PresetCard(
                            preset = preset,
                            color = color,
                            colors = colors,
                            onClick = { onNavigate(Player(preset.preset_id)) }
                        )
                    }
                    if (!isPremiumUnlocked) {
                        item {
                            AdMobNativeAd(
                                colors = colors,
                                isSquare = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// PresetCard restyled matching screenshot detail cards
// ----------------------------------------------------
@Composable
fun PresetCard(
    preset: Preset,
    color: Color,
    colors: ThemeColors,
    onClick: () -> Unit
) {
    val firstStep = preset.steps.firstOrNull()
    val beatHz = firstStep?.beat_frequency_hz
        ?: firstStep?.start_beat_frequency_hz
        ?: 10.0

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left vertical accent strip
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(color)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Beat frequency in large bold category color
                Text(
                    text = "$beatHz Hz",
                    color = color,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                // Preset Title
                Text(
                    text = preset.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                // Preset Description
                Text(
                    text = preset.description,
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// 4. Player Screen (Optimized and Compressed Layout)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    presetId: String,
    onBackClick: () -> Unit,
    viewModel: MainScreenViewModel = viewModel()
) {
    val customPresetsList by viewModel.customPresets.collectAsState()
    val preset = remember(presetId, customPresetsList) {
        viewModel.allPresets.find { it.preset_id == presetId }
            ?: customPresetsList.find { it.presetId == presetId }?.toPreset()
    } ?: return
    val categoryColor = getCategoryColor(preset.category)

    val isPlaying by viewModel.isPlaying.collectAsState()
    val timerSeconds by viewModel.timerSecondsRemaining.collectAsState()
    val context = LocalContext.current
    val headphonesConnected by viewModel.isHeadphoneConnected.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isPremiumUnlocked by viewModel.isPremium.collectAsState()
    val isSchedulerUnlocked by viewModel.isSchedulerUnlockedTemporarily.collectAsState()
    val isMixerFreeUsedToday by viewModel.isMixerFreeUsedToday.collectAsState()

    val colors = rememberThemeColors(isDarkMode)

    val volTone by viewModel.volumeTone.collectAsState()
    val volWhite by viewModel.volumeWhite.collectAsState()
    val volPink by viewModel.volumePink.collectAsState()
    val volBrown by viewModel.volumeBrown.collectAsState()
    val volRain by viewModel.volumeRain.collectAsState()
    val volRiver by viewModel.volumeRiver.collectAsState()
    val volOcean by viewModel.volumeOcean.collectAsState()
    val volCampfire by viewModel.volumeCampfire.collectAsState()
    val volWind by viewModel.volumeWind.collectAsState()
    val volCoffeeShop by viewModel.volumeCoffeeShop.collectAsState()

    var volume by remember { mutableStateOf(1.0f) }
    var showTimerSheet by remember { mutableStateOf(false) }
    var showVolumeDialog by remember { mutableStateOf(false) }
    var showBreathingDialog by remember { mutableStateOf(false) }
    var showPaywallDialog by remember { mutableStateOf(false) }

    var showHeadphoneToast by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var headphoneJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    var isCurrentSessionFreeAllowed by remember { mutableStateOf(!isMixerFreeUsedToday) }

    // Premium nature sounds (Rain, River, Ocean) are unlocked if premium, if rewarded-ad unlocked, if free daily quota not yet used,
    // OR if we are currently playing and this session was started while the free quota was still available.
    val isPremiumSoundsUnlocked = isPremiumUnlocked || isSchedulerUnlocked || !isMixerFreeUsedToday || (isPlaying && isCurrentSessionFreeAllowed)

    val currentIsPlaying by androidx.compose.runtime.rememberUpdatedState(isPlaying)
    val currentIsFreeAllowed by androidx.compose.runtime.rememberUpdatedState(isCurrentSessionFreeAllowed)
    val currentIsPremium by androidx.compose.runtime.rememberUpdatedState(isPremiumUnlocked)
    val currentIsSchedulerUnlocked by androidx.compose.runtime.rememberUpdatedState(isSchedulerUnlocked)

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            if (currentIsFreeAllowed && currentIsPlaying && !currentIsPremium && !currentIsSchedulerUnlocked) {
                viewModel.recordMixerFreeUse()
            }
        }
    }

    androidx.activity.compose.BackHandler {
        if (isCurrentSessionFreeAllowed && isPlaying && !isPremiumUnlocked && !isSchedulerUnlocked) {
            viewModel.recordMixerFreeUse()
        }
        onBackClick()
    }

    val activeBeatHz = remember(preset) {
        preset.steps.firstOrNull()?.beat_frequency_hz
            ?: preset.steps.firstOrNull()?.start_beat_frequency_hz
            ?: 10.0
    }

    val currentPlayingPreset by viewModel.currentPreset.collectAsState()
    val currentBeatFrequency by viewModel.currentBeatFrequency.collectAsState()
    val realtimeBeatHz = if (isPlaying && currentPlayingPreset?.preset_id == preset.preset_id) {
        currentBeatFrequency
    } else {
        activeBeatHz
    }

    Scaffold(
        containerColor = colors.background
        // Fixed: TopAppBar is removed completely to save 56.dp of vertical space,
        // allowing the ad card to show fully on one screen without scrolling.
        // Users can easily go back using the system/gesture Back button.
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp) // Merapatkan spasi vertikal antar elemen
            ) {
            // Header Preset Info
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(48.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = preset.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = preset.category,
                        fontSize = 14.sp,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }

                val customPresetsList by viewModel.customPresets.collectAsState()
                val isFav = customPresetsList.any { it.presetId == preset.preset_id && it.isFavorite }
                IconButton(
                    onClick = { viewModel.toggleFavorite(preset) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) ColorAccentBrain else colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Compressed dynamic audio wave visualizer (Height reduced from 130.dp to 90.dp)
            AudioWaveVisualizer(
                isPlaying = isPlaying,
                beatFrequency = realtimeBeatHz,
                color = categoryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp) // Compressed!
            )

            // Dynamic frequency and brainwave state display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "${preset.carrier_frequency_hz} Hz Carrier • " + String.format("%.1f Hz", realtimeBeatHz) + " Beat",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = getBrainwaveStateName(realtimeBeatHz),
                    fontSize = 11.sp,
                    color = categoryColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Guided Breathing Exercise Button
            Button(
                onClick = {
                    if (!isPlaying) {
                        viewModel.startPlayback(preset)
                        viewModel.setVolume(volume)
                        if (!headphonesConnected) {
                            headphoneJob?.cancel()
                            headphoneJob = coroutineScope.launch {
                                showHeadphoneToast = true
                                kotlinx.coroutines.delay(4000)
                                showHeadphoneToast = false
                            }
                        }
                    }
                    showBreathingDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = categoryColor.copy(alpha = 0.15f),
                    contentColor = categoryColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .border(1.dp, categoryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guided Breathing Exercise",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Headphone Warning Bar removed dynamically to show as a floating toast overlay

            // Remaining Duration Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Remaining Duration", color = colors.textSecondary, fontSize = 13.sp)
                Text(
                    text = if (timerSeconds > 0) {
                        val minutes = timerSeconds / 60
                        val seconds = timerSeconds % 60
                        String.format("%02d:%02d", minutes, seconds)
                    } else {
                        "Continuous"
                    },
                    color = colors.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Collapsible Audio Mixer Panel
            var isMixerExpanded by remember { mutableStateOf(true) }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isMixerExpanded = !isMixerExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Audio Mixer & Layering",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = colors.text
                                )
                            }
                            Icon(
                                imageVector = if (isMixerExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isMixerExpanded) "Collapse" else "Expand",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isMixerExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 1. Free Sounds Block
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MixerSliderRow(
                                    label = "Binaural Beat",
                                    value = volTone,
                                    color = categoryColor,
                                    colors = colors,
                                    onValueChange = { viewModel.updateMixerLevels(it, volWhite, volPink, volBrown) }
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Background Noise Masking",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    NoiseToggleChip(
                                        label = "White",
                                        icon = "⚪",
                                        isActive = volWhite > 0f,
                                        activeColor = Color(0xFFE0E0E0),
                                        colors = colors,
                                        onClick = {
                                            val newWhite = if (volWhite > 0f) 0.0f else 0.15f
                                            viewModel.updateMixerLevels(volTone, newWhite, volPink, volBrown)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    NoiseToggleChip(
                                        label = "Pink",
                                        icon = "🌸",
                                        isActive = volPink > 0f,
                                        activeColor = ColorAccentStudy,
                                        colors = colors,
                                        onClick = {
                                            val newPink = if (volPink > 0f) 0.0f else 0.15f
                                            viewModel.updateMixerLevels(volTone, volWhite, newPink, volBrown)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    NoiseToggleChip(
                                        label = "Brown",
                                        icon = "🟫",
                                        isActive = volBrown > 0f,
                                        activeColor = ColorAccentSpirit,
                                        colors = colors,
                                        onClick = {
                                            val newBrown = if (volBrown > 0f) 0.0f else 0.15f
                                            viewModel.updateMixerLevels(volTone, volWhite, volPink, newBrown)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border.copy(alpha = 0.5f)))
                            Spacer(modifier = Modifier.height(8.dp))

                            // 2. Premium Sounds Block
                            Text(
                                text = "Premium Nature Sounds",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryColor,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MixerSliderRow(
                                        label = "Rain Sound 🌧️",
                                        value = volRain,
                                        color = Color(0xFF5DBEEA),
                                        colors = colors,
                                        onValueChange = { viewModel.updateNatureMixerLevels(it, volRiver, volOcean, volCampfire, volWind, volCoffeeShop) }
                                    )
                                    MixerSliderRow(
                                        label = "River Sound 🌊",
                                        value = volRiver,
                                        color = Color(0xFF008080),
                                        colors = colors,
                                        onValueChange = { viewModel.updateNatureMixerLevels(volRain, it, volOcean, volCampfire, volWind, volCoffeeShop) }
                                    )
                                    MixerSliderRow(
                                        label = "Ocean Waves 🏄",
                                        value = volOcean,
                                        color = Color(0xFF2A52BE),
                                        colors = colors,
                                        onValueChange = { viewModel.updateNatureMixerLevels(volRain, volRiver, it, volCampfire, volWind, volCoffeeShop) }
                                    )
                                    MixerSliderRow(
                                        label = "Campfire Crackle 🔥",
                                        value = volCampfire,
                                        color = Color(0xFFFF8C00),
                                        colors = colors,
                                        onValueChange = { viewModel.updateNatureMixerLevels(volRain, volRiver, volOcean, it, volWind, volCoffeeShop) }
                                    )
                                    MixerSliderRow(
                                        label = "Cozy Wind 💨",
                                        value = volWind,
                                        color = Color(0xFFB0C4DE),
                                        colors = colors,
                                        onValueChange = { viewModel.updateNatureMixerLevels(volRain, volRiver, volOcean, volCampfire, it, volCoffeeShop) }
                                    )
                                    MixerSliderRow(
                                        label = "Coffee Shop ☕",
                                        value = volCoffeeShop,
                                        color = Color(0xFF8B4513),
                                        colors = colors,
                                        onValueChange = { viewModel.updateNatureMixerLevels(volRain, volRiver, volOcean, volCampfire, volWind, it) }
                                    )
                                }

                                // Glassmorphic Lock Overlay for Premium Sounds only
                                if (!isPremiumSoundsUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(colors.card.copy(alpha = 0.92f), shape = RoundedCornerShape(8.dp))
                                            .border(1.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = categoryColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Unlock Nature Sounds",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = colors.text
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        com.monliev.brainwave.audio.playback.AdMobManager.showRewardedAd(
                                                            activity = context as android.app.Activity,
                                                            onUserEarnedReward = { viewModel.unlockSchedulerTemporarily() },
                                                            onAdClosed = {}
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("Unlock Free (Watch Ad)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                                OutlinedButton(
                                                    onClick = { showPaywallDialog = true },
                                                    border = BorderStroke(1.dp, colors.border),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("Go Premium", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.text)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Playback Actions (Timer, Play/Pause circle, Volume popup trigger)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleep Timer Icon Trigger
                IconButton(
                    onClick = { showTimerSheet = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(colors.card, shape = CircleShape)
                        .border(1.dp, colors.border, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Timer",
                        tint = colors.text,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Circle (Middle)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .background(categoryColor, shape = CircleShape)
                        .clickable {
                            if (!isPlaying) {
                                viewModel.startPlayback(preset)
                                viewModel.setVolume(volume)
                                viewModel.updateMixerLevels(volTone, volWhite, volPink, volBrown)
                                viewModel.updateNatureMixerLevels(volRain, volRiver, volOcean, volCampfire, volWind, volCoffeeShop)
                                if (!headphonesConnected) {
                                    headphoneJob?.cancel()
                                    headphoneJob = coroutineScope.launch {
                                        showHeadphoneToast = true
                                        kotlinx.coroutines.delay(4000)
                                        showHeadphoneToast = false
                                    }
                                }
                            } else {
                                viewModel.togglePlayPause()
                                // Record daily free mixer session usage when user explicitly pauses/stops
                                if (isCurrentSessionFreeAllowed && !isPremiumUnlocked && !isSchedulerUnlocked) {
                                    viewModel.recordMixerFreeUse()
                                    isCurrentSessionFreeAllowed = false
                                }
                            }
                        }
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Volume Popup Trigger
                IconButton(
                    onClick = { showVolumeDialog = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(colors.card, shape = CircleShape)
                        .border(1.dp, colors.border, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = colors.text,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // AdMob Native Square Card: Positioned at the bottom, now adaptive in height!
            if (!isPremiumUnlocked) {
                AdMobNativeAd(
                    colors = colors,
                    isSquare = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = categoryColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Deep relaxation mode active.",
                            fontSize = 12.sp,
                            color = colors.textSecondary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        } // Column ends

        // Floating Headphone Warning overlay (Centered, fadeIn + fadeOut)
        AnimatedVisibility(
            visible = showHeadphoneToast,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ColorAccentSpirit.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { showHeadphoneToast = false } // Tap to dismiss
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ColorAccentSpirit,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "For best results, please use headphones 🎧",
                        color = colors.text,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    } // Box ends
} // Scaffold lambda ends

    if (showTimerSheet) {
        SleepTimerBottomSheet(
            colors = colors,
            onDismiss = { showTimerSheet = false },
            onSelectMinutes = { mins ->
                viewModel.setSleepTimer(mins)
                showTimerSheet = false
            }
        )
    }

    if (showVolumeDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showVolumeDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Adjust Volume",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            viewModel.setVolume(it)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = categoryColor,
                            activeTrackColor = categoryColor,
                            inactiveTrackColor = colors.border
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showVolumeDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "OK", color = Color.White)
                    }
                }
            }
        }
    }

    if (showBreathingDialog) {
        BreathingExerciseDialog(
            onDismiss = { showBreathingDialog = false },
            categoryColor = categoryColor,
            colors = colors
        )
    }

    if (showPaywallDialog) {
        PaywallDialog(
            onDismiss = { showPaywallDialog = false },
            colors = colors,
            viewModel = viewModel
        )
    }
}

// ----------------------------------------------------
// 5. About Screen Dialog (Offline, full verbatim text)
// ----------------------------------------------------
@Composable
fun AboutScreenDialog(
    onDismiss: () -> Unit,
    colors: ThemeColors
) {
    var showPrivacy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }

    val privacyPolicyText = remember {
        """
        Privacy Policy
        Last updated: August 16, 2026
        Developer / Publisher: Monliev Labs
        Contact email: support@monliev.com

        1. Overview
        Brainwave — Sleep & Focus ("the App", "we", "our") is a brainwave entrainment app for Android. This Privacy Policy explains what information we collect, how we use it, and your rights regarding your data.

        Short version: This app is designed to work completely offline. We do not collect your personal information, we do not send your usage data to our servers, and we do not sell your data to anyone.

        2. Information We Collect
        We do not require you to create an account or provide any personal data to use the App. The App works without registration.
        Google AdMob SDK is integrated to deliver personalized ads for non-premium users, which may process advertising identifiers and device characteristics according to Google policies.
        Local data (settings, presets, and history logs) is stored entirely in local room storage on your device.

        3. System Permissions
        - WakeLock: Utilized to maintain continuous playback when the device screen is off.
        - Foreground Service: Required to show active control notifications.
        - Alarm Scheduling: To trigger auto-start playback at configured sleep/bedtime times.
        """.trimIndent()
    }

    val termsOfServiceText = remember {
        """
        Terms of Service
        Last updated: August 16, 2026
        Publisher: Monliev Labs
        Contact email: support@monliev.com

        1. Acceptance of Terms
        By downloading or using Brainwave — Sleep & Focus ("the App"), you agree to these Terms of Service. If you do not agree, please do not use the App.

        2. Medical & Safety Disclaimer
        Information about brainwave frequencies and their uses is educational in nature. The effects of brainwave entrainment vary between individuals and are not intended to diagnose, treat, cure, or prevent any disease or medical condition.
        Consult a medical professional before using audio entrainment tools, especially if you have a specific medical condition (including epilepsy, seizures) or are pregnant.
        Do not use while driving or operating machinery.

        3. Billing & Payments
        Premium upgrades are processed securely via Google Play Billing. Monliev Labs does not store payment credentials.
        """.trimIndent()
    }

    if (showPrivacy) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPrivacy = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Privacy Policy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.text, modifier = Modifier.padding(bottom = 12.dp))
                    LazyColumn(modifier = Modifier.weight(1f, fill = false).height(300.dp)) {
                        item {
                            Text(text = privacyPolicyText, fontSize = 12.sp, lineHeight = 16.sp, color = colors.textSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showPrivacy = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "Done", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else if (showTerms) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showTerms = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Terms of Service", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.text, modifier = Modifier.padding(bottom = 12.dp))
                    LazyColumn(modifier = Modifier.weight(1f, fill = false).height(300.dp)) {
                        item {
                            Text(text = termsOfServiceText, fontSize = 12.sp, lineHeight = 16.sp, color = colors.textSecondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showTerms = false },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "Done", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "About Brainwave",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        item {
                            Text(
                                text = "Brainwave — Sleep & Focus is a premium entrainment tool built with real-time continuous phase audio synthesis.\n\n" +
                                       "Medical Disclaimer:\n" +
                                       "Information about brainwave frequencies and their uses is educational in nature. The effects of brainwave entrainment vary between individuals and are not intended to diagnose, treat, cure, or prevent any disease or medical condition. Consult a medical professional before using audio entrainment tools, especially if you have a specific medical condition (including epilepsy) or are pregnant. Do not use while driving or operating machinery.",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Privacy & ToS Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { showPrivacy = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Privacy Policy", fontSize = 13.sp, color = ColorAccentBrain, fontWeight = FontWeight.Bold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                        }
                        androidx.compose.material3.TextButton(
                            onClick = { showTerms = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Terms of Service", fontSize = 13.sp, color = ColorAccentBrain, fontWeight = FontWeight.Bold, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(text = "Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 6. Sleep Timer Bottom Sheet Picker
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    colors: ThemeColors,
    onDismiss: () -> Unit,
    onSelectMinutes: (Int) -> Unit
) {
    val options = listOf(
        Pair("Off / Cancel", 0),
        Pair("5 minutes", 5),
        Pair("15 minutes", 15),
        Pair("30 minutes", 30),
        Pair("45 minutes", 45),
        Pair("60 minutes", 60)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Text(
                text = "Select Timer Duration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            options.forEach { (label, minutes) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectMinutes(minutes) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = label, color = colors.text, fontSize = 15.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 7. Sticky Mini Player Bar Composable
// ----------------------------------------------------
@Composable
fun MiniPlayerBar(
    viewModel: MainScreenViewModel,
    onNavigateToPlayer: (String) -> Unit,
    colors: ThemeColors,
    isFloating: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currentPreset by viewModel.currentPreset.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val preset = currentPreset ?: return
    val accentColor = getCategoryColor(preset.category)

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        // Fixed: If isFloating is false, make bottom corners flat (0.dp) to align perfectly with Bottom Navigation top border!
        shape = if (isFloating) {
            RoundedCornerShape(12.dp)
        } else {
            RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onNavigateToPlayer(preset.preset_id) }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Accent color strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Small Category Icon
            Icon(
                imageVector = getCategoryIcon(preset.category),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Title & Category text details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = preset.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = preset.category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Dynamic Play/Pause Button
            IconButton(
                onClick = { viewModel.togglePlayPause() }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = colors.text
                )
            }
            // Stop button
            IconButton(
                onClick = { viewModel.stopPlayback() }
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colors.text, shape = RoundedCornerShape(2.dp))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

// ----------------------------------------------------
// 8. Siri-like Sine Audio Wave Visualizer Composable
// ----------------------------------------------------
@Composable
fun AudioWaveVisualizer(
    isPlaying: Boolean,
    beatFrequency: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2

        if (!isPlaying) {
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(0f, midY)
            path.lineTo(width, midY)
            drawPath(
                path = path,
                color = color.copy(alpha = 0.3f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
            return@Canvas
        }

        val waveParams = listOf(
            Triple(1.0f, 1.0f, 0.8f),      // amplitudeScale, frequencyScale, alpha
            Triple(0.6f, 1.5f, 0.4f),
            Triple(0.3f, 2.0f, 0.2f)
        )

        val baseAmplitude = 25.dp.toPx()
        val waveFreqFactor = (beatFrequency.coerceIn(1.0, 30.0) / 15.0) * 0.02

        waveParams.forEach { (ampScale, freqScale, alpha) ->
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(0f, midY)
            
            for (x in 0..width.toInt() step 4) {
                val angle = (x * waveFreqFactor * freqScale) + phaseShift
                val y = midY + (baseAmplitude * ampScale * kotlin.math.sin(angle)).toFloat()
                path.lineTo(x.toFloat(), y)
            }
            path.lineTo(width, midY)
            
            drawPath(
                path = path,
                color = color.copy(alpha = alpha),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )
        }
    }
}

// ----------------------------------------------------
// 9. AdMob Native Advanced Ad Composable
// ----------------------------------------------------
@Composable
fun AdMobNativeAd(
    colors: ThemeColors,
    isSquare: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nativeAdState by remember { mutableStateOf<com.google.android.gms.ads.nativead.NativeAd?>(null) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, context.getString(com.monliev.brainwave.R.string.admob_native_ad_id))
            .forNativeAd { ad : com.google.android.gms.ads.nativead.NativeAd ->
                nativeAdState = ad
            }
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    // Fail silently
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    com.monliev.brainwave.audio.playback.AdMobManager.isNavigatingExternally = true
                }
            })
            .build()
        adLoader.loadAd(com.google.android.gms.ads.AdRequest.Builder().build())

        onDispose {
            nativeAdState?.destroy()
        }
    }

    val ad = nativeAdState

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.card),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSquare) Modifier.heightIn(min = 180.dp) else Modifier.height(115.dp)
            )
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
    ) {
        if (ad != null) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    val layoutName = if (isSquare) "layout_native_ad_square" else "layout_native_ad_horizontal"
                    val layoutId = ctx.resources.getIdentifier(layoutName, "layout", ctx.packageName)
                    android.view.LayoutInflater.from(ctx).inflate(layoutId, null)
                },
                update = { view ->
                    val ctx = view.context
                    val card = view.findViewById<android.widget.LinearLayout>(
                        ctx.resources.getIdentifier("ad_card", "id", ctx.packageName)
                    )
                    val bgDrawable = card?.background as? android.graphics.drawable.GradientDrawable
                    bgDrawable?.setColor(colors.card.toArgb())
                    val borderPx = (1 * ctx.resources.displayMetrics.density).toInt()
                    bgDrawable?.setStroke(borderPx, colors.border.toArgb())

                    val nativeAdView = view.findViewById<com.google.android.gms.ads.nativead.NativeAdView>(
                        ctx.resources.getIdentifier("native_ad_view", "id", ctx.packageName)
                    )
                    val headline = view.findViewById<android.widget.TextView>(
                        ctx.resources.getIdentifier("ad_headline", "id", ctx.packageName)
                    )
                    val body = view.findViewById<android.widget.TextView>(
                        ctx.resources.getIdentifier("ad_body", "id", ctx.packageName)
                    )
                    val icon = view.findViewById<android.widget.ImageView>(
                        ctx.resources.getIdentifier("ad_icon", "id", ctx.packageName)
                    )
                    val cta = view.findViewById<android.widget.Button>(
                        ctx.resources.getIdentifier("ad_call_to_action", "id", ctx.packageName)
                    )
                    val badge = view.findViewById<android.widget.TextView>(
                        ctx.resources.getIdentifier("ad_badge", "id", ctx.packageName)
                    )

                    // Bind colors
                    headline?.setTextColor(colors.text.toArgb())
                    body?.setTextColor(colors.textSecondary.toArgb())
                    badge?.setTextColor(ColorAccentBrain.toArgb())

                    if (isSquare) {
                        val advertiser = view.findViewById<android.widget.TextView>(
                            ctx.resources.getIdentifier("ad_advertiser", "id", ctx.packageName)
                        )
                        advertiser?.setTextColor(colors.textTertiary.toArgb())
                        advertiser?.text = ad.advertiser ?: "Sponsored"
                    }

                    // Populate data
                    headline?.text = ad.headline
                    body?.text = ad.body
                    if (ad.icon != null) {
                        icon?.setImageDrawable(ad.icon?.drawable)
                        icon?.visibility = android.view.View.VISIBLE
                    } else {
                        icon?.visibility = android.view.View.GONE
                    }

                    if (ad.callToAction != null) {
                        cta?.text = ad.callToAction
                        cta?.visibility = android.view.View.VISIBLE
                    } else {
                        cta?.visibility = android.view.View.INVISIBLE
                    }

                    // Register views to enable click redirect handling by Google Mobile Ads SDK!
                    nativeAdView?.headlineView = headline
                    nativeAdView?.bodyView = body
                    nativeAdView?.iconView = icon
                    nativeAdView?.callToActionView = cta
                    nativeAdView?.setNativeAd(ad)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading Ad...",
                    color = colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// 10. Guided Breathing Exercise (Box Breathing 4-4-4-4s)
// ----------------------------------------------------
enum class BreathingPhase(val instruction: String) {
    INHALE("Breathe In"),
    HOLD_IN("Hold Your Breath"),
    EXHALE("Breathe Out"),
    HOLD_OUT("Hold Empty")
}

@Composable
fun BreathingExerciseDialog(
    onDismiss: () -> Unit,
    categoryColor: Color,
    colors: ThemeColors
) {
    var phase by remember { mutableStateOf(BreathingPhase.INHALE) }
    var secondsRemaining by remember { mutableIntStateOf(4) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            for (sec in 4 downTo 1) {
                secondsRemaining = sec
                kotlinx.coroutines.delay(1000)
            }
            phase = when (phase) {
                BreathingPhase.INHALE -> BreathingPhase.HOLD_IN
                BreathingPhase.HOLD_IN -> BreathingPhase.EXHALE
                BreathingPhase.EXHALE -> BreathingPhase.HOLD_OUT
                BreathingPhase.HOLD_OUT -> BreathingPhase.INHALE
            }
        }
    }

    val scaleTarget = when (phase) {
        BreathingPhase.INHALE -> 2.0f
        BreathingPhase.HOLD_IN -> 2.0f
        BreathingPhase.EXHALE -> 1.0f
        BreathingPhase.HOLD_OUT -> 1.0f
    }

    val scale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(
            durationMillis = 4000,
            easing = FastOutSlowInEasing
        ),
        label = "breathingScale"
    )

    androidx.compose.ui.window.Dialog(
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false // Forces immersive full-screen dialog
        ),
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.95f))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            // Close Button (Top Right)
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .background(colors.card, shape = CircleShape)
                    .border(1.dp, colors.border, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.text
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Title Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    Text(
                        text = "Breathing Guide",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Text(
                        text = "Box Breathing 4-4-4-4s",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Breathing concentric bubble layout (Concentric glow shapes)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(260.dp)
                        .weight(1f)
                ) {
                    // Outer glow loop (alpha 0.08f)
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(scale * 1.35f)
                            .background(categoryColor.copy(alpha = 0.08f), CircleShape)
                    )
                    // Middle glow loop (alpha 0.20f)
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(scale * 1.15f)
                            .background(categoryColor.copy(alpha = 0.20f), CircleShape)
                    )
                    // Core solid bubble with dynamic counter text inside
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(scale)
                            .background(categoryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${secondsRemaining}s",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bottom Instructions
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    Text(
                        text = phase.instruction,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = categoryColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Relax and coordinate your breathing with the rhythm of the bubble.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    viewModel: MainScreenViewModel
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val colors = rememberThemeColors(isDarkMode)
    val sessionLogs by viewModel.sessionLogs.collectAsState()

    val totalMins = remember(sessionLogs) { viewModel.getTotalMinutesListened() }
    val streakDays = remember(sessionLogs) { viewModel.getDailyStreak() }
    val totalSessions = sessionLogs.size

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold, color = colors.text) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Daily Streak
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Daily Streak",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$streakDays Days",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = ColorAccentSpirit // Orange accent for fire/streak
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Consecutive days listened",
                            fontSize = 12.sp,
                            color = colors.textTertiary
                        )
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .background(ColorAccentSpirit.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = ColorAccentSpirit,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Card 2: Listen Time Summary Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Minutes Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Total Time", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$totalMins mins", fontSize = 20.sp, fontWeight = FontWeight.Black, color = colors.text)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Minutes listened", fontSize = 11.sp, color = colors.textTertiary)
                    }
                }

                // Sessions Count Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.card),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Sessions", fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$totalSessions", fontSize = 20.sp, fontWeight = FontWeight.Black, color = colors.text)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Completed runs", fontSize = 11.sp, color = colors.textTertiary)
                    }
                }
            }

            // Card 3: Listen Time by Category
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.card),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Listening Time by Category",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val categories = listOf("STUDY", "SPIRIT", "SLEEP", "BODY", "BRAIN")
                    val catMinutes = categories.map { it to viewModel.getMinutesByCategory(category = it) }
                    val maxMinutes = catMinutes.maxOfOrNull { it.second } ?: 1

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        catMinutes.forEach { (cat, mins) ->
                            val catColor = getCategoryColor(cat)
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textSecondary)
                                    Text(text = "$mins mins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .background(colors.border.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                ) {
                                    val progressFraction = mins.toFloat() / maxMinutes.toFloat()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = progressFraction.coerceAtLeast(0.01f).coerceAtMost(1f))
                                            .fillMaxHeight()
                                            .background(catColor, RoundedCornerShape(4.dp))
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Logs summary
            if (sessionLogs.isNotEmpty()) {
                Text(
                    text = "Recent Sessions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    modifier = Modifier.padding(top = 8.dp)
                )
                sessionLogs.take(5).forEach { log ->
                    val categoryColor = getCategoryColor(log.category)
                    val formattedDate = remember(log.timestamp) {
                        val sdf = java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault())
                        sdf.format(java.util.Date(log.timestamp))
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.card),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(categoryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = log.presetTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
                                Text(
                                    text = "${log.category} • Played for ${(log.durationSeconds / 60)}m",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Text(text = formattedDate, fontSize = 10.sp, color = colors.textTertiary)
                        }
                    }
                }
            }
        }
    }
}

fun getBrainwaveStateName(frequency: Double): String {
    return when {
        frequency < 4.0 -> "Delta (Deep Sleep / Healing)"
        frequency < 8.0 -> "Theta (Meditation / Dream / REM Sleep)"
        frequency < 12.0 -> "Alpha (Relaxed Alertness / Creativity)"
        frequency < 14.0 -> "Low Beta (Active Focus / Learning)"
        frequency < 30.0 -> "Beta (High Concentration / Cognitive Focus)"
        else -> "Gamma (Peak Mind State / Hyper-activity)"
    }
}

@Composable
fun PaywallDialog(
    colors: ThemeColors,
    viewModel: MainScreenViewModel,
    onDismiss: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf(1) } // 0: monthly, 1: lifetime
    var showCongrats by remember { mutableStateOf(false) }

    if (showCongrats) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {
            showCongrats = false
            onDismiss()
        }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.background),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = null,
                        tint = ColorAccentSleep,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Congratulations!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "You are now a Premium user of Brainwave! Enjoy an ad-free experience, unlimited custom library presets, and alarms scheduler.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = {
                            showCongrats = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Awesome!", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.background),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, colors.border, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Brainwave Premium",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                        }
                    }

                    // Value Proposition list
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(
                            "🚫  AD-FREE experience (All native ads removed)",
                            "💾  Create unlimited custom presets in Library",
                            "⏰  Configure automatic scheduled bedtime alarms",
                            "🎛️  Access to Audio Mixer & Layering controls"
                        ).forEach { perk ->
                            Text(text = perk, fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Plan selection
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Plan 1: Monthly
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selectedPlan == 0) ColorAccentBrain.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(12.dp))
                                .border(1.dp, if (selectedPlan == 0) ColorAccentBrain else colors.border, RoundedCornerShape(12.dp))
                                .clickable { selectedPlan = 0 }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Monthly Plan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.text)
                                Text(text = "Renews automatically. Cancel anytime.", fontSize = 11.sp, color = colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "$1.99 / mo", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorAccentBrain)
                        }

                        // Plan 2: Lifetime (Best value)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selectedPlan == 1) ColorAccentBrain.copy(alpha = 0.1f) else Color.Transparent, RoundedCornerShape(12.dp))
                                .border(1.dp, if (selectedPlan == 1) ColorAccentBrain else colors.border, RoundedCornerShape(12.dp))
                                .clickable { selectedPlan = 1 }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Lifetime Unlock", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.text)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "BEST VALUE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Text(text = "Pay once. Access forever.", fontSize = 11.sp, color = colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "$9.99", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorAccentBrain)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Purchase Action Button
                    Button(
                        onClick = {
                            viewModel.setPremium(true)
                            showCongrats = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorAccentBrain),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Unlock Premium Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Restore Purchases link
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.setPremium(true)
                            showCongrats = true
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Restore Purchases", color = colors.textTertiary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    colors: ThemeColors,
    onClick: (() -> Unit)? = null,
    action: (@Composable () -> Unit)? = null
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .then(clickModifier)
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = colors.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
private fun MixerSliderRow(
    label: String,
    value: Float,
    color: Color,
    colors: ThemeColors,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
            Text(
                text = String.format("%d%%", (value * 100).toInt()),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = colors.border
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun NoiseToggleChip(
    label: String,
    icon: String,
    isActive: Boolean,
    activeColor: Color,
    colors: ThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) activeColor.copy(alpha = 0.2f) else colors.card.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) activeColor else colors.border.copy(alpha = 0.3f)),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isActive) colors.text else colors.textSecondary
            )
        }
    }
}

