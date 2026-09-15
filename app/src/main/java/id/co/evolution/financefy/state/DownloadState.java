package id.co.evolution.financefy.state;

public class DownloadState {
    public static final int IDLE = 0;
    public static final int RUNNING = 1;
    public static final int SUCCESS = 2;
    public static final int FAILED = 3;

    public int status;
    public int progress;
    public double speedMb;

    public DownloadState(int status, int progress, double speedMb) {
        this.status = status;
        this.progress = progress;
        this.speedMb = speedMb;
    }
}
