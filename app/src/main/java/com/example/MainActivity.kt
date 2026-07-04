package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.VideoCreation
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.VideoGeneratorViewModel
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

enum class StudioTab {
    SCRIPT_REEL,
    ANIMATE_PHOTO,
    LIBRARY,
    AI_ASSISTANTS
}

@Composable
fun MainAppScreen() {
    val viewModel: VideoGeneratorViewModel = viewModel()
    var currentTab by remember { mutableStateOf(StudioTab.SCRIPT_REEL) }
    val isGenerating by viewModel.isGenerating.collectAsState()
    val creations by viewModel.creations.collectAsState()
    val context = LocalContext.current

    // Active expanded item for full playback and analysis
    var activePlaybackVideo by remember { mutableStateOf<VideoCreation?>(null) }
    
    // Auto-update playback item if the database updates and we are watching it
    LaunchedEffect(creations) {
        activePlaybackVideo?.let { active ->
            val updated = creations.find { it.id == active.id }
            if (updated != null && updated.status != active.status) {
                activePlaybackVideo = updated
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            StudioBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0C0E))
                .padding(innerPadding)
        ) {
            // Elegant Studio Header
            StudioHeader(viewModel = viewModel)

            // Live generation log ticker if generating
            LiveLogsTicker(viewModel = viewModel)

            // Main tab views
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    StudioTab.SCRIPT_REEL -> {
                        ScriptToReelScreen(
                            viewModel = viewModel,
                            onVideoGenerated = {
                                Toast.makeText(context, "Generation initiated. Check Library!", Toast.LENGTH_LONG).show()
                                currentTab = StudioTab.LIBRARY
                            }
                        )
                    }
                    StudioTab.ANIMATE_PHOTO -> {
                        AnimatePhotoScreen(
                            viewModel = viewModel,
                            onVideoGenerated = {
                                Toast.makeText(context, "Photo animation scheduled. Rendering...", Toast.LENGTH_LONG).show()
                                currentTab = StudioTab.LIBRARY
                            }
                        )
                    }
                    StudioTab.LIBRARY -> {
                        LibraryScreen(
                            viewModel = viewModel,
                            onVideoSelected = { activePlaybackVideo = it }
                        )
                    }
                    StudioTab.AI_ASSISTANTS -> {
                        AIAssistantsScreen(
                            viewModel = viewModel,
                            onImageSentToAnimate = { imageUri ->
                                Toast.makeText(context, "Asset loaded to Animate Photo!", Toast.LENGTH_SHORT).show()
                                currentTab = StudioTab.ANIMATE_PHOTO
                            }
                        )
                    }
                }
            }
        }

        // Expanded Video Playback & Gemini Pro Analysis Dialog
        activePlaybackVideo?.let { video ->
            PlaybackAndAnalysisOverlay(
                video = video,
                viewModel = viewModel,
                onClose = { activePlaybackVideo = null }
            )
        }
    }
}

