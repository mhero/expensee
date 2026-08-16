package com.mac.expensee.core.common.money

/**
 * Small, explicit set of supported ISO-4217 currencies. Kept as an enum (rather than accepting
 * any string) so invalid currency codes are impossible to construct in the domain layer.
 */
enum class CurrencyCode(val isoCode: String, val symbol: String, val minorUnitDigits: Int) {
    USD("USD", "$", 2),
    EUR("EUR", "\u20ac", 2),
    GBP("GBP", "\u00a3", 2),
    JPY("JPY", "\u00a5", 0),
    ;

    companion object {
        fun fromIsoCode(code: String): CurrencyCode =
            entries.firstOrNull { it.isoCode == code } ?: USD
    }
}
