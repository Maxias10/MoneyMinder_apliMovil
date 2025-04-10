package edu.unicauca.navegacionaplimovil


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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


   private val _listaIngresos = mutableStateListOf<Pair<String, Double>>()
   val listaIngresos: List<Pair<String, Double>> get() = _listaIngresos


   fun agregarIngreso(monto: Double, categoria: String) {
       ingresos += monto
       _listaIngresos.add(categoria to monto)
       _ingresosPorCategoria[categoria] = (_ingresosPorCategoria[categoria] ?: 0.0) + monto
   }


   fun agregarGasto(monto: Double) {
       gastos += monto
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


   Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
       Column(horizontalAlignment = Alignment.End) {
           if (expanded) {
               // Añadir Ingreso: Sueldo
               ExtendedFloatingActionButton(
                   text = { Text("Añadir Sueldo") },
                   icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Añadir Sueldo") },
                   onClick = {
                       viewModel.agregarIngreso(500.0, "Sueldo")
                       expanded = false
                   },
                   containerColor = Color(0xFF4CAF50) // Verde
               )
               Spacer(modifier = Modifier.height(8.dp))


               // Añadir Ingreso: Regalo
               ExtendedFloatingActionButton(
                   text = { Text("Añadir Regalo") },
                   icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Añadir Regalo") },
                   onClick = {
                       viewModel.agregarIngreso(150.0, "Regalo")
                       expanded = false
                   },
                   containerColor = Color(0xFFFFC107) // Amarillo
               )
               Spacer(modifier = Modifier.height(8.dp))


               // Añadir Gasto
               ExtendedFloatingActionButton(
                   text = { Text("Añadir Gasto") },
                   icon = { Icon(Icons.Default.MoneyOff, contentDescription = "Añadir Gasto") },
                   onClick = {
                       viewModel.agregarGasto(100.0)
                       expanded = false
                   },
                   containerColor = Color(0xFFF44336) // Rojo
               )
               Spacer(modifier = Modifier.height(8.dp))


               // Volver
               ExtendedFloatingActionButton(
                   text = { Text("Volver") },
                   icon = { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") },
                   onClick = {
                       navController.popBackStack()
                       expanded = false
                   },
                   containerColor = Color(0xFF2196F3) // Azul
               )
               Spacer(modifier = Modifier.height(16.dp))
           }


           // Botón principal para mostrar/ocultar el menú
           FloatingActionButton(
               onClick = { expanded = !expanded },
               containerColor = Color(0xFF6200EE)
           ) {
               Icon(Icons.Default.Add, contentDescription = "Opciones")
           }
       }
   }
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
fun PantallaGastos(navController: NavHostController, viewModel: FinanzasViewModel) {
   Column(
       modifier = Modifier.fillMaxSize().padding(16.dp),
       horizontalAlignment = Alignment.CenterHorizontally,
       verticalArrangement = Arrangement.Center
   ) {
       Text("Estadísticas de Gastos", style = MaterialTheme.typography.titleLarge)
       Spacer(modifier = Modifier.height(16.dp))
       Text("Gastos totales: $${viewModel.gastos}")
   }
}


@Composable
fun PantallaConfiguracion(navController: NavHostController, viewModel: FinanzasViewModel) {
   var meta by remember { mutableStateOf(viewModel.metaAhorro.toString()) }


   Column(
       modifier = Modifier.fillMaxSize().padding(16.dp),
       horizontalAlignment = Alignment.CenterHorizontally,
       verticalArrangement = Arrangement.Center
   ) {
       Text("Configuración de Ahorro", style = MaterialTheme.typography.titleLarge)
       Spacer(modifier = Modifier.height(8.dp))
       OutlinedTextField(
           value = meta,
           onValueChange = { meta = it },
           label = { Text("Meta de ahorro") }
       )
       Spacer(modifier = Modifier.height(8.dp))
       Button(onClick = {
           meta.toDoubleOrNull()?.let { nuevaMeta ->
               viewModel.actualizarMetaAhorro(nuevaMeta)
               Log.d("PantallaConfiguracion", "Meta de ahorro actualizada a $meta")
           }
       }) {
           Text("Guardar Meta")
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
           Text("Editar Meta de Ahorro")
       }
       Spacer(modifier = Modifier.height(8.dp))
       Button(onClick = { navController.popBackStack() }) {
           Text("Volver")
       }
   }
}


