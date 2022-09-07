package id.co.evolution.financefy.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ModelUserWithSavings {
    @Embedded
    public ModelUser user;
    @Relation(parentColumn = "id_user",
            entityColumn = "id_savings_user")
    public List<ModelSavings> savings;


}
