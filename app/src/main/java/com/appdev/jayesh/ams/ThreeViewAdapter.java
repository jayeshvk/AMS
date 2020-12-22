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


public class ThreeViewAdapter extends ArrayAdapter<Employee> {

    private Context mContext;
    private List<Employee> employeeList = new ArrayList<>();

    public ThreeViewAdapter(@NonNull Context context, ArrayList<Employee> list) {
        super(context, 0, list);
        mContext = context;
        employeeList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.threeviewadapter_list_item, parent, false);

        Employee mEmployee = employeeList.get(position);

        TextView id = listItem.findViewById(R.id.id);
        id.setText(mEmployee.getId()+"");

        TextView text1 = listItem.findViewById(R.id.text1);
        text1.setText(mEmployee.getEmployeeName());

        TextView text2 = listItem.findViewById(R.id.text2);
        text2.setText(mEmployee.getWage() + "");

        return listItem;
    }
}