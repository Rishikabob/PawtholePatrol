package com.example.pawtholepatrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pawtholepatrol.feature.root.PawtholeApp
import com.example.pawtholepatrol.ui.theme.PawtholePatrolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PawtholePatrolTheme {
                PawtholeApp()
            }
        }
    }
}
