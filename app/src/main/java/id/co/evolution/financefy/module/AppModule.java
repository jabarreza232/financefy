package id.co.evolution.financefy.module;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.SavedStateHandle;
import androidx.room.Room;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import id.co.evolution.financefy.db.FinanceDB;
import id.co.evolution.financefy.db.FinanceDao;
import id.co.evolution.financefy.db.SavingsDao;
import id.co.evolution.financefy.db.SavingsProgressDao;
import id.co.evolution.financefy.db.UserDao;
import id.co.evolution.financefy.helper.FinanceFilter;
import id.co.evolution.financefy.helper.LocalizedWeekHelper;
import id.co.evolution.financefy.helper.SavingsFilter;
import id.co.evolution.financefy.helper.TinyDb;

@Module
@InstallIn(ApplicationComponent.class)
public class AppModule {

    //    @Target(ElementType.METHOD)
//    @Retention(RetentionPolicy.RUNTIME)
//    @MapKey
//    @interface ViewModelKey {
//        Class<? extends ViewModel> value();
//    }
    @NonNull
    @Provides
    public static FinanceDB getDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, FinanceDB.class, "db_finance")//if we want in memory builder  ithink we can add it here
                .fallbackToDestructiveMigration().build();
    }

//    @Provides
//    ViewModelFactory viewModelFactory(FinanceRepository financeRepository) {
//        return new ViewModelFactory(financeRepository, savedStateHandle());
//    }

    /*
        @Provides
        @IntoMap
        @ViewModelKey(ViewModelFinance.class)
        ViewModel viewModel1() {
            return new ViewModelFinance();
        }
    */

//    @Provides
//    FinanceRepository financeRepository(@NonNull FinanceDB financeDB) {
//        return new FinanceRepository(financeDB.financeDao());
//    }
//
//    @Provides
//    UserRepository userRepository(@NonNull FinanceDB financeDB) {
//        return new UserRepository(financeDB.userDao());
//    }

    @Provides
    TinyDb tinyDb(@ApplicationContext Context context){
        return new TinyDb(context);
    }

    @Provides
    SavedStateHandle savedStateHandle() {
        return new SavedStateHandle();
    }

    @Provides
    FinanceFilter financeFilter(){
        return new FinanceFilter();
    }

    @Provides
    SavingsFilter savingsFilter(){
        return new SavingsFilter();
    }

    @Provides
    LocalizedWeekHelper localizedWeekHelper(){
        return new LocalizedWeekHelper();
    }

    @Provides
    FinanceDao financeDao(@NonNull FinanceDB financeDB) {
        return financeDB.financeDao();
    }

    @Provides
    UserDao userDao(@NonNull FinanceDB financeDB) {
        return financeDB.userDao();
    }
    @Provides
    SavingsDao savingsDao(@NonNull FinanceDB financeDB) {
        return financeDB.savingsDao();
    }
    @Provides
    SavingsProgressDao savingsProgressDao(@NonNull FinanceDB financeDB) {
        return financeDB.savingsPrgoressDao();
    }

}
