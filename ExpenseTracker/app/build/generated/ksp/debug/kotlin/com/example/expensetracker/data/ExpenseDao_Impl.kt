package com.example.expensetracker.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ExpenseDao_Impl(
  __db: RoomDatabase,
) : ExpenseDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfExpense: EntityInsertAdapter<Expense>

  private val __deleteAdapterOfExpense: EntityDeleteOrUpdateAdapter<Expense>
  init {
    this.__db = __db
    this.__insertAdapterOfExpense = object : EntityInsertAdapter<Expense>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `expenses` (`id`,`amountMinor`,`currency`,`merchant`,`note`,`category`,`categorySource`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Expense) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.amountMinor)
        statement.bindText(3, entity.currency)
        statement.bindText(4, entity.merchant)
        statement.bindText(5, entity.note)
        statement.bindText(6, __Category_enumToString(entity.category))
        statement.bindText(7, __CategorySource_enumToString(entity.categorySource))
        statement.bindLong(8, entity.timestamp)
      }
    }
    this.__deleteAdapterOfExpense = object : EntityDeleteOrUpdateAdapter<Expense>() {
      protected override fun createQuery(): String = "DELETE FROM `expenses` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Expense) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insert(expense: Expense): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfExpense.insertAndReturnId(_connection, expense)
    _result
  }

  public override suspend fun delete(expense: Expense): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfExpense.handle(_connection, expense)
  }

  public override fun observeAll(): Flow<List<Expense>> {
    val _sql: String = "SELECT * FROM expenses ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfAmountMinor: Int = getColumnIndexOrThrow(_stmt, "amountMinor")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfMerchant: Int = getColumnIndexOrThrow(_stmt, "merchant")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfCategorySource: Int = getColumnIndexOrThrow(_stmt, "categorySource")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<Expense> = mutableListOf()
        while (_stmt.step()) {
          val _item: Expense
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpAmountMinor: Long
          _tmpAmountMinor = _stmt.getLong(_columnIndexOfAmountMinor)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpMerchant: String
          _tmpMerchant = _stmt.getText(_columnIndexOfMerchant)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCategory: Category
          _tmpCategory = __Category_stringToEnum(_stmt.getText(_columnIndexOfCategory))
          val _tmpCategorySource: CategorySource
          _tmpCategorySource = __CategorySource_stringToEnum(_stmt.getText(_columnIndexOfCategorySource))
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = Expense(_tmpId,_tmpAmountMinor,_tmpCurrency,_tmpMerchant,_tmpNote,_tmpCategory,_tmpCategorySource,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeTotalsByCategory(): Flow<List<CategoryTotal>> {
    val _sql: String = "SELECT category AS category, SUM(amountMinor) AS total FROM expenses GROUP BY category"
    return createFlow(__db, false, arrayOf("expenses")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfCategory: Int = 0
        val _columnIndexOfTotal: Int = 1
        val _result: MutableList<CategoryTotal> = mutableListOf()
        while (_stmt.step()) {
          val _item: CategoryTotal
          val _tmpCategory: Category
          _tmpCategory = __Category_stringToEnum(_stmt.getText(_columnIndexOfCategory))
          val _tmpTotal: Long
          _tmpTotal = _stmt.getLong(_columnIndexOfTotal)
          _item = CategoryTotal(_tmpCategory,_tmpTotal)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __Category_enumToString(_value: Category): String = when (_value) {
    Category.FOOD -> "FOOD"
    Category.TRANSPORT -> "TRANSPORT"
    Category.AIRTIME_DATA -> "AIRTIME_DATA"
    Category.RENT_UTILITIES -> "RENT_UTILITIES"
    Category.SHOPPING -> "SHOPPING"
    Category.HEALTH -> "HEALTH"
    Category.EDUCATION -> "EDUCATION"
    Category.ENTERTAINMENT -> "ENTERTAINMENT"
    Category.PEER_DEBTS -> "PEER_DEBTS"
    Category.HELB_INCOME -> "HELB_INCOME"
    Category.FEES -> "FEES"
    Category.OTHER -> "OTHER"
  }

  private fun __CategorySource_enumToString(_value: CategorySource): String = when (_value) {
    CategorySource.USER -> "USER"
    CategorySource.RULES -> "RULES"
    CategorySource.MODEL -> "MODEL"
  }

  private fun __Category_stringToEnum(_value: String): Category = when (_value) {
    "FOOD" -> Category.FOOD
    "TRANSPORT" -> Category.TRANSPORT
    "AIRTIME_DATA" -> Category.AIRTIME_DATA
    "RENT_UTILITIES" -> Category.RENT_UTILITIES
    "SHOPPING" -> Category.SHOPPING
    "HEALTH" -> Category.HEALTH
    "EDUCATION" -> Category.EDUCATION
    "ENTERTAINMENT" -> Category.ENTERTAINMENT
    "PEER_DEBTS" -> Category.PEER_DEBTS
    "HELB_INCOME" -> Category.HELB_INCOME
    "FEES" -> Category.FEES
    "OTHER" -> Category.OTHER
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  private fun __CategorySource_stringToEnum(_value: String): CategorySource = when (_value) {
    "USER" -> CategorySource.USER
    "RULES" -> CategorySource.RULES
    "MODEL" -> CategorySource.MODEL
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
