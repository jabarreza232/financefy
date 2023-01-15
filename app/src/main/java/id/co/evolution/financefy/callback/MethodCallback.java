package id.co.evolution.financefy.callback;

import java.util.List;

import id.co.evolution.financefy.model.ModelSavingsProgress;

public interface MethodCallback<T> {
    void onClick(List<T> data, int position);
}


