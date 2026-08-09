package xyz.axie.portmapper.i18n

import java.util.ResourceBundle

class I18n(language: Language) {
    private val bundle = ResourceBundle.getBundle(
        "i18n.messages",
        language.locale
    )

    operator fun get(key: String): String =
        bundle.getString(key)
}