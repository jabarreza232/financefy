package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.helper.TinyDb.isExternalStorageWritable;
import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.AboutActivity;
import id.co.evolution.financefy.activity.ExportHistoryActivity;
import id.co.evolution.financefy.activity.FAQActivity;
import id.co.evolution.financefy.activity.NotificationActivity;
import id.co.evolution.financefy.activity.PinActivity;
import id.co.evolution.financefy.activity.SwitchThemeActivity;
import id.co.evolution.financefy.databinding.FragmentSettingsBinding;
import id.co.evolution.financefy.dialog.DateRangeDialog;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogLoading;
import id.co.evolution.financefy.dialog.DialogSettingPin;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelRepository;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
import id.co.evolution.financefy.repository.WorkManagerModelRepository;
import id.co.evolution.financefy.state.DownloadState;
import id.co.evolution.financefy.viewmodel.LlmViewModel;
import id.co.evolution.financefy.viewmodel.ViewModelFinance;
import id.co.evolution.financefy.viewmodel.ViewModelSavingsProgress;

@AndroidEntryPoint
public class FragmentSettings extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 100;

    MainActivity mainActivity;
    @Inject
    TinyDb tinyDb;
    public ViewModelFinance viewModelFinance;
    public ViewModelSavingsProgress viewModelSavingsProgress;
    @Inject
    FinanceRepository financeRepository;
    @Inject
    SavingsProgressRepository savingsProgressRepository;
    DialogLoading dialogLoading;
    public List<ModelFinance> dataFinance = new ArrayList<>();
    public List<ModelSavingsProgress> dataSaving = new ArrayList<>();

    public ModelUser user;
    private InterstitialAd mInterstitialAd;
    private long currentDownloadId = -1; // Menyimpan ID proses unduhan
    private boolean isDownloading = false; // Mencegah dialog muncul saat sedang unduh
    private static final String TAG = "FragmentSettings";
    private Locale locale;
    public enum MENU{

        FROM_SWITCH_THEME,
        FROM_EXPORT_EXCEL
    }
    MENU menuSettings;
    public FragmentSettings() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = ((MainActivity) context);
    }

    FragmentSettingsBinding binding;
    File modelFile;
    private LlmViewModel viewModel;
    private Long filterStartDate = null;
    private Long filterEndDate = null;
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        user = tinyDb.getObject("user", ModelUser.class);
        locale =user.getType_currency().equalsIgnoreCase("IDR")? Tools.getLocaleIDN():Tools.getLocaleUS();
        dialogLoading = new DialogLoading(getActivity());
        binding.cvSwitchTheme.setOnClickListener(v -> {
            menuSettings = MENU.FROM_SWITCH_THEME;
            adRequest();

        });


