package id.co.evolution.financefy.model;

import androidx.lifecycle.LiveData;

import id.co.evolution.financefy.state.DownloadState;

public interface ModelRepository {
    LiveData<DownloadState> downloadModel(String url, String fileName);
    LiveData<DownloadState> getDownloadState(); // Tambahan baru
    void cancelDownload();
}
