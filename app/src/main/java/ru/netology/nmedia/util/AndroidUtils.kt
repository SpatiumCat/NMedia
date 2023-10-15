package ru.netology.nmedia.util

import android.content.Context
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import ru.netology.nmedia.DateSeparator
import ru.netology.nmedia.Post
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
