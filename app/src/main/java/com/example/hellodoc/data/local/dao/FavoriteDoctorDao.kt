package com.example.hellodoc.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hellodoc.data.local.entity.FavoriteDoctorEntity

@Dao
interface FavoriteDoctorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favorite: FavoriteDoctorEntity): Long

    @Query("""
        SELECT * FROM favorite_doctors
        WHERE ownerId = :ownerId
        ORDER BY savedAt DESC, id DESC
    """)
    fun observeAll(
        ownerId: String
    ): LiveData<List<FavoriteDoctorEntity>>

    @Query("""
        SELECT * FROM favorite_doctors
        WHERE ownerId = :ownerId
          AND (
              doctorName LIKE '%' || :query || '%'
              OR specialty LIKE '%' || :query || '%'
              OR city LIKE '%' || :query || '%'
              OR personalLabel LIKE '%' || :query || '%'
          )
        ORDER BY savedAt DESC, id DESC
    """)
    fun search(
        ownerId: String,
        query: String
    ): LiveData<List<FavoriteDoctorEntity>>

    @Query("""
        SELECT * FROM favorite_doctors
        WHERE ownerId = :ownerId AND id = :id
        LIMIT 1
    """)
    suspend fun getById(
        ownerId: String,
        id: Long
    ): FavoriteDoctorEntity?

    @Update
    suspend fun update(favorite: FavoriteDoctorEntity): Int

    @Delete
    suspend fun delete(favorite: FavoriteDoctorEntity): Int
}