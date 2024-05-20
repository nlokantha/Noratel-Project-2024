package com.example.noratelproject2024.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noratelproject2024.Models.JobCard;
import com.example.noratelproject2024.R;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class SelectJobCardAdapter extends RecyclerView.Adapter<SelectJobCardAdapter.JobcardViewHolder> {
    private static final String TAG = "demo";
    List<JobCard> mjobCardList;
    List<JobCard> jobCardListFull;
    private OnJobCardSelectedListener listener;
    public interface OnJobCardSelectedListener{
        void onJobCardSelected(JobCard jobCard);
    }

    public SelectJobCardAdapter(List<JobCard> jobCardList, OnJobCardSelectedListener listener) {
        this.mjobCardList = jobCardList;
        this.listener = listener;
        jobCardListFull = new ArrayList<>(mjobCardList);
    }

    @NonNull
    @Override
    public JobcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectjobcard_items, parent, false);
        return new JobcardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull JobcardViewHolder holder, int position) {

        JobCard jobCard = mjobCardList.get(position);
        holder.textViewJobCardNo.setText(jobCard.getJObCardNo());
        holder.textViewStatus.setText(jobCard.getStatus());
        holder.textViewJC_Serial_No.setText(jobCard.getJC_Serial_No());
        holder.textViewDate.setText(jobCard.getDate());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), jobCard.getJObCardNo(), Toast.LENGTH_SHORT).show();
                listener.onJobCardSelected(jobCard);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mjobCardList.size();
    }

    public void filter(String text) {
        jobCardListFull = new ArrayList<>(mjobCardList);
        mjobCardList.clear();
        if (text.isEmpty()) {
            mjobCardList.addAll(jobCardListFull);
        } else {
            text = text.toLowerCase();
            Log.d(TAG, "filter: jobCardListFull" + jobCardListFull.size());
            for (JobCard jobCard : jobCardListFull) {
                Log.d(TAG, "filter: jobCardListFull" + jobCardListFull.size());
                if (jobCard.getJObCardNo().toLowerCase().contains(text)) {
                    mjobCardList.add(jobCard);
                    Log.d(TAG, "filter: adaptor  "+mjobCardList.size());
                }
            }
        }
        notifyDataSetChanged();
    }
    public void updateList(List<JobCard> newList) {
        mjobCardList.clear();
        mjobCardList.addAll(newList);
        notifyDataSetChanged();
    }



    class JobcardViewHolder extends RecyclerView.ViewHolder{
        TextView textViewJobCardNo,textViewStatus,textViewDate,textViewJC_Serial_No;

        public JobcardViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewJobCardNo = itemView.findViewById(R.id.textViewJobCardNo);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewJC_Serial_No = itemView.findViewById(R.id.textViewJC_Serial_No);


        }
    }
}
