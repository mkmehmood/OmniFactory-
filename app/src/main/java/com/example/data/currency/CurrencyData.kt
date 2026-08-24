package com.example.data.currency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flagEmoji: String = "",
    val country: String = "",
    val region: String = "All"
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
    CurrencyInfo("USD", "United States Dollar", "$", "", "United States / America", "Americas"),
    CurrencyInfo("EUR", "Euro", "€", "", "European Union / Germany / France / Italy / Spain", "Europe"),
    CurrencyInfo("GBP", "British Pound Sterling", "£", "", "United Kingdom / Britain / England", "Europe"),
    CurrencyInfo("JPY", "Japanese Yen", "¥", "", "Japan", "Asia"),
    CurrencyInfo("CAD", "Canadian Dollar", "CA$", "", "Canada", "Americas"),
    CurrencyInfo("AUD", "Australian Dollar", "A$", "", "Australia", "Asia"),
    CurrencyInfo("CHF", "Swiss Franc", "CHF", "", "Switzerland", "Europe"),
    CurrencyInfo("CNY", "Chinese Yuan Renminbi", "¥", "", "China", "Asia"),
    CurrencyInfo("INR", "Indian Rupee", "₹", "", "India", "Asia"),
    CurrencyInfo("PKR", "Pakistani Rupee", "Rs", "", "Pakistan", "Asia"),
    CurrencyInfo("AED", "United Arab Emirates Dirham", "AED", "", "United Arab Emirates / Dubai", "Middle East"),
    CurrencyInfo("SAR", "Saudi Riyal", "SAR", "", "Saudi Arabia", "Middle East"),
    CurrencyInfo("SGD", "Singapore Dollar", "S$", "", "Singapore", "Asia"),
    CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", "", "New Zealand", "Asia"),
    CurrencyInfo("BRL", "Brazilian Real", "R$", "", "Brazil", "Americas"),
    CurrencyInfo("ZAR", "South African Rand", "R", "", "South Africa", "Africa"),
    CurrencyInfo("TRY", "Turkish Lira", "₺", "", "Turkey", "Europe"),
    CurrencyInfo("MXN", "Mexican Peso", "Mex$", "", "Mexico", "Americas"),
    CurrencyInfo("KRW", "South Korean Won", "₩", "", "South Korea", "Asia"),
    CurrencyInfo("SEK", "Swedish Krona", "kr", "", "Sweden", "Europe"),
    CurrencyInfo("NOK", "Norwegian Krone", "kr", "", "Norway", "Europe"),
    CurrencyInfo("DKK", "Danish Krone", "kr", "", "Denmark", "Europe"),
    CurrencyInfo("PLN", "Polish Zloty", "zł", "", "Poland", "Europe"),
    CurrencyInfo("THB", "Thai Baht", "฿", "", "Thailand", "Asia"),
    CurrencyInfo("IDR", "Indonesian Rupiah", "Rp", "", "Indonesia", "Asia"),
    CurrencyInfo("MYR", "Malaysian Ringgit", "RM", "", "Malaysia", "Asia"),
    CurrencyInfo("PHP", "Philippine Peso", "₱", "", "Philippines", "Asia"),
    CurrencyInfo("HKD", "Hong Kong Dollar", "HK$", "", "Hong Kong", "Asia"),
    CurrencyInfo("TWD", "New Taiwan Dollar", "NT$", "", "Taiwan", "Asia"),
    CurrencyInfo("ILS", "Israeli New Shekel", "₪", "", "Israel", "Middle East"),
    CurrencyInfo("EGP", "Egyptian Pound", "E£", "", "Egypt", "Africa"),
    CurrencyInfo("KWD", "Kuwaiti Dinar", "KD", "", "Kuwait", "Middle East"),
    CurrencyInfo("QAR", "Qatari Riyal", "QR", "", "Qatar", "Middle East"),
    CurrencyInfo("BHD", "Bahraini Dinar", "BD", "", "Bahrain", "Middle East"),
    CurrencyInfo("OMR", "Omani Rial", "OMR", "", "Oman", "Middle East"),
    CurrencyInfo("NGN", "Nigerian Naira", "₦", "", "Nigeria", "Africa"),
    CurrencyInfo("KES", "Kenyan Shilling", "KSh", "", "Kenya", "Africa"),
    CurrencyInfo("BDT", "Bangladeshi Taka", "৳", "", "Bangladesh", "Asia"),
    CurrencyInfo("VND", "Vietnamese Dong", "₫", "", "Vietnam", "Asia")
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
