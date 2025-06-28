package com.example.urbanmaintenancemanager.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.urbanmaintenancemanager.data.local.model.Drawing
import com.example.urbanmaintenancemanager.data.local.model.DrawingType
import com.example.urbanmaintenancemanager.data.local.model.DrawingWorkerCrossRef
import com.example.urbanmaintenancemanager.data.local.model.Report
import com.example.urbanmaintenancemanager.data.local.model.Worker
import com.example.urbanmaintenancemanager.data.local.model.WorkerGroup
import com.example.urbanmaintenancemanager.data.local.model.Absence

@Database(
    entities = [Worker::class, Report::class, Drawing::class, DrawingWorkerCrossRef::class, WorkerGroup::class, Absence::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workerDao(): WorkerDao
    abstract fun reportDao(): ReportDao
    abstract fun drawingDao(): DrawingDao
    abstract fun workerGroupDao(): WorkerGroupDao
    abstract fun absenceDao(): AbsenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "urban_maintenance_manager_db"
                )
                .addMigrations(MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Since there are no schema changes, this is empty.
                // Future migrations will have SQL commands here.
                // For example:
                // db.execSQL("ALTER TABLE workers ADD COLUMN new_field TEXT")
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromDrawingType(value: DrawingType): String {
        return value.name
    }

    @TypeConverter
    fun toDrawingType(value: String): DrawingType {
        return DrawingType.valueOf(value)
    }
} 