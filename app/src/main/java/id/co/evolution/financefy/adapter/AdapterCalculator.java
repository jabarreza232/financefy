package id.co.evolution.financefy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ListCalculatorBinding;
import id.co.evolution.financefy.databinding.ListFilterBinding;
import id.co.evolution.financefy.model.ModelFilter;

public class AdapterCalculator extends RecyclerView.Adapter<AdapterCalculator.ViewHolder> {
    Context context;
    List<String> data;
    MethodCallback methodCallback;

    public AdapterCalculator(Context context, List<String> data, MethodCallback methodCallback) {
        this.context = context;
        this.data = data;
        this.methodCallback = methodCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_calculator, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.bindData(data.get(position), position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ListCalculatorBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void bindData(String modelFilter, int position) {
            binding.txtName.setText(modelFilter);

            itemView.setOnClickListener(v -> {
                methodCallback.onClick(data, position);
            });
        }
    }

    public interface MethodCallback {
        void onClick(List<String> data, int position);
    }
}
