package ca.gbc.assignment1

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Views we need to use after onCreate(). "lateinit" means they are assigned in onCreate().
    private lateinit var editHours: EditText
    private lateinit var editRate: EditText
    private lateinit var editTax: EditText
    private lateinit var textPay: TextView
    private lateinit var textOvertimePay: TextView
    private lateinit var textTotalPay: TextView
    private lateinit var textTax: TextView

    // onCreate() runs once when the activity is first created: load the layout and find the views.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarPadding(findViewById(R.id.main))

        // findViewById() connects each Kotlin variable to the view with that id in activity_main.xml
        editHours = findViewById(R.id.editHours)
        editRate = findViewById(R.id.editRate)
        editTax = findViewById(R.id.editTax)
        textPay = findViewById(R.id.textPay)
        textOvertimePay = findViewById(R.id.textOvertimePay)
        textTotalPay = findViewById(R.id.textTotalPay)
        textTax = findViewById(R.id.textTax)

        // When the Calculate button is tapped, run calculatePay()
        val buttonCalculate: Button = findViewById(R.id.buttonCalculate)
        buttonCalculate.setOnClickListener { calculatePay() }
    }

    private fun calculatePay() {
        // 1. Read and validate the three inputs. readNumber() returns null if the input is bad
        //    (and shows an error on that field), so we stop here instead of crashing.
        val hours = readNumber(editHours) ?: return
        val hourlyRate = readNumber(editRate) ?: return
        val taxPercent = readNumber(editTax) ?: return

        // Extra rule for tax: it is a percentage, so it must be between 0 and 100.
        if (taxPercent > 100) {
            editTax.error = getString(R.string.error_tax_range)
            editTax.requestFocus()
            return
        }

        // The user types a percentage like 13, but the formula needs 0.13
        val taxRate = taxPercent / 100

        // 2. Pay logic, exactly as the assignment states.
        val pay: Double
        val overtimePay: Double
        if (hours <= 40) {
            // No overtime: every hour is paid at the normal rate
            pay = hours * hourlyRate
            overtimePay = 0.0
        } else {
            // First 40 hours at the normal rate...
            pay = 40 * hourlyRate
            // ...every hour above 40 is paid at 1.5x the normal rate ("time and a half")
            overtimePay = (hours - 40) * hourlyRate * 1.5
        }
        val totalPay = pay + overtimePay
        // Tax is calculated on the regular pay, as the assignment specifies
        val tax = pay * taxRate

        // 3. Show the results. getString() fills the %1$.2f placeholder in strings.xml,
        //    which formats the number with exactly 2 decimals (e.g. "Pay: $400.00").
        textPay.text = getString(R.string.result_pay, pay)
        textOvertimePay.text = getString(R.string.result_overtime_pay, overtimePay)
        textTotalPay.text = getString(R.string.result_total_pay, totalPay)
        textTax.text = getString(R.string.result_tax, tax)
    }

    // Reads the text from an EditText and converts it to a Double.
    // Returns null (and shows an error on the field via setError) if it is empty, not a number, or negative.
    private fun readNumber(editText: EditText): Double? {
        val text = editText.text.toString().trim()

        if (text.isEmpty()) {
            editText.error = getString(R.string.error_required)
            editText.requestFocus()
            return null
        }

        // toDoubleOrNull() returns null instead of throwing an exception for input like "." or "abc"
        val number = text.toDoubleOrNull()
        if (number == null) {
            editText.error = getString(R.string.error_invalid_number)
            editText.requestFocus()
            return null
        }

        if (number < 0) {
            editText.error = getString(R.string.error_negative)
            editText.requestFocus()
            return null
        }

        return number
    }

    // MENU, step 1: Android calls this to build the ActionBar menu.
    // menuInflater turns res/menu/main_menu.xml into real menu items. Returning true shows the menu.
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // MENU, step 2: Android calls this when a menu item is tapped. We check which item it was by its id.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                // An explicit Intent opens AboutActivity on top of this one.
                // MainActivity stays in the back stack underneath, so we can return to it later.
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
