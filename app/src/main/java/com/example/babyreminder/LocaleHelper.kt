package com.example.babyreminder

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    // Java Locale converts "he" to "iw" internally. We normalize to "he" for consistency.
    private fun normalizeLanguageCode(code: String?): String? {
        return when (code) {
            "iw" -> "he"
            else -> code
        }
    }

    // Apply the saved locale to the context
    fun onAttach(context: Context): Context {
        val lang = normalizeLanguageCode(getPersistedData(context, Locale.getDefault().language))
        return updateResources(context, lang)
    }

    // Get the currently saved language code
    fun getLanguage(context: Context): String? {
        return normalizeLanguageCode(getPersistedData(context, Locale.getDefault().language))
    }

    // Set the new locale and persist it
    fun setLocale(context: Context, language: String?): Context {
        val lang = normalizeLanguageCode(language)
        persist(context, lang)

        // Use AppCompatDelegate for API 33+ per-app language support
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeList = LocaleListCompat.forLanguageTags(lang ?: "en")
            AppCompatDelegate.setApplicationLocales(localeList)
        }

        return updateResources(context, lang)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String? {
        val preferences = getPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage)
    }

    private fun persist(context: Context, language: String?) {
        val preferences = getPreferences(context)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    // Update the app's resources to reflect the new language
    private fun updateResources(context: Context, language: String?): Context {
        val langCode = language ?: Locale.getDefault().language
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("Locale.Helper.Prefs", Context.MODE_PRIVATE)
    }
}
