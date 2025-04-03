package data.chat

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


// AppChatDatabase.kt
@Database(entities = [CachedChatListItem::class, CachedMessage::class], version = 2) // Tăng version lên 2
abstract class AppChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppChatDatabase? = null

        fun getDatabase(context: Context): AppChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppChatDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Thêm để tự động xóa dữ liệu cũ khi version tăng
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}