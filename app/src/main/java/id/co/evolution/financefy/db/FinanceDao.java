package id.co.evolution.financefy.db;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;

@Dao
public interface FinanceDao {
    @Query("SELECT * FROM finance")
    LiveData<List<ModelFinance>> getAll();

    @Query("SELECT * FROM finance WHERE id_finance IN(:financeIds)")
    List<ModelFinance> loadAllbyIds(int[] financeIds);

    @Query("SELECT * FROM finance WHERE date IN(:date)")
    LiveData<List<ModelFinance>> loadAllbyWeek(List<String> date);

    @Query("SELECT * FROM finance WHERE month IN(:month)")
    LiveData<List<ModelFinance>> loadAllbyMonth(String month);

    @Query("SELECT * FROM finance WHERE type LIKE:type")
    LiveData<List<ModelFinance>> findByType(String type);


    @Query("SELECT * FROM finance WHERE type LIKE:type AND month IN(:month)")
    LiveData<List<ModelFinance>> findByTypeAndMonth(String type, String month);

    @Query("SELECT * FROM finance WHERE type LIKE:type AND date IN(:date)")
    LiveData<List<ModelFinance>> findByTypeAndWeek(String type, List<String> date);

    @Query("SELECT * FROM finance WHERE id_finance =:id LIMIT 1")
    ModelFinance findById(int id);

    @Insert
    void insertAll(ModelFinance... user);

    @Update
    void update(ModelFinance user);

    @Delete
    void delete(ModelFinance user);

}
