package com.example.sellersection

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.sellersection.databinding.ActivityOpeningBinding

class OpeningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpeningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOpeningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(HomeFrag())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.homeIcon -> replaceFragment(HomeFrag())
                R.id.messageIcon -> replaceFragment(ChatFrag())
                R.id.accountIcon -> replaceFragment(AccountFrag())
                else -> {
                }
            }
            true
        }

    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragFrame, fragment)
        fragmentTransaction.commit()
    }

}