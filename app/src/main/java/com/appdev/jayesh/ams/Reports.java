package com.appdev.jayesh.ams;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class Reports extends AppCompatActivity {
    private Spinner employeeSpinner;
    private ArrayAdapter<Employee> empAdapter;
    private DatabaseHelper dbh;
    private ArrayList<Employee> employeeArrayList;
    private int mYear, mMonth, mDay, mHour, mMinute;

    private ListView listView;
    private ReportViewAdapter mAdapter;
    private ArrayList<EmployeeReport> employeeReports = new ArrayList<>();

    private String fromreverse, toreverse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports);

        dbh = DatabaseHelper.getInstance(this);
        employeeSpinner = findViewById(R.id.employeeSpinner);
        employeeArrayList = new ArrayList<>();
        employeeArrayList = dbh.getAllEmployee();

        empAdapter = new ArrayAdapter<Employee>(this, android.R.layout.simple_spinner_dropdown_item, employeeArrayList);
        empAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(empAdapter);

        employeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listView = findViewById(R.id.reportlist);
        mAdapter = new ReportViewAdapter(this, employeeReports);
        listView.setAdapter(mAdapter);


    }

    public void datePicker(final View viewq) {
        {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            String month = UHelper.intLeadingZero(2, (monthOfYear + 1));
                            String date = UHelper.intLeadingZero(2, dayOfMonth);

                            switch (viewq.getId()) {
                                case R.id.etfromdate:
                                    EditText fromdate = findViewById(R.id.etfromdate);
                                    fromdate.setText(date + "-" + month + "-" + year);
                                    fromreverse = (year + "-" + month + "-" + date);
                                    break;
                                case R.id.ettodate:
                                    EditText todate = findViewById(R.id.ettodate);
                                    todate.setText(date + "-" + month + "-" + year);
                                    toreverse = (year + "-" + month + "-" + date);
                                    break;
                            }
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    }

    public void buttonFind(View view) {
        generateReport();
    }

    private void generateReport() {
        double totals = 0;
        double totalhours = 0;
        TextView tvtotals = findViewById(R.id.totals);
        Employee e = (Employee) employeeSpinner.getSelectedItem();
        if (e != null) {
            employeeReports = dbh.employeeReport(e.getId(), fromreverse + " 00:00:00", toreverse + " 00:00:00");

            if (employeeReports.size() > 0) {
                for (EmployeeReport er : employeeReports
                ) {
                    System.out.println(er.getHours());
                    long mili = Long.parseLong(er.getHours());
                    Double hours = UHelper.parseDouble(convertMillis(mili));
                    Double amount = hours * (e.getWage() / 8);
                    totals += amount;
                    totalhours += hours;
                    er.setHours(convertMillis(mili));
                    er.setAmount(UHelper.stringDouble(amount + ""));
                    er.setDate(UHelper.dateFormatymdhmsTOddmyyyy(er.getDate()));
                }
            }
            tvtotals.setText("Total Hours : " + UHelper.stringDouble(totalhours + "") + " Total Amount : " + UHelper.stringDouble(totals + ""));

            mAdapter.clear();
            mAdapter.addAll(employeeReports);
            mAdapter.notifyDataSetChanged();
            listView.invalidate();
        }
    }

    public String convertMillis(long mili) {
        double hours = 0;
        double minutes = 0;
        double seconds = 0;
        float d = 0;
        double day = 8 * 60 * 60 * 1000;
        double hour = 60 * 60 * 1000;
        double minute = 60 * 1000;

        if (mili > 0) {
            hours = (mili) / hour;
            minutes = ((mili % hour) / minute) / 60;
        }

        return UHelper.stringDouble(hours + "");
    }

}
