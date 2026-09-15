package id.co.evolution.financefy.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.util.List;
import java.util.UUID;

import id.co.evolution.financefy.model.ModelRepository;
import id.co.evolution.financefy.state.DownloadState;
import id.co.evolution.financefy.worker.ModelDownloadWorker;

public class WorkManagerModelRepository implements ModelRepository {
    private final WorkManager workManager;
    private UUID currentWorkId;
    private final MediatorLiveData<DownloadState> downloadState = new MediatorLiveData<>();
    private static final String UNIQUE_WORK_NAME = "LLM_MODEL_DOWNLOAD";
    public WorkManagerModelRepository(Context context) {
        this.workManager = WorkManager.getInstance(context.getApplicationContext());
        this.downloadState.setValue(new DownloadState(DownloadState.IDLE, 0, 0));

        observeExistingWork();
    }
    private void observeExistingWork() {
        // Ambil LiveData berdasarkan nama unik, bukan ID acak
        LiveData<List<WorkInfo>> workInfoLiveData = workManager.getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME);

        downloadState.addSource(workInfoLiveData, workInfos -> {
            if (workInfos != null && !workInfos.isEmpty()) {
                // Karena ini unique, harusnya hanya ada 1 task dalam list
                WorkInfo workInfo = workInfos.get(0);

                if (workInfo.getState() == WorkInfo.State.RUNNING) {
                    Data progressData = workInfo.getProgress();
                    int progress = progressData.getInt(ModelDownloadWorker.PROGRESS, 0);
                    double speed = progressData.getDouble(ModelDownloadWorker.SPEED, 0.0);
                    downloadState.setValue(new DownloadState(DownloadState.RUNNING, progress, speed));
                }
                else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    downloadState.setValue(new DownloadState(DownloadState.SUCCESS, 100, 0));
                }
                else if (workInfo.getState() == WorkInfo.State.FAILED || workInfo.getState() == WorkInfo.State.CANCELLED) {
                    downloadState.setValue(new DownloadState(DownloadState.FAILED, 0, 0));
                }
            }
        });
    }
    @Override
    public LiveData<DownloadState> downloadModel(String url, String fileName) {
        Data inputData = new Data.Builder()
                .putString("URL", url)
                .putString("FILENAME", fileName)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(ModelDownloadWorker.class)
                .setInputData(inputData)
                .build();

        currentWorkId = request.getId();
        workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
        );

        // Pantau WorkManager dan ubah menjadi DownloadState
        downloadState.addSource(workManager.getWorkInfoByIdLiveData(currentWorkId), workInfo -> {
            if (workInfo != null) {
                if (workInfo.getState() == WorkInfo.State.RUNNING) {
                    Data progressData = workInfo.getProgress();
                    int progress = progressData.getInt(ModelDownloadWorker.PROGRESS, 0);
                    double speed = progressData.getDouble(ModelDownloadWorker.SPEED, 0.0);
                    downloadState.setValue(new DownloadState(DownloadState.RUNNING, progress, speed));
                }
                else if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    downloadState.setValue(new DownloadState(DownloadState.SUCCESS, 100, 0));
                }
                else if (workInfo.getState() == WorkInfo.State.FAILED || workInfo.getState() == WorkInfo.State.CANCELLED) {
                    downloadState.setValue(new DownloadState(DownloadState.FAILED, 0, 0));
                }
            }
        });

        return downloadState;
    }
    @Override
    public LiveData<DownloadState> getDownloadState() {
        return downloadState;
    }
    @Override
    public void cancelDownload() {
        if (currentWorkId != null) {
            workManager.cancelWorkById(currentWorkId);
        }
    }
}
