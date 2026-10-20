package com.pnc.adventureworksmobile.presentation.home

import androidx.lifecycle.ViewModel
import com.pnc.adventureworksmobile.core.session.DemoCustomer
import com.pnc.adventureworksmobile.core.session.DemoCustomerSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val demoCustomer: DemoCustomer,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    session: DemoCustomerSession,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(demoCustomer = session.activeCustomer),
    )

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}
