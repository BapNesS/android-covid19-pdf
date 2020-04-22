package fr.useless.depdero.core.tool

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber
import java.io.File

/**
 * Extension method to show toast for Context.
 */
fun Context?.toast(@StringRes textId: Int, duration: Int = Toast.LENGTH_LONG) = this?.let { Toast.makeText(it, textId, duration).show() }

/**
 * Transforms static java function Snackbar.make() to an extension function on View.
 */
fun View.showSnackbar(@StringRes textId: Int, timeLength: Int = BaseTransientBottomBar.LENGTH_LONG, @StringRes actionTextId: Int? = null) {
    val bar = Snackbar.make(this, textId, timeLength)
    if ( actionTextId != null ) {
        bar.setAction( actionTextId ) {
            bar.dismiss()
        }
    }
    bar.show()
}



fun TextInputLayout.quickToString() : String { return this.editText?.text.toString() }
fun TextInputLayout.quickSet(newValue: String) { this.editText?.setText( newValue ) }


fun Context.rateApp() {
    try {
        val rateIntent = rateIntentForUrl("market://details")
        startActivity(rateIntent)
    } catch (e: ActivityNotFoundException) {
        val rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details")
        startActivity(rateIntent)
    }
}

private fun Context.rateIntentForUrl(url: String): Intent {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(java.lang.String.format("%s?id=%s", url, packageName))
    )
    var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    flags = flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
    intent.addFlags(flags)
    return intent
}

/**
 * Extension method to share for Context.
 */
fun Context.share(text: String, subject: String = ""): Boolean {
    val intent = Intent()
    intent.type = "text/plain"
    intent.putExtra(EXTRA_SUBJECT, subject)
    intent.putExtra(EXTRA_TEXT, text)
    try {
        startActivity(createChooser(intent, null))
        return true
    } catch (e: ActivityNotFoundException) {
        return false
    }
}



fun Context.getShowPdfFileIntent(path: String) : Intent {
    val intent = Intent(ACTION_VIEW)

    val fileName = path.split("/").last()


    val pdfFile = File(filesDir, fileName)
    Timber.d(pdfFile.absolutePath)
    val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".pdfprovider", pdfFile)

    intent.data = uri
    //intent.type = "application/pdf"
    intent.addFlags(FLAG_ACTIVITY_NO_HISTORY)
    intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION)
    return intent
}

/**
 * Extension method to provide hide keyboard for [Fragment].
 */
fun Fragment.hideSoftKeyboard() {
    activity?.hideSoftKeyboard()
}

fun Activity.hideSoftKeyboard() {
    if (currentFocus != null) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }
}



fun Array<TextInputLayout>.noNullOrEmpty(): Boolean {
    return this.filter {
        it.editText?.text.isNullOrEmpty()
    }.count() == 0
}