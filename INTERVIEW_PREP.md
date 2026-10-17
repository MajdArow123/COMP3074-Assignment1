# COMP3074 Assignment 1: Interview Prep

**Student:** Mjd Arow · **ID:** 101513153 · **Package:** `ca.gbc.assignment1`

The app has two screens:

- **MainActivity** is a pay calculator. You type in hours, an hourly rate and a tax %, and it shows Pay, Overtime Pay, Total Pay and Tax.
- **AboutActivity** shows my name and student ID. You open it from the "About" item in the ActionBar.

---

## 1. Activity lifecycle used

An **Activity** is one screen. Android calls certain methods on it at certain moments. These are its "lifecycle callbacks".

| Callback | Where I use it | What happens |
|---|---|---|
| `onCreate()` | Both activities | Runs **once** when the screen is created. I call `setContentView()` to load the XML layout, then use `findViewById()` to get the views and attach the button click listener. |
| `onStart()` / `onResume()` | Not overridden | Android calls these when the screen becomes visible and then interactive. |
| `onPause()` / `onStop()` | Not overridden | When AboutActivity opens on top, MainActivity is **paused, then stopped**. It is *not destroyed*: it waits in the back stack. |
| `onDestroy()` | Triggered by `finish()` | When I call `finish()` in AboutActivity, it gets destroyed and removed from the back stack. |

**Full flow:**
1. App launches → `MainActivity.onCreate → onStart → onResume`.
2. Tap About → `MainActivity.onPause` → `AboutActivity.onCreate → onStart → onResume` → `MainActivity.onStop`.
3. Tap the Up arrow → `finish()` → `AboutActivity.onPause` → `MainActivity.onRestart → onStart → onResume` → `AboutActivity.onStop → onDestroy`.

In step 3, MainActivity is **restarted**, not re-created. `onCreate` does not run again, which is why the inputs and results are still on the screen.

---

## 2. Navigation, and why no new MainActivity is created

**Going forward (Main → About):**
```kotlin
startActivity(Intent(this, AboutActivity::class.java))
```
This is an **explicit Intent**: it names the exact class to open. Android pushes AboutActivity on top of the **back stack**:

```
[ AboutActivity ]   ← top, visible
[ MainActivity  ]   ← stopped but still alive
```

**Going back (About → Main):**
```kotlin
supportActionBar?.setDisplayHomeAsUpEnabled(true)   // in onCreate: shows the ← arrow

override fun onSupportNavigateUp(): Boolean {
    finish()      // close this screen
    return true   // "I handled it"
}
```
`finish()` destroys AboutActivity and pops it off the stack. MainActivity is underneath, so it comes back to the front: the **same object**, with its data still there.

**Why not `startActivity(Intent(this, MainActivity::class.java))`?** That would **push a second MainActivity** on top:
```
[ MainActivity #2 ]  ← new, empty form
[ AboutActivity   ]
[ MainActivity #1 ]
```
That wastes memory and loses the user's inputs. Pressing the system Back button would also show the About screen again, which is confusing.

I checked this on the emulator. After pressing Up, `adb shell dumpsys activity` showed the task contained **one** activity, and it was the original MainActivity record. The typed values and results were still there.

`android:parentActivityName=".MainActivity"` in the manifest records the parent relationship. My `onSupportNavigateUp()` override does the actual navigation.

---

## 3. How the menu is inflated and handled

1. **Define it** in `res/menu/main_menu.xml`. It has one `<item>` with id `action_about`, title `@string/menu_about`, and `app:showAsAction="ifRoom"`. "ifRoom" means: show it directly in the ActionBar if there is space, otherwise put it in the ⋮ overflow menu.
2. **Inflate it.** Android calls `onCreateOptionsMenu(menu)` when building the ActionBar. `menuInflater.inflate(R.menu.main_menu, menu)` turns the XML into real menu items. Returning `true` means "show this menu".
3. **Handle taps.** Android calls `onOptionsItemSelected(item)`. I use `when (item.itemId)` to check which item was tapped. For `R.id.action_about` I start AboutActivity and return `true`, which means "handled". Anything else goes to `super.onOptionsItemSelected(item)`.

The menu only appears because the theme has an ActionBar (`Theme.MaterialComponents.DayNight.DarkActionBar`). A `NoActionBar` theme would have nowhere to show it.

---

## 4. How input validation works

All three fields go through one helper, `readNumber(editText)`. It returns either a valid `Double` or `null`:

1. `editText.text.toString().trim()` reads the text and removes spaces.
2. **Empty?** → `editText.error = "This field is required"` and return `null`.
3. `text.toDoubleOrNull()` tries to convert. For input like `"."` it **returns `null` instead of throwing an exception** (`toDouble()` would crash the app). Null → "Please enter a valid number".
4. **Negative?** → "Value cannot be negative".
5. Otherwise it returns the number.

In `calculatePay()`:
```kotlin
val hours = readNumber(editHours) ?: return
```
`?:` is the **Elvis operator**. If the left side is `null`, it runs the right side, so `return` stops the calculation right there. The app shows the error and never crashes.

