package com.example.expensetracker.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class FinancialContext(
    val totalSpentMinor: Long = 0L,
    val totalIncomeMinor: Long = 0L,
    val netBalanceMinor: Long = 0L,
    val dailyBurnMinor: Long = 0L,
    val safeDailyLimitMinor: Long = 0L,
    val daysOfRunwayRemaining: Int = 0,
    val burnTrend: String = "STABLE",
    val topCategories: List<Pair<String, Long>> = emptyList(),
    val unpaidLoans: List<Pair<String, Long>> = emptyList(),
    val chamaGoals: List<Pair<String, Long>> = emptyList(),
    val anomaliesCount: Int = 0,
    val semesterDaysRemaining: Int = 0,
    val recommendedDailyBudgetMinor: Long = 0L
)

object GeminiFinancialAdvisor {

    suspend fun getFinancialAdvice(
        apiKey: String?,
        context: FinancialContext,
        query: String? = null
    ): String = withContext(Dispatchers.IO) {
        if (!apiKey.isNullOrBlank()) {
            try {
                val apiResponse = callGeminiApi(apiKey.trim(), buildPrompt(context, query))
                if (apiResponse.isNotBlank()) {
                    return@withContext apiResponse
                }
            } catch (_: Exception) {
                // Fall back to offline rule-based advisory engine
            }
        }
        generateOfflineAdvice(context, query)
    }

    private fun callGeminiApi(apiKey: String, promptText: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000

        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
        }

        OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
            writer.write(requestBody.toString())
            writer.flush()
        }

        if (conn.responseCode in 200..299) {
            val responseStr = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { it.readText() }
            val json = JSONObject(responseStr)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val parts = candidate.optJSONObject("content")?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        return ""
    }

    private fun buildPrompt(context: FinancialContext, query: String?): String {
        val sb = StringBuilder()
        sb.append("You are PesaPouch Financial Advisor, a smart AI assistant for Kenyan university students and young adults.\n")
        sb.append("Here is the user's current financial context:\n")
        sb.append("- Total Spent: KES %.2f\n".format(context.totalSpentMinor / 100.0))
        sb.append("- Total Income: KES %.2f\n".format(context.totalIncomeMinor / 100.0))
        sb.append("- Net Balance: KES %.2f\n".format(context.netBalanceMinor / 100.0))
        sb.append("- Daily Burn Rate: KES %.2f\n".format(context.dailyBurnMinor / 100.0))
        sb.append("- Safe Daily Limit: KES %.2f\n".format(context.safeDailyLimitMinor / 100.0))
        sb.append("- Runway Remaining: ${context.daysOfRunwayRemaining} days\n")
        sb.append("- Burn Trend: ${context.burnTrend}\n")
        sb.append("- Semester Days Remaining: ${context.semesterDaysRemaining}\n")
        sb.append("- Recommended Daily Budget: KES %.2f\n".format(context.recommendedDailyBudgetMinor / 100.0))

        if (context.unpaidLoans.isNotEmpty()) {
            sb.append("- Unpaid Loans: ")
            sb.append(context.unpaidLoans.joinToString { "${it.first}: KES %.2f".format(it.second / 100.0) })
            sb.append("\n")
        }

        if (context.chamaGoals.isNotEmpty()) {
            sb.append("- Chama Savings Goals: ")
            sb.append(context.chamaGoals.joinToString { "${it.first}: KES %.2f".format(it.second / 100.0) })
            sb.append("\n")
        }

        if (context.topCategories.isNotEmpty()) {
            sb.append("- Top Spending Categories: ")
            sb.append(context.topCategories.joinToString { "${it.first}: KES %.2f".format(it.second / 100.0) })
            sb.append("\n")
        }

        if (context.anomaliesCount > 0) {
            sb.append("- Unexplained Spending Anomalies: ${context.anomaliesCount}\n")
        }

        if (!query.isNullOrBlank()) {
            sb.append("\nUser Query: $query\n")
            sb.append("Please provide tailored, actionable advice addressing the query and financial context.")
        } else {
            sb.append("\nPlease provide a comprehensive financial audit with actionable tips.")
        }

        return sb.toString()
    }

    private fun generateOfflineAdvice(context: FinancialContext, query: String?): String {
        val sb = StringBuilder()

        if (!query.isNullOrBlank()) {
            sb.append("🤖 **PesaPouch AI Coach Advice**\n\n")
            sb.append("Regarding: \"$query\"\n\n")
        } else {
            sb.append("📊 **PesaPouch Smart Financial Audit**\n\n")
        }

        // Status overview
        if (context.daysOfRunwayRemaining in 1..14) {
            sb.append("⚠️ **Runway Alert**: You have only **${context.daysOfRunwayRemaining} days** of runway left at your current daily burn (KES %.2f/day).\n\n".format(context.dailyBurnMinor / 100.0))
        } else if (context.daysOfRunwayRemaining > 14) {
            sb.append("✅ **Runway Health**: You have **${context.daysOfRunwayRemaining} days** of estimated runway remaining.\n\n")
        }

        // Loan Warning
        if (context.unpaidLoans.isNotEmpty()) {
            val loanDetails = context.unpaidLoans.joinToString { "${it.first} (KES %.2f)".format(it.second / 100.0) }
            sb.append("🚨 **High Interest Debt Priority**: You have active unpaid loan(s): $loanDetails. Clear mobile loans immediately to avoid daily compounding interest!\n\n")
        }

        // Spending / Budgeting Tip
        if (context.dailyBurnMinor > context.safeDailyLimitMinor && context.safeDailyLimitMinor > 0) {
            val overage = (context.dailyBurnMinor - context.safeDailyLimitMinor) / 100.0
            sb.append("💡 **Daily Limit**: Your daily burn is **KES %.2f** above your recommended limit (KES %.2f/day). Try trimming discretionary expenses.\n\n".format(overage, context.safeDailyLimitMinor / 100.0))
        } else {
            sb.append("💡 **Budget Tip**: Your recommended daily spending cap is **KES %.2f** for the remaining ${context.semesterDaysRemaining} days of the semester.\n\n".format(context.recommendedDailyBudgetMinor / 100.0))
        }

        // Chama Goals
        if (context.chamaGoals.isNotEmpty()) {
            val chamaSummary = context.chamaGoals.joinToString { "${it.first} (Saved: KES %.2f)".format(it.second / 100.0) }
            sb.append("🤝 **Chama Goals**: Great job contributing to your Chama targets: $chamaSummary.\n\n")
        }

        sb.append("ℹ️ *Tip: Set your Gemini API key in Settings for AI-powered personalized responses.*")

        return sb.toString()
    }
}
