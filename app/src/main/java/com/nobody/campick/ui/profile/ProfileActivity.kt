package com.nobody.campick.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.nobody.campick.ui.theme.CampickTheme

class ProfileActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_MEMBER_ID = "member_id"
        private const val EXTRA_IS_OWN_PROFILE = "is_own_profile"

        fun newIntent(context: Context, memberId: String?, isOwnProfile: Boolean): Intent {
            return Intent(context, ProfileActivity::class.java).apply {
                putExtra(EXTRA_MEMBER_ID, memberId)
                putExtra(EXTRA_IS_OWN_PROFILE, isOwnProfile)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val memberId = intent.getStringExtra(EXTRA_MEMBER_ID)
        val isOwnProfile = intent.getBooleanExtra(EXTRA_IS_OWN_PROFILE, false)

        setContent {
            CampickTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ProfileScreen(
                        modifier = Modifier.padding(innerPadding),
                        memberId = memberId,
                        isOwnProfile = isOwnProfile,
                        showBackButton = true, // Always show back button for stack navigation
                        onBackClick = {
                            finish() // Go back to previous activity
                        },
                        onEditProfile = {
                            // TODO: Implement profile edit functionality
                        },
                        onLogout = {
                            // TODO: Implement logout functionality
                            // For now, just close the activity
                            finish()
                        },
                        onAccountDeletion = {
                            // TODO: Implement account deletion functionality
                            // For now, just close the activity
                            finish()
                        },
                        onSettingsClick = {
                            // TODO: Navigate to settings
                        }
                    )
                }
            }
        }
    }
}