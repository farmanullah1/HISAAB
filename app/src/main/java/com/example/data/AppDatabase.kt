package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.NeedDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.AccountEntity
import com.example.data.entity.CategoryEntity
import com.example.data.entity.NeedEntity
import com.example.data.entity.TransactionEntity
import com.example.model.AccountType
import com.example.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Main Room Database for Hisaab.
 * 4 tables: accounts, transactions, categories, needs.
 */
@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        NeedEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun needDao(): NeedDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hisaab_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateStarterData(database)
                }
            }
        }

        private suspend fun populateStarterData(db: AppDatabase) {
            val accountDao = db.accountDao()
            val categoryDao = db.categoryDao()

            if (accountDao.getAccountCount() == 0) {
                val starterAccounts = listOf(
                    AccountEntity(
                        name = "Cash",
                        type = AccountType.CASH,
                        openingBalancePaisa = 0L,
                        colorHex = "#7C9A6B"
                    ),
                    AccountEntity(
                        name = "Bank",
                        type = AccountType.BANK,
                        openingBalancePaisa = 0L,
                        colorHex = "#6BA88A"
                    )
                )
                accountDao.insertAccounts(starterAccounts)
            }

            if (categoryDao.getCategoryCount() == 0) {
                val defaultCategories = listOf(
                    CategoryEntity(name = "Food & Dining", type = TransactionType.EXPENSE, iconKey = "food", colorHex = "#C67B7B"),
                    CategoryEntity(name = "Groceries", type = TransactionType.EXPENSE, iconKey = "groceries", colorHex = "#7C9A6B"),
                    CategoryEntity(name = "Fuel & Transport", type = TransactionType.EXPENSE, iconKey = "fuel", colorHex = "#6BA88A"),
                    CategoryEntity(name = "Bills", type = TransactionType.EXPENSE, iconKey = "bills", colorHex = "#D6A15C"),
                    CategoryEntity(name = "Rent", type = TransactionType.EXPENSE, iconKey = "rent", colorHex = "#A084E8"),
                    CategoryEntity(name = "Healthcare", type = TransactionType.EXPENSE, iconKey = "health", colorHex = "#E06D85"),
                    CategoryEntity(name = "Shopping", type = TransactionType.EXPENSE, iconKey = "shopping", colorHex = "#4E82A6"),
                    CategoryEntity(name = "Education", type = TransactionType.EXPENSE, iconKey = "education", colorHex = "#5E9387"),
                    CategoryEntity(name = "Other", type = TransactionType.EXPENSE, iconKey = "other", colorHex = "#8A8F99"),

                    CategoryEntity(name = "Salary", type = TransactionType.INCOME, iconKey = "salary", colorHex = "#6BA88A"),
                    CategoryEntity(name = "Freelance", type = TransactionType.INCOME, iconKey = "freelance", colorHex = "#7C9A6B"),
                    CategoryEntity(name = "Gift", type = TransactionType.INCOME, iconKey = "gift", colorHex = "#E06D85"),
                    CategoryEntity(name = "Refund", type = TransactionType.INCOME, iconKey = "refund", colorHex = "#D6A15C"),
                    CategoryEntity(name = "Other", type = TransactionType.INCOME, iconKey = "other", colorHex = "#8A8F99")
                )
                categoryDao.insertCategories(defaultCategories)
            }
        }
    }
}

typealias HisaabDatabase = AppDatabase
