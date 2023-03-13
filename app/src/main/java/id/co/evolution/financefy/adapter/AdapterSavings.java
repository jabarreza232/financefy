package id.co.evolution.financefy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.vipulasri.timelineview.TimelineView;

import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.callback.MethodCallback;
import id.co.evolution.financefy.databinding.ListFinanceBinding;
import id.co.evolution.financefy.fragment.FragmentAll;
import id.co.evolution.financefy.fragment.FragmentAll.TYPE_LAYOUT_MANAGER;
import id.co.evolution.financefy.model.ModelNestedSavings;

public class AdapterSavings extends RecyclerView.Adapter<AdapterSavings.ViewHolder> {
    List<ModelNestedSavings> data;
    MethodCallback methodCallback;
    TYPE_LAYOUT_MANAGER type;
    int total_value;

    public void setTotal_value(int total_value) {
        this.total_value = total_value;
    }

    public AdapterSavings( List<ModelNestedSavings> data, MethodCallback methodCallback) {
        this.data = data;
        this.methodCallback = methodCallback;
    }

    public void setType(FragmentAll.TYPE_LAYOUT_MANAGER type) {
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_finance, viewGroup, false);
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

        public void bindData(ModelNestedSavings modelNestedFinance){
            showNestedSavings(modelNestedFinance);
        }

        private void showNestedSavings(ModelNestedSavings modelNestedFinance){
            AdapterNestedSavings adapterFinance = new AdapterNestedSavings(total_value, modelNestedFinance.getSavingsProgresses(), (data, position) -> methodCallback.onClick(data,position));

            if (type == TYPE_LAYOUT_MANAGER.GRID) {
                binding.rvFinance.setLayoutManager(new GridLayoutManager(itemView.getContext(), 2));
            } else if (type == TYPE_LAYOUT_MANAGER.VERTICAL) {
                binding.rvFinance.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            }
            binding.rvFinance.setNestedScrollingEnabled(false);
            binding.rvFinance.setAdapter(adapterFinance);
        }
    }
}
