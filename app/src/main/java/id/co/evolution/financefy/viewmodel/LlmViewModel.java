package id.co.evolution.financefy.viewmodel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import id.co.evolution.financefy.model.ModelRepository;
import id.co.evolution.financefy.state.DownloadState;

public class LlmViewModel extends ViewModel{
    private final ModelRepository repository;

    // Idealnya disuntikkan menggunakan Dependency Injection (Dagger/Hilt)
    public LiveData<DownloadState> getDownloadState() {
        return repository.getDownloadState();
    }
    public LlmViewModel(ModelRepository repository) {
        this.repository = repository;
    }

    public LiveData<DownloadState> startDownload(String url, String fileName) {
        return repository.downloadModel(url, fileName);
    }

    public void cancelDownload() {
        repository.cancelDownload();
    }
}
