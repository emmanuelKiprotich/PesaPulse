package com.example.expensetracker.ai

import com.example.expensetracker.data.Expense
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.roundToLong

enum class BurnTrend {
    ACCELERATING,
    ON_TRACK,
    SURPLUS
}

data class RunwayForecast(
    val dailyBurnRateMinor: Long,         // Average spend per day in cents
    val targetSafeDailyLimitMinor: Long,  // Safe daily spend to survive the cycle
    val daysOfRunwayRemaining: Int,       // Estimated days until balance hits 0
    val predictedDepletionDate: Date?,    // Projected calendar date of 0 balance
    val trend: BurnTrend,
    val regressionSlope: Double,          // Linear slope (spending rate change per day)
    val rSquared: Double,                 // Fit confidence of linear regression model (0..1)
    val message: String,
)

/**
 * Predictive Machine Learning & Time-Series Regression Engine for Budget Runway Forecasting.
 *
 * Employs Ordinary Least Squares (OLS) Linear Regression:
 *   y = m * x + c
 * where x is time in days and y is daily cumulative spend.
 *
 * Forecasts the exact calendar depletion date when the student's liquid upkeep fund
 * will hit zero KES, and computes the required target burn rate to safely survive until semester or month end.
 */
object SpendingForecaster {

    /**
     * Forecasts runway from expense history, current available balance, and days remaining in the cycle.
     */
    fun forecastRunway(
        expenses: List<Expense>,
        availableBalanceMinor: Long,
        cycleDaysRemaining: Int = 30
    ): RunwayForecast {
        val nonIncomeExpenses = expenses.filter { !it.isIncome }
        if (nonIncomeExpenses.isEmpty() || availableBalanceMinor <= 0) {
            val safeDaily = if (cycleDaysRemaining > 0) availableBalanceMinor / cycleDaysRemaining else 0L
            return RunwayForecast(
                dailyBurnRateMinor = 0L,
                targetSafeDailyLimitMinor = safeDaily,
                daysOfRunwayRemaining = if (availableBalanceMinor > 0) 999 else 0,
                predictedDepletionDate = null,
                trend = BurnTrend.SURPLUS,
                regressionSlope = 0.0,
                rSquared = 1.0,
                message = "No expense data recorded yet. Spend safely within your daily budget."
            )
        }

        // Group expenses by calendar day offset
        val minTimestamp = nonIncomeExpenses.minOf { it.timestamp }
        val maxTimestamp = nonIncomeExpenses.maxOf { it.timestamp }
        val timespanDays = max(1, TimeUnit.MILLISECONDS.toDays(maxTimestamp - minTimestamp).toInt() + 1)

        val dailyTotals = nonIncomeExpenses.groupBy {
            TimeUnit.MILLISECONDS.toDays(it.timestamp - minTimestamp).toInt()
        }.mapValues { entry -> entry.value.sumOf { it.amountMinor } }

        val n = timespanDays
        val xValues = (0 until n).map { it.toDouble() }

        // Compute cumulative spend time series
        var cumulative = 0.0
        val yValues = xValues.map { day ->
            cumulative += (dailyTotals[day.toInt()] ?: 0L).toDouble()
            cumulative
        }

        // Ordinary Least Squares (OLS) Linear Trend: y = mx + c
        val meanX = xValues.average()
        val meanY = yValues.average()

        var numerator = 0.0
        var denomX = 0.0
        var denomY = 0.0

        for (i in 0 until n) {
            val xDiff = xValues[i] - meanX
            val yDiff = yValues[i] - meanY
            numerator += xDiff * yDiff
            denomX += xDiff * xDiff
            denomY += yDiff * yDiff
        }

        val slope = if (denomX > 0.0) numerator / denomX else (yValues.last() / n)
        val rSquared = if (denomX * denomY > 0.0) (numerator * numerator) / (denomX * denomY) else 0.5

        // Daily burn rate (minor units)
        val dailyBurnMinor = max(100L, slope.roundToLong())
        val targetSafeDailyMinor = if (cycleDaysRemaining > 0) {
            max(0L, availableBalanceMinor / cycleDaysRemaining)
        } else 0L

        // Forecast days until balance reaches 0: D_runway = Balance / Velocity
        val daysOfRunway = (availableBalanceMinor / dailyBurnMinor).toInt().coerceAtLeast(0)

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOfRunway)
        val depletionDate = cal.time

        val trend = when {
            daysOfRunway < cycleDaysRemaining -> BurnTrend.ACCELERATING
            daysOfRunway <= cycleDaysRemaining + 7 -> BurnTrend.ON_TRACK
            else -> BurnTrend.SURPLUS
        }

        val message = when (trend) {
            BurnTrend.ACCELERATING -> {
                val deficitDays = cycleDaysRemaining - daysOfRunway
                "⚠️ High burn velocity: Projected to run out $deficitDays days before cycle ends. Reduce daily spend to target."
            }
            BurnTrend.ON_TRACK -> "⚖️ Sustainable burn rate: Pacing closely aligns with your target cycle timeline."
            BurnTrend.SURPLUS -> "🛡️ Conservative spending: You have a comfortable buffer exceeding target cycle requirements."
        }

        return RunwayForecast(
            dailyBurnRateMinor = dailyBurnMinor,
            targetSafeDailyLimitMinor = targetSafeDailyMinor,
            daysOfRunwayRemaining = daysOfRunway,
            predictedDepletionDate = depletionDate,
            trend = trend,
            regressionSlope = slope,
            rSquared = rSquared.coerceIn(0.0, 1.0),
            message = message
        )
    }
}
