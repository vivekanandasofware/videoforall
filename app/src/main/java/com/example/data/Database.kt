package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "video_creations")
data class VideoCreation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "TEXT", "AUDIO", "SCRIPT", "IMAGE_ANIMATION", "TEMPLATE"
    val inputText: String,
    val generatedPrompt: String,
    val videoUrl: String, // Local or remote url, or mock resource path
    val aspectRatio: String, // "16:9", "9:16", "1:1", etc.
    val durationSec: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "GENERATING", "SUCCESS", "FAILED"
    val thumbnailUri: String, // Image preview URL or placeholder
    val analysisText: String? = null, // Gemini Pro analysis result
    val platformTemplate: String? = null // "TIKTOK", "INSTAGRAM", "YOUTUBE", "NONE"
)

@Dao
interface VideoCreationDao {
    @Query("SELECT * FROM video_creations ORDER BY timestamp DESC")
    fun getAllCreations(): Flow<List<VideoCreation>>

    @Query("SELECT * FROM video_creations WHERE id = :id")
    suspend fun getCreationById(id: Int): VideoCreation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(creation: VideoCreation): Long

    @Query("UPDATE video_creations SET status = :status, videoUrl = :videoUrl, thumbnailUri = :thumbnailUri, generatedPrompt = :generatedPrompt WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, videoUrl: String, thumbnailUri: String, generatedPrompt: String)

    @Query("UPDATE video_creations SET analysisText = :analysisText WHERE id = :id")
    suspend fun updateAnalysis(id: Int, analysisText: String)

    @Query("DELETE FROM video_creations WHERE id = :id")
    suspend fun deleteCreation(id: Int)
}

@Database(entities = [VideoCreation::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoCreationDao(): VideoCreationDao
}
