package com.mahfouz.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mahfouz.app.data.local.dao.CategoryDao
import com.mahfouz.app.data.local.dao.InvoiceDao
import com.mahfouz.app.data.local.entity.CategoryEntity
import com.mahfouz.app.data.local.entity.InvoiceEntity
import com.mahfouz.app.data.local.entity.InvoiceItemEntity

@Database(
    entities = [
        CategoryEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MahfouzDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: MahfouzDatabase? = null

        fun getDatabase(context: Context): MahfouzDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MahfouzDatabase::class.java,
                    "mahfouz_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
