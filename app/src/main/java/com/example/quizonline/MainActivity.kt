package com.example.quizonline

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizonline.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var quizModelList: MutableList<QuizModel>
    private lateinit var adapter: QuizlistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Chỉ cần gọi hàm hiển thị tên người dùng
        displayUsername()

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    // Đã ở trong MainActivity, không cần chuyển đến chính nó nữa
                    true
                }
                R.id.aboutus -> {
                    // Chuyển đến trang AboutUsActivity
                    val intent = Intent(this, AboutUsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.profile -> {
                    // Chuyển đến trang ProfileActivity
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Khởi tạo danh sách và RecyclerView
        quizModelList = mutableListOf()
        adapter = QuizlistAdapter(quizModelList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Lấy dữ liệu từ Firebase
        getDataFromFirebase()
    }

    private fun displayUsername() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(userId)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name")
                        if (userName != null) {
                            // Hiển thị tên người dùng trong giao diện
                            Toast.makeText(this, "Hello, $userName!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Xử lý khi có lỗi xảy ra
                    Toast.makeText(this, "Failed to retrieve user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupRecyclerView() {
        binding.progressBar.visibility = View.GONE
        adapter.notifyDataSetChanged()
    }

    private fun getDataFromFirebase() {
        binding.progressBar.visibility = View.VISIBLE
        val database = FirebaseDatabase.getInstance()
        val myRef = database.reference

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val quizModel = snapshot.getValue(QuizModel::class.java)
                        if (quizModel != null) {
                            quizModelList.add(quizModel)
                        }
                    }
                }
                setupRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý khi có lỗi xảy ra
                Toast.makeText(applicationContext, "Failed to retrieve data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
