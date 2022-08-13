package id.co.evolution.financefy.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import javax.inject.Inject;

public class ModelUserWithFinance {
    @Embedded
    public ModelUser user;
    @Relation(parentColumn = "id_user",
            entityColumn = "id_finance_user")
    public List<ModelFinance> finances;
}
