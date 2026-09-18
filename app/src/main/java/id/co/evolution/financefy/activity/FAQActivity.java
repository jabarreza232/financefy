package id.co.evolution.financefy.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.adapter.AdapterFAQ;
import id.co.evolution.financefy.databinding.ActivityFaqBinding;
import id.co.evolution.financefy.model.ModelFaq;

public class FAQActivity extends AppCompatActivity {
    private ActivityFaqBinding binding;
    private AdapterFAQ adapter;
    private List<ModelFaq> faqList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_faq);
        binding.btnBack.setOnClickListener(v -> finish());

        // Setup RecyclerView
        binding.rvFaq.setLayoutManager(new LinearLayoutManager(this));

        // Load Data
        loadFaqData();
    }
    private void loadFaqData() {
        faqList = new ArrayList<>();

        faqList.add(new ModelFaq(
                "1. Bagaimana cara menggunakan Financefy?",
                "Financefy digunakan untuk mencatat arus kas (Pemasukan & Pengeluaran) serta mengatur target tabungan Anda. Anda dapat menambahkan transaksi secara manual melalui tombol (+) atau memindai struk belanja secara otomatis menggunakan kamera."
        ));

        faqList.add(new ModelFaq(
                "2. Bagaimana cara kerja Scan AI (LLM Lokal)?",
                "Fitur Scan AI memungkinkan aplikasi membaca struk/nota belanja Anda dan otomatis mengisi nominal serta kategori pengeluaran tanpa koneksi internet. Pastikan Anda telah mengunduh 'Model AI' di menu Pengaturan sebelum menggunakan fitur ini."
        ));

        faqList.add(new ModelFaq(
                "3. Seberapa aman data dan pengaturan PIN saya?",
                "Sangat aman. Kode PIN Anda tidak pernah dikirim ke server luar. Data tersebut dienkripsi dan disimpan secara lokal di dalam memori internal HP Anda. Jika fitur PIN diaktifkan, aplikasi tidak dapat diakses tanpa kode tersebut."
        ));

        faqList.add(new ModelFaq(
                "4. Bagaimana cara mengaktifkan Login Biometrik?",
                "Setelah Anda membuat PIN, Anda dapat mengaktifkan 'Login Sidik Jari (Biometrik)' di menu Pengaturan. Aplikasi akan menggunakan sensor sidik jari/wajah bawaan HP Anda agar Anda bisa login lebih cepat tanpa harus mengetik angka PIN."
        ));

        faqList.add(new ModelFaq(
                "5. Bagaimana cara mengubah tampilan/tema aplikasi?",
                "Masuk ke menu Pengaturan > Ubah Tema. Anda dapat menyesuaikan warna dominan aplikasi, serta memilih mode tampilan antara Terang (Light Mode), Gelap (Dark Mode), atau mengikuti pengaturan bawaan sistem (System Default)."
        ));

        // Terapkan ke adapter
        adapter = new AdapterFAQ(faqList);
        binding.rvFaq.setAdapter(adapter);
    }
}