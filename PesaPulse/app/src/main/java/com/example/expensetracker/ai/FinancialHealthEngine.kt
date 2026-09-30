package com.example.expensetracker.ai

import com.example.expensetracker.data.Category
import com.example.expensetracker.data.ChamaGoal
import com.example.expensetracker.data.Expense
import com.example.expensetracker.data.MobileLoan

data class HealthPillar(
    val name: String,
    val score: Int,
    val maxScore: Int,
    val summary: String,
)

data class FinancialHealthReport(
    val totalScore: Int,               // 0..100
    val grade: String,                  // "A+", "A", "B", "C", "D", "F"
    val status: String,                 // "Optimal", "Good", "Needs Attention", "High Risk"
    val pillars: List<HealthPillar>,
    val dynamicAiRecommendation: String
)

/**
 * Multi-Criteria Algorithmic Financial Health Index & Expert Advisory Engine.
 *
 * Evaluates the user's comprehensive financial state across 5 core pillars:
 *  1. Savings & Chama Buffer (25 pts)
 *  2. High-Interest Debt Exposure (25 pts)
 *  3. Burn Rate Velocity vs Target (20 pts)
 *  4. Spending Discipline & Anomaly Frequency (15 pts)
 *  5. Essential vs Discretionary Ratio (15 pts)
 *
 * Produces an overall composite score (0-100), letter grade, and dynamic targeted advice.
 */
object FinancialHealthEngine {

