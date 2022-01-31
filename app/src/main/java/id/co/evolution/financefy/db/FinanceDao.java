package id.co.evolution.financefy.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelUser;

@Dao
public interface FinanceDao {
    @Query("SELECT * FROM finance")
  LiveData <List<ModelFinance>> getAll();

    @Query("SELECT * FROM finance WHERE id_finance IN(:financeIds)")
    List<ModelFinance> loadAllbyIds(int[] financeIds);

    @Query("SELECT * FROM finance WHERE date IN(:date)")
    List<ModelFinance> loadAllbyDate(String date);
    @Query("SELECT * FROM finance WHERE month IN(:month)")
    List<ModelFinance> loadAllbyMonth(String month);

    @Query("SELECT * FROM finance WHERE category LIKE:category LIMIT 1")
    ModelFinance findByName(String category);

    @Query("SELECT * FROM finance WHERE id_finance =:id LIMIT 1")
    ModelFinance findById(int id);

    @Insert
    void insertAll(ModelFinance... user);

    @Update
    void update(ModelFinance user);

    @Delete
    void delete(ModelFinance user);

}
