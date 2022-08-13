package id.co.evolution.financefy.db;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelUserWithFinance;

@Dao
public interface FinanceDao {
    @Query("SELECT * FROM finance")
    LiveData<List<ModelFinance>> getAll();

    @Query("SELECT * FROM finance WHERE id_finance IN(:financeIds)")
    List<ModelFinance> loadAllbyIds(int[] financeIds);

    @Query("SELECT * FROM finance WHERE date IN(:date) AND id_finance_user IN(:id_user)")
    LiveData<List<ModelFinance>> loadAllbyWeek(List<String> date,int id_user);

    @Query("SELECT * FROM finance WHERE month IN(:month) AND id_finance_user IN(:id_user)")
    LiveData<List<ModelFinance>> loadAllbyMonth(String month, int id_user);

    @Query("SELECT * FROM finance WHERE type LIKE:type")
    LiveData<List<ModelFinance>> findByType(String type);


    @Query("SELECT * FROM finance WHERE type LIKE:type AND month IN(:month)AND id_finance_user IN(:id_user)")
    LiveData<List<ModelFinance>> findByTypeAndMonth(String type, String month,int id_user);

    @Query("SELECT * FROM finance WHERE type LIKE:type AND date IN(:date)AND id_finance_user IN(:id_user)")
    LiveData<List<ModelFinance>> findByTypeAndWeek(String type, List<String> date,int id_user);

    @Query("SELECT * FROM finance WHERE id_finance =:id LIMIT 1")
    LiveData<ModelFinance> findById(int id);


    @Transaction
    @Query("SELECT * FROM user WHERE id_user =:id")
    LiveData<List<ModelUserWithFinance>> findFinanceByUserId(int id);

    @Insert
    void insertAll(ModelFinance... finances);

    @Update
    void update(ModelFinance finance);

    @Delete
    void delete(ModelFinance finance);

}
