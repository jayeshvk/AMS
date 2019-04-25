package com.appdev.jayesh.ams;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ReportDetailRecyclerViewAdapter extends RecyclerView.Adapter<ReportDetailRecyclerViewAdapter.MyViewHolder> {

    private List<ReportsDetailedModel> reportsDetailedModels;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView out, in, date, hours;

        public MyViewHolder(View view) {
            super(view);
            out = view.findViewById(R.id.out);
            date = view.findViewById(R.id.date);
            in = view.findViewById(R.id.in);
            hours = view.findViewById(R.id.hours);
        }

    }


    public ReportDetailRecyclerViewAdapter(List<ReportsDetailedModel> reportsDetailedModels) {
        this.reportsDetailedModels = reportsDetailedModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_report_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ReportsDetailedModel reportsDetailedModel = reportsDetailedModels.get(position);
        holder.out.setText(reportsDetailedModel.getOut());
        holder.date.setText(reportsDetailedModel.getDate());
        holder.in.setText(reportsDetailedModel.getIn());
        holder.hours.setText(reportsDetailedModel.getHours());


    }


    @Override
    public int getItemCount() {
        return reportsDetailedModels.size();
    }
}