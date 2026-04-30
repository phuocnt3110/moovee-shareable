package com.nphstudio.mooveeon.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val IS_LANGUAGE_SELECTED = "Locale.Helper.Is.Language.Selected"

    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        TranslationHelper.loadTranslations(context, language)
        return updateResources(context, language)
    }

    fun isLanguageSelected(context: Context): Boolean {
        val preferences = context.getSharedPreferences(SELECTED_LANGUAGE, Context.MODE_PRIVATE)
        return preferences.getBoolean(IS_LANGUAGE_SELECTED, false)
    }

    fun onAttach(context: Context): Context {
        val lang = getPersistedData(context, Locale.getDefault().language)
        TranslationHelper.loadTranslations(context, lang)
        return updateResources(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context, Locale.getDefault().language)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        val preferences = context.getSharedPreferences(SELECTED_LANGUAGE, Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage) ?: defaultLanguage
    }

    private fun persist(context: Context, language: String) {
        val preferences = context.getSharedPreferences(SELECTED_LANGUAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.putBoolean(IS_LANGUAGE_SELECTED, true)
        editor.apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}