package sreich.countthedays


import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.support.v7.app.ActionBar
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.support.v4.content.FileProvider
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import java.io.File
import java.io.ObjectOutputStream

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
   * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
   * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()

        val defprefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val a = defprefs.all
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean
            = PreferenceFragment::class.java.name == fragmentName
            || GeneralPreferenceFragment::class.java.name == fragmentName
            || DataSyncPreferenceFragment::class.java.name == fragmentName

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class DataSyncPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data_sync)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("sync_frequency"))

            val importBackupPref = findPreference(getString(R.string.importBackup))
            importBackupPref.setOnPreferenceClickListener {
                importBackupIntent()
                true
            }

            val exportBackupPref = findPreference(getString(R.string.exportBackup))
            exportBackupPref.setOnPreferenceClickListener {
                exportBackupIntent()
                true
            }

        }

        private fun importBackupIntent() {
            //Toast.makeText(this.activity.applicationContext, "import toast", 5000).show()
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        private fun exportBackupIntent() {
            //Toast.makeText(this.activity.applicationContext, "export toast", 5000).show()

            val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            //sharingIntent.type = "*/*"
            val fileUri = saveBackup()
            sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, fileUri)

            startActivity(Intent.createChooser(sharingIntent, "Share backup to..."))
        }

        private fun saveBackup(): Uri {
            val fileName = "Count The Days-${System.currentTimeMillis()}.backup"
            val backupFile = File(this.activity.applicationContext.filesDir, fileName)

            File(this.activity.applicationContext.filesDir,
                 MainActivity.Settings.settingsSaveFileName +
                         MainActivity.Settings.settingsSaveFileExtension).copyTo(backupFile)

            // wrap File object into a content provider
            val fileUri = FileProvider.getUriForFile(this.activity.applicationContext,
                                                     "sreich.countthedays.fileprovider", backupFile)

            return fileUri
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }


            return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            when (preference) {
                is ListPreference -> {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    val listPreference = preference
                    val index = listPreference.findIndexOfValue(stringValue)

                    val summary = if (index >= 0) {
                        listPreference.entries[index]
                    } else {
                        null
                    }

                    // Set the summary to reflect the new value.
                    preference.summary = summary
                }
                else -> // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                                                                     PreferenceManager
                                                                             .getDefaultSharedPreferences(
                                                                                     preference.context)
                                                                             .getString(preference.key, ""))
        }
    }
}
