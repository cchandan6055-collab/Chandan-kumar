package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "voice_history")
data class VoiceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "en"
)

@Dao
interface VoiceHistoryDao {
    @Query("SELECT * FROM voice_history ORDER BY timestamp DESC")
    fun getAllVoiceHistory(): Flow<List<VoiceHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceCommand(item: VoiceHistoryEntity): Long

    @Query("DELETE FROM voice_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM voice_history")
    suspend fun clearAll()
}
