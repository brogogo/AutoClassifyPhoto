package com.example.autoclassify.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ClassificationEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ClassificationDatabase : RoomDatabase() {
    abstract fun classificationDao(): ClassificationDao

    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: ClassificationDatabase? = null

        fun getInstance(context: Context): ClassificationDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ClassificationDatabase::class.java,
                    "auto_classify.db"
                ).build().also { database ->
                    INSTANCE = database
                }
            }
    }
}
