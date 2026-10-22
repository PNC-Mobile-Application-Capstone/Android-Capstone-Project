package com.pnc.adventureworksmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pnc.adventureworksmobile.presentation.navigation.AdventureWorksApp
import com.pnc.adventureworksmobile.ui.theme.AdventureWorksMobileTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdventureWorksMobileTheme {
                AdventureWorksApp()
            }
        }
    }
}
