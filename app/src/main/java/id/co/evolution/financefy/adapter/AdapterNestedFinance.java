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

import com.github.vipulasri.timelineview.TimelineView;

import java.util.List;
import java.util.Locale;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.callback.MethodCallback;
import id.co.evolution.financefy.databinding.ListFinanceBinding;
import id.co.evolution.financefy.databinding.ListNestedFinanceBinding;
import id.co.evolution.financefy.dialog.DialogPreviewImage;
import id.co.evolution.financefy.model.ModelFinance;

public class AdapterNestedFinance extends RecyclerView.Adapter<AdapterNestedFinance.ViewHolder> {
    Context context;
    List<ModelFinance> data;
    MethodCallback methodCallback;
    Locale locale;
    DialogPreviewImage dialogPreviewImage;
    public AdapterNestedFinance(Context context, List<ModelFinance> data, MethodCallback methodCallback) {
        this.context = context;
        this.data = data;
        this.methodCallback = methodCallback;
        dialogPreviewImage = new DialogPreviewImage(context);
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_nested_finance, viewGroup, false);

        return new ViewHolder(view, i);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int i) {
        holder.binding.txtJudul.setText(data.get(i).getKategori());
        holder.binding.keterangan.setText(data.get(i).getKeterangan().isEmpty()||data.get(i).getKeterangan()==null?"Tidak ada deskripsi":data.get(i).getKeterangan());
        holder.binding.jumlah.setText(data.get(i).getJumlahDesc(locale));
        if(data.get(i).getPhoto()!=null){
            holder.binding.btnCamera.setVisibility(View.VISIBLE);
            holder.binding.btnCamera.setOnClickListener(v->{
                dialogPreviewImage.show(data.get(i).getPhoto());
            });
        }else{
            holder.binding.btnCamera.setVisibility(View.GONE);
        }
        if (data.get(i).getTipe().equalsIgnoreCase("pengeluaran")) {
            holder.binding.jumlah.setTextColor(ContextCompat.getColor(context, R.color.red));

        } else {
            holder.binding.jumlah.setTextColor(ContextCompat.getColor(context, R.color.green));
        }

        holder.binding.placeFinance.setOnClickListener(v->{
            methodCallback.onClick(data, i);
        });

        showImageCategory(holder,data.get(i).getKategori());
    }
    private void showImageCategory(ViewHolder holder,String category){
        //PENGELUARAN
        if(category.contains(context.getString(R.string.belanja_umum))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_shopping_basket_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_shopping));
        }else if(category.contains(context.getString(R.string.makanan))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_fastfood_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_food));
        }else if(category.contains(context.getString(R.string.pulsa_hp))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_phonelink_ring_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_pulsa));
        }else if(category.contains(context.getString(R.string.transportasi))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_emoji_transportation_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_transport));
        }else if(category.contains(context.getString(R.string.tagihan))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_credit_card_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_bill));
        }else if(category.contains(context.getString(R.string.paket_internet))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_language_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_network));

        }

        //PEMASUKAN
        if(category.contains(context.getString(R.string.gaji))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_account_balance_wallet_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_gaji));
        }else if(category.contains(context.getString(R.string.bonus))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_monetization_on_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_bonus));
        }else if(category.contains(context.getString(R.string.hasil_usaha))){
            holder.binding.imgCategory.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.baseline_business_24));
            holder.binding.imgCategory.setBackgroundColor(ContextCompat.getColor(context,R.color.color_hasil_usaha));
        }
    }
    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        ListNestedFinanceBinding binding;

        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);

        }
    }
}
