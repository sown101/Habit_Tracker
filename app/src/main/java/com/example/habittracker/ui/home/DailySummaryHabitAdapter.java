package com.example.habittracker.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.R;

import java.util.ArrayList;
import java.util.List;

public class DailySummaryHabitAdapter
        extends RecyclerView.Adapter<DailySummaryHabitAdapter.ViewHolder> {

    private final List<String> items;

    public DailySummaryHabitAdapter(List<String> items) {
        if (items == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = new ArrayList<>(items);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_summary_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String line = items.get(position);
        holder.tvHabitLine.setText(line);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvHabitLine;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHabitLine = itemView.findViewById(R.id.tvHabitLine);
        }
    }
}