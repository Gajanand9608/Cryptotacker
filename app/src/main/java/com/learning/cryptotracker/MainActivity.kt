package com.learning.cryptotracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.learning.cryptotracker.core.presentation.util.ObserveAsEvents
import com.learning.cryptotracker.core.presentation.util.toString
import com.learning.cryptotracker.crypto.presentation.coin_detail.CoinDetailScreen
import com.learning.cryptotracker.crypto.presentation.coin_list.CoinListEvent
import com.learning.cryptotracker.crypto.presentation.coin_list.CoinListScreen
import com.learning.cryptotracker.crypto.presentation.coin_list.CoinListViewModel
import com.learning.cryptotracker.ui.theme.CryptoTrackerTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = koinViewModel<CoinListViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    val context = LocalContext.current
                    val lifecycleOwner = LocalLifecycleOwner.current
                    ObserveAsEvents(events = viewModel.events, key1 = lifecycleOwner.lifecycle){
                        when(it){
                            is CoinListEvent.Error -> {
                                Toast.makeText(context,it.error.toString(context), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    when {
                        state.selectedCoin != null -> {
                            CoinDetailScreen(state = state, modifier = Modifier.padding(innerPadding))
                        }

                        else -> {
                            CoinListScreen(state = state, modifier = Modifier.padding(innerPadding), onAction = viewModel::onAction)
                        }
                    }
                }
            }
        }
    }
}
