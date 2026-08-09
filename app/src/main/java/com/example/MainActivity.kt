package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BlockerHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BlockerViewModel
import com.example.viewmodel.BlockerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as BlockerApplication
            val viewModel: BlockerViewModel = viewModel(
                factory = BlockerViewModelFactory(app, app.repository)
            )
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            MyApplicationTheme(themeSetting = appTheme) {
                BlockerHomeScreen(viewModel = viewModel)
            }
        }
    }
}
