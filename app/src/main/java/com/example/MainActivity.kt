package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MeetingSummarizerApp
import com.example.ui.MeetingSummarizerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MeetingSummarizerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeetingSummarizerApp(viewModel = viewModel)
        }
    }
}
