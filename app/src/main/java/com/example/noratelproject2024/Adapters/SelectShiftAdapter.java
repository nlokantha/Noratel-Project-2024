package com.example.noratelproject2024.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.noratelproject2024.Models.Shift;
import com.example.noratelproject2024.R;

import java.util.List;

public class SelectShiftAdapter extends RecyclerView.Adapter<SelectShiftAdapter.SelectShiftViewHolder> {
    List<Shift> shiftList;
    private OnShiftSelectedListener listener;

    public interface OnShiftSelectedListener {
        void onShiftSelected(Shift selectedShift);
    }

    public SelectShiftAdapter(List<Shift> shiftList, OnShiftSelectedListener listener) {
        this.shiftList = shiftList;
        this.listener = listener;
    }

    public SelectShiftAdapter(List<Shift> shiftList) {
        this.shiftList = shiftList;
    }

    @NonNull
    @Override
    public SelectShiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectshift_item, parent, false);
        return new SelectShiftViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectShiftViewHolder holder, int position) {
        Shift shift = shiftList.get(position);
        holder.textViewRoster.setText(shift.getRoster().trim());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(v.getContext(), shift.getRoster(), Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onShiftSelected(shift);
                }
            }
        });

    }
    @Override
    public int getItemCount() {
        return shiftList.size();
    }
    class SelectShiftViewHolder extends RecyclerView.ViewHolder{
        TextView textViewRoster;

        public SelectShiftViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewRoster = itemView.findViewById(R.id.textViewRoster);
        }
    }
}
