package com.arcentales.alumnosapp.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.arcentales.alumnosapp.data.model.Alumno
import com.arcentales.alumnosapp.data.model.EstadoAlumno
import com.arcentales.alumnosapp.ui.viewmodel.AlumnoViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlumnoScreen(
    viewModel: AlumnoViewModel,
    alumnoId: Int?,
    onBack: () -> Unit
) {
    val esEdicion = alumnoId != null
    val alumnoSeleccionado by viewModel.alumnoSeleccionado.collectAsState()
    val context = LocalContext.current

    // ── Estado del formulario ────────────────────────────────────────────
    var nombres    by remember { mutableStateOf("") }
    var apellidos  by remember { mutableStateOf("") }
    var dni        by remember { mutableStateOf("") }
    var correo     by remember { mutableStateOf("") }
    var telefono   by remember { mutableStateOf("") }
    var grado      by remember { mutableStateOf("") }
    var seccion    by remember { mutableStateOf("") }
    var direccion  by remember { mutableStateOf("") }
    var estado     by remember { mutableStateOf(EstadoAlumno.ACTIVO) }
    var fotoUri    by remember { mutableStateOf<String?>(null) }
    var estadoMenuExpanded by remember { mutableStateOf(false) }

    // ── Errores de validación ────────────────────────────────────────────
    var errorNombres   by remember { mutableStateOf(false) }
    var errorApellidos by remember { mutableStateOf(false) }
    var errorDni       by remember { mutableStateOf(false) }

    // ── Cargar datos para edición ────────────────────────────────────────
    LaunchedEffect(alumnoId) {
        if (alumnoId != null) viewModel.cargarAlumno(alumnoId)
    }

    LaunchedEffect(alumnoSeleccionado) {
        alumnoSeleccionado?.let { a ->
            nombres   = a.nombres
            apellidos = a.apellidos
            dni       = a.dni
            correo    = a.correo
            telefono  = a.telefono
            grado     = a.grado
            seccion   = a.seccion
            direccion = a.direccion
            estado    = a.estado
            fotoUri   = a.fotoUri
        }
    }

    // ── Cámara y galería ─────────────────────────────────────────────────
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) fotoUri = cameraUri?.toString()
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fotoUri = it.toString() }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraUri = crearUriCamara(context)
            cameraLauncher.launch(cameraUri!!)
        }
    }

    var showFotoMenu by remember { mutableStateOf(false) }

    // ── Lógica de guardado ───────────────────────────────────────────────
    fun guardar() {
        errorNombres   = nombres.isBlank()
        errorApellidos = apellidos.isBlank()
        errorDni       = dni.isBlank()
        if (errorNombres || errorApellidos || errorDni) return

        val alumno = Alumno(
            id        = if (esEdicion) alumnoId!! else 0,
            nombres   = nombres.trim(),
            apellidos = apellidos.trim(),
            dni       = dni.trim(),
            correo    = correo.trim(),
            telefono  = telefono.trim(),
            grado     = grado.trim(),
            seccion   = seccion.trim(),
            direccion = direccion.trim(),
            estado    = estado,
            esFavorito = alumnoSeleccionado?.esFavorito ?: false,
            fotoUri   = fotoUri
        )

        if (esEdicion) viewModel.actualizarAlumno(alumno)
        else           viewModel.agregarAlumno(alumno)

        viewModel.limpiarSeleccion()
        onBack()
    }

    // ────────────────────────────────────────────────────────────────────

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (esEdicion) "Editar Alumno" else "Nuevo Alumno", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.limpiarSeleccion(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = ::guardar) {
                        Text("GUARDAR", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Avatar / Foto ────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val alumnoPreview = Alumno(
                    nombres = nombres.ifBlank { "?" },
                    apellidos = apellidos,
                    dni = dni,
                    fotoUri = fotoUri
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { showFotoMenu = true }
                ) {
                    AvatarAlumno(alumno = alumnoPreview, size = 100)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showFotoMenu,
                    onDismissRequest = { showFotoMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tomar foto") },
                        leadingIcon = { Icon(Icons.Default.CameraAlt, null) },
                        onClick = {
                            showFotoMenu = false
                            cameraPermission.launch(Manifest.permission.CAMERA)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Elegir de la galería") },
                        leadingIcon = { Icon(Icons.Default.Photo, null) },
                        onClick = {
                            showFotoMenu = false
                            galleryLauncher.launch("image/*")
                        }
                    )
                    if (fotoUri != null) {
                        DropdownMenuItem(
                            text = { Text("Quitar foto") },
                            leadingIcon = { Icon(Icons.Default.Delete, null) },
                            onClick = { showFotoMenu = false; fotoUri = null }
                        )
                    }
                }
            }

            SectionLabel("Datos personales")

            // ── Campos del formulario ────────────────────────────────────
            AlumnoTextField(
                value = nombres,
                onValueChange = { nombres = it; errorNombres = false },
                label = "Nombres *",
                icon = Icons.Default.Person,
                isError = errorNombres,
                supportingText = if (errorNombres) "Campo obligatorio" else null,
                capitalization = KeyboardCapitalization.Words
            )

            AlumnoTextField(
                value = apellidos,
                onValueChange = { apellidos = it; errorApellidos = false },
                label = "Apellidos *",
                icon = Icons.Default.Person,
                isError = errorApellidos,
                supportingText = if (errorApellidos) "Campo obligatorio" else null,
                capitalization = KeyboardCapitalization.Words
            )

            AlumnoTextField(
                value = dni,
                onValueChange = { dni = it; errorDni = false },
                label = "DNI / Código *",
                icon = Icons.Default.Badge,
                isError = errorDni,
                supportingText = if (errorDni) "Campo obligatorio" else null,
                keyboardType = KeyboardType.Number
            )

            SectionLabel("Contacto")

            AlumnoTextField(
                value = correo,
                onValueChange = { correo = it },
                label = "Correo electrónico",
                icon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            AlumnoTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = "Teléfono",
                icon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone
            )

            AlumnoTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = "Dirección",
                icon = Icons.Default.Home,
                capitalization = KeyboardCapitalization.Sentences
            )

            SectionLabel("Información académica")

            AlumnoTextField(
                value = grado,
                onValueChange = { grado = it },
                label = "Grado",
                icon = Icons.Default.School,
                capitalization = KeyboardCapitalization.Words
            )

            AlumnoTextField(
                value = seccion,
                onValueChange = { seccion = it },
                label = "Sección",
                icon = Icons.Default.Class,
                capitalization = KeyboardCapitalization.Characters
            )

            // ── Selector de estado ───────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = estadoMenuExpanded,
                onExpandedChange = { estadoMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = estado.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado") },
                    leadingIcon = { Icon(Icons.Default.Circle, contentDescription = null,
                        tint = when (estado) {
                            EstadoAlumno.ACTIVO   -> com.arcentales.alumnosapp.ui.theme.ColorActivo
                            EstadoAlumno.INACTIVO -> com.arcentales.alumnosapp.ui.theme.ColorInactivo
                            EstadoAlumno.GRADUADO -> com.arcentales.alumnosapp.ui.theme.ColorGraduado
                            EstadoAlumno.RETIRADO -> com.arcentales.alumnosapp.ui.theme.ColorRetirado
                        }) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoMenuExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = estadoMenuExpanded,
                    onDismissRequest = { estadoMenuExpanded = false }
                ) {
                    EstadoAlumno.values().forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s.label) },
                            onClick = { estado = s; estadoMenuExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Botón guardar ────────────────────────────────────────────
            Button(
                onClick = ::guardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (esEdicion) "Actualizar Alumno" else "Registrar Alumno",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Helpers de UI ────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun AlumnoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

// ── FileProvider helper ───────────────────────────────────────────────────────

fun crearUriCamara(context: Context): Uri {
    val file = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File(file, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}