//        binding.txtDeleteCache.setText("Hapus Cache ("+formatSize(Tools.getCacheSize(getContext()))+")");
//        binding.txtDeleteCache.setOnClickListener(v->{
//            if (checkPermission()) {
//                Tools.clearCache(getContext());
//                binding.txtDeleteCache.setText("Hapus Cache ("+formatSize(Tools.getCacheSize(getContext()))+")");
//            } else {
//                requestPermission();
//            }
//        });
         modelFile = new File(requireContext().getExternalFilesDir(null), "Qwen2_0.5B_Instruct.litertlm");
 // 2. Aksi ketika CardView ditekan (Tampilkan Dialog)
        if (modelFile.exists()) {
            binding.imgArrowLlm.setImageResource(R.drawable.ic_baseline_settings_24);
            binding.switchLlm.setEnabled(true);
        }else{
            binding.switchLlm.setEnabled(false);
        }
        binding.cvDownloadLlm.setOnClickListener(v -> {
           processDownload();
        });
        boolean isLlmActive = tinyDb.getBoolean("isSwitchLLM",false);
        binding.switchLlm.setChecked(isLlmActive);

        binding.switchLlm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Simpan ke TinyDB
            tinyDb.putBoolean("isSwitchLLM", isChecked);

            String pesan = isChecked ?
                    "Scan AI aktif. Privasi data Anda terjaga." :
                    "Mode Scan AI dinonaktifkan.";

            // Membuat Snackbar (menggunakan requireView() karena ini di dalam Fragment)
            Snackbar snackbar = Snackbar.make(requireView(), pesan, Snackbar.LENGTH_SHORT);

            // (PENTING) Jika Snackbar tertutup oleh Bottom Navigation, gunakan Anchor View:
             View bottomNav = requireActivity().findViewById(R.id.bn_main);
             snackbar.setAnchorView(bottomNav);

            snackbar.show();
        });
        binding.cardExportHistory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ExportHistoryActivity.class);
            startActivity(intent);
        });
        binding.imgCancelLlm.setOnClickListener(v -> {
            if (isDownloading && currentDownloadId != -1) {
                DownloadManager manager = (DownloadManager) requireContext().getSystemService(Context.DOWNLOAD_SERVICE);

                // MEMBATALKAN UNDUHAN DAN MENGHAPUS FILE SEMENTARA
                manager.remove(currentDownloadId);

                // Kembalikan status UI ke semula
                isDownloading = false;
                currentDownloadId = -1;

                binding.progressBarLlm.setVisibility(View.GONE);
                binding.imgCancelLlm.setVisibility(View.GONE); // Sembunyikan X
                binding.imgArrowLlm.setVisibility(View.VISIBLE); // Munculkan panah lagi

                binding.txtStatusLlm.setText("Dibatalkan");
                // binding.txtStatusLlm.setTextColor(getResources().getColor(R.color.red));
            }
        });
        binding.txtNotification.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), NotificationActivity.class);
            startActivity(i);
        });
        changeStatusPin();
        boolean isFingerprintActive = tinyDb.getBoolean("is_fingerprint_active");
        binding.switchFingerprint.setChecked(isFingerprintActive);

        binding.switchFingerprint.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Cek apakah HP mendukung sidik jari
                BiometricManager biometricManager = BiometricManager.from(requireContext());
                if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {

                    // Panggil pop-up sidik jari bawaan HP
                    Executor executor = ContextCompat.getMainExecutor(requireContext());
                    BiometricPrompt biometricPrompt = new BiometricPrompt(FragmentSettings.this, executor, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            tinyDb.putBoolean("is_fingerprint_active", true);
                            Toast.makeText(requireContext(), "Sidik Jari berhasil diaktifkan!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            // Kembalikan switch jika batal/gagal
                            binding.switchFingerprint.setChecked(false);
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            // Gagal scan (jari salah)
                        }
                    });

                    BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Verifikasi Sidik Jari")
                            .setSubtitle("Gunakan sidik jari Anda untuk mengaktifkan fitur ini")
                            .setNegativeButtonText("Batal")
                            .build();

                    biometricPrompt.authenticate(promptInfo);

                } else {
                    Toast.makeText(requireContext(), "HP Anda tidak mendukung atau belum mengatur Sidik Jari", Toast.LENGTH_SHORT).show();
                    binding.switchFingerprint.setChecked(false);
                }
            } else {
                // Matikan fitur
                tinyDb.putBoolean("is_fingerprint_active", false);
            }
        });
        binding.txtPinSetting.setOnClickListener(v -> {
            showOnSettingPIN();
        });
        binding.switchPin.setChecked(mainActivity.isPinSetting);

        binding.cardSettingPin.setOnClickListener(v->{
            showOnSettingPIN();
        });
        binding.switchPin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;

            if (isChecked) {
                binding.switchPin.setChecked(false);

                showOnSettingPIN();

            } else {
                binding.switchPin.setChecked(true);

                DialogConfirm dialogConfirm = new DialogConfirm(getContext(), inflater, result -> {
                    if (result.equalsIgnoreCase("yes")) {
                        Toast.makeText(getContext(), "PIN telah berhasil di non aktifkan !", Toast.LENGTH_SHORT).show();

                        // Hapus data dari TinyDB dan Activity
                        tinyDb.putString("pin", "");
                        tinyDb.putBoolean("isSettingPin", false);
                        mainActivity.isPinSetting = false;

                        // Jika pengguna menekan "Yes", matikan switch secara permanen
                        binding.switchPin.setChecked(false);
                    }
                });

                dialogConfirm.showDialogConfirm("Menonaktifkan PIN", "Apakah anda yakin ingin menonaktifkan PIN anda ? ");
            }
        });
        binding.txtMoney.setOnClickListener(v -> {
            setCurrencySettings();
        });
        binding.txtInfo.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AboutActivity.class);
            startActivity(i);
        });
        binding.cvFaq.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FAQActivity.class);
            startActivity(intent);
        });
        binding.txtExportExcel.setOnClickListener(v -> {
            menuSettings = MENU.FROM_EXPORT_EXCEL;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
                exportToExcelWithCondition();

            }else{
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                } else {
                    exportToExcelWithCondition();

                }

            }
        });


