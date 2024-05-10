package com.example.noratelproject2024.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noratelproject2024.Models.Lines;
import com.example.noratelproject2024.Models.Shift;
import com.example.noratelproject2024.R;

import java.util.List;

public class SelectLineAdapter extends RecyclerView.Adapter<SelectLineAdapter.SelectLineViewHolder> {
    List<Lines> linesList;
    private OnLineSelectedListener listener;
    public interface OnLineSelectedListener {
        void onLineSelected(Lines selectedLine);
    }

    public SelectLineAdapter(List<Lines> linesList, OnLineSelectedListener listener) {
        this.linesList = linesList;
        this.listener = listener;
    }

    public SelectLineAdapter(List<Lines> lines) {
        this.linesList = lines;
    }

    @NonNull
    @Override
    public SelectLineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectlines_item, parent, false);
        return new SelectLineViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectLineViewHolder holder, int position) {
        Lines lines = linesList.get(position);
        holder.sub_uniName.setText(lines.getSUB_UNINAME().trim());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(v.getContext(), lines.getSUB_UNINAME(), Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onLineSelected(lines);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return linesList.size();
    }

    class SelectLineViewHolder extends RecyclerView.ViewHolder{
        TextView sub_uniName;
        public SelectLineViewHolder(@NonNull View itemView) {
            super(itemView);
            sub_uniName = itemView.findViewById(R.id.textViewRoster);
        }
    }

}
