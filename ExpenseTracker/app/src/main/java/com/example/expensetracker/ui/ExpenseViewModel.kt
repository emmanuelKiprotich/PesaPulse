package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.ai.Anomaly
import com.example.expensetracker.ai.ExpenseCategorizer
import com.example.expensetracker.ai.Prediction
import com.example.expensetracker.ai.SpendingInsights
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.CategorySource
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.ExpenseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val categorizer: ExpenseCategorizer
) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val anomalies: StateFlow<List<Anomaly>> = expenses
        .map { SpendingInsights.flagAnomalies(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _suggestion = MutableStateFlow<Prediction?>(null)
    val suggestion: StateFlow<Prediction?> = _suggestion.asStateFlow()

    private var suggestJob: Job? = null

    /** Called as the user types a merchant; debounced so the model isn't run on every keystroke. */
    fun onMerchantChanged(merchant: String) {
        suggestJob?.cancel()
        if (merchant.isBlank()) {
            _suggestion.value = null
            return
        }
        suggestJob = viewModelScope.launch {
            delay(300)
            _suggestion.value = categorizer.predict(merchant.trim())
        }
    }

    /**
     * @param chosen category explicitly picked by the user (null = accept the AI suggestion).
     * Returns false if the input is invalid.
     */
    fun addExpense(merchant: String, amountText: String, chosen: Category?): Boolean {
        val amount = amountText.toDoubleOrNull() ?: return false
        if (merchant.isBlank() || amount <= 0) return false

        val suggested = _suggestion.value
        val (category, source) = when {
            chosen != null -> chosen to CategorySource.USER
            suggested != null -> suggested.category to suggested.source
            else -> Category.OTHER to CategorySource.RULES
        }
        viewModelScope.launch {
            repository.add(
                Expense(
                    amountMinor = (amount * 100).roundToLong(),
                    merchant = merchant.trim(),
                    category = category,
                    categorySource = source
                )
            )
        }
        _suggestion.value = null
        return true
    }

    fun delete(expense: Expense) {
        viewModelScope.launch { repository.delete(expense) }
    }
}
