package id.co.evolution.financefy.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.vipulasri.timelineview.TimelineView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelFinance;

public class AdapterFinance extends RecyclerView.Adapter<AdapterFinance.ViewHolder> {
    Context context;
    List<ModelFinance> data;
    MethodCallback methodCallback;

    public AdapterFinance(Context context, List<ModelFinance> data, MethodCallback methodCallback) {
        this.context = context;
        this.data = data;
        this.methodCallback = methodCallback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_finance, viewGroup, false);
        return new ViewHolder(view, i);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int i) {
        holder.date.setText(data.get(i).getDate());
        holder.txtJudul.setText(data.get(i).getKategori());
        holder.txtKeterangan.setText(data.get(i).getKeterangan());
        holder.txtJumlah.setText(data.get(i).getJumlah());
        if (data.get(i).getTipe().equalsIgnoreCase("pengeluaran")) {
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.shape_circle_red));
        } else {
            holder.mTimelineView.setMarker(ContextCompat.getDrawable(context, R.drawable.shape_circle));
        }
        holder.imgEdit.setOnClickListener(new View.OnClickListener() {
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
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.txt_judul)
        TextView txtJudul;
        @BindView(R.id.keterangan)
        TextView txtKeterangan;
        @BindView(R.id.jumlah)
        TextView txtJumlah;
        @BindView(R.id.img_edit)
        ImageView imgEdit;
        @BindView(R.id.place_finance)
        CardView placeFinance;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mTimelineView = itemView.findViewById(R.id.timeline);
            mTimelineView.initLine(viewType);
        }
    }

    public interface MethodCallback {
        void onClick(List<ModelFinance> data, int position);
    }
}
