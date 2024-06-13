package id.co.evolution.financefy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
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
import id.co.evolution.financefy.callback.MethodCallback;
import id.co.evolution.financefy.databinding.ListAnalysisBinding;
import id.co.evolution.financefy.databinding.ListFinanceBinding;
import id.co.evolution.financefy.databinding.ListNestedFinanceBinding;
import id.co.evolution.financefy.helper.Tools;
import id.co.evolution.financefy.model.ModelFinance;

public class AdapterAnalysisFinance extends RecyclerView.Adapter<AdapterAnalysisFinance.ViewHolder> {
    Context context;
    List<ModelFinance> data;
    MethodCallback methodCallback;
    Locale locale;
    public enum LAYOUT_ANALYSIS{
        FROM_ANALYSIS,
        FROM_ACCOUNT
    }
    LAYOUT_ANALYSIS layoutAnalysis;
    public AdapterAnalysisFinance(Context context, List<ModelFinance> data, MethodCallback methodCallback) {
        this.context = context;
        this.data = data;
        this.methodCallback = methodCallback;
    }
    public AdapterAnalysisFinance(Context context, List<ModelFinance> data) {
        this.context = context;
        this.data = data;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    public void setLayoutAnalysis(LAYOUT_ANALYSIS layoutAnalysis) {
        this.layoutAnalysis = layoutAnalysis;
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
        holder.binding.txtKategori.setText(data.get(i).getKategori());
        holder.binding.txtJumlah.setText(data.get(i).getJumlahDesc(locale));
        holder.binding.txtJumlah2.setText(data.get(i).getJumlahDesc(locale));
        holder.binding.txtPercentage.setText(Tools.calculatePercentage(data.get(i).getJumlahValue(), data.get(i).getTotalValue()) + "% ");
        holder.binding.txtPercentage2.setText(Tools.calculatePercentage(data.get(i).getJumlahValue(), data.get(i).getTotalValue()) + "% ");
        holder.binding.progressFinance.setProgress((int)Tools.calculatePercentage(data.get(i).getJumlahValue(), data.get(i).getTotalValue()));
        holder.binding.progressFinance.setMax(100);
        Log.e("TAG", "onBindViewHolder: "+data.get(i).getTotalValue());
        if (data.get(i).getTipe().equalsIgnoreCase("pengeluaran")) {
            holder.binding.txtJumlah.setTextColor(ContextCompat.getColor(context, R.color.red));
            holder.binding.txtJumlah2.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.binding.txtJumlah.setTextColor(ContextCompat.getColor(context, R.color.green));
            holder.binding.txtJumlah2.setTextColor(ContextCompat.getColor(context, R.color.green));
        }
        holder.binding.progressFinance.setProgressDrawable(ContextCompat.getDrawable(context,R.drawable.progress_income_drawable));

        if(layoutAnalysis == LAYOUT_ANALYSIS.FROM_ACCOUNT){
            holder.binding.txtJumlah2.setVisibility(View.VISIBLE);
            holder.binding.txtPercentage2.setVisibility(View.VISIBLE);
            holder.binding.txtJumlah.setVisibility(View.GONE);
            holder.binding.txtPercentage.setVisibility(View.GONE);
            holder.binding.txtPercentage2.setBackground(ContextCompat.getDrawable(context,R.drawable.shape_line_blue));
            holder.binding.txtPercentage2.setTextColor(ContextCompat.getColor(context,R.color.blueColor));
        }

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
