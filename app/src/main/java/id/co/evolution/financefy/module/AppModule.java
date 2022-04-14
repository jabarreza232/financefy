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
import id.co.evolution.financefy.repository.FinanceRepository;

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
    @Provides
    FinanceRepository financeRepository(@NonNull FinanceDB financeDB) {
        return new FinanceRepository(financeDB.financeDao());
    }

    @Provides
    SavedStateHandle savedStateHandle() {
        return new SavedStateHandle();
    }

    @Provides
    FinanceDao financeDao(@NonNull FinanceDB financeDB) {
        return financeDB.financeDao();
    }

}
