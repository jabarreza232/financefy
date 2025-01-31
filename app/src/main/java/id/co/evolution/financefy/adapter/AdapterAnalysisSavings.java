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
import java.util.Locale;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ListAnalysisBinding;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelSavingsProgress;

public class AdapterAnalysisSavings extends RecyclerView.Adapter<AdapterAnalysisSavings.ViewHolder> {
    Context context;
    List<ModelSavingsProgress> data;
    Locale locale;
    long totalValue;

    public AdapterAnalysisSavings(Context context, List<ModelSavingsProgress> data) {
        this.context = context;
        this.data = data;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setTotalValue(long totalValue) {
        this.totalValue = totalValue;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_analysis, viewGroup, false);

        return new ViewHolder(view, i);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @SuppressLint({"RecyclerView", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int i) {
        holder.binding.txtKategori.setText(data.get(i).getTitle());
        holder.binding.txtJumlah.setText(Tools.convertToCurrency(data.get(i).getProcessValue(),locale));
        double percentage = Tools.calculatePercentage(data.get(i).getProcessValue(), totalValue);
        holder.binding.txtPercentage.setText(data.get(i).getPercentage((int)totalValue) + "% ");
        holder.binding.progressFinance.setProgress((int)percentage);
        holder.binding.progressFinance.setMax(100);

        holder.binding.progressFinance.setProgressDrawable(ContextCompat.getDrawable(context,R.drawable.progress_income_drawable));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ListAnalysisBinding binding;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);

        }
    }

}
