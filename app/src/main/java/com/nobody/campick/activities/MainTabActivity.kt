package com.nobody.campick.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.nobody.campick.R
import com.nobody.campick.databinding.ActivityMainTabBinding
import com.nobody.campick.fragments.FavoritesFragment
import com.nobody.campick.fragments.HomeFragment
import com.nobody.campick.fragments.ProfileFragment
import com.nobody.campick.fragments.VehicleRegistrationFragment
import com.nobody.campick.fragments.VehiclesFragment

class MainTabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainTabBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // 초기 화면을 홈으로 설정
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_vehicles -> {
                    replaceFragment(VehiclesFragment())
                    true
                }
                R.id.nav_register -> {
                    replaceFragment(VehicleRegistrationFragment())
                    true
                }
                R.id.nav_favorites -> {
                    replaceFragment(FavoritesFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}