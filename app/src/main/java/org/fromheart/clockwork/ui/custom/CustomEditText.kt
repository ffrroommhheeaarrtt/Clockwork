package org.fromheart.clockwork.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class CustomEditText(context: Context, attrs: AttributeSet) : TextInputEditText(context, attrs) {

    private var click = {}

    fun setOnBackPressListener(click: () -> Unit) {
        this.click = click
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP) {
            if (ViewCompat.getRootWindowInsets(rootView)?.isVisible(WindowInsetsCompat.Type.ime()) == true) {
                click()
            }
            return false
        }
        return super.dispatchKeyEvent(event)
    }
}