The **tax rate** has one extra check: if it is above 100 → "Tax rate must be between 0 and 100". Then I convert the percentage: `taxRate = taxPercent / 100`, so 13 becomes 0.13.

`android:inputType="numberDecimal"` in the XML also makes the phone show a number keyboard. The keyboard alone isn't trusted (users could paste text), so the code checks too.

`editText.error = ...` is Kotlin property syntax for `setError(...)`. It shows the red ! icon and the message popup.

---

## 5. Pay and tax logic

```kotlin
if (hours <= 40) {
    pay = hours * hourlyRate
    overtimePay = 0.0
} else {
    pay = 40 * hourlyRate                              // first 40 hours, normal rate
    overtimePay = (hours - 40) * hourlyRate * 1.5      // extra hours, "time and a half"
}
totalPay = pay + overtimePay
tax = pay * taxRate        // tax on regular pay, exactly as the assignment states
```

Results are shown with `getString(R.string.result_pay, pay)`. The string is `"Pay: $%1$.2f"`:
- `%1$` = first argument
- `.2f` = decimal number with exactly 2 decimals

So 800 is displayed as `Pay: $800.00`.

**Worked examples (all checked on the Pixel 7 emulator):**

| Hours | Rate | Tax % | Pay | Overtime | Total | Tax |
|---|---|---|---|---|---|---|
| 35 | 20 | 13 | 35×20 = **700.00** | **0.00** | **700.00** | 700×0.13 = **91.00** |
| 45 | 20 | 13 | 40×20 = **800.00** | 5×20×1.5 = **150.00** | **950.00** | 800×0.13 = **104.00** |
| 40 | 20 | 13 | 40×20 = **800.00** | **0.00** (40 is not > 40) | **800.00** | **104.00** |
| 40.5 | 18.75 | 0 | 40×18.75 = **750.00** | 0.5×18.75×1.5 = **14.06** | **764.06** | **0.00** |

---

## 6. Other details you might be asked about

- **`findViewById`** connects a Kotlin variable to the view with that id in the XML. The variables are `lateinit var` because they can only be assigned after `setContentView()` runs in `onCreate()`.
- **strings.xml**: all visible text lives there, so it's in one place and can be translated. No text is hardcoded in layouts or code.
- **ScrollView + LinearLayout**: the ScrollView lets the form scroll when the keyboard covers it. A ScrollView can only have **one** child, so everything sits inside one vertical LinearLayout.
- **TextInputLayout**: a Material component that wraps an EditText. It turns the hint into a floating label and draws the outlined box.
- **`applySystemBarPadding()`** (in `SystemBarPadding.kt`): from Android 15, apps draw edge-to-edge, so content starts behind the status bar, ActionBar and navigation bar. This function adds padding equal to those bars (and the keyboard) so nothing is hidden. Without it, the "hours" field ended up under the ActionBar.

---

## 7. Likely interview questions with short answers

1. **Why do you call `finish()` instead of starting MainActivity with an Intent?**
   `finish()` removes AboutActivity from the back stack, so the existing MainActivity shows again with its data. A new Intent would create a second MainActivity on top of the stack.

2. **What does `onSupportNavigateUp()` do, and why return `true`?**
   Android calls it when the ActionBar's Up arrow is tapped. Returning `true` tells Android I handled the event.

3. **How is the options menu created?**
   `onCreateOptionsMenu()` uses `menuInflater.inflate(R.menu.main_menu, menu)` to turn the XML into menu items, then returns `true` so they show.

4. **How do you know which menu item was clicked?**
   In `onOptionsItemSelected()` I compare `item.itemId` with `R.id.action_about` using `when`.

5. **What does `showAsAction="ifRoom"` mean?**
   Show the item in the ActionBar if there is space, otherwise move it to the overflow (⋮) menu.

6. **Why `toDoubleOrNull()` instead of `toDouble()`?**
   `toDouble()` throws a `NumberFormatException` and crashes on bad input. `toDoubleOrNull()` returns `null`, which I check for and turn into an error message.

7. **How is the tax rate converted?**
   The user types a percentage (13). I divide by 100 to get 0.13 before multiplying. I also reject values above 100 and below 0.

8. **Walk through 45 hours at $20 with 13% tax.**
   45 > 40, so pay = 40×20 = 800 and overtime = 5×20×1.5 = 150. Total = 950. Tax = 800×0.13 = 104 (tax is on regular pay, as the spec says).

9. **Which lifecycle methods run when you open About and come back?**
   Opening it: Main `onPause` → About `onCreate/onStart/onResume` → Main `onStop`.
   Going back: About `onPause` → Main `onRestart/onStart/onResume` → About `onStop/onDestroy`.
   Main's `onCreate` does **not** run again.

10. **Why must both activities be in AndroidManifest.xml?**
    Android only launches activities declared in the manifest. Without that entry, `startActivity` crashes with `ActivityNotFoundException`. MainActivity also has the `MAIN`/`LAUNCHER` intent filter, which makes it the app's starting screen.

**Bonus:** *How does `getString(R.string.result_pay, pay)` format money?* It fills `%1$.2f` with `pay`, rounded to 2 decimals, so you get "Pay: $800.00".
