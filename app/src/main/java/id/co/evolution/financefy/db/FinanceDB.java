package id.co.evolution.financefy.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelUser;

@Database(entities = {ModelFinance.class}, version = 1)
public abstract class FinanceDB extends RoomDatabase {
    public abstract FinanceDao financeDao();
}
