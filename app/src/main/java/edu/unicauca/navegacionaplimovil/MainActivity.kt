package edu.unicauca.navegacionaplimovil


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import edu.unicauca.navegacionaplimovil.ui.theme.NavegacionAplimovilTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavegacionAplimovilTheme {
                AppNavegacion()
            }
        }
    }
}


// Modelo de ingreso
data class Ingreso(val monto: Double, val categoria: String)


class FinanzasViewModel(context: Context) : ViewModel() {
    var ingresos by mutableStateOf(0.0)
    var gastos by mutableStateOf(0.0)
    private var _metaAhorro by mutableStateOf(0.0)
    var metaAhorro: Double=0.0
        get() = _metaAhorro
        private set


    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val metaAhorroKey = "meta_ahorro"


    init {
        _metaAhorro = cargarMetaAhorroInterno()
    }


    val saldo: Double get() = ingresos - gastos
    val progresoAhorro: Float get() = if (metaAhorro > 0) {
        (saldo / metaAhorro).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }


    private fun guardarMetaAhorro(meta: Double) {
        sharedPreferences.edit().putFloat(metaAhorroKey, meta.toFloat()).apply()
    }


    private fun cargarMetaAhorroInterno(): Double {
        return sharedPreferences.getFloat(metaAhorroKey, 0.0f).toDouble()
    }


    fun cargarMetaAhorro() {
        _metaAhorro = cargarMetaAhorroInterno()
    }


    private val _ingresosPorCategoria = mutableStateMapOf<String, Double>()
    val ingresosPorCategoria: Map<String, Double> get() = _ingresosPorCategoria
    val gastosPorCategoria: Map<String, Double> get() = _gastosPorCategoria

    private val _listaIngresos = mutableStateListOf<Pair<String, Double>>()
    val listaIngresos: List<Pair<String, Double>> get() = _listaIngresos

    private val _gastosPorCategoria = mutableStateMapOf<String, Double>()
    private val _listaGastos = mutableStateListOf<Pair<String, Double>>()
    val listaGastos: List<Pair<String, Double>> get() = _listaGastos

    fun agregarIngreso(monto: Double, categoria: String) {
        ingresos += monto
        _listaIngresos.add(categoria to monto)
        _ingresosPorCategoria[categoria] = (_ingresosPorCategoria[categoria] ?: 0.0) + monto
    }


    fun agregarGasto(monto: Double, categoria: String) {
        gastos += monto
        _listaGastos.add(categoria to -monto)
        _gastosPorCategoria[categoria] = (_gastosPorCategoria[categoria] ?: 0.0) - monto
    }


    fun actualizarMetaAhorro(nuevaMeta: Double) {
        _metaAhorro = nuevaMeta
        guardarMetaAhorro(nuevaMeta)
        Log.d("FinanzasViewModel", "Meta de ahorro actualizada y guardada: $metaAhorro")
    }
}


class FinanzasViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanzasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanzasViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: FinanzasViewModel = viewModel(factory = FinanzasViewModelFactory(context))


    // Llamada explícita para cargar la meta después de la inicialización del ViewModel
    LaunchedEffect(viewModel) {
        viewModel.cargarMetaAhorro()
    }


    Scaffold(
        floatingActionButton = {
            FloatingActionButtonMenu(navController = navController, viewModel = viewModel)
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController, startDestination = "inicio") {
                composable("inicio") { PantallaInicio(navController, viewModel) }
                composable("ingresos") { PantallaIngresos(navController, viewModel) }
                composable("gastos") { PantallaGastos(navController, viewModel) }
                composable("config") { PantallaConfiguracion(navController, viewModel) }
                composable("metaAhorro") { PantallaMetaAhorro(navController, viewModel) }
            }
        }
    }
}

@Composable
fun FloatingActionButtonMenu(navController: NavController, viewModel: FinanzasViewModel) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "RotationAnimation"
    )

    var mostrarDialogoIngreso by remember { mutableStateOf(false) }
    var mostrarDialogoGasto by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .blur(10.dp)
                    .clickable { expanded = false }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (expanded) {
                    // Botón para mostrar el diálogo
                    ExtendedFloatingActionButton(
                        text = { Text("Añadir ingresos") },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Añadir ingresos") },
                        onClick = {
                            mostrarDialogoIngreso = true
                            expanded = false
                        },
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ExtendedFloatingActionButton(
                        text = { Text("Añadir gastos") },
                        icon = { Icon(Icons.Default.Remove, contentDescription = "Añadir gastos") },
                        onClick = {
                            mostrarDialogoGasto = true
                            expanded = false
                        },
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    containerColor = Color(0xFF6200EE)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (expanded) "Cerrar menú" else "Abrir menú",
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        }
    }

    if (mostrarDialogoIngreso) {
        DialogAgregarIngreso(
            onDismiss = { mostrarDialogoIngreso = false },
            onGuardar = { monto, categoria, nota ->
                viewModel.agregarIngreso(monto, categoria)
                mostrarDialogoIngreso = false
            }
        )
    }
    if (mostrarDialogoGasto) {
        DialogAgregarGasto(
            onDismiss = { mostrarDialogoGasto = false },
            onGuardar = { monto, categoria, nota ->
                viewModel.agregarGasto(monto, categoria)
                mostrarDialogoGasto = false
            }
        )
    }
}

