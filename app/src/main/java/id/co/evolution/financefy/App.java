package id.co.evolution.financefy;

import android.app.Application;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import id.co.evolution.financefy.helper.TinyDb;

// Definition of the Application graph
@HiltAndroidApp
public class App extends Application {

    @Inject
    TinyDb tinyDb;
    @Override
    public void onCreate() {
        super.onCreate();
        changeUINightMode();
    }
    private void changeUINightMode() {
        String nightMode = tinyDb.getString("night_mode");
        if (TextUtils.equals(nightMode, "mode_night_yes")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
