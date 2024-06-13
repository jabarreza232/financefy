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

    @Query("SELECT * FROM finance WHERE date IN(:date) AND id_finance_user IN(:id_user)AND type_currency IN(:type_currency)")
    LiveData<List<ModelFinance>> loadAllbyWeek(List<String> date,int id_user,String type_currency);

    @Query("SELECT * FROM finance WHERE month IN(:month) AND id_finance_user IN(:id_user)AND type_currency IN(:type_currency)")
    LiveData<List<ModelFinance>> loadAllbyMonth(String month, int id_user,String type_currency);
    @Query("SELECT * FROM finance WHERE date IN(:date) AND id_finance_user IN(:id_user)AND type_currency IN(:type_currency)")
    LiveData<List<ModelFinance>> loadAllbyDate(String date, int id_user,String type_currency);

    @Query("SELECT * FROM finance WHERE month IN(:year) AND id_finance_user IN(:id_user)AND type_currency IN(:type_currency)")
    LiveData<List<ModelFinance>> loadAllByYear(String year, int id_user,String type_currency);

    @Query("SELECT * FROM finance WHERE type LIKE:type AND type_currency IN(:type_currency)")
    LiveData<List<ModelFinance>> findByType(String type,String type_currency);


    @Query("SELECT * FROM finance WHERE type LIKE:type AND month IN(:month)AND id_finance_user IN(:id_user)AND type_currency IN(:type_currency)")
    LiveData<List<ModelFinance>> findByTypeAndMonth(String type, String month,int id_user,String type_currency);

    @Query("SELECT * FROM finance WHERE type LIKE:type AND date IN(:date)AND id_finance_user IN(:id_user)AND type_currency IN(:type_currency)")
    LiveData<List<ModelFinance>> findByTypeAndWeek(String type, List<String> date,int id_user,String type_currency);

    @Query("SELECT * FROM finance WHERE id_finance =:id AND type_currency IN(:type_currency) LIMIT 1")
    LiveData<ModelFinance> findById(int id,String type_currency);


    @Transaction
    @Query("SELECT * FROM finance WHERE id_finance_user =:id AND type_currency IN(:type_currency)")
    LiveData<List<ModelFinance>> findFinanceByUserId(int id,String type_currency);

    @Insert
    void insertAll(ModelFinance... finances);

    @Update
    void update(ModelFinance finance);

    @Query("UPDATE finance SET amount=:amount WHERE id_finance =:id_finance AND id_finance_user=:id_user")
    void updateByIdUser(double amount, int id_finance, int id_user);

    @Delete
    void delete(ModelFinance finance);

    @Query("DELETE FROM finance WHERE id_finance_user=:id_user")
    void deleteByIdUser(int id_user);

}
