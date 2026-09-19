package id.co.evolution.financefy.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.ai.edge.litertlm.Backend;
import com.google.ai.edge.litertlm.Conversation;
import com.google.ai.edge.litertlm.ConversationConfig;
import com.google.ai.edge.litertlm.Engine;
import com.google.ai.edge.litertlm.EngineConfig;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelPrimaryColor;

public class HelperResultLLM {
    private Engine litertEngine;
    private Conversation litertConversation;
    public interface MethodCallback{
        void onResultLLM(HashMap<String,String>map);
    }

    public MethodCallback methodCallback;

    public HelperResultLLM(MethodCallback methodCallback) {
        this.methodCallback = methodCallback;
    }

    public Conversation getLitertConversation() {
        return litertConversation;
    }

    public Engine getLitertEngine() {
        return litertEngine;
    }

    public void initLlmInference(Context context) {
        File modelFile = new File(context.getExternalFilesDir(null), "Qwen2_0.5B_Instruct.litertlm");
        TinyDb tinyDb = new TinyDb(context);
        if (modelFile.exists()) {
            new Thread(() -> {
                try {
                    // 1. Persiapkan parameter konfigurasi
                    String modelPath = modelFile.getAbsolutePath();

                    Backend backend = new Backend.CPU();

                    Backend visionBackend = null;
                    Backend audioBackend = null;

                    Integer maxNumTokens = tinyDb.getInt("max_tokens",2054);
                    // Gunakan folder cache bawaan aplikasi Android untuk mempercepat inisialisasi berikutnya
                    String cacheDir = context.getCacheDir().getAbsolutePath();

                    EngineConfig config = new EngineConfig(
                            modelPath,
                            backend,
                            visionBackend,
                            audioBackend,
                            maxNumTokens,
                            cacheDir
                    );

                    litertEngine = new Engine(config);
                    litertEngine.initialize();

                    litertConversation = litertEngine.createConversation(new ConversationConfig());

                    Log.d("LITERT", "Engine & Conversation berhasil diinisialisasi dengan maxTokens: " + maxNumTokens);
                } catch (Exception e) {
                    Log.e("LITERT_ERROR", "Gagal memuat model LiteRT: ", e);

                }
            }).start();
        } else {
            Log.w("LITERT", "File model tidak ditemukan di path: " + modelFile.getAbsolutePath());
        }
    }
    public void showLlmDisabledBottomSheet(Activity activity, LayoutInflater inflater,ModelPrimaryColor modelPrimaryColor) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(activity);
        View bottomSheetView = inflater.inflate(R.layout.layout_bottom_sheet_llm_disabled, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Inisiasi Tombol
        MaterialButton btnBatal = bottomSheetView.findViewById(R.id.bs_btn_batal_llm);
        MaterialButton btnSetting = bottomSheetView.findViewById(R.id.bs_btn_setting_llm);
        int themeColor = modelPrimaryColor.getColorPrimary();

        ImageView imgIllustration = bottomSheetView.findViewById(R.id.bs_img_illustration);

        Tools.setImageTintView(imgIllustration,modelPrimaryColor);
        Tools.setBackgroundTintView(btnSetting,modelPrimaryColor);


        btnBatal.setOnClickListener(v -> bottomSheetDialog.dismiss());

        // Aksi Tombol Pengaturan
        btnSetting.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(activity, MainActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            intent.putExtra("GO_TO_SETTINGS", true);

            activity.startActivity(intent);

            activity.finish();
        });

        // Menampilkan Bottom Sheet
        bottomSheetDialog.show();

