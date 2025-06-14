package com.pasundane_budget.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pasundane_budget.data.local.db.dao.CategoryDao
import com.pasundane_budget.data.local.db.dao.TransactionDao
import com.pasundane_budget.data.local.db.entities.CategoryEntity
import com.pasundane_budget.data.local.db.entities.TransactionEntity

@Database(entities = [CategoryEntity::class, TransactionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}
