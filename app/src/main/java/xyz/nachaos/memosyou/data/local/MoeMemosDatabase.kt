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
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MoeMemosDatabase : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile
        private var INSTANCE: MoeMemosDatabase? = null

        /**
         * Register migrations here as the schema evolves.
         *
         * Example for version 1 -> 2:
         *   val MIGRATION_1_2 = object : Migration(1, 2) {
         *       override fun migrate(db: SupportSQLiteDatabase) {
         *           db.execSQL("ALTER TABLE memos ADD COLUMN new_field TEXT")
         *       }
         *   }
         *
         * Destructive migration is the last-resort fallback.
         * Since this is a sync-first app (data is on the server),
         * dropping and recreating tables is acceptable if migrations fail,
         * but users will lose offline-created unsynced memos.
         * When adding a destructive migration path, consider exporting
         * unsynced data to a file first.
         */
        val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            // MIGRATION_1_2,
        )

        fun getDatabase(context: Context): MoeMemosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoeMemosDatabase::class.java,
                    "moememos_database_localfirst"
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
