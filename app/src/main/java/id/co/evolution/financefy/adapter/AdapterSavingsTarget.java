package id.co.evolution.financefy.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ListSavingsTargetBinding;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.helper.Tools.TYPE;
import id.co.evolution.financefy.model.ModelSavings;

public class AdapterSavingsTarget extends RecyclerView.Adapter<AdapterSavingsTarget.ViewHolder> {

    List<ModelSavings> data;
    MethodCallback methodCallback;
    Calendar today;
    public AdapterSavingsTarget(List<ModelSavings> data, MethodCallback methodCallback) {
        this.data = data;
        this.methodCallback = methodCallback;
        today = Calendar.getInstance();
        today.get(Calendar.YEAR);
        today.get(Calendar.MONTH);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_savings_target, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int i) {
        ModelSavings modelSavings=data.get(i);
        holder.binding.txtJudul.setText(modelSavings.getTitle());
        holder.binding.imgRemove.setVisibility(data.size() > 1 ? View.VISIBLE : View.GONE);
        holder.binding.txtProgress.setText(Tools.convertToCurrency(modelSavings.getProcessValue()) + " s/d " + Tools.convertToCurrency(modelSavings.getTargetValue()));
        holder.binding.progressSavings.setProgress((int) Tools.calculatePercentage(modelSavings.getProcessValue(), modelSavings.getTargetValue()));
        holder.binding.progressSavings.setMax(100);
        holder.binding.txtPercentage.setText(Tools.calculatePercentage((double) modelSavings.getProcessValue(), (double) modelSavings.getTargetValue()) + "%");

        holder.binding.txtDay.setText("Sisa " + Tools.getRestOfTheDay(Tools.getFormattedDateSimple(today.getTimeInMillis()), modelSavings.getDate_target()) + " hari");

        holder.binding.imgRemove.setOnClickListener(v -> methodCallback.onClick(TYPE.REMOVED, data, i));
        holder.binding.imgEdit.setOnClickListener(v -> methodCallback.onClick(TYPE.EDIT, data, i));
        holder.binding.cvSavingsTarget.setOnClickListener(v -> methodCallback.onClick(TYPE.CLICKED, data, i));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ListSavingsTargetBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }


    public interface MethodCallback<T, R> {
        void onClick(T type, List<R> data, int position);
    }
}
