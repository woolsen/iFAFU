package cn.ifafu.ifafu.util

import android.content.Intent

object IntentUtil {

    const val FONT_SIZE = "fontSize"
    const val PADDING = "padding"
    const val PHOTO = "photo"
    const val TEXT_COLOR = "color"
    const val EXAM = "exam"

    /**
     * Checks if all extras are present in an intent.
     *
     * @param intent The intent to check.
     * @param extras The extras to check for.
     * @return `true` if all extras are present, else `false`.
     */
    fun hasAll(intent: Intent, vararg extras: String): Boolean {
        for (extra in extras) {
            if (!intent.hasExtra(extra)) {
                return false
            }
        }
        return true
    }

    /**
     * Checks if any extra is present in an intent.
     *
     * @param intent The intent to check.
     * @param extras The extras to check for.
     * @return `true` if any checked extra is present, else `false`.
     */
    fun hasAny(intent: Intent, vararg extras: String): Boolean {
        for (extra in extras) {
            if (intent.hasExtra(extra)) {
                return true
            }
        }
        return false
    }
}