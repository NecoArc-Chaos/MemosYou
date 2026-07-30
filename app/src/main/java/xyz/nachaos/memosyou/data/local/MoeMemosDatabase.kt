package xyz.nachaos.memosyou.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import xyz.nachaos.memosyou.data.local.dao.MemoDao
import xyz.nachaos.memosyou.data.local.entity.MemoEntity
import xyz.nachaos.memosyou.data.local.entity.ResourceEntity

@Database(
    entities = [MemoEntity::class, ResourceEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MoeMemosDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: MoeMemosDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE memos ADD COLUMN relations TEXT NOT NULL DEFAULT '[]'")
                database.execSQL("ALTER TABLE memos ADD COLUMN location TEXT")
            }
        }

        /**
         * Register migrations here as the schema evolves.
         *
         * Destructive migration is disabled to prevent accidental data loss.
         * If schema changes require migration, implement a proper Migration
         * and add it to ALL_MIGRATIONS.
         */
        val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            MIGRATION_1_2,
        )

        fun getDatabase(context: Context): MoeMemosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoeMemosDatabase::class.java,
                    "moememos_database_localfirst"
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
