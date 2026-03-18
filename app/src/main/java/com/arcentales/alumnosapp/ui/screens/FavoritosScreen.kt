package com.arcentales.alumnosapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arcentales.alumnosapp.data.model.Alumno
import com.arcentales.alumnosapp.ui.theme.ColorFavorito
import com.arcentales.alumnosapp.ui.viewmodel.AlumnoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    viewModel: AlumnoViewModel,
    onBack: () -> Unit,
    onEditClick: (Int) -> Unit
) {
    val favoritos by viewModel.favoritos.collectAsState()
    var alumnoAEliminar by remember { mutableStateOf<Alumno?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = ColorFavorito,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Favoritos", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->

        if (favoritos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Sin favoritos aún",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Toca el ♡ en la lista para marcar un alumno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favoritos, key = { it.id }) { alumno ->
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

    alumnoAEliminar?.let { alumno ->
        AlertDialog(
            onDismissRequest = { alumnoAEliminar = null },
            title = { Text("Eliminar alumno") },
            text  = { Text("¿Deseas eliminar a ${alumno.nombreCompleto}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarAlumno(alumno)
                    alumnoAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { alumnoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}