    fun computeHealthScore(
        expenses: List<Expense>,
        chamaGoals: List<ChamaGoal>,
        mobileLoans: List<MobileLoan>,
        dailyBurnMinor: Long,
        safeDailyLimitMinor: Long,
        anomaliesCount: Int
    ): FinancialHealthReport {
        // 1. Savings & Chama Buffer (Max 25 pts)
        val totalSavedMinor = chamaGoals.sumOf { it.currentSavedMinor }
        val savingsScore = when {
            totalSavedMinor >= 10_000_00L -> 25
            totalSavedMinor >= 5_000_00L -> 20
            totalSavedMinor >= 2_000_00L -> 15
            totalSavedMinor > 0 -> 10
            else -> 4
        }
        val savingsPillar = HealthPillar(
            name = "Savings & Chama Buffer",
            score = savingsScore,
            maxScore = 25,
            summary = if (totalSavedMinor > 0) "Active savings in progress." else "No emergency or chama savings yet."
        )

        // 2. Mobile Debt Exposure (Max 25 pts)
        val unpaidLoans = mobileLoans.filter { !it.isRepaid }
        val totalLoanDebtMinor = unpaidLoans.sumOf { it.principalMinor }
        val debtScore = when {
            totalLoanDebtMinor == 0L -> 25
            totalLoanDebtMinor <= 1_500_00L -> 18
            totalLoanDebtMinor <= 5_000_00L -> 12
            totalLoanDebtMinor <= 10_000_00L -> 6
            else -> 0
        }
        val debtPillar = HealthPillar(
            name = "Debt Exposure (Fuliza/Loans)",
            score = debtScore,
            maxScore = 25,
            summary = if (unpaidLoans.isEmpty()) "Zero active mobile debt." else "${unpaidLoans.size} unpaid mobile loan(s) accumulating compound interest."
        )

        // 3. Burn Rate Velocity (Max 20 pts)
        val burnScore = when {
            safeDailyLimitMinor <= 0L -> 5
            dailyBurnMinor <= safeDailyLimitMinor -> 20
            dailyBurnMinor <= (safeDailyLimitMinor * 1.2).toLong() -> 15
            dailyBurnMinor <= (safeDailyLimitMinor * 1.5).toLong() -> 10
            else -> 4
        }
        val burnPillar = HealthPillar(
            name = "Burn Velocity vs Safe Pace",
            score = burnScore,
            maxScore = 20,
            summary = if (dailyBurnMinor <= safeDailyLimitMinor) "Pacing safely below daily limit." else "Burning funds faster than safe daily limit."
        )

        // 4. Spending Discipline & Anomalies (Max 15 pts)
        val totalTransactions = expenses.size
        val anomalyRatio = if (totalTransactions > 0) anomaliesCount.toFloat() / totalTransactions else 0f
        val disciplineScore = when {
            anomaliesCount == 0 -> 15
            anomalyRatio < 0.10f -> 12
            anomalyRatio < 0.20f -> 8
            else -> 4
        }
        val disciplinePillar = HealthPillar(
            name = "Discipline & Anomaly Rate",
            score = disciplineScore,
            maxScore = 15,
            summary = if (anomaliesCount == 0) "Zero spending spikes detected." else "$anomaliesCount spending spike(s) flagged."
        )

        // 5. Essential vs Discretionary Ratio (Max 15 pts)
        val nonIncome = expenses.filter { !it.isIncome }
        val essentials = nonIncome.filter {
            it.category in setOf(Category.FOOD, Category.RENT_UTILITIES, Category.EDUCATION, Category.HEALTH, Category.TRANSPORT)
        }.sumOf { it.amountMinor }
        val totalOutflow = nonIncome.sumOf { it.amountMinor }

        val essentialRatio = if (totalOutflow > 0) essentials.toFloat() / totalOutflow else 0.7f
        val budgetRatioScore = when {
            essentialRatio in 0.50f..0.85f -> 15 // Healthy 50-80% essentials
            essentialRatio > 0.85f -> 12        // Very tight, little discretionary
            essentialRatio in 0.35f..0.50f -> 9
            else -> 4                           // Overspending on wants/entertainment
        }
        val budgetPillar = HealthPillar(
            name = "Essentials vs Discretionary",
            score = budgetRatioScore,
            maxScore = 15,
            summary = "${(essentialRatio * 100).toInt()}% spent on essentials (food/rent/academics)."
        )

        val pillars = listOf(savingsPillar, debtPillar, burnPillar, disciplinePillar, budgetPillar)
        val totalScore = (savingsScore + debtScore + burnScore + disciplineScore + budgetRatioScore).coerceIn(0, 100)

        val (grade, status) = when {
            totalScore >= 90 -> "A+" to "Optimal Financial Health"
            totalScore >= 80 -> "A" to "Healthy & Balanced"
            totalScore >= 70 -> "B" to "Stable with Room to Grow"
            totalScore >= 55 -> "C" to "Moderate Risk / Action Needed"
            totalScore >= 40 -> "D" to "High Vulnerability"
            else -> "F" to "Critical Financial Distress"
        }

        // Generate tailored dynamic recommendation based on the lowest scoring pillar
        val weakest = pillars.minByOrNull { it.score.toFloat() / it.maxScore }
        val recommendation = when (weakest?.name) {
            "Debt Exposure (Fuliza/Loans)" ->
                "🚨 Priority Alert: Clear your mobile loans immediately! Daily compound interest will erode your semester upkeep."
            "Burn Velocity vs Safe Pace" ->
                "⚠️ Slow Down: Your daily spending is outpacing your safe allowance. Cap your daily spend to the target limit."
            "Savings & Chama Buffer" ->
                "💡 Build a Buffer: Put aside at least KES 200/week into your Chama goal to prevent falling back on emergency mobile loans."
            "Discipline & Anomaly Rate" ->
                "🔍 Review Outliers: You have irregular spending spikes. Check flagged transactions to identify non-essential leaks."
            "Essentials vs Discretionary" ->
                "🎯 Rebalance Budget: Discretionary expenses are taking a large share. Align closer to the campus 50/30/20 guideline."
            else -> "🌟 Keep maintaining your current budgeting discipline and tracking daily M-Pesa transactions."
        }

        return FinancialHealthReport(
            totalScore = totalScore,
            grade = grade,
            status = status,
            pillars = pillars,
            dynamicAiRecommendation = recommendation
        )
    }
}
