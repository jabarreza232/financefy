package id.co.evolution.financefy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ListFilterBinding;
import id.co.evolution.financefy.model.ModelFinance;
import id.co.evolution.financefy.model.ModelNestedFinance;

public class AdapterFilter extends RecyclerView.Adapter<AdapterFilter.ViewHolder> {
    Context context;
    List<String> data;
    MethodCallback methodCallback;

    public AdapterFilter(Context context, List<String> data, MethodCallback methodCallback) {
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
        holder.bindData(data.get(position));
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

        public void bindData(String name) {
            binding.txtName.setText(name);

            itemView.setOnClickListener(v->{
                methodCallback.onClick(data,getAdapterPosition());
            });
        }
    }

    public interface MethodCallback {
        void onClick(List<String> data, int position);
    }
}
