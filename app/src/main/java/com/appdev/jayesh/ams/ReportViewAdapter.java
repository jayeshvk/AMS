package com.appdev.jayesh.ams;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;


public class ReportViewAdapter extends ArrayAdapter<EmployeeReport> {

    private Context mContext;
    private List<EmployeeReport> employeeReports = new ArrayList<>();

    public ReportViewAdapter(@NonNull Context context, ArrayList<EmployeeReport> list) {
        super(context, 0, list);
        mContext = context;
        employeeReports = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.reportviewadapter_list_item, parent, false);

        EmployeeReport employeeReport = employeeReports.get(position);

        TextView date = listItem.findViewById(R.id.date);
        date.setText(employeeReport.getDate());

        TextView hours = listItem.findViewById(R.id.hours);
        hours.setText(employeeReport.getHours());

        TextView amount = listItem.findViewById(R.id.amount);
        amount.setText(employeeReport.amount);

        return listItem;
    }
}