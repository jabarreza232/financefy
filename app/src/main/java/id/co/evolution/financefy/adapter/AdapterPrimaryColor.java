package id.co.evolution.financefy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.callback.MethodCallback;
import id.co.evolution.financefy.databinding.ListPrimaryColorBinding;
import id.co.evolution.financefy.model.ModelPrimaryColor;

public class AdapterPrimaryColor extends RecyclerView.Adapter<AdapterPrimaryColor.ViewHolder> {
    List<ModelPrimaryColor> data;
    MethodCallback methodCallback;

    public AdapterPrimaryColor( List<ModelPrimaryColor> data, MethodCallback methodCallback) {

        this.data = data;
        this.methodCallback = methodCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_primary_color, parent, false);
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
        ListPrimaryColorBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void bindData(ModelPrimaryColor modelPrimaryColor, int position) {
           binding.cvPrimaryColor.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(),modelPrimaryColor.getColorPrimary()));

            itemView.setOnClickListener(v -> {
                methodCallback.onClick(data, position);
            });
        }

    }

}
