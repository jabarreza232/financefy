package id.co.evolution.financefy.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import id.co.evolution.financefy.model.ModelFinance;

@Database(entities = {ModelFinance.class}, version = 1,exportSchema = false)
public abstract class FinanceDB extends RoomDatabase {
    public abstract FinanceDao financeDao();
}
