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
import com.tallerapp.features.finanzas.EgresoFormScreen
import com.tallerapp.features.finanzas.FinanzasScreen
import com.tallerapp.features.finanzas.IngresoFormScreen
import com.tallerapp.features.reportes.ReportesScreen
import com.tallerapp.features.trabajos.TrabajoDetalleScreen
import com.tallerapp.features.trabajos.TrabajoFormScreen
import com.tallerapp.features.trabajos.TrabajosListScreen
import com.tallerapp.features.finanzas.FinanzasViewModel
import com.tallerapp.features.finanzas.egreso.EgresoFormViewModel
import com.tallerapp.features.finanzas.ingreso.IngresoFormViewModel
import com.tallerapp.features.trabajos.detalle.TrabajoDetalleViewModel
import com.tallerapp.features.trabajos.form.TrabajoFormViewModel
import com.tallerapp.features.trabajos.list.TrabajosListViewModel

/**
 * Grafo de navegación de toda la aplicación. Pantalla inicial: Dashboard.
 * Composición raíz: aquí se construyen los ViewModels con los casos de uso del contenedor.
 * Trabajos (Fase 2) ya tiene funcionalidad real; Finanzas/Reportes siguen como placeholders.
 */
@Composable
fun TallerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destination.DASHBOARD,
    ) {
        composable(Destination.DASHBOARD) {
            DashboardScreen(
                onNuevoTrabajo = { navController.navigate(Destination.NUEVO_TRABAJO) },
                onNuevoIngreso = { navController.navigate(Destination.NUEVO_INGRESO) },
                onNuevoGasto = { navController.navigate(Destination.NUEVO_GASTO) },
                onVerTrabajos = { navController.navigate(Destination.TRABAJOS) },
                onVerFinanzas = { navController.navigate(Destination.FINANZAS) },
                onVerReportes = { navController.navigate(Destination.REPORTES) },
            )
        }

        composable(Destination.TRABAJOS) {
            val container = rememberAppContainer()
            val vm: TrabajosListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { TrabajosListViewModel(container.observarTrabajos) }
                },
            )
            TrabajosListScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onNuevoTrabajo = { navController.navigate(Destination.NUEVO_TRABAJO) },
                onAbrirDetalle = { id -> navController.navigate(Destination.trabajoDetalle(id)) },
            )
        }

        composable(Destination.NUEVO_TRABAJO) {
            val container = rememberAppContainer()
            val vm: TrabajoFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TrabajoFormViewModel(
                            container.crearTrabajo,
                            container.editarTrabajo,
                            container.obtenerTrabajo,
                            trabajoId = null,
                        )
                    }
                },
            )
            TrabajoFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(
            route = Destination.TRABAJO_EDITAR,
            arguments = listOf(navArgument(Destination.ARG_TRABAJO_ID) { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong(Destination.ARG_TRABAJO_ID) ?: 0L
            val container = rememberAppContainer()
            val vm: TrabajoFormViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TrabajoFormViewModel(
                            container.crearTrabajo,
                            container.editarTrabajo,
                            container.obtenerTrabajo,
                            trabajoId = id,
                        )
                    }
                },
            )
            TrabajoFormScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(
            route = Destination.TRABAJO_DETALLE,
            arguments = listOf(navArgument(Destination.ARG_TRABAJO_ID) { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong(Destination.ARG_TRABAJO_ID) ?: 0L
            val container = rememberAppContainer()
            val vm: TrabajoDetalleViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        TrabajoDetalleViewModel(
                            container.obtenerTrabajo,
                            container.cambiarEstado,
                            container.eliminarTrabajo,
                            trabajoId = id,
                        )
                    }
                },
            )
            TrabajoDetalleScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onEditar = { navController.navigate(Destination.trabajoEditar(id)) },
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

        composable(Destination.REPORTES) {
            ReportesScreen(onBack = { navController.popBackStack() })
        }
    }
}
