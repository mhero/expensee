package com.mac.expensee.core.common.money

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

/**
 * Monetary amount represented as an integer number of minor units (e.g. cents) plus a currency.
 * Float/Double are never used for money: binary floating point cannot represent amounts like
 * 0.10 exactly, which silently corrupts totals after enough additions.
 */
data class Money(
    val amountMinorUnits: Long,
    val currency: CurrencyCode,
) : Comparable<Money> {

    init {
        require(amountMinorUnits >= 0) { "Money amounts are stored unsigned; sign is contextual (expense vs refund)." }
    }

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amountMinorUnits = amountMinorUnits + other.amountMinorUnits)
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amountMinorUnits = amountMinorUnits - other.amountMinorUnits)
    }

    override fun compareTo(other: Money): Int {
        requireSameCurrency(other)
        return amountMinorUnits.compareTo(other.amountMinorUnits)
    }

    /** Human-readable major-unit value, e.g. 1050 minor units of USD -> 10.50 */
    fun toMajorUnits(): BigDecimal {
        val divisor = BigDecimal.TEN.pow(currency.minorUnitDigits)
        return BigDecimal(amountMinorUnits).divide(divisor).setScale(currency.minorUnitDigits, RoundingMode.HALF_UP)
    }

    fun formatted(): String = "${currency.symbol}${toMajorUnits()}"

    private fun requireSameCurrency(other: Money) {
        require(currency == other.currency) {
            "Cannot combine amounts in different currencies (${currency.isoCode} vs ${other.currency.isoCode}) " +
                "without an explicit conversion step."
        }
    }

    companion object {
        val ZERO_USD = Money(0, CurrencyCode.USD)

        fun zero(currency: CurrencyCode) = Money(0, currency)

        /** Parses a user-entered decimal string (e.g. "12.50") into minor units for [currency]. */
        fun fromMajorUnits(major: BigDecimal, currency: CurrencyCode): Money {
            val multiplier = 10.0.pow(currency.minorUnitDigits).toLong()
            val minor = major.setScale(currency.minorUnitDigits, RoundingMode.HALF_UP)
                .movePointRight(currency.minorUnitDigits)
                .longValueExact()
            require(multiplier > 0)
            return Money(minor, currency)
        }

        fun sum(amounts: Collection<Money>, currency: CurrencyCode): Money =
            amounts.fold(zero(currency)) { acc, money -> acc + money }
    }
}
