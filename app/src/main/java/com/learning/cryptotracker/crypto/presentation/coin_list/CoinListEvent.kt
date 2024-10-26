package com.learning.cryptotracker.crypto.presentation.coin_list

import com.learning.cryptotracker.core.domain.util.NetworkError

sealed interface CoinListEvent {
    data class Error(val error : NetworkError) : CoinListEvent
}