package com.arcentales.alumnosapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa la tabla "alumnos" en SQLite.
 * Cada propiedad corresponde a una columna de la tabla.
 */
@Entity(tableName = "alumnos")
data class Alumno(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombres: String,
    val apellidos: String,
    val dni: String,
    val correo: String = "",
    val telefono: String = "",
    val grado: String = "",
    val seccion: String = "",
    val direccion: String = "",
    val estado: EstadoAlumno = EstadoAlumno.ACTIVO,
    val esFavorito: Boolean = false,
    val fotoUri: String? = null
) {
    /** Nombre completo para mostrar en listas */
    val nombreCompleto: String
        get() = "$nombres $apellidos"

    /** Iniciales para el avatar cuando no hay foto */
    val iniciales: String
        get() {
            val n = nombres.firstOrNull()?.uppercaseChar() ?: ""
            val a = apellidos.firstOrNull()?.uppercaseChar() ?: ""
            return "$n$a"
        }
}

enum class EstadoAlumno(val label: String) {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    GRADUADO("Graduado"),
    RETIRADO("Retirado")
}
