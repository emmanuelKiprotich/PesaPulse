package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ui.ExpenseScreen
import com.example.expensetracker.ui.ExpenseViewModel

import com.example.expensetracker.ui.theme.PesaPouchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as ExpenseApp).container

        setContent {
            PesaPouchTheme {
                val vm: ExpenseViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            ExpenseViewModel(container.repository, container.categorizer) as T
                    }
                )
                ExpenseScreen(vm)
            }
        }
    }
}
