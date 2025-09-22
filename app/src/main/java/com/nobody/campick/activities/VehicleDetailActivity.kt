package com.nobody.campick.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nobody.campick.ui.theme.CampickTheme
import com.nobody.campick.views.VehicleDetailView
import com.nobody.campick.ui.profile.ProfileActivity

class VehicleDetailActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_VEHICLE_ID = "vehicle_id"

        fun newIntent(context: Context, vehicleId: String): Intent {
            return Intent(context, VehicleDetailActivity::class.java).apply {
                putExtra(EXTRA_VEHICLE_ID, vehicleId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val vehicleId = intent.getStringExtra(EXTRA_VEHICLE_ID) ?: ""

        setContent {
            CampickTheme {
                VehicleDetailView(
                    vehicleId = vehicleId,
                    onBackClick = { finish() },
                    onShareClick = {
                        // TODO: Implement share functionality
                        shareVehicle(vehicleId)
                    },
                    onSellerClick = {
                        // TODO: Navigate to seller profile or show seller modal
                        println("Seller clicked")
                    },
                    onProfileDetailClick = { sellerId, isOwnProfile ->
                        // Launch ProfileActivity with seller information
                        val intent = ProfileActivity.newIntent(
                            context = this@VehicleDetailActivity,
                            memberId = sellerId,
                            isOwnProfile = isOwnProfile
                        )
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun shareVehicle(vehicleId: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "캠핑카를 확인해보세요! 차량 ID: $vehicleId")
            putExtra(Intent.EXTRA_SUBJECT, "캠핑카 추천")
        }
        startActivity(Intent.createChooser(shareIntent, "공유하기"))
    }
}