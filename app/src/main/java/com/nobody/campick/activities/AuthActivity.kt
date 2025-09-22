package com.nobody.campick.activities


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nobody.campick.R

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acivity_auth)

        if (savedInstanceState == null) {
            // 처음 시작 시 LoginFragment 보여주기
            supportFragmentManager.beginTransaction()
                .replace(R.id.authContainer, com.nobody.campick.fragments.LoginFragment())
                .commit()
        }
    }
}