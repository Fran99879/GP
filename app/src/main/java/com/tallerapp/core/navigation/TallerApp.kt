package com.tallerapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tallerapp.core.di.rememberAppContainer
import com.tallerapp.features.dashboard.DashboardScreen
import com.tallerapp.features.dashboard.DashboardViewModel
import com.tallerapp.features.deudas.DeudaFormScreen
import com.tallerapp.features.deudas.DeudasScreen
import com.tallerapp.features.deudas.DeudasViewModel
import com.tallerapp.features.deudas.form.DeudaFormViewModel
import com.tallerapp.features.finanzas.EgresoFormScreen
import com.tallerapp.features.finanzas.FinanzasScreen
import com.tallerapp.features.finanzas.FinanzasViewModel
import com.tallerapp.features.finanzas.IngresoFormScreen
import com.tallerapp.features.finanzas.egreso.EgresoFormViewModel
import com.tallerapp.features.finanzas.ingreso.IngresoFormViewModel
import com.tallerapp.features.reportes.ReportesScreen
import com.tallerapp.features.reportes.ReportesViewModel

/**
 * Grafo de navegación de toda la aplicación. Pantalla inicial: Dashboard.
 * Composición raíz: aquí se construyen los ViewModels con los casos de uso del contenedor.
 */
@Composable
fun TallerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destination.DASHBOARD,
    ) {
        composable(Destination.DASHBOARD) {
            val container = rememberAppContainer()
            val vm: DashboardViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { DashboardViewModel(container.observarDashboard) }
                },
            )
            DashboardScreen(
                viewModel = vm,
                onNuevoIngreso = { navController.navigate(Destination.NUEVO_INGRESO) },
                onNuevoGasto = { navController.navigate(Destination.NUEVO_GASTO) },
                onVerFinanzas = { navController.navigate(Destination.FINANZAS) },
                onVerDeudas = { navController.navigate(Destination.DEUDAS) },
                onVerReportes = { navController.navigate(Destination.REPORTES) },
            )
        }

        composable(Destination.FINANZAS) {
            val container = rememberAppContainer()
            val vm: FinanzasViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        FinanzasViewModel(
                            container.observarResumenDelDia,
                            container.observarIngresosDelDia,
                            container.observarEgresosDelDia,
                            container.eliminarIngreso,
                            container.eliminarEgreso,
                        )
                    }
                },
            )
            FinanzasScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onNuevoIngreso = { navController.navigate(Destination.NUEVO_INGRESO) },
                onNuevoGasto = { navController.navigate(Destination.NUEVO_GASTO) },
                onEditarIngreso = { id -> navController.navigate(Destination.ingresoEditar(id)) },
                onEditarEgreso = { id -> navController.navigate(Destination.egresoEditar(id)) },
            )
        }

        composable(Destination.NUEVO_INGRESO) {
            val container = rememberAppContainer()
            val vm: IngresoFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        IngresoFormViewModel(
                            container.registrarIngreso,
                            container.editarIngreso,
                            container.obtenerIngreso,
                            ingresoId = null,
                        )
                    }
                },
            )
            IngresoFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(
            route = Destination.INGRESO_EDITAR,
            arguments = listOf(navArgument(Destination.ARG_MOVIMIENTO_ID) { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong(Destination.ARG_MOVIMIENTO_ID) ?: 0L
            val container = rememberAppContainer()
            val vm: IngresoFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        IngresoFormViewModel(
                            container.registrarIngreso,
                            container.editarIngreso,
                            container.obtenerIngreso,
                            ingresoId = id,
                        )
                    }
                },
            )
            IngresoFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Destination.NUEVO_GASTO) {
            val container = rememberAppContainer()
            val vm: EgresoFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        EgresoFormViewModel(
                            container.registrarEgreso,
                            container.editarEgreso,
                            container.obtenerEgreso,
                            egresoId = null,
                        )
                    }
                },
            )
            EgresoFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(
            route = Destination.EGRESO_EDITAR,
            arguments = listOf(navArgument(Destination.ARG_MOVIMIENTO_ID) { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong(Destination.ARG_MOVIMIENTO_ID) ?: 0L
            val container = rememberAppContainer()
            val vm: EgresoFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        EgresoFormViewModel(
                            container.registrarEgreso,
                            container.editarEgreso,
                            container.obtenerEgreso,
                            egresoId = id,
                        )
                    }
                },
            )
            EgresoFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Destination.DEUDAS) {
            val container = rememberAppContainer()
            val vm: DeudasViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        DeudasViewModel(
                            container.observarDeudas,
                            container.marcarDeudaCobrada,
                            container.eliminarDeuda,
                        )
                    }
                },
            )
            DeudasScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onNuevaDeuda = { navController.navigate(Destination.NUEVA_DEUDA) },
                onEditarDeuda = { id -> navController.navigate(Destination.deudaEditar(id)) },
            )
        }

        composable(Destination.NUEVA_DEUDA) {
            val container = rememberAppContainer()
            val vm: DeudaFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        DeudaFormViewModel(
                            container.registrarDeuda,
                            container.editarDeuda,
                            container.obtenerDeuda,
                            deudaId = null,
                        )
                    }
                },
            )
            DeudaFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(
            route = Destination.DEUDA_EDITAR,
            arguments = listOf(navArgument(Destination.ARG_DEUDA_ID) { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong(Destination.ARG_DEUDA_ID) ?: 0L
            val container = rememberAppContainer()
            val vm: DeudaFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        DeudaFormViewModel(
                            container.registrarDeuda,
                            container.editarDeuda,
                            container.obtenerDeuda,
                            deudaId = id,
                        )
                    }
                },
            )
            DeudaFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Destination.REPORTES) {
            val container = rememberAppContainer()
            val vm: ReportesViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        ReportesViewModel(
                            container.observarResumenDelDia,
                            container.observarReporteMensual,
                        )
                    }
                },
            )
            ReportesScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
