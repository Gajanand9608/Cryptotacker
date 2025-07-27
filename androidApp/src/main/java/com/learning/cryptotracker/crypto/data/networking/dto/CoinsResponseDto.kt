package com.learning.cryptotracker.crypto.data.networking.dto

import com.learning.cryptotracker.crypto.data.networking.dto.CoinDto
import kotlinx.serialization.Serializable

@Serializable
data class CoinsResponseDto(
    val data: List<CoinDto>
)