package com.example.expensetracker.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.expensetracker.data.Category

// Primary Brand Colors (Kenyan Mobile Money & Fintech Inspired)
val PesaGreen = Color(0xFF00A86B)
val PesaGreenDark = Color(0xFF007A4D)
val PesaGreenLight = Color(0xFFE6F7F0)
val PesaTeal = Color(0xFF0EA5E9)

val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

// Financial Semantic Colors
val MoneyIncome = Color(0xFF10B981)
val MoneyIncomeBg = Color(0xFFECFDF5)
val MoneyExpense = Color(0xFFEF4444)
val MoneyExpenseBg = Color(0xFFFEF2F2)
val MoneyWarning = Color(0xFFF59E0B)
val MoneyWarningBg = Color(0xFFFFFBEB)
val ChamaViolet = Color(0xFF8B5CF6)
val ChamaVioletBg = Color(0xFFF5F3FF)

// Category visual mapping: Icon + Tint Color
fun getCategoryColor(category: Category): Color = when (category) {
    Category.FOOD -> Color(0xFFF97316)
    Category.TRANSPORT -> Color(0xFF0284C7)
    Category.AIRTIME_DATA -> Color(0xFF8B5CF6)
    Category.RENT_UTILITIES -> Color(0xFFD97706)
    Category.SHOPPING -> Color(0xFFEC4899)
    Category.HEALTH -> Color(0xFF0D9488)
    Category.EDUCATION -> Color(0xFF4F46E5)
    Category.ENTERTAINMENT -> Color(0xFFA855F7)
    Category.PEER_DEBTS -> Color(0xFF06B6D4)
    Category.HELB_INCOME -> Color(0xFF10B981)
    Category.FEES -> Color(0xFF64748B)
    Category.OTHER -> Color(0xFF6B7280)
}

fun getCategoryIcon(category: Category): String = when (category) {
    Category.FOOD -> "🍔"
    Category.TRANSPORT -> "🚌"
    Category.AIRTIME_DATA -> "📶"
    Category.RENT_UTILITIES -> "💡"
    Category.SHOPPING -> "🛍️"
    Category.HEALTH -> "💊"
    Category.EDUCATION -> "🎓"
    Category.ENTERTAINMENT -> "🎬"
    Category.PEER_DEBTS -> "🤝"
    Category.HELB_INCOME -> "💰"
    Category.FEES -> "🧾"
    Category.OTHER -> "📦"
}
