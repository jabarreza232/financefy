package id.co.evolution.financefy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.callback.MethodCallback;
import id.co.evolution.financefy.databinding.ListNestedSavingsBinding;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelSavingsProgress;

public class AdapterNestedSavings extends RecyclerView.Adapter<AdapterNestedSavings.ViewHolder> {

    List<ModelSavingsProgress> data;
    MethodCallback methodCallback;
    int total_value;
    Locale locale;

    public AdapterNestedSavings(int total_value, List<ModelSavingsProgress> data, MethodCallback methodCallback) {
        this.total_value = total_value;
        this.data = data;
        this.methodCallback = methodCallback;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_nested_savings, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int i) {
        holder.binding.txtJudul.setText(data.get(i).getTitle());
        holder.binding.keterangan.setVisibility(!data.get(i).getDescription().isEmpty() ? View.VISIBLE : View.GONE);
        holder.binding.keterangan.setText(data.get(i).getDescription());
        holder.binding.jumlah.setText(Tools.convertToCurrency(data.get(i).getProcessValue(),locale));

        holder.binding.txtPercentage.setText(data.get(i).getPercentage(total_value)+"%");
        Log.e("TAG", "onBindViewHolder: "+total_value);

        holder.binding.imgEdit.setOnClickListener(v -> methodCallback.onClick(data, i));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ListNestedSavingsBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);

        }
    }


}
