package com.arcentales.alumnosapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arcentales.alumnosapp.data.database.AlumnoDatabase
import com.arcentales.alumnosapp.data.model.Alumno
import com.arcentales.alumnosapp.data.model.EstadoAlumno
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel: sobrevive a cambios de configuración (rotación de pantalla).
 * Expone los datos como StateFlow para que Compose los observe.
 *
 * Toda operación de BD se lanza en viewModelScope (coroutine en background).
 */
class AlumnoViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AlumnoDatabase.getDatabase(application).alumnoDao()

    // ── ESTADO DE BÚSQUEDA ────────────────────────────────────────────────

    /** Texto de búsqueda actual */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Filtro de estado activo */
    private val _filtroEstado = MutableStateFlow<EstadoAlumno?>(null)
    val filtroEstado: StateFlow<EstadoAlumno?> = _filtroEstado.asStateFlow()

    // ── DATOS REACTIVOS ───────────────────────────────────────────────────

    /**
     * Lista de alumnos filtrada según query y estado.
     * flatMapLatest recalcula cada vez que cambia el query o el filtro.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val alumnos: StateFlow<List<Alumno>> = _searchQuery
        .combine(_filtroEstado) { q, estado -> Pair(q, estado) }
        .flatMapLatest { (query, estado) ->
            val base = if (query.isBlank()) dao.getAllAlumnos()
                       else dao.buscarAlumnos(query.trim())
            base.map { lista ->
                if (estado == null) lista
                else lista.filter { it.estado == estado }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Lista de favoritos */
    val favoritos: StateFlow<List<Alumno>> = dao.getFavoritos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Total de alumnos registrados */
    val totalAlumnos: StateFlow<Int> = dao.contarAlumnos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    // ── ALUMNO SELECCIONADO (para edición) ────────────────────────────────

    private val _alumnoSeleccionado = MutableStateFlow<Alumno?>(null)
    val alumnoSeleccionado: StateFlow<Alumno?> = _alumnoSeleccionado.asStateFlow()

    fun cargarAlumno(id: Int) {
        viewModelScope.launch {
            _alumnoSeleccionado.value = dao.getAlumnoById(id)
        }
    }

    fun limpiarSeleccion() {
        _alumnoSeleccionado.value = null
    }

    // ── ACCIONES CRUD ─────────────────────────────────────────────────────

    fun agregarAlumno(alumno: Alumno) {
        viewModelScope.launch { dao.insertAlumno(alumno) }
    }

    fun actualizarAlumno(alumno: Alumno) {
        viewModelScope.launch { dao.updateAlumno(alumno) }
    }

    fun eliminarAlumno(alumno: Alumno) {
        viewModelScope.launch { dao.deleteAlumno(alumno) }
    }

    fun toggleFavorito(alumno: Alumno) {
        viewModelScope.launch {
            dao.toggleFavorito(alumno.id, !alumno.esFavorito)
        }
    }

    // ── BÚSQUEDA Y FILTROS ────────────────────────────────────────────────

    fun onSearchChange(query: String) { _searchQuery.value = query }

    fun setFiltroEstado(estado: EstadoAlumno?) { _filtroEstado.value = estado }
}
