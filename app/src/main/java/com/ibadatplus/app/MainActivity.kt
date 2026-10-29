package com.ibadatplus.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ibadatplus.app.auth.AuthManager
import com.ibadatplus.app.auth.AuthScreen
import com.ibadatplus.app.hadith.ui.HadithScreen
import com.ibadatplus.app.qibla.ui.QiblaFinderScreen
import com.ibadatplus.app.tasbeeh.ui.TasbeehScreen
import com.ibadatplus.app.ui.alarm.PrayerAlarmSettingsScreen
import com.ibadatplus.app.ui.prayer.PrayerTimesScreen
import com.ibadatplus.app.ui.splash.SplashScreen

enum class AppScreen {
    Splash,
    Auth,
    PrayerTimes,
    AlarmSettings,
    QiblaFinder,
    Hadith,
    Tasbeeh
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

                    // Hardware back button support
                    BackHandler(enabled = currentScreen != AppScreen.PrayerTimes && currentScreen != AppScreen.Splash && currentScreen != AppScreen.Auth) {
                        currentScreen = AppScreen.PrayerTimes
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.Splash -> {
                                SplashScreen(
                                    onSplashFinished = {
                                        currentScreen = if (AuthManager.isUserLoggedIn()) {
                                            AppScreen.PrayerTimes
                                        } else {
                                            AppScreen.Auth
                                        }
                                    }
                                )
                            }
                            AppScreen.Auth -> {
                                AuthScreen(
                                    onAuthSuccess = {
                                        currentScreen = AppScreen.PrayerTimes
                                    },
                                    onSkipGuest = {
                                        currentScreen = AppScreen.PrayerTimes
                                    }
                                )
                            }
                            AppScreen.PrayerTimes -> {
                                PrayerTimesScreen(
                                    onNavigateToAlarmSettings = {
                                        currentScreen = AppScreen.AlarmSettings
                                    },
                                    onNavigateToQiblaFinder = {
                                        currentScreen = AppScreen.QiblaFinder
                                    },
                                    onNavigateToHadith = {
                                        currentScreen = AppScreen.Hadith
                                    },
                                    onNavigateToTasbeeh = {
                                        currentScreen = AppScreen.Tasbeeh
                                    },
                                    onNavigateToAuth = {
                                        currentScreen = AppScreen.Auth
                                    }
                                )
                            }
                            AppScreen.AlarmSettings -> {
                                PrayerAlarmSettingsScreen(
                                    onNavigateBack = {
                                        currentScreen = AppScreen.PrayerTimes
                                    }
                                )
                            }
                            AppScreen.QiblaFinder -> {
                                QiblaFinderScreen(
                                    onNavigateBack = {
                                        currentScreen = AppScreen.PrayerTimes
                                    }
                                )
                            }
                            AppScreen.Hadith -> {
                                HadithScreen(
                                    onNavigateBack = {
                                        currentScreen = AppScreen.PrayerTimes
                                    }
                                )
                            }
                            AppScreen.Tasbeeh -> {
                                TasbeehScreen(
                                    onNavigateBack = {
                                        currentScreen = AppScreen.PrayerTimes
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