        // (Opsional) Memastikan Bottom Sheet terbuka penuh agar terlihat bagus
        FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }
    public Bitmap preprocessReceipt(Bitmap originalBitmap) {
        Mat src = new Mat();
        Utils.bitmapToMat(originalBitmap, src);
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);


        Mat thresholded = new Mat();
        Imgproc.adaptiveThreshold(
                gray,                   // Input
                thresholded,            // Output
                255,                    // Nilai maksimum (Putih)
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, // Metode adaptif
                Imgproc.THRESH_BINARY,  // Tipe threshold (hitam putih tegas)
                11,                     // Ukuran blok piksel (bisa diubah, biasanya ganjil 11-15)
                2                       // Konstanta pengurang (bisa diubah 2-5)
        );

        // 5. Kembalikan Mat menjadi Bitmap Android
        Bitmap processedBitmap = Bitmap.createBitmap(thresholded.cols(), thresholded.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(thresholded, processedBitmap);

        // 6. Bersihkan memori C++ (Sangat penting agar tidak memory leak!)
        src.release();
        gray.release();
        thresholded.release();

        return processedBitmap;
    }
    public BottomSheetDialog showLoadingOcrBottomSheet(Context context, LayoutInflater inflater, Uri imageUri, ModelPrimaryColor modelPrimaryColor) {
       BottomSheetDialog loadingOcrDialog = new BottomSheetDialog(context);
        View view = inflater.inflate(R.layout.layout_bottom_sheet_loading, null);
        loadingOcrDialog.setContentView(view);
        loadingOcrDialog.setCancelable(false); // Jangan biarkan user menutupnya saat loading
        modelPrimaryColor.getColorPrimary();
        ImageView ivPreview = view.findViewById(R.id.bs_loading_image);
        View scanLine = view.findViewById(R.id.bs_scan_line);
        int themeColor = modelPrimaryColor.getColorPrimary();

        TextView txtTitle = view.findViewById(R.id.txt_title_lading);
        MaterialCardView cardBorder = view.findViewById(R.id.card_scan_border);
        ProgressBar pbLoading = view.findViewById(R.id.pb_loading);

        txtTitle.setTextColor(ContextCompat.getColor(context,themeColor));

        cardBorder.setStrokeColor(ContextCompat.getColor(context,themeColor));

        pbLoading.setIndeterminateTintList(ContextCompat.getColorStateList(context,themeColor));
        Tools.setBackgroundTintView(scanLine,modelPrimaryColor);
        // 1. Tampilkan Gambar dari URI
        if (imageUri != null) {
            ivPreview.setImageURI(imageUri);
        }

        // 2. Jalankan Animasi Naik-Turun
        Animation scanAnim = AnimationUtils.loadAnimation(context, R.anim.anim_scan_line);
        scanLine.startAnimation(scanAnim);

        loadingOcrDialog.show();
        return loadingOcrDialog;
    }
    public void showBottomSheetSavingsOcr(Context context, Resources resources, LayoutInflater inflater, String jsonString,ModelPrimaryColor modelPrimaryColor) {
        try {
            JSONObject jsonObject = getCleanJsonObject(jsonString);

            // 1. Ekstrak Field Sesuai Prompt Tabungan/Pemasukan
            String judul = jsonObject.optString("judul", "-");
            String tanggal = jsonObject.optString("tanggal", "-");
            String nominal = jsonObject.optString("nominal","0");
            String catatan = jsonObject.optString("catatan", "-");

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
            View bottomSheetView = inflater.inflate(R.layout.layout_bottom_sheet_savings_ocr, null); // Layout Baru
            bottomSheetDialog.setContentView(bottomSheetView);

            // 2. Deklarasi Komponen UI
            TextView tvNominal = bottomSheetView.findViewById(R.id.bs_tv_nominal);
            TextView tvJudul = bottomSheetView.findViewById(R.id.bs_tv_judul);
            TextView tvTanggal = bottomSheetView.findViewById(R.id.bs_tv_tanggal);
            TextView tvCatatan = bottomSheetView.findViewById(R.id.bs_tv_catatan);

            MaterialButton btnBatal = bottomSheetView.findViewById(R.id.bs_btn_batal);
            MaterialButton btnGunakan = bottomSheetView.findViewById(R.id.bs_btn_gunakan);

            ImageView btnEditNominal = bottomSheetView.findViewById(R.id.bs_btn_edit_nominal);
            ImageView btnEditJudul = bottomSheetView.findViewById(R.id.bs_btn_edit_judul);
            ImageView btnEditTanggal = bottomSheetView.findViewById(R.id.bs_btn_edit_tanggal);
            ImageView btnEditCatatan = bottomSheetView.findViewById(R.id.bs_btn_edit_catatan);
            ImageView iconHeader = bottomSheetView.findViewById(R.id.bs_icon_header);
            int themeColor = modelPrimaryColor.getColorPrimary();


            btnBatal.setStrokeColor(ContextCompat.getColorStateList(context,themeColor));
            btnBatal.setTextColor(ContextCompat.getColor(context,themeColor));
            Tools.setBackgroundTintView(btnGunakan,modelPrimaryColor);
Tools.setImageTintView(iconHeader,modelPrimaryColor);
            // 3. Set Data Awal
            final String[] rawTotalAmount = { nominal };
            tvNominal.setText(Tools.convertCurrencyToValue(nominal));
            tvJudul.setText(judul);
            tvTanggal.setText(tanggal);
            tvCatatan.setText(catatan);

            // =======================================================
            // 4. LOGIKA TOMBOL EDIT
            // =======================================================

            btnEditJudul.setOnClickListener(v -> {
                EditText input = new EditText(context);
                input.setText(tvJudul.getText().toString());
                input.setPadding(40, 40, 40, 40);

                new AlertDialog.Builder(context)
                        .setTitle("Edit Judul / Sumber")
                        .setView(input)
                        .setPositiveButton("Simpan", (dialog, which) -> tvJudul.setText(input.getText().toString().trim()))
                        .setNegativeButton("Batal", null)
                        .show();
            });

            btnEditTanggal.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, (month + 1), year);
                    tvTanggal.setText(selectedDate);
                },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            });

            btnEditNominal.setOnClickListener(v -> {
                EditText input = new EditText(context);
                input.setText(rawTotalAmount[0]);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setPadding(40, 40, 40, 40);

                new AlertDialog.Builder(context)
                        .setTitle("Edit Nominal Pendapatan")
                        .setView(input)
                        .setPositiveButton("Simpan", (dialog, which) -> {
                            String newTotal = input.getText().toString().trim();
                            if (!newTotal.isEmpty()) {
                                rawTotalAmount[0] = newTotal;
                                tvNominal.setText(Tools.convertCurrencyToValue(newTotal));
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            });

            btnEditCatatan.setOnClickListener(v -> {
                EditText input = new EditText(context);
                input.setText(tvCatatan.getText().toString());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setMinLines(4);
                input.setPadding(40, 40, 40, 40);
                input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);

                new AlertDialog.Builder(context)
                        .setTitle("Edit Catatan Tambahan")
                        .setView(input)
                        .setPositiveButton("Simpan", (dialog, which) -> tvCatatan.setText(input.getText().toString().trim()))
                        .setNegativeButton("Batal", null)
                        .show();
            });

            // =======================================================
            // 5. TOMBOL AKSI UTAMA (Batal & Simpan)
            // =======================================================

            btnBatal.setOnClickListener(v -> bottomSheetDialog.dismiss());

            btnGunakan.setOnClickListener(v -> {
                HashMap<String,String> map = new HashMap<>();

                // Masukkan data ke Map untuk dikembalikan ke Activity
                map.put("total", tvNominal.getText().toString());
                map.put("tanggal", Tools.convertFormatDateAi(tvTanggal.getText().toString()));
                map.put("description", tvCatatan.getText().toString());
                map.put("title", tvJudul.getText().toString()); // Tambahkan key "title" untuk form savings

                this.methodCallback.onResultLLM(map);
                bottomSheetDialog.dismiss();
                Toast.makeText(context, "Data berhasil diekstrak", Toast.LENGTH_SHORT).show();
            });

            // =======================================================
            // 6. TAMPILKAN & ATUR BEHAVIOR
            // =======================================================
            bottomSheetDialog.show();

            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(false);

                int screenHeight = resources.getDisplayMetrics().heightPixels;
                behavior.setPeekHeight((int) (screenHeight * 0.45));
                behavior.setHideable(false);
            }

            bottomSheetDialog.setCancelable(false);

            View touchOutside = bottomSheetDialog.findViewById(com.google.android.material.R.id.touch_outside);
            if (touchOutside != null) {
                touchOutside.setOnClickListener(v -> {
                    new AlertDialog.Builder(context)
                            .setTitle("Buang Hasil Scan?")
                            .setMessage("Data pendapatan yang sudah berhasil diekstrak AI akan hilang.")
                            .setPositiveButton("Ya, Buang", (dialog, which) -> {
                                bottomSheetDialog.dismiss();
                            })
                            .setNegativeButton("Lanjut Edit", null)
                            .show();
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal memproses data AI. Format tidak sesuai.", Toast.LENGTH_SHORT).show();
        }
    }
    public void showBottomSheetOcr(Context context, Resources resources, LayoutInflater inflater, String jsonString,ModelPrimaryColor modelPrimaryColor) {
        try {
            JSONObject jsonObject = getCleanJsonObject(jsonString);

            String namaToko = jsonObject.optString("nama_toko", "-");
            String tanggal = jsonObject.optString("tanggal", "-");
            String total = jsonObject.optString("total","0");
            String otherFee = jsonObject.optString("other_fee", "0");
            String daftarProduk = jsonObject.optString("daftar_produk", "-");

            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
            View bottomSheetView = inflater.inflate(R.layout.layout_bottom_sheet_ocr, null);
            bottomSheetDialog.setContentView(bottomSheetView);

            TextView tvTotal = bottomSheetView.findViewById(R.id.bs_tv_total);
            TextView tvToko = bottomSheetView.findViewById(R.id.bs_tv_toko);
            TextView tvTanggal = bottomSheetView.findViewById(R.id.bs_tv_tanggal);
            TextView tvItems = bottomSheetView.findViewById(R.id.bs_tv_items);
            MaterialButton btnBatal = bottomSheetView.findViewById(R.id.bs_btn_batal);
            MaterialButton btnGunakan = bottomSheetView.findViewById(R.id.bs_btn_gunakan);
            ImageView btnEditTotal = bottomSheetView.findViewById(R.id.bs_btn_edit_total);
            ImageView btnEditToko = bottomSheetView.findViewById(R.id.bs_btn_edit_toko);
            ImageView btnEditTanggal = bottomSheetView.findViewById(R.id.bs_btn_edit_tanggal);
            ImageView btnEditItems = bottomSheetView.findViewById(R.id.bs_btn_edit_items);
            ImageView iconHeader = bottomSheetView.findViewById(R.id.bs_icon_header);

            // 1. Variabel untuk menampung total hitungan kita sendiri

            int themeColor = modelPrimaryColor.getColorPrimary();


            btnBatal.setStrokeColor(ContextCompat.getColorStateList(context,themeColor));
            btnBatal.setTextColor(ContextCompat.getColor(context,themeColor));

            Tools.setBackgroundTintView(btnGunakan,modelPrimaryColor);
            Tools.setImageTintView(iconHeader,modelPrimaryColor);
            long calculatedTotal = 0;

            // 2. Susun daftar produk sekaligus menghitung harganya
            StringBuilder itemDetails = new StringBuilder();
            String[] produkArray = daftarProduk.split(",");

            for (String produk : produkArray) {
                String itemText = produk.trim(); // Contoh: "Ultra Plain 200Ml harga 6500"
                itemDetails.append("- ").append(itemText).append("\n");

                String[] parts = itemText.split(" harga ");
                if (parts.length == 2) {
                    try {
                        long hargaItem = Long.parseLong(parts[1].trim());
                        calculatedTotal += hargaItem;
                    } catch (NumberFormatException e) {
                        Log.e("HITUNG", "Gagal membaca angka harga dari item: " + itemText);
                    }
                }
            }
            String finalTotalString = String.valueOf(calculatedTotal);
            String cleanTotal = total.replaceAll("[^0-9]", "");
            if (cleanTotal.isEmpty()) cleanTotal = "0";

            long parsedTotal = Long.parseLong(cleanTotal);

            if(parsedTotal == 0){
                tvTotal.setText(Tools.convertCurrencyToValue(finalTotalString));
            } else {
                tvTotal.setText(Tools.convertCurrencyToValue(cleanTotal));
            }
            tvToko.setText(namaToko);
            tvTanggal.setText(tanggal);


            if (!otherFee.equals("0")) {
                itemDetails.append("\n(Pajak/Biaya Lain/Kembalian: Rp ").append(Tools.convertCurrencyToValue(otherFee)).append(")");
            }
            tvItems.setText(itemDetails.toString().trim());

            btnEditToko.setOnClickListener(v -> {
                EditText input = new EditText(context);
                input.setText(tvToko.getText().toString());
                input.setPadding(40, 40, 40, 40);

                new AlertDialog.Builder(context)
                        .setTitle("Edit Nama Toko")
                        .setView(input)
                        .setPositiveButton("Simpan", (dialog, which) -> tvToko.setText(input.getText().toString().trim()))
                        .setNegativeButton("Batal", null)
                        .show();
            });
            final String[] rawTotalAmount = { String.valueOf(calculatedTotal>0?calculatedTotal:Long.parseLong(total)) };
            // EDIT TANGGAL
            btnEditTanggal.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, (month + 1), year);
                    tvTanggal.setText(selectedDate);
                },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            });

            btnEditTotal.setOnClickListener(v -> {
                EditText input = new EditText(context);
                input.setText(rawTotalAmount[0]); // Tampilkan angka mentah (misal: 42600)
                input.setInputType(InputType.TYPE_CLASS_NUMBER); // Paksa keyboard angka
                input.setPadding(40, 40, 40, 40);

                new AlertDialog.Builder(context)
                        .setTitle("Edit Total Transaksi")
                        .setView(input)
                        .setPositiveButton("Simpan", (dialog, which) -> {
                            String newTotal = input.getText().toString().trim();
                            if (!newTotal.isEmpty()) {
                                rawTotalAmount[0] = newTotal; // Simpan angka mentahnya
                                tvTotal.setText(Tools.convertCurrencyToValue(newTotal)); // Tampilkan dengan Rp di layar
                            }
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            });

            btnEditItems.setOnClickListener(v -> {
                EditText input = new EditText(context);
                input.setText(tvItems.getText().toString());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                input.setMinLines(4);
                input.setPadding(40, 40, 40, 40);
                input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);

                new AlertDialog.Builder(context)
                        .setTitle("Edit Rincian Item")
                        .setView(input)
                        .setPositiveButton("Simpan", (dialog, which) -> tvItems.setText(input.getText().toString().trim()))
                        .setNegativeButton("Batal", null)
                        .show();
            });

            btnBatal.setOnClickListener(v -> bottomSheetDialog.dismiss());

            btnGunakan.setOnClickListener(v -> {
                HashMap<String,String> map = new HashMap<>();

                // 1. Ambil data TERBARU dari TextView yang mungkin sudah diedit user
                String updatedToko = tvToko.getText().toString();
                String updatedTanggal = tvTanggal.getText().toString();
                String updatedItems = tvItems.getText().toString();
                String updatedTotal = rawTotalAmount[0]; // Dari variabel array yang menyimpan angka mentah

                // 2. Susun ulang description
                String finalDescription = "Toko: " + updatedToko + "\nBarang:\n" + updatedItems;

                // 3. Masukkan ke Map
                map.put("total", updatedTotal);

                // Pastikan fungsi Tools Anda bisa menangani format "dd/MM/yyyy" yang baru dari DatePicker
                map.put("tanggal", Tools.convertFormatDateAi(updatedTanggal));
                map.put("description", finalDescription);

                this.methodCallback.onResultLLM(map);
                bottomSheetDialog.dismiss();
                Toast.makeText(context, "Data struk berhasil dimasukkan", Toast.LENGTH_SHORT).show();
            });

            bottomSheetDialog.show();
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(false);

                int screenHeight = resources.getDisplayMetrics().heightPixels;
                behavior.setPeekHeight((int) (screenHeight * 0.45));
                behavior.setHideable(false);
            }

            bottomSheetDialog.setCancelable(false);

            View touchOutside = bottomSheetDialog.findViewById(com.google.android.material.R.id.touch_outside);
            if (touchOutside != null) {
                touchOutside.setOnClickListener(v -> {
                    // Saat area hitam diklik, jangan langsung ditutup! Tampilkan konfirmasi:
                    new AlertDialog.Builder(context)
                            .setTitle("Buang Hasil Scan?")
                            .setMessage("Data yang sudah berhasil diekstrak AI akan hilang dan tidak dimasukkan ke form.")
                            .setPositiveButton("Ya, Buang", (dialog, which) -> {
                                bottomSheetDialog.dismiss();
                            })
                            .setNegativeButton("Lanjut Edit", null) // Batal menutup, biarkan Bottom Sheet tetap ada
                            .show();
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Gagal memproses data AI. Format tidak sesuai.", Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    private static JSONObject getCleanJsonObject(String jsonString) throws JSONException {
        String cleanJson = jsonString;

        int startIndex = cleanJson.indexOf("{");
        int endIndex = cleanJson.lastIndexOf("}");

        if (startIndex != -1 && endIndex != -1 && startIndex <= endIndex) {
            cleanJson = cleanJson.substring(startIndex, endIndex + 1);
        } else {
            throw new JSONException("Format JSON tidak ditemukan dari AI.");
        }
        return new JSONObject(cleanJson);
    }
}