@Composable
fun StudioHeader(viewModel: VideoGeneratorViewModel) {
    val isKeyValid = viewModel.hasApiKey()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF16161C))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = "Logo",
                tint = Color(0xFF00F0FF),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AI SOCIAL STUDIO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Gemini & Veo Video Suite",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // API Connection Status Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (isKeyValid) Color(0x2039FF14) else Color(0x20FF3B30))
                    .border(
                        1.dp,
                        if (isKeyValid) Color(0xFF39FF14).copy(alpha = 0.5f) else Color(0xFFFF3B30).copy(alpha = 0.5f),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isKeyValid) Color(0xFF39FF14) else Color(0xFFFF3B30))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isKeyValid) "API CONNECTED" else "DEMO MODE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isKeyValid) Color(0xFF39FF14) else Color(0xFFFF3B30)
                )
            }
        }

        if (!isKeyValid) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.LightGray.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Configure your GEMINI_API_KEY in the Secrets Panel for live cloud generations.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun LiveLogsTicker(viewModel: VideoGeneratorViewModel) {
    val logs by viewModel.generationLogs.collectAsState()

    AnimatedVisibility(visible = logs.isNotEmpty()) {
        Box(
            Modifier.Companion
                .fillMaxWidth()
                .background(Color(0xFF070709))
                .border(width = 1.dp, color = Color(0xFF1F1F24))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI ENGINE:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00F0FF),
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = logs.lastOrNull() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun StudioBottomNavigation(
    currentTab: StudioTab,
    onTabSelected: (StudioTab) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF16161C),
        tonalElevation = 8.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = currentTab == StudioTab.SCRIPT_REEL,
            onClick = { onTabSelected(StudioTab.SCRIPT_REEL) },
            icon = { Icon(Icons.Default.Movie, contentDescription = "Script to Reel") },
            label = { Text("Script to Reel") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00F0FF),
                selectedTextColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF1E293B)
            ),
            modifier = Modifier.testTag("nav_script_reel")
        )
        NavigationBarItem(
            selected = currentTab == StudioTab.ANIMATE_PHOTO,
            onClick = { onTabSelected(StudioTab.ANIMATE_PHOTO) },
            icon = { Icon(Icons.Default.Image, contentDescription = "Animate Photo") },
            label = { Text("Animate Photo") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00F0FF),
                selectedTextColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF1E293B)
            ),
            modifier = Modifier.testTag("nav_animate_photo")
        )
        NavigationBarItem(
            selected = currentTab == StudioTab.LIBRARY,
            onClick = { onTabSelected(StudioTab.LIBRARY) },
            icon = { Icon(Icons.Default.History, contentDescription = "Library") },
            label = { Text("Library") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00F0FF),
                selectedTextColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF1E293B)
            ),
            modifier = Modifier.testTag("nav_library")
        )
        NavigationBarItem(
            selected = currentTab == StudioTab.AI_ASSISTANTS,
            onClick = { onTabSelected(StudioTab.AI_ASSISTANTS) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Studio Tools") },
            label = { Text("AI Tools") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF00F0FF),
                selectedTextColor = Color(0xFF00F0FF),
                unselectedIconColor = Color.Gray,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color(0xFF1E293B)
            ),
            modifier = Modifier.testTag("nav_ai_assistants")
        )
    }
}

