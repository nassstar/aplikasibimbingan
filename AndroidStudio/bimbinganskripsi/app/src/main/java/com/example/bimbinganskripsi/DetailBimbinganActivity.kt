package com.example.bimbinganskripsi

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bimbinganskripsi.api.ApiConfig
import com.example.bimbinganskripsi.model.BimbinganItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailBimbinganActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_bimbingan)

        val tvCatatan = findViewById<TextView>(R.id.tvDetailCatatan)
        val tvStatus = findViewById<TextView>(R.id.tvDetailStatus)
        val btnFile = findViewById<Button>(R.id.btnLihatFile)
        val btnHapus = findViewById<Button>(R.id.btnHapus) // ID Baru

        @Suppress("DEPRECATION")
        val item = intent.getParcelableExtra<BimbinganItem>("DATA_BIMBINGAN")

        if (item != null) {
            tvCatatan.text = item.catatan
            tvStatus.text = item.status

            // 1. LOGIKA TOMBOL HAPUS
            // Tombol hanya muncul jika status masih "Pending"
            if (item.status.equals("pending", ignoreCase = true)) {
                btnHapus.visibility = View.VISIBLE
            } else {
                btnHapus.visibility = View.GONE
            }

            // 2. AKSI BUKA FILE
            btnFile.setOnClickListener {
                if (!item.file_path.isNullOrEmpty()) {
                    val fullUrl = ApiConfig.FILE_BASE_URL + item.file_path
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)))
                    } catch (e: Exception) {
                        Toast.makeText(this, "Gagal membuka file", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Tidak ada file", Toast.LENGTH_SHORT).show()
                }
            }

            // 3. AKSI HAPUS (DENGAN KONFIRMASI)
            btnHapus.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Hapus Bimbingan")
                    .setMessage("Yakin ingin menghapus data ini? Data yang dihapus tidak bisa dikembalikan.")
                    .setPositiveButton("Ya, Hapus") { _, _ ->
                        hapusData(item.id)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }

        } else {
            finish()
        }
    }

    private fun hapusData(id: Int) {
        val token = getSharedPreferences("APP_SKRIPSI", Context.MODE_PRIVATE).getString("TOKEN", "")!!

        // Tampilkan Loading (Opsional, pakai Toast dulu biar simpel)
        Toast.makeText(this, "Sedang menghapus...", Toast.LENGTH_SHORT).show()

        RetrofitClient.instance.deleteBimbingan(token, id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@DetailBimbinganActivity, "Berhasil dihapus!", Toast.LENGTH_LONG).show()
                    finish() // Tutup halaman ini
                } else {
                    // --- INI PENTING: BACA ERROR BODY ---
                    val errorMsg = response.errorBody()?.string() ?: "Unknown Error"
                    Log.e("DELETE_ERROR", "Code: ${response.code()} | Body: $errorMsg")

                    // Tampilkan Code Error ke Layar
                    Toast.makeText(this@DetailBimbinganActivity, "Gagal: ${response.code()} - $errorMsg", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DELETE_FAIL", "Error: ${t.message}")
                Toast.makeText(this@DetailBimbinganActivity, "Koneksi Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}