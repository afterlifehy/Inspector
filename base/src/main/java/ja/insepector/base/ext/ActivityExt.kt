package ja.insepector.base.ext

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils


fun Activity.i18N(id: Int): String {
    return this.resources.getString(id)
}

fun Activity.isEmpty(value: String): Boolean {
    return TextUtils.isEmpty(value)
}

inline fun <reified T : Activity> Context.startAct(data: Bundle? = null) {
    val intent = Intent(this, T::class.java)
    if (data != null) {
        intent.putExtras(data)
    }

    this.startActivity(intent)
}
