package ca.gbc.assignment1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        applySystemBarPadding(findViewById(R.id.about))

        // Show the back (Up) arrow on the left side of the ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Called when the Up arrow in the ActionBar is tapped.
    // finish() closes (destroys) this AboutActivity, so Android shows the activity underneath it
    // in the back stack: the SAME MainActivity that opened us, with its inputs and results still there.
    // We do NOT start a new Intent to MainActivity, because that would create a second MainActivity.
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
