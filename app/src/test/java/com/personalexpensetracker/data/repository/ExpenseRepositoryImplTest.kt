package com.personalexpensetracker.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.personalexpensetracker.data.database.AppDatabase
import com.personalexpensetracker.data.database.ExpenseDao
import com.personalexpensetracker.domain.model.Expense
import com.personalexpensetracker.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit and integration tests for [ExpenseRepositoryImpl] verifying domain operations,
 * bidirectional mapper conversions, and [BigDecimal] precision integrity.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExpenseRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var repository: ExpenseRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = database.expenseDao()
        repository = ExpenseRepositoryImpl(expenseDao)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveExpense_preservesBigDecimalPrecision() = runBlocking {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val expense = Expense(
            title = "Book Purchase",
            amount = BigDecimal("24.99"),
            category = "Education",
            date = now,
            notes = "Clean Architecture book",
            createdAt = now
        )

        val id = repository.insertExpense(expense)
        val retrieved = repository.getExpenseById(id)

        assertNotNull(retrieved)
        assertEquals(id, retrieved?.id)
        assertEquals("Book Purchase", retrieved?.title)
        assertEquals(BigDecimal("24.99"), retrieved?.amount)
        assertEquals("Education", retrieved?.category)
        assertEquals(now, retrieved?.date)
        assertEquals("Clean Architecture book", retrieved?.notes)
    }

    @Test
    fun updateExpense_modifiesPersistedData() = runBlocking {
        val expense = Expense(
            title = "Coffee",
            amount = BigDecimal("3.50"),
            category = "Food",
            date = Instant.now()
        )
        val id = repository.insertExpense(expense)

        val updated = expense.copy(
            id = id,
            title = "Large Coffee",
            amount = BigDecimal("4.75")
        )
        repository.updateExpense(updated)

        val retrieved = repository.getExpenseById(id)
        assertEquals("Large Coffee", retrieved?.title)
        assertEquals(BigDecimal("4.75"), retrieved?.amount)
    }

    @Test
    fun deleteExpenseById_removesRecord() = runBlocking {
        val expense = Expense(
            title = "Snack",
            amount = BigDecimal("1.99"),
            category = "Food",
            date = Instant.now()
        )
        val id = repository.insertExpense(expense)
        assertNotNull(repository.getExpenseById(id))

        repository.deleteExpenseById(id)
        assertNull(repository.getExpenseById(id))
    }

    @Test
    fun getAllExpenses_emitsMappedDomainList() = runBlocking {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        repository.insertExpense(Expense(title = "A", amount = BigDecimal("10.00"), category = "General", date = now.minus(1, ChronoUnit.HOURS)))
        repository.insertExpense(Expense(title = "B", amount = BigDecimal("20.00"), category = "General", date = now))

        val all = repository.getAllExpenses().first()
        assertEquals(2, all.size)
        assertEquals("B", all[0].title)
        assertEquals(BigDecimal("20.00"), all[0].amount)
        assertEquals("A", all[1].title)
        assertEquals(BigDecimal("10.00"), all[1].amount)
    }

    @Test
    fun getTotalExpensesInCents_returnsAccurateSum() = runBlocking {
        repository.insertExpense(Expense(title = "Item 1", amount = BigDecimal("12.34"), category = "General", date = Instant.now()))
        repository.insertExpense(Expense(title = "Item 2", amount = BigDecimal("87.66"), category = "General", date = Instant.now()))

        val totalCents = repository.getTotalExpensesInCents().first()
        assertEquals(10000L, totalCents)
    }
}

