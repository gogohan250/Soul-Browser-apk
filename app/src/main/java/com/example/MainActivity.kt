package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.browser.BrowserPreferences
import com.example.data.BrowserDatabase
import com.example.data.BrowserRepository
import com.example.ui.MainBrowserScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = BrowserDatabase.getInstance(applicationContext)
        val repository = BrowserRepository(db.browserDao())
        val preferences = BrowserPreferences(applicationContext)

        setContent {
            val browserViewModel: BrowserViewModel = viewModel {
                BrowserViewModel(
                    application = application,
                    repository = repository,
                    preferences = preferences
                )
            }

            val config by browserViewModel.config.collectAsState()
            val useDark = config.isDarkMode || isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = useDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainBrowserScreen(
                        viewModel = browserViewModel,
                        onCloseApp = { finish() }
                    )
                }
            }
        }
    }
}
