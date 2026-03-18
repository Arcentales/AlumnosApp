package com.arcentales.alumnosapp.data.dao

import androidx.room.*
import com.arcentales.alumnosapp.data.model.Alumno
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): define todas las operaciones CRUD
 * que se pueden hacer sobre la tabla "alumnos".
 *
 * Room genera automáticamente la implementación en tiempo de compilación.
 */
@Dao
interface AlumnoDao {

    // ── CONSULTAS ──────────────────────────────────────────────────────────

    /** Devuelve todos los alumnos ordenados por apellido (reactivo con Flow) */
    @Query("SELECT * FROM alumnos ORDER BY apellidos ASC")
    fun getAllAlumnos(): Flow<List<Alumno>>

    /** Devuelve solo los alumnos marcados como favoritos */
    @Query("SELECT * FROM alumnos WHERE esFavorito = 1 ORDER BY apellidos ASC")
    fun getFavoritos(): Flow<List<Alumno>>

    /** Busca alumnos por nombre, apellido o DNI (búsqueda en tiempo real) */
    @Query("""
        SELECT * FROM alumnos
        WHERE nombres    LIKE '%' || :query || '%'
           OR apellidos  LIKE '%' || :query || '%'
           OR dni        LIKE '%' || :query || '%'
           OR correo     LIKE '%' || :query || '%'
        ORDER BY apellidos ASC
    """)
    fun buscarAlumnos(query: String): Flow<List<Alumno>>

    /** Obtiene un alumno por su ID */
    @Query("SELECT * FROM alumnos WHERE id = :id")
    suspend fun getAlumnoById(id: Int): Alumno?

    /** Cuenta el total de alumnos */
    @Query("SELECT COUNT(*) FROM alumnos")
    fun contarAlumnos(): Flow<Int>

    // ── ESCRITURA ──────────────────────────────────────────────────────────

    /** Inserta un alumno; si ya existe (mismo ID) lo reemplaza */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlumno(alumno: Alumno)

    /** Actualiza los datos de un alumno existente */
    @Update
    suspend fun updateAlumno(alumno: Alumno)

    /** Elimina un alumno */
    @Delete
    suspend fun deleteAlumno(alumno: Alumno)

    /** Alterna el estado de favorito */
    @Query("UPDATE alumnos SET esFavorito = :esFavorito WHERE id = :id")
    suspend fun toggleFavorito(id: Int, esFavorito: Boolean)
}
