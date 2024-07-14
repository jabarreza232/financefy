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

import java.util.ArrayList;
import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.callback.MethodCallback;
import id.co.evolution.financefy.databinding.ListFilterBinding;
import id.co.evolution.financefy.model.ModelFilter;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;

public class AdapterFilter extends RecyclerView.Adapter<AdapterFilter.ViewHolder> {
    Context context;
    List<ModelFilter> data;
    MethodCallback methodCallback;

    public AdapterFilter(Context context, List<ModelFilter> data, MethodCallback methodCallback) {
        this.context = context;
        this.data = data;
        this.methodCallback = methodCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_filter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.bindData(data.get(position),position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ListFilterBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void bindData(ModelFilter modelFilter,int position) {
            binding.txtName.setText(modelFilter.getValue());

            if (modelFilter.isChecked()) {
                binding.txtName.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_input_form_calculator));
                binding.txtName.setTextColor(ContextCompat.getColor(context,R.color.white));
            } else {
                binding.txtName.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_selected_filter));
                binding.txtName.setTextColor(ContextCompat.getColor(context,R.color.blackTextColor));
            }

            itemView.setOnClickListener(v -> {
                modelFilter.setChecked(!modelFilter.isChecked());
                logicClickCheck(modelFilter);
                methodCallback.onClick(data, position);
            });
        }

        @SuppressLint("NotifyDataSetChanged")
        public void logicClickCheck(ModelFilter modelFilterChecked) {
            for (int i = 0; i < data.size(); i++) {
                ModelFilter modelFilter = data.get(i);
                modelFilter.setChecked(false);
                if (modelFilter.equals(modelFilterChecked)) {
                    modelFilter.setChecked(true);
                }
            }
            notifyDataSetChanged();
        }
    }

}
