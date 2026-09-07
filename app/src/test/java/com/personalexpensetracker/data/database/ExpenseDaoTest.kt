package com.personalexpensetracker.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Unit and integration tests for [ExpenseDao] executed against an in-memory SQLite database via Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExpenseDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = database.expenseDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveExpenseById() = runBlocking {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val expense = ExpenseEntity(
            title = "Supermarket Groceries",
            amountInCents = 4550L,
            category = "Groceries",
            date = now,
            notes = "Weekly grocery shopping",
            createdAt = now
        )

        val id = expenseDao.insertExpense(expense)
        assertTrue(id > 0)

        val retrieved = expenseDao.getExpenseById(id)
        assertNotNull(retrieved)
        assertEquals(id, retrieved?.id)
        assertEquals("Supermarket Groceries", retrieved?.title)
        assertEquals(4550L, retrieved?.amountInCents)
        assertEquals("Groceries", retrieved?.category)
        assertEquals(now, retrieved?.date)
        assertEquals("Weekly grocery shopping", retrieved?.notes)
    }

    @Test
    fun getAllExpensesReturnsOrderedByDateDescending() = runBlocking {
        val baseTime = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val exp1 = ExpenseEntity(
            title = "Old Expense",
            amountInCents = 1000L,
            category = "Other",
            date = baseTime.minus(2, ChronoUnit.DAYS)
        )
        val exp2 = ExpenseEntity(
            title = "Newest Expense",
            amountInCents = 2000L,
            category = "Other",
            date = baseTime
        )
        val exp3 = ExpenseEntity(
            title = "Middle Expense",
            amountInCents = 1500L,
            category = "Other",
            date = baseTime.minus(1, ChronoUnit.DAYS)
        )

        expenseDao.insertExpenses(listOf(exp1, exp2, exp3))

        val allExpenses = expenseDao.getAllExpenses().first()
        assertEquals(3, allExpenses.size)
        assertEquals("Newest Expense", allExpenses[0].title)
        assertEquals("Middle Expense", allExpenses[1].title)
        assertEquals("Old Expense", allExpenses[2].title)
    }

    @Test
    fun updateExpenseModifiesRecord() = runBlocking {
        val expense = ExpenseEntity(
            title = "Lunch",
            amountInCents = 1200L,
            category = "Food",
            date = Instant.now()
        )
        val id = expenseDao.insertExpense(expense)

        val updated = expense.copy(
            id = id,
            title = "Lunch with Team",
            amountInCents = 1850L
        )
        val rowsAffected = expenseDao.updateExpense(updated)
        assertEquals(1, rowsAffected)

        val retrieved = expenseDao.getExpenseById(id)
        assertEquals("Lunch with Team", retrieved?.title)
        assertEquals(1850L, retrieved?.amountInCents)
    }

    @Test
    fun deleteExpenseRemovesEntity() = runBlocking {
        val expense = ExpenseEntity(
            title = "Coffee",
            amountInCents = 450L,
            category = "Food",
            date = Instant.now()
        )
        val id = expenseDao.insertExpense(expense)
        val inserted = expenseDao.getExpenseById(id)
        assertNotNull(inserted)

        val rowsDeleted = expenseDao.deleteExpense(inserted!!)
        assertEquals(1, rowsDeleted)

        val retrievedAfterDelete = expenseDao.getExpenseById(id)
        assertNull(retrievedAfterDelete)
    }

    @Test
    fun deleteExpenseByIdRemovesRecord() = runBlocking {
        val expense = ExpenseEntity(
            title = "Taxi",
            amountInCents = 2500L,
            category = "Transport",
            date = Instant.now()
        )
        val id = expenseDao.insertExpense(expense)

        val rowsDeleted = expenseDao.deleteExpenseById(id)
        assertEquals(1, rowsDeleted)

        val retrieved = expenseDao.getExpenseById(id)
        assertNull(retrieved)
    }

    @Test
    fun getExpensesByCategoryFiltersCorrectly() = runBlocking {
        val exp1 = ExpenseEntity(title = "Train", amountInCents = 300L, category = "Transport", date = Instant.now())
        val exp2 = ExpenseEntity(title = "Apple", amountInCents = 150L, category = "Food", date = Instant.now())
        val exp3 = ExpenseEntity(title = "Bus", amountInCents = 250L, category = "Transport", date = Instant.now())

        expenseDao.insertExpenses(listOf(exp1, exp2, exp3))

        val transportExpenses = expenseDao.getExpensesByCategory("Transport").first()
        assertEquals(2, transportExpenses.size)
        assertTrue(transportExpenses.all { it.category == "Transport" })
    }

    @Test
    fun getExpensesByDateRangeFiltersCorrectly() = runBlocking {
        val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)
        val day1 = now.minus(5, ChronoUnit.DAYS)
        val day2 = now.minus(3, ChronoUnit.DAYS)
        val day3 = now.minus(1, ChronoUnit.DAYS)

        val exp1 = ExpenseEntity(title = "Day 1", amountInCents = 100L, category = "Misc", date = day1)
        val exp2 = ExpenseEntity(title = "Day 2", amountInCents = 200L, category = "Misc", date = day2)
        val exp3 = ExpenseEntity(title = "Day 3", amountInCents = 300L, category = "Misc", date = day3)

        expenseDao.insertExpenses(listOf(exp1, exp2, exp3))

        val filtered = expenseDao.getExpensesByDateRange(
            startDate = now.minus(4, ChronoUnit.DAYS),
            endDate = now.minus(2, ChronoUnit.DAYS)
        ).first()

        assertEquals(1, filtered.size)
        assertEquals("Day 2", filtered[0].title)
    }

    @Test
    fun getTotalExpensesInCentsAggregatesAccurately() = runBlocking {
        val exp1 = ExpenseEntity(title = "Coffee", amountInCents = 475L, category = "Food", date = Instant.now())
        val exp2 = ExpenseEntity(title = "Lunch", amountInCents = 1525L, category = "Food", date = Instant.now())
        val exp3 = ExpenseEntity(title = "Dinner", amountInCents = 3000L, category = "Food", date = Instant.now())

        expenseDao.insertExpenses(listOf(exp1, exp2, exp3))

        val totalCents = expenseDao.getTotalExpensesInCents().first()
        assertEquals(5000L, totalCents)
    }
}

