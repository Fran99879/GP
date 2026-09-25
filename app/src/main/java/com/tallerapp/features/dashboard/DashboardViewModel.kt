package com.tallerapp.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tallerapp.domain.model.Meta
import com.tallerapp.domain.model.ResumenDashboard
import com.tallerapp.domain.usecase.ObservarDashboardUseCase
import com.tallerapp.domain.usecase.ObservarMetasUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Estado del Dashboard: indicadores en vivo (Frozen Spec 12.1) + metas de ahorro. */
class DashboardViewModel(
    observarDashboard: ObservarDashboardUseCase,
    observarMetas: ObservarMetasUseCase,
) : ViewModel() {

    val state: StateFlow<ResumenDashboard> =
        observarDashboard().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResumenDashboard(),
        )

    val metas: StateFlow<List<Meta>> =
        observarMetas().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )
}
