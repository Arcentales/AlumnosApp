package com.arcentales.alumnosapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.arcentales.alumnosapp.data.model.Alumno
import com.arcentales.alumnosapp.data.model.EstadoAlumno
import com.arcentales.alumnosapp.ui.theme.*
import com.arcentales.alumnosapp.ui.viewmodel.AlumnoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoListScreen(
    viewModel: AlumnoViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onFavoritosClick: () -> Unit
) {
    val alumnos       by viewModel.alumnos.collectAsState()
    val totalAlumnos  by viewModel.totalAlumnos.collectAsState()
    val searchQuery   by viewModel.searchQuery.collectAsState()
    val filtroEstado  by viewModel.filtroEstado.collectAsState()

    // Estado para el diálogo de confirmación de eliminación
    var alumnoAEliminar by remember { mutableStateOf<Alumno?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Registro de Alumnos", fontWeight = FontWeight.Bold)
                        Text(
                            "$totalAlumnos alumno${if (totalAlumnos != 1) "s" else ""} registrado${if (totalAlumnos != 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoritosClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favoritos", tint = ColorFavorito)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                text = { Text("Nuevo Alumno") }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // ── Barra de búsqueda ────────────────────────────────────────
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Buscar por nombre, apellido o DNI…") },
                leadingIcon  = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(28.dp)
            )

            // ── Filtros de estado ────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filtroEstado == null,
                        onClick  = { viewModel.setFiltroEstado(null) },
                        label    = { Text("Todos") }
                    )
                }
                items(EstadoAlumno.values()) { estado ->
                    FilterChip(
                        selected = filtroEstado == estado,
                        onClick  = {
                            viewModel.setFiltroEstado(if (filtroEstado == estado) null else estado)
                        },
                        label = { Text(estado.label) }
                    )
                }
            }

            // ── Lista ────────────────────────────────────────────────────
            if (alumnos.isEmpty()) {
                EmptyState(hayBusqueda = searchQuery.isNotEmpty() || filtroEstado != null)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    items(alumnos, key = { it.id }) { alumno ->
                        AlumnoCard(
                            alumno = alumno,
                            onEdit = { onEditClick(alumno.id) },
                            onDelete = { alumnoAEliminar = alumno },
                            onToggleFavorito = { viewModel.toggleFavorito(alumno) }
                        )
                    }
                }
            }
        }
    }

    // ── Diálogo de confirmación de eliminación ───────────────────────────
    alumnoAEliminar?.let { alumno ->
        AlertDialog(
            onDismissRequest = { alumnoAEliminar = null },
            title = { Text("Eliminar alumno") },
            text  = {
                Text("¿Deseas eliminar a ${alumno.nombreCompleto}? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarAlumno(alumno)
                        alumnoAEliminar = null
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { alumnoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// ── Tarjeta de alumno ────────────────────────────────────────────────────────

@Composable
fun AlumnoCard(
    alumno: Alumno,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar / foto
            AvatarAlumno(alumno = alumno, size = 52)

            Spacer(modifier = Modifier.width(12.dp))

            // Datos
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alumno.nombreCompleto,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (alumno.grado.isNotBlank()) {
                    Text(
                        text = "${alumno.grado}${if (alumno.seccion.isNotBlank()) " – ${alumno.seccion}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (alumno.dni.isNotBlank()) {
                    Text(
                        text = "DNI: ${alumno.dni}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                EstadoBadge(estado = alumno.estado)
            }

            // Acciones
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onToggleFavorito, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (alumno.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (alumno.esFavorito) ColorFavorito else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── Avatar circular ──────────────────────────────────────────────────────────

@Composable
fun AvatarAlumno(alumno: Alumno, size: Int) {
    val sizeDp = size.dp
    if (alumno.fotoUri != null) {
        AsyncImage(
            model = alumno.fotoUri,
            contentDescription = "Foto de ${alumno.nombreCompleto}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(sizeDp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = alumno.iniciales,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.35).sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ── Badge de estado ──────────────────────────────────────────────────────────

@Composable
fun EstadoBadge(estado: EstadoAlumno) {
    val (color, bg) = when (estado) {
        EstadoAlumno.ACTIVO   -> Pair(Color.White, ColorActivo)
        EstadoAlumno.INACTIVO -> Pair(Color.White, ColorInactivo)
        EstadoAlumno.GRADUADO -> Pair(Color.White, ColorGraduado)
        EstadoAlumno.RETIRADO -> Pair(Color.White, ColorRetirado)
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = estado.label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// ── Estado vacío ─────────────────────────────────────────────────────────────

@Composable
fun EmptyState(hayBusqueda: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (hayBusqueda) Icons.Default.SearchOff else Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hayBusqueda) "Sin resultados" else "No hay alumnos aún",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (hayBusqueda) "Prueba con otro término o filtro"
                       else "Toca el botón + para agregar el primero",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
