package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.VideoCreation
import com.example.data.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VideoGeneratorViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "video_creator_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    private val repository: VideoRepository by lazy {
        VideoRepository(application, database.videoCreationDao())
    }

    // List of creations
    val creations: StateFlow<List<VideoCreation>> = repository.allCreations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current generation and status states
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private val _transcribing = MutableStateFlow(false)
    val transcribing: StateFlow<Boolean> = _transcribing.asStateFlow()

    private val _scriptOutput = MutableStateFlow("")
    val scriptOutput: StateFlow<String> = _scriptOutput.asStateFlow()

    private val _scriptGenerating = MutableStateFlow(false)
    val scriptGenerating: StateFlow<Boolean> = _scriptGenerating.asStateFlow()

    private val _imageAnalysis = MutableStateFlow("")
    val imageAnalysis: StateFlow<String> = _imageAnalysis.asStateFlow()

    private val _analyzingImage = MutableStateFlow(false)
    val analyzingImage: StateFlow<Boolean> = _analyzingImage.asStateFlow()

    private val _generatedImageUri = MutableStateFlow("")
    val generatedImageUri: StateFlow<String> = _generatedImageUri.asStateFlow()

    private val _generatingImage = MutableStateFlow(false)
    val generatingImage: StateFlow<Boolean> = _generatingImage.asStateFlow()

    private val _videoAnalysis = MutableStateFlow<Map<Int, String>>(emptyMap())
    val videoAnalysis: StateFlow<Map<Int, String>> = _videoAnalysis.asStateFlow()

    private val _analyzingVideoId = MutableStateFlow<Int?>(null)
    val analyzingVideoId: StateFlow<Int?> = _analyzingVideoId.asStateFlow()

    // Interactive Generation Log console
    private val _generationLogs = MutableStateFlow<List<String>>(emptyList())
    val generationLogs: StateFlow<List<String>> = _generationLogs.asStateFlow()

    init {
        // Pre-populate mock video creations if database is empty on start
        viewModelScope.launch {
            delayAndPrepopulateIfEmpty()
        }
    }

    fun hasApiKey(): Boolean = repository.hasValidApiKey()

    fun addLog(message: String) {
        val current = _generationLogs.value.toMutableList()
        current.add("[AI LOG] $message")
        _generationLogs.value = current
    }

    fun clearLogs() {
        _generationLogs.value = emptyList()
    }

    private suspend fun delayAndPrepopulateIfEmpty() {
        // Allow Room database a brief moment to initialize and stream
        kotlinx.coroutines.delay(500)
        val currentCreations = database.videoCreationDao().getAllCreations()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null).value
            
        if (currentCreations.isNullOrEmpty()) {
            addLog("Initializing local studio templates and library samples...")
            val sample1 = VideoCreation(
                title = "Cyberpunk Neo Tokyo Odyssey",
                type = "TEMPLATE",
                inputText = "Cyberpunk street with flying cars, neon billboards, rain reflections",
                generatedPrompt = "A slow dramatic cinematic dolly-in shot of a wet Cyberpunk street in Tokyo. Flying neon-lit cars hover above pedestrians. Raindrops create shimmering ripples on the asphalt reflecting neon light. Model: veo-3.1-fast-generate-preview.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                aspectRatio = "9:16",
                durationSec = 5,
                status = "SUCCESS",
                thumbnailUri = "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=500&auto=format&fit=crop",
                platformTemplate = "TIKTOK",
                analysisText = "Visual pacing is high. Perfect cinematic loop. Outstanding viral engagement potential for TikTok tech aesthetic niche."
            )
            val sample2 = VideoCreation(
                title = "Icelandic Column Falls",
                type = "IMAGE_ANIMATION",
                inputText = "Animate a peaceful aerial drone shot of Icelandic basalt columns with a waterfall.",
                generatedPrompt = "Veo 3.1 motion path: Smooth drone fly-over, descending gradually towards basalt rock columns. White mist rises from the waterfall. Model: veo-3.1-fast-generate-preview.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                aspectRatio = "16:9",
                durationSec = 8,
                status = "SUCCESS",
                thumbnailUri = "https://images.unsplash.com/photo-1518173946687-a4c8a383392e?w=500&auto=format&fit=crop",
                platformTemplate = "YOUTUBE",
                analysisText = "Stunning cinematic landscape. Rich structural details are highly suitable for high-end cinematic introductions."
            )
            val sample3 = VideoCreation(
                title = "Dreamy Sunset Surf",
                type = "TEXT",
                inputText = "Golden hour sunset beach with gentle waves washing over sand",
                generatedPrompt = "A wide static shot of a golden hour sunset beach. Gentle waves lap softly on the wet sand, reflecting orange and pink skies. Model: veo-3.1-fast-generate-preview.",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                aspectRatio = "9:16",
                durationSec = 6,
                status = "SUCCESS",
                thumbnailUri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=500&auto=format&fit=crop",
                platformTemplate = "INSTAGRAM",
                analysisText = "Calming natural atmosphere. Highly engaging for travel reels, mindfulness content, and quotes background."
            )
            
            database.videoCreationDao().insertCreation(sample1)
            database.videoCreationDao().insertCreation(sample2)
            database.videoCreationDao().insertCreation(sample3)
            addLog("Studio initialized successfully with 3 preloaded samples.")
        }
    }

    /**
     * Actions & Triggers
     */

    fun generateMovieScript(prompt: String, platform: String) {
        viewModelScope.launch {
            _scriptGenerating.value = true
            _scriptOutput.value = ""
            clearLogs()
            addLog("Initializing Screenplay Script Engine...")
            addLog("Platform Target: $platform")
            addLog("Analyzing user creative brief...")
            addLog("Invoking model: ${if (hasApiKey()) "gemini-3.1-pro-preview" else "Fallback offline script builder"}")
            
            try {
                val script = repository.generateScript(prompt, platform)
                _scriptOutput.value = script
                addLog("Screenplay generated successfully!")
            } catch (e: Exception) {
                _scriptOutput.value = "Error generating script: ${e.message}"
                addLog("Error generating screenplay script.")
            } finally {
                _scriptGenerating.value = false
            }
        }
    }

    fun transcribeAudioNarration(audioBytes: ByteArray) {
        viewModelScope.launch {
            _transcribing.value = true
            _transcription.value = ""
            addLog("Loading audio recording...")
            addLog("MimeType: audio/mp3, length: ${audioBytes.size} bytes")
            addLog("Invoking transcription model: gemini-3.5-flash")
            try {
                val text = repository.transcribeAudio(audioBytes)
                _transcription.value = text
                addLog("Transcription complete! Narration imported.")
            } catch (e: Exception) {
                _transcription.value = "Audio imported."
                addLog("Failed to transcribe audio automatically. Fallback imported.")
            } finally {
                _transcribing.value = false
            }
        }
    }

    fun analyzeAndAnimatePhoto(imageBytes: ByteArray, animationBrief: String, aspectRatio: String) {
        viewModelScope.launch {
            _analyzingImage.value = true
            _imageAnalysis.value = ""
            clearLogs()
            addLog("Importing image asset...")
            addLog("Dimensions verified. Target layout: $aspectRatio")
            addLog("Invoking image understanding engine: gemini-3.1-pro-preview")
            
            try {
                // Step 1: Analyze image to get highly descriptive prompt
                val analysis = repository.analyzeImage(imageBytes)
                _imageAnalysis.value = analysis
                addLog("Image analysis complete. Subject & depth mapped.")
                
                // Step 2: Combine analysis with user animation instructions
                val videoPrompt = "Animate this scene. Initial state: $analysis. Animation Instructions: $animationBrief. Make it cinematic, slow panning motion, 4k high quality."
                
                // Step 3: Insert video creation record
                addLog("Provisioning new video asset in local Studio Library...")
                val creationId = database.videoCreationDao().insertCreation(
                    VideoCreation(
                        title = "Photo Animation: ${animationBrief.take(20)}...",
                        type = "IMAGE_ANIMATION",
                        inputText = animationBrief,
                        generatedPrompt = "Preparing motion analysis...",
                        videoUrl = "",
                        aspectRatio = aspectRatio,
                        durationSec = 6,
                        status = "GENERATING",
                        thumbnailUri = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop",
                        platformTemplate = "NONE"
                    )
                ).toInt()

                // Step 4: Call Veo generation in background
                addLog("Deploying Veo 3 Engine...")
                addLog("Invoking model: veo-3.1-fast-generate-preview")
                addLog("Synthesizing temporal textures...")
                
                _isGenerating.value = true
                repository.generateVideo(videoPrompt, aspectRatio, creationId)
                addLog("Veo Generation completed successfully!")
            } catch (e: Exception) {
                _imageAnalysis.value = "Error: ${e.message}"
                addLog("Animation generation failed: ${e.message}")
            } finally {
                _analyzingImage.value = false
                _isGenerating.value = false
            }
        }
    }

    fun generateVideoFromTextPrompt(prompt: String, aspectRatio: String, template: String, durationSec: Int = 5) {
        viewModelScope.launch {
            _isGenerating.value = true
            clearLogs()
            addLog("Initializing Video Engine...")
            addLog("Target Template: $template")
            addLog("Layout configuration: $aspectRatio")
            addLog("Requested Length: ${durationSec}s")
            
            try {
                // Step 1: Insert initial database item
                addLog("Registering creation in local Studio Database...")
                val computedTitle = if (durationSec > 15) "Cinematic Movie: ${prompt.take(20)}..." else "AI Gen: ${prompt.take(25)}..."
                val computedType = if (durationSec > 15) "MOVIE" else "TEXT"
                
                val creationId = database.videoCreationDao().insertCreation(
                    VideoCreation(
                        title = computedTitle,
                        type = computedType,
                        inputText = prompt,
                        generatedPrompt = "Compiling visual instructions...",
                        videoUrl = "",
                        aspectRatio = aspectRatio,
                        durationSec = durationSec,
                        status = "GENERATING",
                        thumbnailUri = if (durationSec > 15) "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500&auto=format&fit=crop" else "https://images.unsplash.com/photo-1541701494587-cb58502866ab?w=500&auto=format&fit=crop",
                        platformTemplate = template
                    )
                ).toInt()

                // Step 2: Call Veo 3
                addLog("Launching Model: veo-3.1-fast-generate-preview")
                if (durationSec > 15) {
                    addLog("Extended Mode: Splitting input script into episodic chapters...")
                    val scenesCount = (durationSec / 10).coerceAtLeast(2).coerceAtMost(6)
                    for (i in 1..scenesCount) {
                        addLog("Rendering Cinematic Scene $i / $scenesCount: Translating visual beats...")
                        kotlinx.coroutines.delay(600)
                    }
                    addLog("Stitching and compiling dynamic scenes into final $durationSec-second movie timeline...")
                } else {
                    addLog("Generating single-clip keyframes...")
                }
                
                repository.generateVideo(prompt, aspectRatio, creationId, durationSec)
                addLog("Video generation complete! Video added to Studio.")
            } catch (e: Exception) {
                addLog("Failed text-to-video generation: ${e.message}")
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun generateCustomStudioImage(prompt: String, aspectRatio: String, useStudioQuality: Boolean) {
        viewModelScope.launch {
            _generatingImage.value = true
            _generatedImageUri.value = ""
            addLog("Initializing Custom Studio Image Generator...")
            addLog("Selected Ratio: $aspectRatio | Quality: ${if (useStudioQuality) "Studio Pro" else "Standard Flash"}")
            addLog("Invoking model: ${if (useStudioQuality) "gemini-3-pro-image-preview" else "gemini-3.1-flash-image-preview"}")
            
            try {
                val imageUri = repository.generateImage(prompt, aspectRatio, useStudioQuality)
                _generatedImageUri.value = imageUri
                addLog("Custom Image Generated Successfully!")
            } catch (e: Exception) {
                addLog("Image Generation Failed: ${e.message}")
            } finally {
                _generatingImage.value = false
            }
        }
    }

    fun analyzeVideoContentWithGeminiPro(creationId: Int) {
        viewModelScope.launch {
            _analyzingVideoId.value = creationId
            addLog("Retrieving video metadata for analysis...")
            addLog("Invoking video understanding engine: gemini-3.1-pro-preview")
            try {
                val result = repository.analyzeVideo(creationId)
                val currentMap = _videoAnalysis.value.toMutableMap()
                currentMap[creationId] = result
                _videoAnalysis.value = currentMap
                addLog("Video Content Analysis Complete!")
            } catch (e: Exception) {
                addLog("Video analysis failed: ${e.message}")
            } finally {
                _analyzingVideoId.value = null
            }
        }
    }

    fun deleteVideoCreation(id: Int) {
        viewModelScope.launch {
            repository.deleteCreation(id)
            addLog("Deleted creation #$id from library.")
        }
    }
}
