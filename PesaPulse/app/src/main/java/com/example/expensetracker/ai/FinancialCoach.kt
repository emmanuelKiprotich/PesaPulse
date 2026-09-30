package com.example.expensetracker.ai

object FinancialCoach {
    val tips = listOf(
        "💡 Semester Budget Pacing Tip: Divide your lump-sum allowance across the semester months to survive until exam week.",
        "⚠️ Fuliza Trap: Fuliza daily interest (~0.5%) adds up to ~180% per annum! Clear mobile loans immediately before spending on non-essentials.",
        "🤝 Chama Savings: Form a small 'chama' with 3 trusted classmates to pool KES 500 weekly for emergency hostel rent or semester books.",
        "💼 Side Hustle Rule: Never mix personal upkeep money with your campus business capital. Reinvest at least 30% of your side-hustle profits.",
        "📊 Budgeting Rule: Follow the 50/30/20 rule adjusted for campus life: 50% Essentials (Food/Rent), 30% Wants, 20% Savings & Emergency Fund.",
    )

    @Suppress("unused")
    suspend fun getAdvice(apiKey: String?, context: FinancialContext, query: String? = null): String {
        return GeminiFinancialAdvisor.getFinancialAdvice(apiKey, context, query)
    }
}
