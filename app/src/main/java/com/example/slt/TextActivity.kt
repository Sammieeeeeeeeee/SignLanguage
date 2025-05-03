package com.example.slt

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

class TextActivity : AppCompatActivity() {

    private lateinit var inputText: EditText
    private lateinit var translateButton: Button
    private lateinit var closeButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SignAdapter
    private val storage: FirebaseStorage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text)

        inputText = findViewById(R.id.inputText)
        translateButton = findViewById(R.id.translateButton)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        closeButton = findViewById(R.id.closeBtn)

        translateButton.setOnClickListener{
            val text = inputText.text.toString().uppercase().filter { it in 'A'..'Z' }
            if(text.isNotEmpty()){
                fetchImageUrls(text)
            }
        }

        closeButton.setOnClickListener{
            finish()
        }
    }

    private fun fetchImageUrls(text: String) {
        val urls = MutableList(text.length) { "" }  // Maintain order
        val chars = text.toList()
        var completed = 0

        for ((index, ch) in chars.withIndex()) {
            val imageRef = storage.reference.child("alphabets/${ch}.png")
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                urls[index] = uri.toString()  // Insert at correct position
                completed++
                if (completed == chars.size) {
                    showImages(urls)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load image for $ch", Toast.LENGTH_SHORT).show()
                urls[index] = ""  // Optional: leave blank or set to placeholder if needed
                completed++
                if (completed == chars.size) {
                    showImages(urls)
                }
            }
        }
    }

    private fun showImages(urls: List<String>) {
        adapter = SignAdapter(urls)
        recyclerView.adapter = adapter
    }
}