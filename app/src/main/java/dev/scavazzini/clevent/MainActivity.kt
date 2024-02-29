package dev.scavazzini.clevent

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.scavazzini.clevent.ui.core.theme.CleventTheme
import dev.scavazzini.clevent.ui.order.OrderScreen
import dev.scavazzini.clevent.ui.order.OrderViewModel
import dev.scavazzini.clevent.ui.receipt.ReceiptScreen
import dev.scavazzini.clevent.ui.receipt.ReceiptViewModel
import dev.scavazzini.clevent.ui.recharge.RechargeScreen
import dev.scavazzini.clevent.ui.recharge.RechargeViewModel
import dev.scavazzini.clevent.ui.settings.SettingsScreen
import dev.scavazzini.clevent.ui.settings.SettingsViewModel

private enum class Screens(
    val label: String,
    val image: ImageVector,
) {
    OrderScreen(
        label = "Order",
        image = Icons.Filled.ShoppingBasket,
    ),
    RechargeScreen(
        label = "Recharge",
        image = Icons.Filled.AttachMoney
    ),
    ReceiptScreen(
        label = "Receipt",
        image = Icons.Filled.Receipt,
    ),
    SettingsScreen(
        label = "Settings",
        image = Icons.Filled.Settings,
    ),
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var mNfcAdapter: NfcAdapter? = null
    private var mPendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        initializeNFCAdapter()

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentScreen = navBackStackEntry?.destination?.route

            CleventTheme {
                Column(Modifier.background(MaterialTheme.colorScheme.background)) {
                    NavHost(
                        navController = navController,
                        startDestination = Screens.OrderScreen.name,
                        modifier = Modifier
                            .statusBarsPadding()
                            .weight(1f),
                    ) {
                        val modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)

                        composable(Screens.OrderScreen.name) {
                            OrderScreen(
                                viewModel = hiltViewModel<OrderViewModel>(),
                                modifier = modifier,
                            )
                        }
                        composable(Screens.RechargeScreen.name) {
                            RechargeScreen(
                                viewModel = hiltViewModel<RechargeViewModel>(),
                                modifier = modifier,
                            )
                        }
                        composable(Screens.ReceiptScreen.name) {
                            ReceiptScreen(
                                viewModel = hiltViewModel<ReceiptViewModel>(),
                                modifier = modifier,
                            )
                        }
                        composable(Screens.SettingsScreen.name) {
                            SettingsScreen(
                                viewModel = hiltViewModel<SettingsViewModel>(),
                                modifier = modifier,
                            )
                        }
                    }

                    NavigationBar {
                        val screens = listOf(
                            Screens.OrderScreen,
                            Screens.RechargeScreen,
                            Screens.ReceiptScreen,
                            Screens.SettingsScreen,
                        )

                        screens.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.image, contentDescription = null) },
                                label = { Text(screen.label) },
                                selected = currentScreen == screen.name,
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                onClick = {
                                    navController.navigate(screen.name) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun initializeNFCAdapter() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this) ?: return

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        mPendingIntent = PendingIntent.getActivity(
            /* context = */ this,
            /* requestCode = */ 0,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_MUTABLE,
        )
    }

    public override fun onResume() {
        super.onResume()

        mNfcAdapter?.enableForegroundDispatch(
            /* activity = */ this,
            /* intent = */ mPendingIntent,
            /* filters = */ null, // TODO: Should we apply some kind of filter?
            /* techLists = */ null, // TODO: Interesting parameter to get only supported techs
        )
    }

    public override fun onPause() {
        super.onPause()
        mNfcAdapter?.disableForegroundDispatch(this)
    }

}