//        changeColorThemeSettings(mainActivity.modelPrimaryColor);
        return binding.getRoot();
    }
    private void showDialogLlmSettings() {
        // 1. CEK STATUS UNDUHAN LLM
        // Ganti variabel ini dengan logika/fungsi Anda yang mengecek apakah file model.bin sudah ada di storage

        // 2. SIAPKAN CUSTOM VIEW
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_llm_settings, null);

        Slider sliderToken = dialogView.findViewById(R.id.slider_dialog_max_token);
        TextView txtTokenValue = dialogView.findViewById(R.id.txt_dialog_token_value);
        TextView txtTokenIndicator = dialogView.findViewById(R.id.txt_dialog_token_indicator);

        float savedToken = tinyDb.getInt("max_tokens",2048);

        sliderToken.setValue(savedToken);
        updateTokenUI(savedToken, txtTokenValue, txtTokenIndicator);

        // Listener Slider
        sliderToken.addOnChangeListener((slider, value, fromUser) -> {
            updateTokenUI(value, txtTokenValue, txtTokenIndicator);
        });

        // 3. TAMPILKAN DIALOG
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Simpan", (dialogInterface, i) -> {
                    // Simpan ke SharedPreferences saat user klik Simpan
                    float finalValue = sliderToken.getValue();
                    tinyDb.putInt("max_tokens",(int)finalValue);
                    Toast.makeText(requireContext(), "Pengaturan AI berhasil disimpan", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .create();

        dialog.show();
    }

    // Fungsi pembantu untuk mengupdate teks indikator
    private void updateTokenUI(float value, TextView txtVal, TextView txtInd) {
        int token = (int) value;
        txtVal.setText(token + " Token");

        if (token <= 512) {
            txtInd.setText("Performa: Sangat Cepat\nSaran: Berisiko 'Lazy', kurang cocok untuk struk belanja panjang.");
        } else if (token <= 1024) {
            txtInd.setText("Performa: Seimbang\nSaran: Cukup akurat untuk struk belanja pendek atau menengah.");
        } else {
            txtInd.setText("Performa: Sedikit Lebih Lama\nSaran: Sangat Akurat! Disarankan agar semua item pada struk terbaca sempurna.");
        }
    }
    private void processDownload() {
        if (modelFile.exists()) {
            showDialogLlmSettings();
            return;
        }
        showDownloadDialog();
    }

    private void showDownloadDialog() {
        View dialogView = requireActivity().getLayoutInflater().inflate(R.layout.dialog_llm, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_cancel_dialog).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_download_dialog).setOnClickListener(v -> {
            dialog.dismiss();
            executeDownload();
        });

        dialog.show();
    }

    private void executeDownload() {
        String url = "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm?download=true";
        String fileName = "Qwen2_0.5B_Instruct.litertlm";

        // Set UI awal
        binding.progressBarLlm.setVisibility(View.VISIBLE);
        binding.imgArrowLlm.setVisibility(View.INVISIBLE);
        binding.imgCancelLlm.setVisibility(View.VISIBLE);

        binding.imgCancelLlm.setOnClickListener(v -> viewModel.cancelDownload());

        // Observasi status dari ViewModel
        viewModel.startDownload(url, fileName).observe(getViewLifecycleOwner(), state -> {
            switch (state.status) {
                case DownloadState.RUNNING:
                    binding.progressBarLlm.setProgress(state.progress);
                    binding.txtStatusLlm.setText(String.format("%d%% (%.1f MB/s)", state.progress, state.speedMb));
                    break;

                case DownloadState.SUCCESS:
                    binding.progressBarLlm.setVisibility(View.GONE);
                    binding.imgCancelLlm.setVisibility(View.GONE);
                    binding.imgArrowLlm.setVisibility(View.VISIBLE);
                    binding.txtStatusLlm.setText("Terunduh");
                    binding.switchLlm.setEnabled(true);
                    binding.txtStatusLlm.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextGreen));
                    break;

                case DownloadState.FAILED:
                    binding.progressBarLlm.setVisibility(View.GONE);
                    binding.imgCancelLlm.setVisibility(View.GONE);
                    binding.imgArrowLlm.setVisibility(View.VISIBLE);
                    binding.txtStatusLlm.setText("Dibatalkan / Gagal");
                    binding.txtStatusLlm.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    break;
            }
        });
    }
    private void setupViewModel() {
        // Inisialisasi manual (Jika belum memakai Hilt/Dagger)
        ModelRepository repository = new WorkManagerModelRepository(requireContext());
        viewModel = new LlmViewModel(repository);
        observeDownloadStatus();
    }

    private void observeDownloadStatus() {
        viewModel.getDownloadState().observe(getViewLifecycleOwner(), state -> {
            switch (state.status) {
                case DownloadState.IDLE:
                    // Cek ketersediaan file seperti kode awal Anda
                    File modelFile = new File(requireContext().getExternalFilesDir(null), "Qwen2_0.5B_Instruct.litertlm");
                    if (modelFile.exists()) {
                        setUITerunduh();
                    } else {
                        setUIBelumTerunduh();
                    }
                    break;

                case DownloadState.RUNNING:
                    // Jika user pindah Activity lalu kembali, UI langsung menyesuaikan!
                    binding.progressBarLlm.setVisibility(View.VISIBLE);
                    binding.imgArrowLlm.setVisibility(View.INVISIBLE);
                    binding.imgCancelLlm.setVisibility(View.VISIBLE);

                    binding.progressBarLlm.setProgress(state.progress);
                    binding.txtStatusLlm.setText(String.format("%d%% (%.1f MB/s)", state.progress, state.speedMb));
                    binding.txtStatusLlm.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextYellow)); // Atau warna progress
                    break;

                case DownloadState.SUCCESS:
                    setUITerunduh();
                    break;

                case DownloadState.FAILED:
                    setUIBelumTerunduh(); // Atau tampilkan pesan gagal
                    break;
            }
        });
    }

    // Method Helper untuk UI
    private void setUITerunduh() {
        binding.progressBarLlm.setVisibility(View.GONE);
        binding.imgCancelLlm.setVisibility(View.GONE);
        binding.imgArrowLlm.setVisibility(View.VISIBLE);

        binding.txtStatusLlm.setText("Terunduh");
        binding.txtStatusLlm.setTextColor(ContextCompat.getColor(getContext(), R.color.colorTextGreen));
    }

    private void setUIBelumTerunduh() {
        binding.progressBarLlm.setVisibility(View.GONE);
        binding.imgCancelLlm.setVisibility(View.GONE);
        binding.imgArrowLlm.setVisibility(View.VISIBLE);

        binding.txtStatusLlm.setText("Belum Terunduh");
        binding.txtStatusLlm.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
    }
    private void adRequest(){
    AdRequest adRequest = new AdRequest.Builder().build();
    //official ad unit id = ca-app-pub-5068422046187558/6331529776
    //example ad unit id = ca-app-pub-3940256099942544/1033173712
    dialogLoading.show("Silahkan Tunggu...");
    InterstitialAd.load(getContext(),"ca-app-pub-5068422046187558/6331529776", adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd;
                    Log.i(TAG, "onAdLoaded");
                    mInterstitialAd.show(getActivity());

                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Log.d(TAG, "Ad was clicked.");
                            dialogLoading.show("Silahkan Tunggu...");
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Log.d(TAG, "Ad dismissed fullscreen content.");
                            dialogLoading.dismiss();
                            if(menuSettings == MENU.FROM_SWITCH_THEME){
                                Intent i = new Intent(getContext(), SwitchThemeActivity.class);
                                startActivity(i);
                                getActivity().finish();
                            }else{
                                showBottomSheetExportFilter();
                            }

                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Called when ad fails to show.
                            Log.e(TAG, "Ad failed to show fullscreen content.");
                            mInterstitialAd = null;
                            dialogLoading.dismiss();

                        }

                        @Override
                        public void onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Log.d(TAG, "Ad recorded an impression.");
                            dialogLoading.show("Silahkan Tunggu...");

                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Log.d(TAG, "Ad showed fullscreen content.");
                            dialogLoading.dismiss();

                        }
                    });

                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    // Handle the error
                    Log.d(TAG, loadAdError.toString());
                    mInterstitialAd = null;
                    dialogLoading.dismiss();
                }
            });

}

