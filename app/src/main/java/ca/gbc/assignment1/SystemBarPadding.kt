package ca.gbc.assignment1

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// On Android 15+ apps are drawn "edge-to-edge": the screen content starts at the very top
// (behind the status bar and ActionBar) and goes to the very bottom (behind the navigation bar).
// This adds padding equal to those bars (and the keyboard) so none of our content is hidden.
fun applySystemBarPadding(view: View) {
    val left = view.paddingLeft
    val top = view.paddingTop
    val right = view.paddingRight
    val bottom = view.paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        v.setPadding(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom)
        insets
    }
}
