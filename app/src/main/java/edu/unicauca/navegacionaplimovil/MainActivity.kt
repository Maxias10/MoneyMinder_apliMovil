package edu.unicauca.navegacionaplimovil

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
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

class FinanzasViewModel : ViewModel() {
    var ingresos by mutableStateOf(0.0)
    var gastos by mutableStateOf(0.0)
    var metaAhorro by mutableStateOf(0.0)

    val saldo: Double get() = ingresos - gastos

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
}



@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val viewModel: FinanzasViewModel = viewModel()

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
            meta.toDoubleOrNull()?.let {
                viewModel.metaAhorro = it
                Log.d("PantallaConfiguracion", "Meta de ahorro actualizada a $meta")
            }
        }) {
            Text("Guardar Meta")
        }
    }
}