// ==========================================
// TAB 1: SCRIPT TO REEL SCREEN
// ==========================================
@Composable
fun ScriptToReelScreen(
    viewModel: VideoGeneratorViewModel,
    onVideoGenerated: () -> Unit
) {
    var conceptPrompt by remember { mutableStateOf("") }
    var selectedRatio by remember { mutableStateOf("9:16") } // "9:16" or "16:9"
    var selectedPlatform by remember { mutableStateOf("TIKTOK") } // "TIKTOK", "INSTAGRAM", "YOUTUBE"
    var selectedDurationMode by remember { mutableStateOf("AUTO") } // "AUTO", "SHORT", "MEDIUM", "LONG_MOVIE"
    val isScriptGenerating by viewModel.scriptGenerating.collectAsState()
    val scriptOutput by viewModel.scriptOutput.collectAsState()
    val isGeneratingVideo by viewModel.isGenerating.collectAsState()

    val computedDuration = remember(conceptPrompt, scriptOutput, selectedDurationMode) {
        when (selectedDurationMode) {
            "SHORT" -> 5
            "MEDIUM" -> 15
            "LONG_MOVIE" -> 60
            else -> { // AUTO mode: auto-scale based on text/script length
                val textLength = if (scriptOutput.isNotEmpty()) scriptOutput.length else conceptPrompt.length
                when {
                    textLength > 600 -> 90
                    textLength > 300 -> 60
                    textLength > 150 -> 30
                    textLength > 80 -> 15
                    else -> 5
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "1. Write Video Prompt or Narrative Script",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = conceptPrompt,
                onValueChange = { conceptPrompt = it },
                placeholder = { Text("Enter your vision: e.g., 'An epic journey across Icelandic volcanoes with a futuristic hovercraft during pink sunset'") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("concept_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = Color(0xFF1F1F24),
                    focusedContainerColor = Color(0xFF16161C),
                    unfocusedContainerColor = Color(0xFF16161C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. Select Platform & Layout Template",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Platform choices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("TIKTOK" to "9:16", "INSTAGRAM" to "1:1", "YOUTUBE" to "16:9").forEach { (platform, ratio) ->
                    val isSelected = selectedPlatform == platform
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF16161C))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00F0FF) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedPlatform = platform
                                selectedRatio = if (ratio == "1:1") "9:16" else ratio // Veo requires 16:9 or 9:16
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = platform,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF00F0FF) else Color.LightGray
                            )
                            Text(
                                text = if (ratio == "1:1") "Vertical 9:16" else ratio,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "3. Duration & Cinematic Movie Mode",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Duration selector buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "AUTO" to "Auto-Scale\n(Based on text)",
                    "SHORT" to "Social Reel\n(5s short)",
                    "MEDIUM" to "Cinematic\n(15s medium)",
                    "LONG_MOVIE" to "Full Movie\n(60s+ long)"
                ).forEach { (mode, label) ->
                    val isSelected = selectedDurationMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF16161C))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00F0FF) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                selectedDurationMode = mode
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = mode.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF00F0FF) else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Feedback Banner about length
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF16161C))
                    .border(1.dp, Color(0xFF1F1F24), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (computedDuration > 15) Icons.Default.Videocam else Icons.Default.Movie,
                        contentDescription = "Info",
                        tint = if (computedDuration > 15) Color(0xFF39FF14) else Color(0xFF00F0FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (computedDuration > 15) "EPISODIC MOVIE RENDER ACTIVE" else "STANDARD SOCIAL REEL ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (computedDuration > 15) Color(0xFF39FF14) else Color(0xFF00F0FF)
                        )
                        Text(
                            text = "Target video duration: ${computedDuration} seconds. ${
                                if (selectedDurationMode == "AUTO") "Dynamically auto-scaled from text volume (${if (scriptOutput.isNotEmpty()) "Script" else "Prompt"} length: ${if (scriptOutput.isNotEmpty()) scriptOutput.length else conceptPrompt.length} chars)."
                                else "Fixed preset mode selected."
                            }",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.generateMovieScript(conceptPrompt, selectedPlatform) },
                    enabled = conceptPrompt.isNotBlank() && !isScriptGenerating,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(50.dp)
                        .testTag("generate_script_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7209B7),
                        disabledContainerColor = Color(0xFF2E1C3F)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isScriptGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Description, contentDescription = "Write Script")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Draft Script", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        viewModel.generateVideoFromTextPrompt(conceptPrompt, selectedRatio, selectedPlatform, computedDuration)
                        onVideoGenerated()
                    },
                    enabled = conceptPrompt.isNotBlank() && !isGeneratingVideo,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("generate_video_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00F0FF),
                        disabledContainerColor = Color(0xFF0F393D)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGeneratingVideo) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Default.Movie, contentDescription = "Generate Video", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (computedDuration > 15) "Generate Movie" else "Generate Reel", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Script result output area
            AnimatedVisibility(visible = scriptOutput.isNotEmpty() || isScriptGenerating) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF16161C))
                        .border(1.dp, Color(0xFF24242E), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Script",
                            tint = Color(0xFFB5179E)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Screenplay Storyboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Copy Script Button
                        val clipboard = LocalClipboardManager.current
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(scriptOutput))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Script", tint = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (isScriptGenerating) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFFB5179E))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Gemini Pro planning storyboard layout...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Text(
                            text = scriptOutput,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Trigger direct video generation from this specific script prompt
                        Button(
                            onClick = {
                                viewModel.generateVideoFromTextPrompt(
                                    prompt = "Cinematic video based on script: $conceptPrompt\n\nScript: $scriptOutput",
                                    aspectRatio = selectedRatio,
                                    template = selectedPlatform,
                                    durationSec = computedDuration
                                )
                                onVideoGenerated()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB5179E))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Animate Screenplay")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (computedDuration > 15) "Animate Storyboard to Movie" else "Animate Screenplay to Veo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: ANIMATE PHOTO SCREEN
