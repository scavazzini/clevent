package dev.scavazzini.clevent.ui

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.io.NFCListener
import dev.scavazzini.clevent.io.NFCReader
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var nfcReader: NFCReader
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private var mNfcAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeBottomNavigation()
        initializeNFCAdapter()
    }

    private fun initializeBottomNavigation() {
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        val appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_order, R.id.navigation_recharge, R.id.navigation_receipt).build()

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)
    }

    private fun initializeNFCAdapter() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this) ?: return
        mPendingIntent = PendingIntent.getActivity(this, 0, Intent(this,
                javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE)
    }

    public override fun onResume() {
        super.onResume()
        mNfcAdapter?.enableForegroundDispatch(this, mPendingIntent, null, null)
    }

    public override fun onPause() {
        super.onPause()
        mNfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val fragment = getListenerFragment() ?: return

        try {
            val (tag, customer) = nfcReader.extract(intent)
            fragment.onTagRead(tag, customer)

        } catch (e: Exception) {
            fragment.onInvalidTagRead(intent.getParcelableExtra(NfcAdapter.EXTRA_TAG))
        }
    }

    private fun getListenerFragment(): NFCListener? {
        // TODO: This does not seem to be the best way to notify the current fragment.
        val fragment = supportFragmentManager.fragments[0].childFragmentManager.fragments[0]

        if (fragment !is NFCListener) {
            return null
        }
        return fragment
    }

    override fun onSupportNavigateUp() = navController.navigateUp() || super.onSupportNavigateUp()
}