//    private void openDateRangePicker() {
//        DateRangeDialog dateRangeDialog = new DateRangeDialog(getActivity(), (startDate, endDate, datesList) -> {
//            Log.e("cek:", startDate + " : " + endDate);
//
//            if (user.getCategory().equalsIgnoreCase(getString(R.string.menabung))) {
//
//                // 1. Tampung LiveData di variabel
//                LiveData<List<ModelSavingsProgress>> liveDataSaving = viewModelSavingsProgress.getSavingsByWeek(datesList, mainActivity.modelSavings.getId(), user.getType_currency());
//
//                // 2. Gunakan cara penjabaran (new Observer) agar kita bisa menyebut 'this'
//                liveDataSaving.observe(getViewLifecycleOwner(), new Observer<List<ModelSavingsProgress>>() {
//                    @Override
//                    public void onChanged(List<ModelSavingsProgress> modelFinances) {
//                        // 3. HANCURKAN OBSERVER SEGERA SETELAH DATA DITERIMA (One-Shot Request)
//                        liveDataSaving.removeObserver(this);
//
//                        dataSaving = modelFinances;
//                        if (dataSaving.isEmpty()) {
//                            Toast.makeText(getContext(), "Tidak ada data di rentang tanggal ini", Toast.LENGTH_SHORT).show();
//                        } else {
//                            exportExcelByData(new ArrayList<>(dataSaving));
//                        }
//                    }
//                });
//
//            } else {
//
//                // 1. Tampung LiveData di variabel
//                LiveData<List<ModelFinance>> liveDataFinance = viewModelFinance.getFinanceByWeek(datesList, user.getId(), user.getType_currency());
//
//                // 2. Gunakan cara penjabaran (new Observer)
//                liveDataFinance.observe(getViewLifecycleOwner(), new Observer<List<ModelFinance>>() {
//                    @Override
//                    public void onChanged(List<ModelFinance> modelFinances) {
//                        // 3. HANCURKAN OBSERVER SEGERA (One-Shot Request)
//                        liveDataFinance.removeObserver(this);
//
//                        dataFinance = modelFinances;
//                        if (dataFinance.isEmpty()) {
//                            Toast.makeText(getContext(), "Tidak ada data keuangan di rentang tanggal ini", Toast.LENGTH_SHORT).show();
//                        } else {
//                            exportExcelByData(new ArrayList<>(dataFinance));
//                        }
//                    }
//                });
//            }
//        });
//
//        dateRangeDialog.show();
//    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() != null && getActivity().getIntent() != null) {
            boolean triggerExport = getActivity().getIntent().getBooleanExtra("ACTION_TRIGGER_EXPORT", false);

            if (triggerExport) {
                // Tampilkan Bottom Sheet
                showBottomSheetExportFilter();

                // Hapus pesan agar Bottom Sheet tidak muncul lagi kalau user pindah tab lalu kembali ke Settings
                getActivity().getIntent().removeExtra("ACTION_TRIGGER_EXPORT");
            }
        }
        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);

        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);

        viewModelFinance.init(financeRepository);
        viewModelSavingsProgress.init(savingsProgressRepository);
        setupViewModel();
        viewModelFinance.getAllFinanceByDate(getFormattedDateSimple(System.currentTimeMillis()), user.getId(), user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
            dataFinance = modelFinances;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan
                exportToExcelWithCondition();

            } else {
                // Izin ditolak
                Toast.makeText(getContext(), "Izin ditolak, aplikasi tidak dapat menyimpan atau membaca file.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void showDialogSettingPin(){
        DialogSettingPin dialogSettingPin = new DialogSettingPin(getContext(), getLayoutInflater(), new DialogSettingPin.DialogInterfaceCallback() {
            @Override
            public void onSubmit(String result) {
                tinyDb.putString("pin", result);
                tinyDb.putBoolean("isSettingPin", true);
                mainActivity.isPinSetting = true;
                changeStatusPin();
            }
        });

        dialogSettingPin.show();
    }
    private void showBiometricPrompt() {
        BiometricManager biometricManager = BiometricManager.from(getContext());

        // Pastikan HP mendukung biometrik dan sudah ada sidik jari yang terdaftar
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {

            Executor executor = ContextCompat.getMainExecutor(getContext());
            BiometricPrompt biometricPrompt = new BiometricPrompt(getActivity(), executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    showDialogSettingPin();
                }

                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);

                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    // Sidik jari salah/tidak dikenali (Sistem Android akan mengurus pesannya otomatis)
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Sidik Jari")
                    .setSubtitle("Gunakan sidik jari untuk mengubah pin")
                    .setNegativeButtonText("Batal") // Tombol fallback ke keypad
                    .build();

            biometricPrompt.authenticate(promptInfo);
        }
    }
    private void showOnSettingPIN() {
        if(!mainActivity.isPinSetting&&tinyDb.getBoolean("is_fingerprint_active")){
            showBiometricPrompt();
        }else{
            showDialogSettingPin();
        }
    }

    private void changeStatusPin() {
        binding.switchPin.setChecked(mainActivity.isPinSetting);

    }

    private void setCurrencySettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Mata Uang");
        final String[] tipe = {"IDR (Rp)", "USD ($)"};

        builder.setItems(tipe, (dialog, which) -> {
            switch (which) {
                case 0:
                    tinyDb.putString("currency", "IDR");
                    dialog.dismiss();
                    break;
                case 1:
                    tinyDb.putString("currency", "USD");
                    dialog.dismiss();
                    break;
            }

            binding.txtSelectedMoney.setText(tipe[which]);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void exportToExcelWithCondition() {
        // Buat Workbook baru

        adRequest();
    }
    public static <T> boolean isOfType(Object input) {
        return input != null; // won't compile
    }
    private void exportExcelByData(List<Object> data, String filterCategory) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(user.getType());

        Row headerNameUserRow = sheet.createRow(0);
        headerNameUserRow.createCell(0).setCellValue("Nama: " + user.getName());

        Row headerRow = sheet.createRow(1);
        headerRow.createCell(0).setCellValue("Kategori: " + user.getCategory());

        // Buat CellStyle untuk Wrap Text sekali saja di luar loop
        CellStyle wrapTextStyle = workbook.createCellStyle();
        wrapTextStyle.setWrapText(true);

        if (filterCategory.equalsIgnoreCase(getString(R.string.jurnal_keuangan))) {
            List<ModelFinance> dataFinance = new ArrayList<>();
            for (Object object : data) {
                dataFinance.add((ModelFinance) object);
            }

            Row columnHeaderRow = sheet.createRow(6);
            columnHeaderRow.createCell(0).setCellValue("ID");
            columnHeaderRow.createCell(1).setCellValue("Jumlah");
            columnHeaderRow.createCell(2).setCellValue("Kategori");
            columnHeaderRow.createCell(3).setCellValue("Keterangan");
            columnHeaderRow.createCell(4).setCellValue("Tanggal");
            columnHeaderRow.createCell(5).setCellValue("Tipe");
            columnHeaderRow.createCell(6).setCellValue("Bulan");

            int rowNum = 7;
            for (ModelFinance modelFinance : dataFinance) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(modelFinance.getId());
                dataRow.createCell(1).setCellValue(modelFinance.getJumlahDesc(locale));
                dataRow.createCell(2).setCellValue(modelFinance.getKategori());

                // Terapkan Wrap Text tanpa memanipulasi Height secara manual
                Cell cellKet = dataRow.createCell(3);
                cellKet.setCellStyle(wrapTextStyle);
                cellKet.setCellValue(modelFinance.getKeterangan());

                dataRow.createCell(4).setCellValue(modelFinance.getDate());
                dataRow.createCell(5).setCellValue(modelFinance.getTipe());
                dataRow.createCell(6).setCellValue(modelFinance.getMonth());
            }

            // SET COLUMN WIDTH DI LUAR LOOP (Setelah semua data masuk)
            sheet.setColumnWidth(0, 3000); // ID
            sheet.setColumnWidth(1, 5000); // Jumlah
            sheet.setColumnWidth(2, 5000); // Kategori
            sheet.setColumnWidth(3, 10000); // Keterangan (Dibuat lebar agar Wrap Text bekerja)
            sheet.setColumnWidth(4, 4000); // Tanggal
            sheet.setColumnWidth(5, 4000); // Tipe
            sheet.setColumnWidth(6, 4000); // Bulan

        } else {
            Row headerSavingsNameRow = sheet.createRow(2);
            headerSavingsNameRow.createCell(0).setCellValue("Target: " + mainActivity.modelSavings.getTitle());

            Row headerSavingsTargetRow = sheet.createRow(3);
            headerSavingsTargetRow.createCell(0).setCellValue("Jumlah: " + Tools.convertToCurrency(mainActivity.modelSavings.getTargetValue(), locale));

            List<ModelSavingsProgress> dataSavings = new ArrayList<>();
            for (Object object : data) {
                dataSavings.add((ModelSavingsProgress) object);
            }

            Row columnHeaderRow = sheet.createRow(6);
            columnHeaderRow.createCell(0).setCellValue("ID");
            columnHeaderRow.createCell(1).setCellValue("Judul");
            columnHeaderRow.createCell(2).setCellValue("Keterangan");
            columnHeaderRow.createCell(3).setCellValue("Jumlah");
            columnHeaderRow.createCell(4).setCellValue("Tanggal");
            columnHeaderRow.createCell(5).setCellValue("Bulan");

            int rowNum = 7;
            for (ModelSavingsProgress modelSavingsProgress : dataSavings) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(modelSavingsProgress.getId());
                dataRow.createCell(1).setCellValue(modelSavingsProgress.getTitle());

                Cell cellKet = dataRow.createCell(2);
                cellKet.setCellStyle(wrapTextStyle);
                cellKet.setCellValue(modelSavingsProgress.getDescription());

                dataRow.createCell(3).setCellValue(Tools.convertToCurrency(modelSavingsProgress.getProcessValue(), locale));
                dataRow.createCell(4).setCellValue(modelSavingsProgress.getDate_progress_savings());
                dataRow.createCell(5).setCellValue(modelSavingsProgress.getMonth());
            }

            // SET COLUMN WIDTH DI LUAR LOOP
            sheet.setColumnWidth(0, 3000); // ID
            sheet.setColumnWidth(1, 6000); // Judul
            sheet.setColumnWidth(2, 10000); // Keterangan
            sheet.setColumnWidth(3, 5000); // Jumlah
            sheet.setColumnWidth(4, 4000); // Tanggal
            sheet.setColumnWidth(5, 4000); // Bulan
        }

        String fileName = user.getCategory() + "_" + Calendar.getInstance().getTimeInMillis() + ".xlsx";
        File file;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file = new File(getContext().getExternalFilesDir(null), fileName);
        } else {
            file = new File(Environment.getExternalStorageDirectory(), fileName);
        }

        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
            workbook.close();

            Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);

            Toast.makeText(getContext(), "Berhasil diekspor!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Bagikan Excel via..."));

        } catch (IOException e) {
            Toast.makeText(getContext(), "Gagal menyimpan file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
   private void showBottomSheetExportFilter() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_export, null);
        bottomSheetDialog.setContentView(view);

        // Inisiasi Komponen
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_filter);

        // Layout Kontainer
        LinearLayout layoutMonth = view.findViewById(R.id.layout_filter_month);
        LinearLayout layoutYear = view.findViewById(R.id.layout_filter_year);
        LinearLayout layoutRange = view.findViewById(R.id.layout_filter_range);

        // Input Dropdown
        AutoCompleteTextView dropdownMonth = view.findViewById(R.id.dropdown_month);
        AutoCompleteTextView dropdownYearForMonth = view.findViewById(R.id.dropdown_year_for_month);
        AutoCompleteTextView dropdownYearOnly = view.findViewById(R.id.dropdown_year_only);

        // Rentang Tanggal
       TextInputEditText etPickDateRange = view.findViewById(R.id.et_pick_date_range);
        MaterialButton btnExportAction = view.findViewById(R.id.btn_export_action);

        // 1. Setup Tab Layout Item
        tabLayout.addTab(tabLayout.newTab().setText("Bulan"));
        tabLayout.addTab(tabLayout.newTab().setText("Tahun"));
        tabLayout.addTab(tabLayout.newTab().setText("Rentang"));

        // 2. Setup Data Dropdown (Bulan & Tahun)
        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        String[] years = {"2024", "2025", "2026", "2027"}; // Bisa di-generate dinamis

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, months);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, years);

       dropdownMonth.setAdapter(monthAdapter);
       dropdownYearForMonth.setAdapter(yearAdapter);
       dropdownYearOnly.setAdapter(yearAdapter);

       Calendar now = Calendar.getInstance();

       String currentMonth = months[now.get(Calendar.MONTH)];
       String currentYear = String.valueOf(now.get(Calendar.YEAR));

       // Isi ke dalam dropdown (Gunakan false agar tidak memunculkan popup list saat di-set)
       dropdownMonth.setText(currentMonth, false);
       dropdownYearForMonth.setText(currentYear, false);
       dropdownYearOnly.setText(currentYear, false);

        // 3. Logika Perpindahan Tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Sembunyikan semua terlebih dahulu
                layoutMonth.setVisibility(View.GONE);
                layoutYear.setVisibility(View.GONE);
                layoutRange.setVisibility(View.GONE);

                // Tampilkan berdasarkan tab yang diklik
                switch (tab.getPosition()) {
                    case 0: layoutMonth.setVisibility(View.VISIBLE); break;
                    case 1: layoutYear.setVisibility(View.VISIBLE); break;
                    case 2: layoutRange.setVisibility(View.VISIBLE); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
       AutoCompleteTextView dropdownCategory = view.findViewById(R.id.dropdown_category);

       String[] kategoriList = {"Jurnal Uang", "Menabung"};
       ArrayAdapter<String> kategoriAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, kategoriList);
       dropdownCategory.setAdapter(kategoriAdapter);

       String categoryFromNotif = null;
       if (getActivity() != null && getActivity().getIntent() != null) {
           categoryFromNotif = getActivity().getIntent().getStringExtra("EXPORT_CATEGORY");
       }

       if (categoryFromNotif != null && !categoryFromNotif.isEmpty()) {
           dropdownCategory.setText(categoryFromNotif, false); // Dari notifikasi
       } else {
           dropdownCategory.setText(user.getCategory(), false); // Default aplikasi
       }

        // 5. Logika Tombol Ekspor
        btnExportAction.setOnClickListener(v -> {
            int selectedTab = tabLayout.getSelectedTabPosition();
            List<String> datesList = new ArrayList<>();

            if (selectedTab == 0) {
                // ==========================================
                // TAB 1: EKSPOR BERDASARKAN BULAN & TAHUN
                // ==========================================
                String selectedMonth = dropdownMonth.getText().toString();
                String selectedYear = dropdownYearForMonth.getText().toString();

                if (selectedMonth.isEmpty() || selectedYear.isEmpty()) {
                    Toast.makeText(requireContext(), "Pilih bulan dan tahun terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return; // Hentikan proses jika kosong
                }

                int monthIndex = getMonthIndex(selectedMonth);
                int year = Integer.parseInt(selectedYear);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthIndex);

                // Atur ke tanggal 1 (Hari pertama di bulan tersebut)
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                long startOfMonth = calendar.getTimeInMillis();

                // Atur ke tanggal terakhir di bulan tersebut (misal 28, 30, atau 31)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                long endOfMonth = calendar.getTimeInMillis();

                // Buat List Tanggal dan panggil ViewModel
                datesList = generateDatesBetween(startOfMonth, endOfMonth);
                String selectedCategory = dropdownCategory.getText().toString();
                fetchDataAndExport(datesList,selectedCategory);
                bottomSheetDialog.dismiss();

            } else if (selectedTab == 1) {
                // ==========================================
                // TAB 2: EKSPOR BERDASARKAN TAHUN FULL
                // ==========================================
                String selectedYear = dropdownYearOnly.getText().toString();

                if (selectedYear.isEmpty()) {
                    Toast.makeText(requireContext(), "Pilih tahun terlebih dahulu!", Toast.LENGTH_SHORT).show();
                    return; // Hentikan proses jika kosong
                }

                int year = Integer.parseInt(selectedYear);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);

                // Atur ke 1 Januari
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                long startOfYear = calendar.getTimeInMillis();

                // Atur ke 31 Desember
                calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                calendar.set(Calendar.DAY_OF_MONTH, 31);
                long endOfYear = calendar.getTimeInMillis();

                // Buat List Tanggal (365/366 hari) dan panggil ViewModel
                datesList = generateDatesBetween(startOfYear, endOfYear);
                String selectedCategory = dropdownCategory.getText().toString();
                fetchDataAndExport(datesList,selectedCategory);
                bottomSheetDialog.dismiss();

            } else {
                // ==========================================
                // TAB 3: EKSPOR RENTANG TANGGAL BEBAS
                // ==========================================
                if (filterStartDate != null && filterEndDate != null) {
                    datesList = generateDatesBetween(filterStartDate, filterEndDate);
                    fetchDataAndExport(datesList,dropdownCategory.getText().toString());
                    bottomSheetDialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Pilih rentang tanggal di kalender terlebih dahulu!", Toast.LENGTH_SHORT).show();
                }
            }
        });
       etPickDateRange.setOnClickListener(v -> {
           MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
           builder.setTitleText("Pilih Rentang Tanggal");
           MaterialDatePicker<Pair<Long, Long>> picker = builder.build();

           picker.addOnPositiveButtonClickListener(selection -> {
               filterStartDate = selection.first;
               filterEndDate = selection.second;

               SimpleDateFormat sdfUI = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
               String startString = sdfUI.format(new Date(filterStartDate));
               String endString = sdfUI.format(new Date(filterEndDate));

               // 3. Masukkan teks hasil pilihan langsung ke dalam OutlinedBox
               etPickDateRange.setText(startString + " - " + endString);
           });

           picker.show(getParentFragmentManager(), "DATE_RANGE_PICKER");
       });
        bottomSheetDialog.show();
    }
    private int getMonthIndex(String monthName) {
        String[] months = {"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(monthName)) {
                return i;
            }
        }
        return 0; // Fallback otomatis ke Januari
    }
    private void fetchDataAndExport(List<String> datesList,String categoryFilter) {
        if (categoryFilter.equalsIgnoreCase(getString(R.string.menabung))) {

            // Panggil ViewModel Tabungan
            LiveData<List<ModelSavingsProgress>> liveDataSaving;
            if (datesList == null || datesList.isEmpty()) {
                // TODO: Ganti dengan fungsi ViewModel Anda untuk mengambil SEMUA data tanpa filter tanggal
                liveDataSaving = viewModelSavingsProgress.getAllSavings();
            } else {
                liveDataSaving = viewModelSavingsProgress.getSavingsByWeek(datesList, mainActivity.modelSavings.getId(), user.getType_currency());
            }

            liveDataSaving.observe(getViewLifecycleOwner(), new Observer<List<ModelSavingsProgress>>() {
                @Override
                public void onChanged(List<ModelSavingsProgress> modelFinances) {
                    liveDataSaving.removeObserver(this); // Cegah memory leak
                    if (modelFinances == null || modelFinances.isEmpty()) {
                        Toast.makeText(getContext(), "Tidak ada data pada rentang waktu tersebut", Toast.LENGTH_SHORT).show();
                    } else {
                        exportExcelByData(new ArrayList<>(modelFinances),categoryFilter);
                    }
                }
            });

        } else {

            // Panggil ViewModel Keuangan (Jurnal Keuangan)
            LiveData<List<ModelFinance>> liveDataFinance;
            if (datesList == null || datesList.isEmpty()) {
                // TODO: Ganti dengan fungsi ViewModel Anda untuk mengambil SEMUA data tanpa filter tanggal
                liveDataFinance = viewModelFinance.getAllFinance();
            } else {
                liveDataFinance = viewModelFinance.getFinanceByWeek(datesList, user.getId(), user.getType_currency());
            }

            liveDataFinance.observe(getViewLifecycleOwner(), new Observer<List<ModelFinance>>() {
                @Override
                public void onChanged(List<ModelFinance> modelFinances) {
                    liveDataFinance.removeObserver(this); // Cegah memory leak
                    if (modelFinances == null || modelFinances.isEmpty()) {
                        Toast.makeText(getContext(), "Tidak ada data pada rentang waktu tersebut", Toast.LENGTH_SHORT).show();
                    } else {
                        exportExcelByData(new ArrayList<>(modelFinances),categoryFilter);
                    }
                }
            });
        }
    }

    // Fungsi untuk membuat list string tanggal (yyyy-MM-dd) dari Long rentang waktu
    private List<String> generateDatesBetween(long startDateMillis, long endDateMillis) {
        List<String> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startDateMillis);

        // FORMAT SESUAI DATABASE: "September 17, 2026"
        // Gunakan Locale.US/ENGLISH agar format teksnya tidak berubah menjadi format lokal (dd MMMM yyyy)
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);

        while (calendar.getTimeInMillis() <= endDateMillis) {
            dates.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dates;
    }

    // Fungsi untuk membuat list string tanggal (yyyy-MM-dd) untuk bulan ini
    private List<String> generateDatesForThisMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set ke tanggal 1 bulan ini

        long startOfMonth = calendar.getTimeInMillis();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); // Set ke hari terakhir bulan ini
        long endOfMonth = calendar.getTimeInMillis();

        return generateDatesBetween(startOfMonth, endOfMonth);
    }
}