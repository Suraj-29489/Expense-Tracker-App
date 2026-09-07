package com.personalexpensetracker.domain.repository

import com.personalexpensetracker.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * Repository interface defining persistence and query contracts for Expense data.
 * Pure domain abstraction independent of underlying database technology.
 */
interface ExpenseRepository {

    /**
     * Observe all recorded expenses ordered by date descending.
     */
    fun getAllExpenses(): Flow<List<Expense>>

    /**
     * Retrieve a specific expense by unique identifier.
     */
    suspend fun getExpenseById(id: Long): Expense?

    /**
     * Insert a new expense and return its generated row ID.
     */
    suspend fun insertExpense(expense: Expense): Long

    /**
     * Update an existing expense.
     */
    suspend fun updateExpense(expense: Expense)

    /**
     * Delete an existing expense.
     */
    suspend fun deleteExpense(expense: Expense)

    /**
     * Delete an expense by unique identifier.
     */
    suspend fun deleteExpenseById(id: Long)

    /**
     * Observe expenses belonging to a specific category.
     */
    fun getExpensesByCategory(category: String): Flow<List<Expense>>

    /**
     * Observe expenses occurring within an inclusive date range.
     */
    fun getExpensesByDateRange(startDate: Instant, endDate: Instant): Flow<List<Expense>>

    /**
     * Observe total spending in minor currency units (cents).
     */
    fun getTotalExpensesInCents(): Flow<Long?>
}

