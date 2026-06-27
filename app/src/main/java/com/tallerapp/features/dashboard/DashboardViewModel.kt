package com.tallerapp.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.ResumenDashboard
import com.tallerapp.domain.usecase.ObservarDashboardUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Estado del Dashboard: indicadores en vivo (Frozen Spec 12.1). */
class DashboardViewModel(
    observarDashboard: ObservarDashboardUseCase,
) : ViewModel() {

    val state: StateFlow<ResumenDashboard> =
        observarDashboard().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResumenDashboard(),
        )
}
