package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        DepositRequestEntity::class,
        WithdrawalRequestEntity::class,
        AppSettingsEntity::class,
        GameHistoryEntity::class,
        GameItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "royal_spin_casino.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    val dao = database.appDao()
                                    // Seed default admin settings
                                    dao.insertOrUpdateSetting(
                                        AppSettingsEntity("jazzcash_number", "03227422095")
                                    )
                                    dao.insertOrUpdateSetting(
                                        AppSettingsEntity("jazzcash_title", "JazzCash Official Cashier")
                                    )
                                    dao.insertOrUpdateSetting(
                                        AppSettingsEntity("force_next_result", "AUTO")
                                    )
                                    // Low payout mode setting (Admin controlled)
                                    dao.insertOrUpdateSetting(
                                        AppSettingsEntity("low_winning_rate", "true")
                                    )
                                    // Default Games (No pre-registered admin; user registers 03073715192 with their own password!)
                                    dao.insertGame(
                                        GameItemEntity(
                                            id = "zoo_car",
                                            title = "Zoo & Car Roulette",
                                            subtitle = "8 Multipliers + Special Zero Wipeout",
                                            iconEmoji = "🏎️",
                                            category = "ROULETTE",
                                            isEnabled = true,
                                            activePlayers = 340
                                        )
                                    )
                                    dao.insertGame(
                                        GameItemEntity(
                                            id = "dragon_tiger",
                                            title = "Dragon vs Tiger",
                                            subtitle = "Fast 2-Card Battle with 8x Tie",
                                            iconEmoji = "🐉",
                                            category = "CARD",
                                            isEnabled = true,
                                            activePlayers = 215
                                        )
                                    )
                                    dao.insertGame(
                                        GameItemEntity(
                                            id = "7_up_down",
                                            title = "7 Up 7 Down",
                                            subtitle = "Double Dice High / Low Prediction",
                                            iconEmoji = "🎲",
                                            category = "CASINO",
                                            isEnabled = true,
                                            activePlayers = 180
                                        )
                                    )
                                    dao.insertGame(
                                        GameItemEntity(
                                            id = "mines_lucky",
                                            title = "Lucky Mines",
                                            subtitle = "Multiply cash every step without hitting bomb",
                                            iconEmoji = "💎",
                                            category = "CASINO",
                                            isEnabled = true,
                                            activePlayers = 95
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
