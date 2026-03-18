

AlumnosApp — Aplicación de Registro de Alumnos con Jetpack Compose
Aplicación Android de gestión de alumnos desarrollada con Jetpack Compose, Room, Navigation Compose y arquitectura MVVM.
Basada en la guía de contactsApp, adaptada para el registro escolar.
________________________________________
Requisitos previos
•	Android Studio Ladybug o superior
•	JDK 11 o superior
•	SDK mínimo: Android 8.0 (API 26)
•	SDK objetivo: Android 15 (API 36)
________________________________________
Cómo ejecutar el proyecto
1.	Clonar o descomprimir el proyecto
2.	Abrir la carpeta AlumnosApp en Android Studio
3.	Esperar a que Gradle sincronice las dependencias
4.	Conectar un dispositivo físico o iniciar un emulador
5.	Presionar Run ▶ o Shift + F10
________________________________________
Estructura del proyecto
app/src/main/java/com/carevalojesus/alumnosapp/
│
├── MainActivity.kt                        # Punto de entrada
│
├── data/
│   ├── model/
│   │   └── Alumno.kt                      # Entidad Room + enum EstadoAlumno
│   ├── dao/
│   │   └── AlumnoDao.kt                   # CRUD + búsqueda + favoritos
│   └── database/
│       └── AlumnoDatabase.kt             # Singleton Room + TypeConverters
│
└── ui/
    ├── navigation/
    │   └── AppNavigation.kt              # Rutas de navegación
    ├── screens/
    │   ├── AlumnoListScreen.kt           # Lista principal con búsqueda y filtros
    │   ├── AddEditAlumnoScreen.kt        # Formulario agregar / editar
    │   └── FavoritosScreen.kt           # Pantalla de favoritos
    ├── viewmodel/
    │   └── AlumnoViewModel.kt           # Lógica de negocio + StateFlow
    └── theme/
        ├── Color.kt                     # Paleta de colores + estados
        ├── Theme.kt                     # Material3 + dynamic color
        └── Type.kt                      # Tipografía
________________________________________
Arquitectura MVVM
[Vista (Screens)]  <-->  [ViewModel]  <-->  [Room DAO]  <-->  [SQLite]
Capa	Archivo	Responsabilidad
Model	Alumno.kt	Entidad con todos los campos del alumno
Model	AlumnoDao.kt	Consultas SQL: getAllAlumnos, buscarAlumnos, toggleFavorito…
Model	AlumnoDatabase.kt	Instancia única de la BD con conversor de enum
ViewModel	AlumnoViewModel.kt	Estado reactivo, búsqueda, filtros, CRUD
View	*Screen.kt	Composables que renderizan la interfaz
________________________________________
Datos del alumno
Campo	Tipo	Obligatorio
Nombres	String	✅
Apellidos	String	✅
DNI / Código	String	✅
Correo electrónico	String	—
Teléfono	String	—
Grado	String	—
Sección	String	—
Dirección	String	—
Estado	Enum (Activo/Inactivo/Graduado/Retirado)	—
Favorito	Boolean	—
Foto de perfil	URI (cámara o galería)	—
________________________________________
Funcionalidades
•	📋 Ver alumnos — lista con avatar, nombre, grado, DNI y estado
•	🔍 Búsqueda en tiempo real — por nombre, apellido, DNI o correo
•	🎛️ Filtros por estado — Todos / Activo / Inactivo / Graduado / Retirado
•	➕ Agregar alumno — formulario completo con validación
•	✏️ Editar alumno — misma pantalla precargada
•	🗑️ Eliminar — con diálogo de confirmación
•	⭐ Favoritos — marcar/desmarcar con pantalla dedicada
•	📸 Foto de perfil — desde cámara o galería; avatar con iniciales si no hay foto
•	💾 Persistencia — Room (SQLite), los datos sobreviven al cierre de la app
________________________________________
Dependencias principales
Librería	Uso
androidx.room	Base de datos local
androidx.navigation:navigation-compose	Navegación entre pantallas
androidx.lifecycle:lifecycle-viewmodel-compose	ViewModel + Compose
androidx.compose.material3	Material Design 3
androidx.compose.material:material-icons-extended	Iconos adicionales
io.coil-kt:coil-compose	Carga de imágenes desde URI
com.google.devtools.ksp	Procesador de anotaciones para Room
________________________________________
Flujo de datos
Usuario toca "Registrar Alumno"
        │
        ▼
AddEditAlumnoScreen  →  viewModel.agregarAlumno(alumno)
        │
        ▼
AlumnoViewModel  →  dao.insertAlumno(alumno)  [coroutine]
        │
        ▼
Room inserta en SQLite y notifica al Flow
        │
        ▼
AlumnoListScreen recibe nueva lista via collectAsState()
        │
        ▼
La UI se recompone y muestra el nuevo alumno
________________________________________

