package com.arcentales.alumnosapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.arcentales.alumnosapp.data.dao.AlumnoDao
import com.arcentales.alumnosapp.data.model.Alumno
import com.arcentales.alumnosapp.data.model.EstadoAlumno

/**
 * Conversor para que Room pueda guardar el enum EstadoAlumno como String en SQLite.
 */
class Converters {
    @TypeConverter
    fun fromEstado(estado: EstadoAlumno): String = estado.name

    @TypeConverter
    fun toEstado(value: String): EstadoAlumno = EstadoAlumno.valueOf(value)
}

/**
 * Base de datos Room — patrón Singleton para garantizar una sola instancia.
 *
 * @Database  declara las entidades y la versión del esquema.
 * @TypeConverters conecta los conversores para tipos no primitivos.
 */
@Database(
    entities = [Alumno::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AlumnoDatabase : RoomDatabase() {

    /** Punto de acceso al DAO */
    abstract fun alumnoDao(): AlumnoDao

    companion object {
        @Volatile
        private var INSTANCE: AlumnoDatabase? = null

        /**
         * Retorna la instancia existente o crea una nueva (thread-safe con synchronized).
         */
        fun getDatabase(context: Context): AlumnoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlumnoDatabase::class.java,
                    "alumnos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
