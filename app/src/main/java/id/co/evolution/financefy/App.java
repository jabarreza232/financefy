package id.co.evolution.financefy;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

// Definition of the Application graph
@HiltAndroidApp
public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
    }

}
