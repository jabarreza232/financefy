package id.co.evolution.financefy.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelUser;

@Database(entities = {ModelFinance.class, ModelUser.class}, version = 2,exportSchema = false)
public abstract class FinanceDB extends RoomDatabase {
    public abstract FinanceDao financeDao();
    public abstract UserDao userDao();
}
