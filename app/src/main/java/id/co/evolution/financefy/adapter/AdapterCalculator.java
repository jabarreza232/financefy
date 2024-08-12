package id.co.evolution.financefy.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.callback.MethodCallback;
import id.co.evolution.financefy.databinding.ListCalculatorBinding;

public class AdapterCalculator extends RecyclerView.Adapter<AdapterCalculator.ViewHolder> {
    Context context;
    List<String> data;
    MethodCallback methodCallback;
    int selectedPosition = -1;
    int lastSelectedPosition = -1;

    public AdapterCalculator(Context context, List<String> data, MethodCallback methodCallback) {
        this.context = context;
        this.data = data;
        this.methodCallback = methodCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_calculator, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        holder.bindData(data.get(position), position);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ListCalculatorBinding binding;
        TextView activeButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void bindData(String modelFilter, int position) {
            binding.txtName.setText(modelFilter);

//            itemView.setOnClickListener(v -> {
//                methodCallback.onClick(data, position);
//            });
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        methodCallback.onClick(data, position);
                        binding.txtName.setBackgroundColor(ContextCompat.getColor(context, R.color.blueColor));
                        binding.txtName.setSelected(true);
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if(binding.txtName.isSelected()){
                            binding.txtName.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_selected_calculator));
                            binding.txtName.setSelected(false);
                        }else{
                            binding.txtName.setBackgroundColor(ContextCompat.getColor(context, R.color.blueColor));
                            binding.txtName.setSelected(true);
                        }

                        // Do what you want
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        binding.txtName.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_selected_calculator));
                        binding.txtName.setSelected(false);
                        // Do what you want
                        return true;
                    }

                    return false;
                }
            });


        }

        private void setActiveButton(TextView selectedButton) {
            // Reset the previous active button
            if (activeButton != null) {
                activeButton.setSelected(false);
            }
            // Set the new active button
            selectedButton.setBackgroundColor(ContextCompat.getColor(context, R.color.blueColor));
            activeButton = selectedButton;
        }
    }
}
