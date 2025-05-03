package com.example.slt

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.slt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.CNNmodel.setOnClickListener {
            val intent = Intent(this@MainActivity, CNNActivity::class.java)
            startActivity(intent)
        }
        binding.DTWmodel.setOnClickListener {
            val intent = Intent(this@MainActivity, DTWActivity::class.java)
            startActivity(intent)
        }
        binding.textRecognize.setOnClickListener{
            val intent = Intent(this@MainActivity, TextActivity::class.java)
            startActivity(intent)
        }
        binding.exitButton.setOnClickListener{
            finish()
        }
    }
}