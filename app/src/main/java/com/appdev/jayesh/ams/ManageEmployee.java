package com.appdev.jayesh.ams;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageEmployee extends AppCompatActivity {

    private DatabaseHelper dbh;

    private EditText employeeName, wage;
    private Button saveButton;

    private ListView listView;
    private ThreeViewAdapter mAdapter;
    private ArrayList<Employee> employeeArrayList = new ArrayList<>();

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent();
        intent.putExtra("MESSAGE", "message");
        setResult(2, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_employee);

        listView = findViewById(R.id.list);
        mAdapter = new ThreeViewAdapter(this, employeeArrayList);
        listView.setAdapter(mAdapter);

        dbh = DatabaseHelper.getInstance(this);


        employeeName = findViewById(R.id.employeeName);
        wage = findViewById(R.id.employeeWage);
        saveButton = findViewById(R.id.save);

        List<Employee> employee = dbh.getAllEmployee();
        employeeArrayList.addAll(employee);
        Collections.sort(employeeArrayList, Employee.empNameCompare);
        mAdapter.notifyDataSetChanged();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Employee emp = new Employee(employeeName.getText().toString(), UHelper.parseDouble(wage.getText().toString()));
                dbh.addEmployee(emp);
                employeeName.setText(null);
                wage.setText(null);
                employeeArrayList.add(emp);
                Collections.sort(employeeArrayList, Employee.empNameCompare);
                mAdapter.notifyDataSetChanged();
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                modifyData((Employee) parent.getItemAtPosition(position), position);
                return false;
            }
        });


    }

    private void modifyData(final Employee tempEmployee, final int position) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View v = layoutInflater.inflate(R.layout.modify, null);

        final EditText etName = v.findViewById(R.id.empName);
        final EditText etWage = v.findViewById(R.id.empWage);
        etName.setText(tempEmployee.getEmployeeName());
        etWage.setText(tempEmployee.getWage() + "");

        new AlertDialog.Builder(this)
                .setTitle("Update")
                .setView(v)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Employee model = new Employee(tempEmployee.getId(), etName.getText().toString(), UHelper.parseDouble(etWage.getText().toString()));
                                dbh.updateEmployee(model);
                                employeeArrayList.set(position, new Employee(tempEmployee.getId(), etName.getText().toString(), UHelper.parseDouble(etWage.getText().toString())));
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                )
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        int stat = dbh.deleteEmployee(tempEmployee.getId());
                        if (stat != 0) {
                            employeeArrayList.remove(position);
                            mAdapter.notifyDataSetChanged();
                            Toast.makeText(getApplicationContext(), "Employee Deleted", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(getApplicationContext(), "Delete the Employee Transactions first", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }
}


