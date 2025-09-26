package com.nobody.campick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import com.nobody.campick.models.chat.ChatSeller
import com.nobody.campick.models.chat.ChatVehicle
import androidx.compose.ui.Modifier
import com.nobody.campick.ui.theme.CampickTheme
import com.nobody.campick.views.ChatRoom
import com.nobody.campick.views.FavoritesView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampickTheme {
                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                    FavoritesView(
                        onVehicleClick = { vehicleId ->
                            println("차량 클릭됨: $vehicleId")
                        }
                    )
                    }
                }
            }
        }
    }
}

