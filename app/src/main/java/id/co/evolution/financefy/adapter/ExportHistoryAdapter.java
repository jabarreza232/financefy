package id.co.evolution.financefy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import id.co.evolution.financefy.R;
public class ExportHistoryAdapter extends RecyclerView.Adapter<ExportHistoryAdapter.ViewHolder> {

    private final List<File> fileList;
    private final OnFileClickListener onFileClickListener;
    private final OnFileShareListener onFileShareListener;

    // Interface untuk menangani aksi klik (Buka File)
    public interface OnFileClickListener {
        void onFileClick(File file);
    }

    // Interface untuk menangani aksi klik ikon Share (Bagikan File)
    public interface OnFileShareListener {
        void onShareClick(File file);
    }

    public ExportHistoryAdapter(List<File> fileList, OnFileClickListener onFileClickListener, OnFileShareListener onFileShareListener) {
        this.fileList = fileList;
        this.onFileClickListener = onFileClickListener;
        this.onFileShareListener = onFileShareListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sesuaikan nama layout dengan XML item yang kita buat sebelumnya
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_export_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = fileList.get(position);

        // 1. Set Nama File (Menghilangkan prefix path yang panjang)
        holder.txtFileName.setText(file.getName());

        // 2. Format Tanggal Terakhir Dimodifikasi / Dibuat
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy • HH:mm", new Locale("id", "ID"));
        String formattedDate = sdf.format(new Date(file.lastModified()));

        // 3. Format Ukuran File (Konversi Byte ke KB/MB)
        String formattedSize = formatFileSize(file.length());

        // Gabungkan teks tanggal dan ukuran
        holder.txtFileDate.setText(formattedDate + "  |  " + formattedSize);

        // 4. Handle Event Klik (Seluruh Kartu) -> Buka File
        holder.itemView.setOnClickListener(v -> {
            if (onFileClickListener != null) {
                onFileClickListener.onFileClick(file);
            }
        });

        // 5. Handle Event Klik (Ikon Share Saja) -> Bagikan File
        holder.imgShare.setOnClickListener(v -> {
            if (onFileShareListener != null) {
                onFileShareListener.onShareClick(file);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    // Fungsi canggih untuk mengubah Byte menjadi format KB, MB, GB secara otomatis
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    // Kelas ViewHolder untuk mendeklarasikan View dari XML
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtFileName, txtFileDate;
        ImageView imgShare;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFileName = itemView.findViewById(R.id.txt_file_name);
            txtFileDate = itemView.findViewById(R.id.txt_file_date);
            imgShare = itemView.findViewById(R.id.img_share);
        }
    }
}