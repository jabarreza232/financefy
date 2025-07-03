package id.co.evolution.financefy.fragment;

import static id.co.evolution.financefy.helper.TinyDb.isExternalStorageWritable;
import static id.co.evolution.financefy.helper.Tools.getFormattedDateSimple;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.util.Pair;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.co.evolution.financefy.MainActivity;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.activity.AboutActivity;
import id.co.evolution.financefy.activity.NotificationActivity;
import id.co.evolution.financefy.activity.SwitchThemeActivity;
import id.co.evolution.financefy.databinding.FragmentSettingsBinding;
import id.co.evolution.financefy.dialog.DateRangeDialog;
import id.co.evolution.financefy.dialog.DialogConfirm;
import id.co.evolution.financefy.dialog.DialogLoading;
import id.co.evolution.financefy.dialog.DialogSettingPin;
import id.co.evolution.financefy.helper.TinyDb;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelSavingsProgress;
import id.co.evolution.financefy.model.ModelUser;
import id.co.evolution.financefy.repository.FinanceRepository;
import id.co.evolution.financefy.repository.SavingsProgressRepository;
import id.co.evolution.financefy.repository.SavingsRepository;
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

        binding.txtNotification.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), NotificationActivity.class);
            startActivity(i);
        });
        changeStatusPin();
        binding.txtPinSetting.setOnClickListener(v -> {
            showDialogSettingPIN();
        });
        if (mainActivity.isPinSetting) {
            binding.txtStatusPin.setText("Aktif");
            binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        } else {
            binding.txtStatusPin.setText("Tidak Aktif");
            binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
        binding.txtStatusPin.setOnClickListener(v -> {
            if (mainActivity.isPinSetting) {
                DialogConfirm dialogConfirm = new DialogConfirm(getContext(), inflater, result -> {

                    if (result.equalsIgnoreCase("yes")) {
                        Toast.makeText(getContext(), "PIN telah berhasil di non aktifkan !", Toast.LENGTH_SHORT).show();
                        tinyDb.putString("pin", "");
                        tinyDb.putBoolean("isSettingPin", false);
                        mainActivity.isPinSetting = false;
                        binding.txtStatusPin.setText("Tidak Aktif");
                        binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(), R.color.red));

                    }
                });
                dialogConfirm.showDialogConfirm("Menonaktifkan PIN", "Apakah anda yakin ingin menonaktifkan PIN anda ? ");

            } else {
                showDialogSettingPIN();
            }

        });
        binding.txtMoney.setOnClickListener(v -> {
            setCurrencySettings();
        });
        binding.txtInfo.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), AboutActivity.class);
            startActivity(i);
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
                                openDateRangePicker();
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

    private void openDateRangePicker() {
        DateRangeDialog dateRangeDialog = new DateRangeDialog(getActivity(), (startDate, endDate,datesList) -> {
            Log.e("cek:",startDate +" : "+endDate);

            if(user.getCategory().equalsIgnoreCase(getString(R.string.menabung))){
                viewModelSavingsProgress.getSavingsByWeek(datesList,mainActivity.modelSavings.getId(), user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
                    dataSaving = modelFinances;
                    exportExcelByData(new ArrayList<>(dataSaving));
                });

            }else{
                viewModelFinance.getFinanceByWeek(datesList,user.getId(), user.getType_currency()).observe(getViewLifecycleOwner(), modelFinances -> {
                    dataFinance = modelFinances;
                    exportExcelByData(new ArrayList<>(dataFinance));
                });

            }
        });

        dateRangeDialog.show();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModelFinance = new ViewModelProvider(this).get(ViewModelFinance.class);

        viewModelSavingsProgress = new ViewModelProvider(this).get(ViewModelSavingsProgress.class);

        viewModelFinance.init(financeRepository);
        viewModelSavingsProgress.init(savingsProgressRepository);

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

    private void showDialogSettingPIN() {
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

    private void changeStatusPin() {
        if (tinyDb.getBoolean("isSettingPin")) {
            binding.txtStatusPin.setText("Aktif");
            binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
        } else {
            binding.txtStatusPin.setText("Tidak Aktif");
            binding.txtStatusPin.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }

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
    private void exportExcelByData(List<Object> data) {

        Workbook workbook = new XSSFWorkbook();

        // Buat Sheet baru
        Sheet sheet = workbook.createSheet(user.getType());
        Row headerNameUserRow = sheet.createRow(0);
        headerNameUserRow.createCell(0).setCellValue("Nama: " + user.getName());

        Row headerRow = sheet.createRow(1);
        headerRow.createCell(0).setCellValue("Kategori: " + user.getCategory());

        // Buat Header Row
        if(user.getCategory().equalsIgnoreCase(getString(R.string.jurnal_keuangan))){
            List<ModelFinance> dataFinance = new ArrayList<>();
            for (Object object:data){
                dataFinance.add((ModelFinance)object);
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
                sheet.setColumnWidth(1, (modelFinance.getJumlahDesc(locale).length() * 400));
                dataRow.createCell(2).setCellValue(modelFinance.getKategori());
                sheet.setColumnWidth(2, (modelFinance.getKategori().length() * 400));
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setWrapText(true); // Mengaktifkan pembungkusan teks


                Cell cell = dataRow.createCell(3);
                cell.setCellStyle(cellStyle);

                cell.setCellValue(modelFinance.getKeterangan());
                dataRow.setHeightInPoints(((float) modelFinance.getKeterangan().length() / 20) * sheet.getDefaultRowHeightInPoints()); // Sesuaikan tinggi baris

                sheet.setColumnWidth(3, Math.min((modelFinance.getKeterangan().length() + 2) * 256, 20000)); // 20000 adalah batas maksimum width

                dataRow.createCell(4).setCellValue(modelFinance.getDate());
                sheet.setColumnWidth(4, (modelFinance.getDate().length() * 400));

                dataRow.createCell(5).setCellValue(modelFinance.getTipe());
                sheet.setColumnWidth(5, (modelFinance.getTipe().length() * 400));

                dataRow.createCell(6).setCellValue(modelFinance.getMonth());
                sheet.setColumnWidth(6, (modelFinance.getMonth().length() * 400));


            }

        }else{
            Row headerSavingsNameRow = sheet.createRow(2);
            headerSavingsNameRow.createCell(0).setCellValue("Target: " + mainActivity.modelSavings.getTitle());
            sheet.setColumnWidth(0, (mainActivity.modelSavings.getTitle().length() * 400));

            Row headerSavingsTargetRow = sheet.createRow(3);
            headerSavingsTargetRow.createCell(0).setCellValue("Jumlah: " + Tools.convertToCurrency(mainActivity.modelSavings.getTargetValue(),locale));
            sheet.setColumnWidth(0, (Tools.convertToCurrency(mainActivity.modelSavings.getTargetValue(),locale).length() * 400));

            List<ModelSavingsProgress> dataSavings  = new ArrayList<>();
            for (Object object:data){
                dataSavings.add((ModelSavingsProgress)object);
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
                sheet.setColumnWidth(1, (modelSavingsProgress.getTitle().length() * 400));
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setWrapText(true); // Mengaktifkan pembungkusan teks


                Cell cell = dataRow.createCell(2);
                cell.setCellStyle(cellStyle);

                cell.setCellValue(modelSavingsProgress.getDescription());
                dataRow.setHeightInPoints(((float) modelSavingsProgress.getDescription().length() / 20) * sheet.getDefaultRowHeightInPoints()); // Sesuaikan tinggi baris

                sheet.setColumnWidth(2, Math.min((modelSavingsProgress.getDescription().length() + 2) * 256, 20000)); // 20000 adalah batas maksimum width


                dataRow.createCell(3).setCellValue(Tools.convertToCurrency(modelSavingsProgress.getProcessValue(),locale));
                sheet.setColumnWidth(3, (Tools.convertToCurrency(modelSavingsProgress.getProcessValue(),locale).length() * 400));

                dataRow.createCell(4).setCellValue(modelSavingsProgress.getDate_progress_savings());
                sheet.setColumnWidth(4, (modelSavingsProgress.getDate_progress_savings().length() * 400));

                dataRow.createCell(5).setCellValue(modelSavingsProgress.getMonth());
                sheet.setColumnWidth(5, (modelSavingsProgress.getMonth().length() * 400));


            }

        }

        // Simpan file Excel ke Penyimpanan

        String fileName = user.getCategory()+"_"+Calendar.getInstance().getTimeInMillis() + ".xlsx";

        File file;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Buat file Excel seperti yang Anda lakukan sebelumnya
            file = new File(getContext().getExternalFilesDir(null), fileName);
        } else {
            file = new File(Environment.getExternalStorageDirectory(), fileName);
        }

        try (FileOutputStream fileOut = new FileOutputStream(file)) {

            workbook.write(fileOut);
            workbook.close();

// Bagikan file atau buka dengan aplikasi lain menggunakan Intent
            Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file);

//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
//            startActivity(Intent.createChooser(intent, "Share Excel File"));
//// Tambahkan flag untuk memberikan izin kepada aplikasi penerima untuk mengakses file
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Toast.makeText(getContext(), "File berhasil disimpan di: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Excel File"));

        } catch (IOException e) {
            Toast.makeText(getContext(), "Gagal menyimpan file: " + e.getMessage(), Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
    }

    class DataModel {
        private int id;
        private String nama;
        private int nilai;

        public DataModel(int id, String nama, int nilai) {
            this.id = id;
            this.nama = nama;
            this.nilai = nilai;
        }

        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public int getNilai() {
            return nilai;
        }
    }
}