package com.example.data.currency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class CurrencyInfo(
    val code: String,
    val name: String,
    val country: String,
    val symbol: String,
    val flagEmoji: String,
    val region: String = "Global"
)

data class CurrencyRateState(
    val baseCode: String = "USD",
    val rates: Map<String, Double> = defaultRates,
    val lastUpdated: String = "Live Fallback Rates",
    val isLoading: Boolean = false,
    val isLiveOnline: Boolean = false,
    val errorMessage: String? = null
)

val supportedCurrencies = listOf(
    CurrencyInfo("USD", "US Dollar", "United States", "$", "🇺🇸", "Americas"),
    CurrencyInfo("EUR", "Euro", "European Union / Germany / France", "€", "🇪🇺", "Europe"),
    CurrencyInfo("GBP", "Pound Sterling", "United Kingdom / Britain", "£", "🇬🇧", "Europe"),
    CurrencyInfo("JPY", "Japanese Yen", "Japan", "¥", "🇯🇵", "Asia"),
    CurrencyInfo("CAD", "Canadian Dollar", "Canada", "CA$", "🇨🇦", "Americas"),
    CurrencyInfo("AUD", "Australian Dollar", "Australia", "A$", "🇦🇺", "Asia"),
    CurrencyInfo("CHF", "Swiss Franc", "Switzerland", "CHF", "🇨🇭", "Europe"),
    CurrencyInfo("CNY", "Chinese Yuan", "China", "¥", "🇨🇳", "Asia"),
    CurrencyInfo("INR", "Indian Rupee", "India", "₹", "🇮🇳", "Asia"),
    CurrencyInfo("PKR", "Pakistani Rupee", "Pakistan", "Rs", "🇵🇰", "Asia"),
    CurrencyInfo("AED", "UAE Dirham", "United Arab Emirates / Dubai", "AED", "🇦🇪", "Middle East"),
    CurrencyInfo("SAR", "Saudi Riyal", "Saudi Arabia", "SAR", "🇸🇦", "Middle East"),
    CurrencyInfo("SGD", "Singapore Dollar", "Singapore", "S$", "🇸🇬", "Asia"),
    CurrencyInfo("NZD", "New Zealand Dollar", "New Zealand", "NZ$", "🇳🇿", "Asia"),
    CurrencyInfo("BRL", "Brazilian Real", "Brazil", "R$", "🇧🇷", "Americas"),
    CurrencyInfo("ZAR", "South African Rand", "South Africa", "R", "🇿🇦", "Africa"),
    CurrencyInfo("TRY", "Turkish Lira", "Turkey", "₺", "🇹🇷", "Middle East"),
    CurrencyInfo("MXN", "Mexican Peso", "Mexico", "Mex$", "🇲🇽", "Americas"),
    CurrencyInfo("KRW", "South Korean Won", "South Korea", "₩", "🇰🇷", "Asia"),
    CurrencyInfo("SEK", "Swedish Krona", "Sweden", "kr", "🇸🇪", "Europe"),
    CurrencyInfo("NOK", "Norwegian Krone", "Norway", "kr", "🇳🇴", "Europe"),
    CurrencyInfo("DKK", "Danish Krone", "Denmark", "kr", "🇩🇰", "Europe"),
    CurrencyInfo("PLN", "Polish Zloty", "Poland", "zł", "🇵🇱", "Europe"),
    CurrencyInfo("THB", "Thai Baht", "Thailand", "฿", "🇹🇭", "Asia"),
    CurrencyInfo("IDR", "Indonesian Rupiah", "Indonesia", "Rp", "🇮🇩", "Asia"),
    CurrencyInfo("MYR", "Malaysian Ringgit", "Malaysia", "RM", "🇲🇾", "Asia"),
    CurrencyInfo("PHP", "Philippine Peso", "Philippines", "₱", "🇵🇭", "Asia"),
    CurrencyInfo("HKD", "Hong Kong Dollar", "Hong Kong", "HK$", "🇭🇰", "Asia"),
    CurrencyInfo("TWD", "New Taiwan Dollar", "Taiwan", "NT$", "🇹🇼", "Asia"),
    CurrencyInfo("ILS", "Israeli Shekel", "Israel", "₪", "🇮🇱", "Middle East"),
    CurrencyInfo("EGP", "Egyptian Pound", "Egypt", "E£", "🇪🇬", "Africa"),
    CurrencyInfo("KWD", "Kuwaiti Dinar", "Kuwait", "KD", "🇰🇼", "Middle East"),
    CurrencyInfo("QAR", "Qatari Riyal", "Qatar", "QR", "🇶🇦", "Middle East"),
    CurrencyInfo("BHD", "Bahraini Dinar", "Bahrain", "BD", "🇧🇭", "Middle East"),
    CurrencyInfo("OMR", "Omani Rial", "Oman", "OMR", "🇴🇲", "Middle East"),
    CurrencyInfo("NGN", "Nigerian Naira", "Nigeria", "₦", "🇳🇬", "Africa"),
    CurrencyInfo("KES", "Kenyan Shilling", "Kenya", "KSh", "🇰🇪", "Africa"),
    CurrencyInfo("BDT", "Bangladeshi Taka", "Bangladesh", "৳", "🇧🇩", "Asia"),
    CurrencyInfo("VND", "Vietnamese Dong", "Vietnam", "₫", "🇻🇳", "Asia")
)

val defaultRates = mapOf(
    "USD" to 1.0,
    "EUR" to 0.924,
    "GBP" to 0.789,
    "JPY" to 154.65,
    "CAD" to 1.378,
    "AUD" to 1.528,
    "CHF" to 0.897,
    "CNY" to 7.245,
    "INR" to 83.42,
    "PKR" to 278.50,
    "AED" to 3.6725,
    "SAR" to 3.751,
    "SGD" to 1.348,
    "NZD" to 1.642,
    "BRL" to 5.421,
    "ZAR" to 18.25,
    "TRY" to 32.85,
    "MXN" to 18.15,
    "KRW" to 1380.0,
    "SEK" to 10.55,
    "NOK" to 10.62,
    "DKK" to 6.89,
    "PLN" to 3.98,
    "THB" to 36.75,
    "IDR" to 16250.0,
    "MYR" to 4.71,
    "PHP" to 58.60,
    "HKD" to 7.81,
    "TWD" to 32.45,
    "ILS" to 3.72,
    "EGP" to 47.90,
    "KWD" to 0.307,
    "QAR" to 3.64,
    "BHD" to 0.377,
    "OMR" to 0.385,
    "NGN" to 1485.0,
    "KES" to 129.50,
    "BDT" to 117.50,
    "VND" to 25420.0
)

class CurrencyRepository {

    suspend fun fetchLiveRates(baseCode: String = "USD"): Result<Pair<Map<String, Double>, String>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://open.er-api.com/v6/latest/$baseCode")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val ratesJson = json.getJSONObject("rates")
                val lastUpdate = json.optString("time_last_update_utc", "Recently updated")

                val ratesMap = mutableMapOf<String, Double>()
                val keys = ratesJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    ratesMap[key] = ratesJson.optDouble(key, 1.0)
                }

                Result.success(Pair(ratesMap, lastUpdate))
            } else {
                Result.failure(Exception("HTTP Error: ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
