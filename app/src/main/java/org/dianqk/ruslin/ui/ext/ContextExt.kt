package org.dianqk.ruslin.ui.ext

import android.content.Context
import android.widget.Toast
import org.dianqk.ruslin.R


private var toast: Toast? = null

fun Context.showToast(message: String?, duration: Int = Toast.LENGTH_SHORT) {
    toast?.cancel()
    toast = Toast.makeText(this, message, duration)
    toast?.show()
}

fun Context.showComingSoon() {
    showToast(this.getString(R.string.coming_soon))
}
