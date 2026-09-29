package com.example.expensetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetracker.data.Category
import com.example.expensetracker.data.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(vm: ExpenseViewModel) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    val anomalies by vm.anomalies.collectAsStateWithLifecycle()
    val suggestion by vm.suggestion.collectAsStateWithLifecycle()

    var merchant by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var chosen by remember { mutableStateOf<Category?>(null) }

    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Expense Tracker", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it; chosen = null; vm.onMerchantChanged(it) },
                label = { Text("Merchant / description") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (KES)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // AI suggestion + manual override chips
            suggestion?.let {
                Text(
                    "Suggested: ${it.category.pretty()} (${(it.confidence * 100).toInt()}%, ${it.source.name.lowercase()})",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { c ->
                    FilterChip(
                        selected = (chosen ?: suggestion?.category) == c,
                        onClick = { chosen = c },
                        label = { Text(c.pretty()) }
                    )
                }
            }

            Button(
                onClick = {
                    if (vm.addExpense(merchant, amount, chosen)) {
                        merchant = ""; amount = ""; chosen = null
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add expense") }

            if (anomalies.isNotEmpty()) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Insights", style = MaterialTheme.typography.titleSmall)
                        anomalies.take(3).forEach { Text("• ${it.message}", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(expenses, key = { it.id }) { ExpenseRow(it, onDelete = { vm.delete(it) }) }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(expense.merchant, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${expense.category.pretty()} · ${expense.categorySource.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column {
                Text("%,.2f %s".format(expense.amountMinor / 100.0, expense.currency))
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

private fun Category.pretty() = name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
