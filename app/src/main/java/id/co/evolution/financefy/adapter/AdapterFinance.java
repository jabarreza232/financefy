package id.co.evolution.financefy.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import id.co.evolution.financefy.model.ModelNestedFinance;

public class AdapterFinance extends RecyclerView.Adapter<AdapterFinance.ViewHolder> {
    Context context;
    List<ModelNestedFinance> data;
    MethodCallback methodCallback;
    TYPE_LAYOUT_MANAGER type;

    public enum TYPE_LAYOUT_MANAGER {
        GRID,
        HORIZONTAL,
        VERTICAL
    }

    public AdapterFinance(Context context, List<ModelNestedFinance> data, MethodCallback methodCallback) {
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
        holder.setIsRecyclable(false);
        holder.binding.date.setText(data.get(i).getDate());
        holder.bindData(data.get(i));
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
        ListFinanceBinding binding;
        TimelineView mTimelineView;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
        }

        public void bindData(ModelNestedFinance modelNestedFinance){

            AdapterNestedFinance adapterFinance = new AdapterNestedFinance(context, modelNestedFinance.getFinances(), new AdapterNestedFinance.MethodCallback() {
                @Override
                public void onClick(List<ModelFinance> data, int position) {
                    methodCallback.onClick(data,position);
                }
            });
            if (type == AdapterFinance.TYPE_LAYOUT_MANAGER.GRID) {
                binding.rvFinance.setLayoutManager(new GridLayoutManager(context, 2));
            } else if (type == AdapterFinance.TYPE_LAYOUT_MANAGER.VERTICAL) {
                binding.rvFinance.setLayoutManager(new LinearLayoutManager(context));
            }
            binding.rvFinance.setNestedScrollingEnabled(false);
            binding.rvFinance.setAdapter(adapterFinance);
        }
    }

    public interface MethodCallback {
        void onClick(List<ModelFinance> data, int position);
    }
}
