package com.tallerapp.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import com.tallerapp.core.di.rememberAppContainer
import com.tallerapp.features.agenda.AgendaScreen
import com.tallerapp.features.agenda.AgendaViewModel
import com.tallerapp.features.ajustes.AjustesScreen
import com.tallerapp.features.calculadora.CalculadoraScreen
import com.tallerapp.features.categorias.CategoriasScreen
import com.tallerapp.features.categorias.CategoriasViewModel
import com.tallerapp.features.cuentas.CuentasScreen
import com.tallerapp.features.cuentas.CuentasViewModel
import com.tallerapp.features.metas.MetasScreen
import com.tallerapp.features.metas.MetasViewModel
import com.tallerapp.features.negocios.NegociosScreen
import com.tallerapp.features.negocios.NegociosViewModel
import com.tallerapp.features.negocios.recordarNombreNegocioActual
import com.tallerapp.features.remito.RemitoScreen
import com.tallerapp.features.perfil.PerfilScreen
import com.tallerapp.features.recurrentes.RecurrentesScreen
import com.tallerapp.features.recurrentes.RecurrentesViewModel
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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val rutaActual by navController.currentBackStackEntryAsState()
    val ruta = rutaActual?.destination?.route
    // Destinos con barra inferior (los mismos que la barra lateral del escritorio).
    val rutasRaiz = DESTINOS_RAIZ.map { it.ruta }.toSet()
    val nivelesRaiz = rutasRaiz + Destination.AJUSTES

    val abrirMenu: () -> Unit = { scope.launch { drawerState.open() } }
    fun irA(destino: String) {
        scope.launch { drawerState.close() }
        navController.navigate(destino) {
            popUpTo(Destination.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = ruta in nivelesRaiz,
        drawerContent = { AppDrawer(ruta) { irA(it) } },
    ) {
        Scaffold(
            bottomBar = { if (ruta in rutasRaiz) BarraInferior(ruta) { irA(it) } },
        ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.DASHBOARD,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        ) {
        composable(Destination.DASHBOARD) {
            val container = rememberAppContainer()
            val vm: DashboardViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { DashboardViewModel(container.observarDashboard, container.observarMetas) }
                },
            )
            DashboardScreen(
                viewModel = vm,
                onOpenMenu = abrirMenu,
                onNuevoIngreso = { navController.navigate(Destination.NUEVO_INGRESO) },
                onNuevoGasto = { navController.navigate(Destination.NUEVO_GASTO) },
                onVerFinanzas = { navController.navigate(Destination.FINANZAS) },
                onVerDeudas = { navController.navigate(Destination.DEUDAS) },
                onVerReportes = { navController.navigate(Destination.REPORTES) },
                onVerAjustes = { navController.navigate(Destination.AJUSTES) },
                onVerPerfil = { navController.navigate(Destination.PERFIL) },
                onVerNegocios = { navController.navigate(Destination.NEGOCIOS) },
                onVerCalculadora = { navController.navigate(Destination.CALCULADORA) },
                onVerMetas = { navController.navigate(Destination.METAS) },
                onVerAgenda = { navController.navigate(Destination.AGENDA) },
            )
        }

        composable(Destination.PERFIL) {
            PerfilScreen(onBack = { navController.popBackStack() })
        }

        composable(Destination.AGENDA) {
            val container = rememberAppContainer()
            val vm: AgendaViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AgendaViewModel(
                            container.observarAgenda,
                            container.guardarAgendaItem,
                            container.marcarAgendaHecho,
                            container.eliminarAgendaItem,
                        )
                    }
                },
            )
            AgendaScreen(
                viewModel = vm,
                onOpenMenu = abrirMenu,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destination.REMITO) {
            RemitoScreen(
                negocioNombre = recordarNombreNegocioActual(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Destination.CALCULADORA) {
            CalculadoraScreen(onBack = { navController.popBackStack() })
        }

        composable(Destination.AJUSTES) {
            AjustesScreen(
                onBack = { navController.popBackStack() },
                onVerCategorias = { navController.navigate(Destination.CATEGORIAS) },
                onVerCuentas = { navController.navigate(Destination.CUENTAS) },
                onVerMetas = { navController.navigate(Destination.METAS) },
                onVerRecurrentes = { navController.navigate(Destination.RECURRENTES) },
                onVerNegocios = { navController.navigate(Destination.NEGOCIOS) },
                onVerAgenda = { navController.navigate(Destination.AGENDA) },
            )
        }

        composable(Destination.NEGOCIOS) {
            val container = rememberAppContainer()
            val vm: NegociosViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        NegociosViewModel(
                            container.observarNegocios,
                            container.crearNegocio,
                            container.renombrarNegocio,
                        )
                    }
                },
            )
            NegociosScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Destination.METAS) {
            val container = rememberAppContainer()
            val vm: MetasViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        MetasViewModel(container.observarMetas, container.guardarMeta, container.eliminarMeta)
                    }
                },
            )
            MetasScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Destination.RECURRENTES) {
            val container = rememberAppContainer()
            val vm: RecurrentesViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        RecurrentesViewModel(
                            container.observarRecurrentes,
                            container.observarCategorias,
                            container.observarCuentas,
                            container.guardarRecurrente,
                            container.eliminarRecurrente,
                        )
                    }
                },
            )
            RecurrentesScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Destination.CUENTAS) {
            val container = rememberAppContainer()
            val vm: CuentasViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CuentasViewModel(
                            container.saldosCuentas,
                            container.guardarCuenta,
                            container.eliminarCuenta,
                        )
                    }
                },
            )
            CuentasScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Destination.CATEGORIAS) {
            val container = rememberAppContainer()
            val vm: CategoriasViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        CategoriasViewModel(
                            container.observarCategorias,
                            container.guardarCategoria,
                            container.eliminarCategoria,
                        )
                    }
                },
            )
            CategoriasScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Destination.FINANZAS) {
            val container = rememberAppContainer()
            val vm: FinanzasViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        FinanzasViewModel(
                            container.observarIngresosRango,
                            container.observarEgresosRango,
                            container.observarCategorias,
                            container.eliminarIngreso,
                            container.eliminarEgreso,
                        )
                    }
                },
            )
            FinanzasScreen(
                viewModel = vm,
                onOpenMenu = abrirMenu,
                onBack = { navController.popBackStack() },
                onNuevoIngreso = { navController.navigate(Destination.NUEVO_INGRESO) },
                onNuevoGasto = { navController.navigate(Destination.NUEVO_GASTO) },
                onEditarIngreso = { id -> navController.navigate(Destination.ingresoEditar(id)) },
                onEditarEgreso = { id -> navController.navigate(Destination.egresoEditar(id)) },
                onVerCuentas = { navController.navigate(Destination.CUENTAS) },
                onVerCategorias = { navController.navigate(Destination.CATEGORIAS) },
                onVerRecurrentes = { navController.navigate(Destination.RECURRENTES) },
                onVerMetas = { navController.navigate(Destination.METAS) },
                onVerRemito = { navController.navigate(Destination.REMITO) },
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
                            container.observarCuentas,
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
                            container.observarCuentas,
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
                            container.observarCategorias,
                            container.observarCuentas,
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
                            container.observarCategorias,
                            container.observarCuentas,
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
                onOpenMenu = abrirMenu,
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
                            container.observarContactos,
                            container.agregarContacto,
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
                            container.observarContactos,
                            container.agregarContacto,
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
                            container.observarCategorias,
                            container.observarEvolucion,
                            container.observarComparativaNegocios,
                        )
                    }
                },
            )
            ReportesScreen(viewModel = vm, onOpenMenu = abrirMenu, onBack = { navController.popBackStack() })
        }
        }
        }
    }
}
