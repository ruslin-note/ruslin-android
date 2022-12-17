package org.dianqk.ruslin.ui.ext

import android.content.Context
import androidx.core.os.ConfigurationCompat
import java.text.SimpleDateFormat
import java.util.*

fun Date.formatAsYmdHms(context: Context): String {
    val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
    return df.format(this)
}
