package com.nobody.campick.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nobody.campick.R
import com.nobody.campick.fragments.ProfileFragment

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_MEMBER_ID = "member_id"

        fun newIntent(context: Context, memberId: String? = null): Intent {
            return Intent(context, ProfileActivity::class.java).apply {
                memberId?.let { putExtra(EXTRA_MEMBER_ID, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_fragment)

        val memberId = intent.getStringExtra(EXTRA_MEMBER_ID)

        // ProfileFragment 추가 (백스택 없이)
        if (savedInstanceState == null) {
            val profileFragment = ProfileFragment.newInstance(memberId)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .commit()
        }
    }
}