// ==========================================
@Composable
fun AnimatePhotoScreen(
    viewModel: VideoGeneratorViewModel,
    onVideoGenerated: () -> Unit
) {
    // Beautiful stock starting frames that can be selected for instant preview/generation
    val samplePhotos = listOf(
        "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop" to "Cyberpunk Cyber City",
        "https://images.unsplash.com/photo-1518173946687-a4c8a383392e?w=500&auto=format&fit=crop" to "Mountain Lake Cascade",
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop" to "Sunset Beach Escape",
        "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop" to "Fantasy Dream Castle"
    )

    val motionTemplates = listOf(
        Triple("3D Camera Orbit", "A slow 360-degree camera orbit with volumetric lighting and floating particle effects.", Icons.Default.Videocam),
        Triple("Cinematic Zoom", "Slow dramatic zoom-in with ultra shallow depth of field and soft visual focus.", Icons.Default.Movie),
        Triple("Epic Drone Pan", "Sweeping drone flyby, panning down slowly with visual depth and subtle drift.", Icons.Default.AutoAwesome),
        Triple("Dream Time-lapse", "Fast moving clouds overhead, gradual twilight transition, and starry night sky.", Icons.Default.Info),
        Triple("Parallax Slide", "Ken Burns vertical panning coupled with horizontal parallax sliding.", Icons.Default.Image)
    )

    var selectedPhotoUrl by remember { mutableStateOf(samplePhotos[0].first) }
    var selectedTemplateIndex by remember { mutableStateOf(-1) }
    var animationBrief by remember { mutableStateOf("Slow cinematic zoom into the neon lights, cars slowly hovering, steam rising from puddles") }
    var selectedRatio by remember { mutableStateOf("9:16") } // "16:9" or "9:16"
    val imageAnalysis by viewModel.imageAnalysis.collectAsState()
    val isAnalyzing by viewModel.analyzingImage.collectAsState()
    val generatedImageUri by viewModel.generatedImageUri.collectAsState()

    // Handle incoming custom generated image if applicable
    LaunchedEffect(generatedImageUri) {
        if (generatedImageUri.isNotEmpty()) {
            selectedPhotoUrl = generatedImageUri
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Text(
                text = "1. Choose Initial Photo Frame",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Display of currently active photo frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(16.dp))
            ) {
                AsyncImage(
                    model = selectedPhotoUrl,
                    contentDescription = "Initial frame",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                Text(
                    text = if (selectedPhotoUrl.startsWith("data:")) "Custom Generated Studio Art" else "Sample Source Frame",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00F0FF),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid of photo selections
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                samplePhotos.forEach { (url, title) ->
                    val isSelected = selectedPhotoUrl == url
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                2.dp,
                                if (isSelected) Color(0xFF00F0FF) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedPhotoUrl = url }
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "2. Select Cinematic Motion Template",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                itemsIndexed(motionTemplates) { index, (title, description, icon) ->
                    val isSelected = selectedTemplateIndex == index
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(115.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF16161C))
                            .border(
                                1.5.dp,
                                if (isSelected) Color(0xFF00F0FF) else Color(0xFF1F1F24),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTemplateIndex = index
                                animationBrief = description
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) Color(0xFF00F0FF) else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF00F0FF) else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "3. Refine Motion Instructions",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = animationBrief,
                onValueChange = { 
                    animationBrief = it
                    if (selectedTemplateIndex >= 0 && motionTemplates[selectedTemplateIndex].second != it) {
                        selectedTemplateIndex = -1
                    }
                },
                placeholder = { Text("How should the image move? e.g., 'Make the water cascade rapidly and add camera orbit'...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .testTag("motion_brief_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = Color(0xFF1F1F24),
                    focusedContainerColor = Color(0xFF16161C),
                    unfocusedContainerColor = Color(0xFF16161C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Aspect ratio selection
            Text(
                text = "4. Target Aspect Ratio",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("9:16" to "TikTok / Instagram Reels", "16:9" to "YouTube Landscape").forEach { (ratio, desc) ->
                    val isSelected = selectedRatio == ratio
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1E293B) else Color(0xFF16161C))
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF00F0FF) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedRatio = ratio }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = ratio,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF00F0FF) else Color.White
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main CTA Button
            Button(
                onClick = {
                    // Start process: Analyse first, then generate video via Veo model
                    viewModel.analyzeAndAnimatePhoto(
                        imageBytes = ByteArray(100), // Mock bytes or load selected sample
                        animationBrief = animationBrief,
                        aspectRatio = selectedRatio
                    )
                    onVideoGenerated()
                },
                enabled = animationBrief.isNotBlank() && !isAnalyzing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("animate_photo_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini Pro mapping layout layers...", color = Color.Black, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Animate", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Animate Photo with Veo", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// TAB 3: LIBRARY SCREEN
// ==========================================
@Composable
fun LibraryScreen(
    viewModel: VideoGeneratorViewModel,
    onVideoSelected: (VideoCreation) -> Unit
) {
    val creations by viewModel.creations.collectAsState()

    if (creations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Empty",
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Creations Yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Your generated social reels will appear here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Library Creations Studio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Manage, play, and run Gemini analytics on generated assets",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(creations) { item ->
                    VideoCreationCard(
                        item = item,
                        onClick = { onVideoSelected(item) },
                        onDelete = { viewModel.deleteVideoCreation(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun VideoCreationCard(
    item: VideoCreation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("video_creation_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16161C)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            // Thumbnail
            AsyncImage(
                model = item.thumbnailUri,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Transparent overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            // Status label badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (item.status) {
                            "SUCCESS" -> Color(0xFF39FF14).copy(alpha = 0.2f)
                            "GENERATING" -> Color(0xFF00F0FF).copy(alpha = 0.2f)
                            else -> Color(0xFFFF3B30).copy(alpha = 0.2f)
                        }
                    )
                    .border(
                        1.dp,
                        when (item.status) {
                            "SUCCESS" -> Color(0xFF39FF14)
                            "GENERATING" -> Color(0xFF00F0FF)
                            else -> Color(0xFFFF3B30)
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (item.status) {
                        "SUCCESS" -> Color(0xFF39FF14)
                        "GENERATING" -> Color(0xFF00F0FF)
                        else -> Color(0xFFFF3B30)
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            // Aspect Ratio Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = item.aspectRatio,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }

            // Play Icon
            if (item.status == "SUCCESS") {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center)
                )
            }

            // Platform tag icon
            if (item.platformTemplate != null) {
                Text(
                    text = item.platformTemplate,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00F0FF),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                )
            }
        }

        // Info details
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.inputText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (item.durationSec > 15) "${item.durationSec}s Cinematic Movie" else "${item.durationSec}s Reel",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (item.durationSec > 15) Color(0xFF39FF14) else Color.Gray,
                    fontWeight = if (item.durationSec > 15) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onDelete() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 4: AI ASSISTANTS & MULTIMODAL PLAYGROUND
// ==========================================
@Composable
fun AIAssistantsScreen(
    viewModel: VideoGeneratorViewModel,
    onImageSentToAnimate: (String) -> Unit
) {
    var assistantPrompt by remember { mutableStateOf("") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var useStudioQuality by remember { mutableStateOf(true) }
    val generatingImage by viewModel.generatingImage.collectAsState()
    val generatedImageUri by viewModel.generatedImageUri.collectAsState()

    var isRecordingAudio by remember { mutableStateOf(false) }
    val isTranscribing by viewModel.transcribing.collectAsState()
    val transcription by viewModel.transcription.collectAsState()

    val context = LocalContext.current

    // Audio recording request launcher
    val recordPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                isRecordingAudio = true
            } else {
                Toast.makeText(context, "Microphone access denied. Using AI narrative logs.", Toast.LENGTH_LONG).show()
                // Use simulated transcription
                viewModel.transcribeAudioNarration(ByteArray(10))
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Microphone Audio Narration Segment
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16161C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.KeyboardVoice,
                            contentDescription = "Voice Input",
                            tint = Color(0xFF39FF14)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Voice Narration Transcriber",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Speak your video ideas using model gemini-3.5-flash",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val currentPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.RECORD_AUDIO
                                )
                                if (currentPermission == PackageManager.PERMISSION_GRANTED) {
                                    isRecordingAudio = !isRecordingAudio
                                    if (!isRecordingAudio) {
                                        // Stop and trigger transcription
                                        viewModel.transcribeAudioNarration(ByteArray(100))
                                    }
                                } else {
                                    recordPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecordingAudio) Color.Red else Color(0xFF39FF14)
                            ),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(
                                imageVector = if (isRecordingAudio) Icons.Default.Close else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = if (isRecordingAudio) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRecordingAudio) "Stop & Transcribe" else "Record Speech",
                                color = if (isRecordingAudio) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Simulated Fast Narrative Trigger
                        Button(
                            onClick = { viewModel.transcribeAudioNarration(ByteArray(0)) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24242E)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Simulate Narrative", color = Color.White)
                        }
                    }

                    // Recording wave anim or result
                    if (isRecordingAudio) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(color = Color.Red, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording audio stream... [Live Waveform]",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isTranscribing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CircularProgressIndicator(color = Color(0xFF39FF14))
                    } else if (transcription.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF24242E))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TRANSCRIBED VOICE NARRATION:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF39FF14)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = transcription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        // Custom Visual Image Generation Segment
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16161C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Image Gen",
                            tint = Color(0xFF00F0FF)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Studio Image Creator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Generate visual frames with specific layout ratios",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = assistantPrompt,
                        onValueChange = { assistantPrompt = it },
                        placeholder = { Text("What image do you want to create? e.g. 'A futuristic robot painter crafting neon waves, cinematic digital art'...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("assistant_image_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = Color(0xFF24242E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ratio picker list: 1:1, 9:16, 16:9, etc.
                    Text(
                        text = "Layout Aspect Ratio:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1:1", "9:16", "16:9", "4:3", "21:9").forEach { r ->
                            val isSel = selectedRatio == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF1E293B) else Color(0xFF0C0C0E))
                                    .border(
                                        1.dp,
                                        if (isSel) Color(0xFF00F0FF) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedRatio = r }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = r,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color(0xFF00F0FF) else Color.LightGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Studio Pro Quality (gemini-3-pro)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { useStudioQuality = !useStudioQuality }
                        ) {
                            Icon(
                                imageVector = if (useStudioQuality) Icons.Default.AutoAwesome else Icons.Default.Close,
                                contentDescription = "Quality",
                                tint = if (useStudioQuality) Color(0xFF00F0FF) else Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.generateCustomStudioImage(
                                assistantPrompt,
                                selectedRatio,
                                useStudioQuality
                            )
                        },
                        enabled = assistantPrompt.isNotBlank() && !generatingImage,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF))
                    ) {
                        if (generatingImage) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Generate Image Frame", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Render generated preview
                    if (generatedImageUri.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF00F0FF), RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = generatedImageUri,
                                contentDescription = "Generated art",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Button(
                                onClick = { onImageSentToAnimate(generatedImageUri) },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB5179E))
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Animate")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send to Animate Photo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// EXPANDED PLAYBACK AND GEMINI ANALYSIS OVERLAY
// ==========================================
@Composable
fun PlaybackAndAnalysisOverlay(
    video: VideoCreation,
    viewModel: VideoGeneratorViewModel,
    onClose: () -> Unit
) {
    val analysisMap by viewModel.videoAnalysis.collectAsState()
    val analyzingVideoId by viewModel.analyzingVideoId.collectAsState()
    val isAnalyzing = analyzingVideoId == video.id
    val videoAnalysisResult = analysisMap[video.id] ?: video.analysisText

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false) {}
            .testTag("playback_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Source format: ${video.aspectRatio} | Type: ${video.type}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(onClick = { /* Export simulation */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Export to TikTok", tint = Color(0xFF00F0FF))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Player
            VideoPlayerView(
                videoUrl = video.videoUrl,
                thumbnailUrl = video.thumbnailUri,
                aspectRatio = if (video.aspectRatio == "9:16") 9f / 16f else 16f / 9f,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Section
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16161C)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Gemini",
                                tint = Color(0xFFB5179E)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Social Insights Report",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Model: gemini-3.1-pro-preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (isAnalyzing) {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                CircularProgressIndicator(color = Color(0xFFB5179E))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Analyzing scene visuals and motion templates...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else if (videoAnalysisResult != null) {
                        item {
                            Text(
                                text = videoAnalysisResult,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray,
                                lineHeight = 20.sp
                            )
                        }
                    } else {
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "No insights compiled for this video asset.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.analyzeVideoContentWithGeminiPro(video.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB5179E))
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Analyze")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Compile Performance Insights", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
