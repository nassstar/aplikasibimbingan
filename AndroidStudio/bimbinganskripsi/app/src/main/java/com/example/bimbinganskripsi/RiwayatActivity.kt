package com.example.bimbinganskripsi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.bimbinganskripsi.model.RiwayatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var layoutKosong: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        rvRiwayat = findViewById(R.id.rvRiwayat)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        layoutKosong = findViewById(R.id.layoutKosong)

        rvRiwayat.layoutManager = LinearLayoutManager(this)

        // HAPUS getDataBimbingan() DARI SINI
        // getDataBimbingan() <--- Hapus atau komen baris ini di onCreate

        swipeRefresh.setOnRefreshListener {
            getDataBimbingan()
        }
    }

    // --- TAMBAHKAN FUNGSI INI (LIFECYCLE) ---
    override fun onResume() {
        super.onResume()
        // Fungsi ini akan jalan otomatis saat kamu kembali dari DetailActivity
        getDataBimbingan()
    }
    // ----------------------------------------

    private fun getDataBimbingan() {
        // ... (Kode getDataBimbingan biarkan tetap sama seperti sebelumnya) ...
        swipeRefresh.isRefreshing = true
        val sharedPref = getSharedPreferences("APP_SKRIPSI", Context.MODE_PRIVATE)
        val token = sharedPref.getString("TOKEN", "")!!
        val idSkripsi = sharedPref.getString("ID_SKRIPSI", "")

        if (idSkripsi.isNullOrEmpty()) {
            swipeRefresh.isRefreshing = false
            rvRiwayat.visibility = View.GONE
            layoutKosong.visibility = View.VISIBLE
            return
        }

        RetrofitClient.instance.getRiwayat(token, idSkripsi).enqueue(object : Callback<RiwayatResponse> {
            override fun onResponse(call: Call<RiwayatResponse>, response: Response<RiwayatResponse>) {
                swipeRefresh.isRefreshing = false // Stop loading

                if (response.isSuccessful) {
                    val listData = response.body()?.data

                    if (!listData.isNullOrEmpty()) {
                        val adapter = RiwayatAdapter(listData) { item ->
                            val intent = Intent(this@RiwayatActivity, DetailBimbinganActivity::class.java)
                            intent.putExtra("DATA_BIMBINGAN", item)
                            startActivity(intent)
                        }
                        rvRiwayat.adapter = adapter
                        rvRiwayat.visibility = View.VISIBLE
                        layoutKosong.visibility = View.GONE
                    } else {
                        rvRiwayat.visibility = View.GONE
                        layoutKosong.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<RiwayatResponse>, t: Throwable) {
                swipeRefresh.isRefreshing = false
                Toast.makeText(this@RiwayatActivity, "Error koneksi", Toast.LENGTH_SHORT).show()
            }
        })
    }
}