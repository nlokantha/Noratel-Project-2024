package com.example.noratelproject2024.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noratelproject2024.Models.JobCard;
import com.example.noratelproject2024.R;

import java.util.List;

public class SelectJobCardAdapter extends RecyclerView.Adapter<SelectJobCardAdapter.JobcardViewHolder> {
    List<JobCard> jobCardList;
    private OnJobCardSelectedListener listener;
    public interface OnJobCardSelectedListener{
        void onJobCardSelected(JobCard jobCard);
    }

    public SelectJobCardAdapter(List<JobCard> jobCardList, OnJobCardSelectedListener listener) {
        this.jobCardList = jobCardList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectjobcard_items, parent, false);
        return new JobcardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull JobcardViewHolder holder, int position) {

        JobCard jobCard = jobCardList.get(position);
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
        return jobCardList.size();
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