@Composable
fun DialogAgregarIngreso(
    onDismiss: () -> Unit,
    onGuardar: (Double, String, String) -> Unit
) {
    var montoText by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Ingreso") },
        text = {
            Column {
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Valor del ingreso") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Categoría") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        // Aquí puedes abrir otro diálogo o mostrar un Toast, etc.
                    }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Agregar categoría")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val monto = montoText.toDoubleOrNull() ?: 0.0
                if (monto > 0 && categoria.isNotBlank()) {
                    onGuardar(monto, categoria, nota)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
@Composable
fun DialogAgregarGasto(
    onDismiss: () -> Unit,
    onGuardar: (Double, String, String) -> Unit
) {
    var montoText by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Gasto") },
        text = {
            Column {
                OutlinedTextField(
                    value = montoText,
                    onValueChange = { montoText = it },
                    label = { Text("Valor del ingreso") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Categoría") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        // Aquí puedes abrir otro diálogo o mostrar un Toast, etc.
                    }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Agregar categoría")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val monto = montoText.toDoubleOrNull() ?: 0.0
                if (monto > 0 && categoria.isNotBlank()) {
                    onGuardar(monto, categoria, nota)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Savings, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = false,
            onClick = { navController.navigate("inicio") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.AttachMoney, contentDescription = "Ingresos") },
            label = { Text("Ingresos") },
            selected = false,
            onClick = { navController.navigate("ingresos") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.MoneyOff, contentDescription = "Gastos") },
            label = { Text("Gastos") },
            selected = false,
            onClick = { navController.navigate("gastos") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Configuración") },
            label = { Text("Config") },
            selected = false,
            onClick = { navController.navigate("config") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.TrendingUp, contentDescription = "Meta") },
            label = { Text("Meta") },
            selected = false,
            onClick = { navController.navigate("metaAhorro") } // Navegar a la pantalla de meta
        )
    }
}


@Composable
fun PantallaInicio(navController: NavHostController, viewModel: FinanzasViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Resumen Financiero", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Total Ingresos: $${viewModel.ingresos}")
        Text(text = "Total Gastos: $${viewModel.gastos}")
        Text(text = "Saldo Disponible: $${viewModel.saldo}")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("metaAhorro") }) {
            Text("Ver Meta de Ahorro")
        }
    }
}


@Composable
fun PantallaIngresos(navController: NavHostController, viewModel: FinanzasViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Lista de Ingresos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))


        if (viewModel.listaIngresos.isEmpty()) {
            Text("No hay ingresos registrados.")
        } else {
            viewModel.listaIngresos.forEach { (categoria, monto) ->
                Text("Categoría: $categoria - Monto: $monto")
            }


            Spacer(modifier = Modifier.height(24.dp))
            Divider(thickness = 2.dp)
            Spacer(modifier = Modifier.height(8.dp))


            Text("Totales por categoría", style = MaterialTheme.typography.titleMedium)
            viewModel.ingresosPorCategoria.forEach { (categoria, total) ->
                Text("$categoria: $total")
            }
        }
    }
}

@Composable
fun PantallaGastos(navController: NavHostController, viewModel: FinanzasViewModel = viewModel()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Lista de Gastos", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))


        if (viewModel.listaIngresos.isEmpty()) {
            Text("No hay gastos registrados.")
        } else {
            viewModel.listaIngresos.forEach { (categoria, monto) ->
                Text("Categoría: $categoria - Monto: $monto")
            }


            Spacer(modifier = Modifier.height(24.dp))
            Divider(thickness = 2.dp)
            Spacer(modifier = Modifier.height(8.dp))


            Text("Totales por categoría", style = MaterialTheme.typography.titleMedium)
            viewModel.gastosPorCategoria.forEach { (categoria, total) ->
                Text("$categoria: $total")
            }
        }
    }
}


@Composable
fun PantallaConfiguracion(navController: NavHostController, viewModel: FinanzasViewModel) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Correo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Lógica de Login (por ahora solo impresión)
                Log.d("Login", "Usuario: $correo - Contraseña: $contrasena")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde
        ) {
            Icon(Icons.Filled.Login, contentDescription = "Iniciar sesión")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                // Lógica de Registro (por ahora solo impresión)
                Log.d("Registro", "Nuevo usuario: $correo - Contraseña: $contrasena")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)) // Azul
        ) {
            Icon(Icons.Filled.PersonAdd, contentDescription = "Registrarse")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Registrarse")
        }
    }
}

@Composable
fun PantallaMetaAhorro(navController: NavHostController, viewModel: FinanzasViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mi Meta de Ahorro", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))


        Text("Meta establecida: $${viewModel.metaAhorro}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Saldo actual: $${viewModel.saldo}")
        Spacer(modifier = Modifier.height(16.dp))


        // Barra de progreso
        LinearProgressIndicator(progress = viewModel.progresoAhorro)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Progreso: ${(viewModel.progresoAhorro * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium
        )


        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { navController.navigate("config") }) {
            Text("Guardar Meta")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}


