package com.nphstudio.mooveeon.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

object TranslationHelper {
    private var translations: Map<String, String> = emptyMap()
    private var currentLanguage: String = ""

    fun loadTranslations(context: Context, languageCode: String) {
        if (currentLanguage == languageCode && translations.isNotEmpty()) return

        try {
            val fileName = "locales/$languageCode.json"
            val inputStream = context.assets.open(fileName)
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<Map<String, String>>() {}.type
            translations = Gson().fromJson(reader, type)
            currentLanguage = languageCode
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to English if file not found
            if (languageCode != "en") {
                loadTranslations(context, "en")
            }
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return translations[key] ?: defaultValue
    }
}
