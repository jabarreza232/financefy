package id.co.evolution.financefy.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelFaq;
public class AdapterFAQ extends RecyclerView.Adapter<AdapterFAQ.FaqViewHolder>{
    private final List<ModelFaq> faqList;

    public AdapterFAQ(List<ModelFaq> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
        ModelFaq faq = faqList.get(position);
        holder.txtQuestion.setText(faq.getQuestion());
        holder.txtAnswer.setText(faq.getAnswer());
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    public static class FaqViewHolder extends RecyclerView.ViewHolder {
        TextView txtQuestion, txtAnswer;

        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            txtQuestion = itemView.findViewById(R.id.txt_question);
            txtAnswer = itemView.findViewById(R.id.txt_answer);
        }
    }
}
