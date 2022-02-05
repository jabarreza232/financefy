package id.co.evolution.financefy;

import android.app.Application;
import androidx.room.Room;
import android.content.Context;

import id.co.evolution.financefy.db.FinanceDB;

public class App extends Application {
    public static FinanceDB db;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static FinanceDB getDatabase(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context, FinanceDB.class, "db_finance")//if we want in memory builder  ithink we can add it here
                    .allowMainThreadQueries().build();
        }
        return db;
    }


}
