package xyz.axie.portmapper.i18n

import java.util.Locale

enum class Language(
    val code: String,
    val locale: Locale
) {
    ENGLISH("en", Locale.ENGLISH),
    RUSSIAN("ru", Locale.forLanguageTag("ru"));

    companion object {
        fun systemLanguage(): Language =
            if (Locale.getDefault().language == "ru") {
                RUSSIAN
            } else {
                ENGLISH
            }
    }
}