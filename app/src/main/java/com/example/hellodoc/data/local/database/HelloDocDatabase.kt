package com.example.hellodoc.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hellodoc.data.local.dao.FavoriteDoctorDao
import com.example.hellodoc.data.local.entity.FavoriteDoctorEntity

@Database(
    entities = [FavoriteDoctorEntity::class],
    version = 1,
    exportSchema = false
)
abstract class HelloDocDatabase : RoomDatabase() {

    abstract fun favoriteDoctorDao(): FavoriteDoctorDao

    companion object {

        @Volatile
        private var INSTANCE: HelloDocDatabase? = null

        fun getDatabase(context: Context): HelloDocDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HelloDocDatabase::class.java,
                    "hellodoc_local.db"
                ).build().also { database ->
                    INSTANCE = database
                }
            }
        }
    }
    //Nous appliquons le pattern Singleton pour partager une seule instance de la base Room dans l’application. INSTANCE conserve cette instance et getDatabase() la réutilise. synchronized protège sa création si plusieurs threads y accèdent simultanément. @Volatile garantit que les threads voient sa valeur à jour
}