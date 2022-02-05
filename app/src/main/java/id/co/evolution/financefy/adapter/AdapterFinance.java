package id.co.evolution.financefy.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.databinding.ListFinanceBinding;
import id.co.evolution.financefy.model.ModelFinance;

public class AdapterFinance extends RecyclerView.Adapter<AdapterFinance.ViewHolder> {
    Context context;
    List<ModelFinance> data;
    MethodCallback methodCallback;
    TYPE_LAYOUT_MANAGER type;

    public enum TYPE_LAYOUT_MANAGER {
        GRID,
        HORIZONTAL,
        VERTICAL
    }

    public AdapterFinance(Context context, List<ModelFinance> data, MethodCallback methodCallback) {
        this.context = context;
        this.data = data;
        this.methodCallback = methodCallback;
    }

    public void setType(TYPE_LAYOUT_MANAGER type) {
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_finance, viewGroup, false);
        return new ViewHolder(view, i);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int i) {
        holder.binding.date.setText(data.get(i).getDate());
        holder.binding.txtJudul.setText(data.get(i).getKategori());
        holder.binding.keterangan.setText(data.get(i).getKeterangan());
        holder.binding.jumlah.setText(data.get(i).getJumlah());

        if (type == TYPE_LAYOUT_MANAGER.GRID || type == TYPE_LAYOUT_MANAGER.HORIZONTAL) {
            holder.mTimelineView.setStartLineColor(ContextCompat.getColor(context, android.R.color.transparent), i);
            holder.mTimelineView.setEndLineColor(ContextCompat.getColor(context, android.R.color.transparent), i);
        } else {
            holder.mTimelineView.setVisibility(View.VISIBLE);
        }

        if (data.get(i).getTipe().equalsIgnoreCase("pengeluaran")) {
            holder.binding.jumlah.setTextColor(ContextCompat.getColor(context, R.color.red));
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_baseline_trending_down_24));
        } else {
            holder.binding.jumlah.setTextColor(ContextCompat.getColor(context, R.color.green));
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.ic_baseline_trending_up_24));
        }


        holder.binding.imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodCallback.onClick(data, i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TimelineView mTimelineView;
        ListFinanceBinding binding;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
        }
    }

    public interface MethodCallback {
        void onClick(List<ModelFinance> data, int position);
    }
}
