package com.example.expensetracker.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {
    fun observeAll(): Flow<List<Expense>> = dao.observeAll()
    fun observeTotalsByCategory(): Flow<List<CategoryTotal>> = dao.observeTotalsByCategory()
    suspend fun add(expense: Expense) = dao.insert(expense)
    suspend fun delete(expense: Expense) = dao.delete(expense)
}
