package com.learning.cryptotracker.crypto.data.networking

import android.content.Context
import android.util.Log
import com.learning.cryptotracker.core.domain.util.NetworkError
import com.learning.cryptotracker.crypto.data.mappers.toCoin
import com.learning.cryptotracker.crypto.data.networking.dto.CoinsResponseDto
import com.learning.cryptotracker.crypto.domain.Coin
import com.learning.cryptotracker.crypto.domain.CoinDataSource
import com.learning.cryptotracker.crypto.domain.CoinPrice
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime
import com.learning.cryptotracker.core.domain.util.Result
import com.learning.cryptotracker.crypto.data.mappers.toCoinPrice
import com.learning.cryptotracker.crypto.data.networking.dto.CoinHistoryDto
import kotlinx.coroutines.delay

class LocalCoinDataSource(private val context: Context) : CoinDataSource {

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }

    override suspend fun getCoins(): Result<List<Coin>, NetworkError> {
        delay(500)
        return try {
            val raw = context.assets.open("coin_list_mock.json").bufferedReader().use { it.readText() }
            val dto = json.decodeFromString<CoinsResponseDto>(raw)
            Result.Success(dto.data.map { it.toCoin() })
        } catch (e: Exception) {
            Result.Error(NetworkError.NO_INTERNET)
        }
    }


    override suspend fun getCoinHistory(
        coinId: String,
        start: ZonedDateTime,
        end: ZonedDateTime
    ): Result<List<CoinPrice>, NetworkError> {
        delay(500)
        return try {
            val raw = context.assets.open("coin_detail_screen.json").bufferedReader().use { it.readText() }
            val dto = json.decodeFromString<CoinHistoryDto>(raw)
            Result.Success(dto.data.map { it.toCoinPrice() })
        } catch (e: Exception) {
            Result.Error(NetworkError.NO_INTERNET)
        }
    }
}
