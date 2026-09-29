package com.example.expensetracker.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _expenseDao: Lazy<ExpenseDao> = lazy {
    ExpenseDao_Impl(this)
  }

  private val _peerDebtDao: Lazy<PeerDebtDao> = lazy {
    PeerDebtDao_Impl(this)
  }

  private val _chamaGoalDao: Lazy<ChamaGoalDao> = lazy {
    ChamaGoalDao_Impl(this)
  }

  private val _mobileLoanDao: Lazy<MobileLoanDao> = lazy {
    MobileLoanDao_Impl(this)
  }

  private val _sideHustleDao: Lazy<SideHustleDao> = lazy {
    SideHustleDao_Impl(this)
  }

  private val _recurringBillDao: Lazy<RecurringBillDao> = lazy {
    RecurringBillDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "2e4a124fbbb49c6dbfb22cf100555f29", "6741a03a92a16c4446359a2b80b8f393") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amountMinor` INTEGER NOT NULL, `currency` TEXT NOT NULL, `merchant` TEXT NOT NULL, `note` TEXT NOT NULL, `category` TEXT NOT NULL, `categorySource` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `peer_debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `peerName` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `description` TEXT NOT NULL, `isOwedToMe` INTEGER NOT NULL, `isSettled` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `chama_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `targetAmountMinor` INTEGER NOT NULL, `currentSavedMinor` INTEGER NOT NULL, `deadline` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `mobile_loans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `provider` TEXT NOT NULL, `principalMinor` INTEGER NOT NULL, `dailyInterestRatePercent` REAL NOT NULL, `dateTakenMillis` INTEGER NOT NULL, `isRepaid` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `side_hustle_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `businessName` TEXT NOT NULL, `isIncome` INTEGER NOT NULL, `amountMinor` INTEGER NOT NULL, `description` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `recurring_bills` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amountMinor` INTEGER NOT NULL, `dueDayOfMonth` INTEGER NOT NULL, `category` TEXT NOT NULL, `isActive` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2e4a124fbbb49c6dbfb22cf100555f29')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `expenses`")
        connection.execSQL("DROP TABLE IF EXISTS `peer_debts`")
        connection.execSQL("DROP TABLE IF EXISTS `chama_goals`")
        connection.execSQL("DROP TABLE IF EXISTS `mobile_loans`")
        connection.execSQL("DROP TABLE IF EXISTS `side_hustle_transactions`")
        connection.execSQL("DROP TABLE IF EXISTS `recurring_bills`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsExpenses: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExpenses.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("amountMinor", TableInfo.Column("amountMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("currency", TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("merchant", TableInfo.Column("merchant", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("categorySource", TableInfo.Column("categorySource", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExpenses.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExpenses: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExpenses: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoExpenses: TableInfo = TableInfo("expenses", _columnsExpenses, _foreignKeysExpenses, _indicesExpenses)
        val _existingExpenses: TableInfo = read(connection, "expenses")
        if (!_infoExpenses.equals(_existingExpenses)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |expenses(com.example.expensetracker.data.Expense).
              | Expected:
              |""".trimMargin() + _infoExpenses + """
              |
              | Found:
              |""".trimMargin() + _existingExpenses)
        }
        val _columnsPeerDebts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPeerDebts.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeerDebts.put("peerName", TableInfo.Column("peerName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeerDebts.put("amountMinor", TableInfo.Column("amountMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeerDebts.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeerDebts.put("isOwedToMe", TableInfo.Column("isOwedToMe", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeerDebts.put("isSettled", TableInfo.Column("isSettled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPeerDebts.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPeerDebts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPeerDebts: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPeerDebts: TableInfo = TableInfo("peer_debts", _columnsPeerDebts, _foreignKeysPeerDebts, _indicesPeerDebts)
        val _existingPeerDebts: TableInfo = read(connection, "peer_debts")
        if (!_infoPeerDebts.equals(_existingPeerDebts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |peer_debts(com.example.expensetracker.data.PeerDebt).
              | Expected:
              |""".trimMargin() + _infoPeerDebts + """
              |
              | Found:
              |""".trimMargin() + _existingPeerDebts)
        }
        val _columnsChamaGoals: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChamaGoals.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChamaGoals.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChamaGoals.put("targetAmountMinor", TableInfo.Column("targetAmountMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChamaGoals.put("currentSavedMinor", TableInfo.Column("currentSavedMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChamaGoals.put("deadline", TableInfo.Column("deadline", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChamaGoals: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesChamaGoals: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoChamaGoals: TableInfo = TableInfo("chama_goals", _columnsChamaGoals, _foreignKeysChamaGoals, _indicesChamaGoals)
        val _existingChamaGoals: TableInfo = read(connection, "chama_goals")
        if (!_infoChamaGoals.equals(_existingChamaGoals)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |chama_goals(com.example.expensetracker.data.ChamaGoal).
              | Expected:
              |""".trimMargin() + _infoChamaGoals + """
              |
              | Found:
              |""".trimMargin() + _existingChamaGoals)
        }
        val _columnsMobileLoans: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMobileLoans.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMobileLoans.put("provider", TableInfo.Column("provider", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMobileLoans.put("principalMinor", TableInfo.Column("principalMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMobileLoans.put("dailyInterestRatePercent", TableInfo.Column("dailyInterestRatePercent", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMobileLoans.put("dateTakenMillis", TableInfo.Column("dateTakenMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMobileLoans.put("isRepaid", TableInfo.Column("isRepaid", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMobileLoans: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMobileLoans: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMobileLoans: TableInfo = TableInfo("mobile_loans", _columnsMobileLoans, _foreignKeysMobileLoans, _indicesMobileLoans)
        val _existingMobileLoans: TableInfo = read(connection, "mobile_loans")
        if (!_infoMobileLoans.equals(_existingMobileLoans)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |mobile_loans(com.example.expensetracker.data.MobileLoan).
              | Expected:
              |""".trimMargin() + _infoMobileLoans + """
              |
              | Found:
              |""".trimMargin() + _existingMobileLoans)
        }
        val _columnsSideHustleTransactions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSideHustleTransactions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSideHustleTransactions.put("businessName", TableInfo.Column("businessName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSideHustleTransactions.put("isIncome", TableInfo.Column("isIncome", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSideHustleTransactions.put("amountMinor", TableInfo.Column("amountMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSideHustleTransactions.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSideHustleTransactions.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSideHustleTransactions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSideHustleTransactions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSideHustleTransactions: TableInfo = TableInfo("side_hustle_transactions", _columnsSideHustleTransactions, _foreignKeysSideHustleTransactions, _indicesSideHustleTransactions)
        val _existingSideHustleTransactions: TableInfo = read(connection, "side_hustle_transactions")
        if (!_infoSideHustleTransactions.equals(_existingSideHustleTransactions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |side_hustle_transactions(com.example.expensetracker.data.SideHustleTransaction).
              | Expected:
              |""".trimMargin() + _infoSideHustleTransactions + """
              |
              | Found:
              |""".trimMargin() + _existingSideHustleTransactions)
        }
        val _columnsRecurringBills: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsRecurringBills.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("amountMinor", TableInfo.Column("amountMinor", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("dueDayOfMonth", TableInfo.Column("dueDayOfMonth", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsRecurringBills.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysRecurringBills: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesRecurringBills: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoRecurringBills: TableInfo = TableInfo("recurring_bills", _columnsRecurringBills, _foreignKeysRecurringBills, _indicesRecurringBills)
        val _existingRecurringBills: TableInfo = read(connection, "recurring_bills")
        if (!_infoRecurringBills.equals(_existingRecurringBills)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |recurring_bills(com.example.expensetracker.data.RecurringBill).
              | Expected:
              |""".trimMargin() + _infoRecurringBills + """
              |
              | Found:
              |""".trimMargin() + _existingRecurringBills)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "expenses", "peer_debts", "chama_goals", "mobile_loans", "side_hustle_transactions", "recurring_bills")
  }

  public override fun clearAllTables() {
    super.performClear(false, "expenses", "peer_debts", "chama_goals", "mobile_loans", "side_hustle_transactions", "recurring_bills")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ExpenseDao::class, ExpenseDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PeerDebtDao::class, PeerDebtDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ChamaGoalDao::class, ChamaGoalDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MobileLoanDao::class, MobileLoanDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SideHustleDao::class, SideHustleDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(RecurringBillDao::class, RecurringBillDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun expenseDao(): ExpenseDao = _expenseDao.value

  public override fun peerDebtDao(): PeerDebtDao = _peerDebtDao.value

  public override fun chamaGoalDao(): ChamaGoalDao = _chamaGoalDao.value

  public override fun mobileLoanDao(): MobileLoanDao = _mobileLoanDao.value

  public override fun sideHustleDao(): SideHustleDao = _sideHustleDao.value

  public override fun recurringBillDao(): RecurringBillDao = _recurringBillDao.value
}
