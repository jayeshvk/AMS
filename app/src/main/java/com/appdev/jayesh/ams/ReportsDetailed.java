package com.appdev.jayesh.ams;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReportsDetailed extends AppCompatActivity {
    private Spinner employeeSpinner;
    private ArrayAdapter<Employee> empAdapter;
    private DatabaseHelper dbh;
    private ArrayList<Employee> employeeArrayList;
    private int mYear, mMonth, mDay, mHour, mMinute;

    private String fromreverse, toreverse;

    RecyclerView recyclerView;
    List<ReportsDetailedModel> data;
    ReportDetailRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports_detailed);

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
                extractReport();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        recyclerView = findViewById(R.id.dr_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ReportsDetailed.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        data = new ArrayList<>();
        adapter = new ReportDetailRecyclerViewAdapter(data);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                ReportsDetailedModel rdl = new ReportsDetailedModel();
                rdl = data.get(position);
                empLogViewPopup(rdl, position);

            }
        }));
    }

    private void empLogViewPopup(final ReportsDetailedModel rdl, final int position) {
        final Dialog dialog = new Dialog(this);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setContentView(R.layout.emplogview_popup_window);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();

        EmployeeLog e = dbh.getEmployeeLogById(rdl.getId());

        final EditText loginDate = dialog.findViewById(R.id.etLoginDate);
        final EditText logoutDate = dialog.findViewById(R.id.etLogoutDate);
        final EditText loginTime = dialog.findViewById(R.id.etLoginTime);
        final EditText logoutTime = dialog.findViewById(R.id.etLogoutTime);
        final EditText text = dialog.findViewById(R.id.text);

        Button save = dialog.findViewById(R.id.save);
        Button delete = dialog.findViewById(R.id.delete);
        Button cancel = dialog.findViewById(R.id.cancel);

        String indate = (UHelper.militoddmmyyyyhhmmss(e.getLoginTime())).substring(0, 10);
        String outdate = (UHelper.militoddmmyyyyhhmmss(e.getLogoutTime())).substring(0, 10);

        loginDate.setText(indate);
        loginTime.setText(rdl.getIn());
        logoutDate.setText(outdate);
        logoutTime.setText(rdl.getOut());
        text.setText(e.getText());

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                EmployeeLog e = new EmployeeLog();

                String intime = UHelper.dateFormatdmyhmaToymdhms(loginDate.getText().toString() + " " + loginTime.getText().toString());
                long inmili = UHelper.ymdhmsTomili(intime);
                e.setLoginTime(inmili);

                String indate = UHelper.dateFormatdmyhmaToymdhms(loginDate.getText().toString() + " " + "12:00 AM");
                e.setLoginDate(indate);

                String outtime = UHelper.dateFormatdmyhmaToymdhms(logoutDate.getText().toString() + " " + logoutTime.getText().toString());
                long outmili = UHelper.ymdhmsTomili(outtime);
                e.setLogoutTime(outmili);

                e.setText(text.getText().toString());

                if ((inmili > outmili)) {
                    Toast.makeText(getApplicationContext(), "Incorrect Time", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    e.setTimeDiff(outmili - inmili);
                    e.setId(rdl.getId());
                    dbh.updateEmployeeLog(e);
                    Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();

                    ReportsDetailedModel rdm = new ReportsDetailedModel();
                    rdm.setId(e.getId());
                    rdm.setDate(UHelper.dateFormatymdhmsTOddmyyyy(e.getLoginDate()));

                    String in = UHelper.militoddmmyyyyhhmmss(e.getLoginTime());
                    String out = UHelper.militoddmmyyyyhhmmss(e.getLogoutTime());

                    rdm.setIn(UHelper.timeFormathmsTOhma(in.substring(19 - 8)));
                    rdm.setOut(UHelper.timeFormathmsTOhma(out.substring(19 - 8)));
                    int time[] = UHelper.convertMillis(e.getTimeDiff());

                    String d = UHelper.intLeadingZero(2, time[0]);
                    String h = UHelper.intLeadingZero(2, time[1]);
                    String m = UHelper.intLeadingZero(2, time[2]);

                    rdm.setHours(d + "d " + h + "H " + m + "m");
                    data.set(position, rdm);
                    adapter.notifyDataSetChanged();
                    setTotals();
                    dialog.dismiss();
                }


            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbh.deleteEmployeeLogbyId(rdl.getId());
                dialog.dismiss();
                data.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void buttonFind(View view) {
        extractReport();
    }

    private void extractReport() {
        data.clear();
        Employee e = (Employee) employeeSpinner.getSelectedItem();
        ArrayList<EmployeeLog> employeeLogs;
        if (e != null) {
            employeeLogs = dbh.getDetailedEmployeeLogList(e.getId(), fromreverse + " 00:00:00", toreverse + " 23:59:59");

            if (employeeLogs.size() > 0) {
                for (EmployeeLog el : employeeLogs
                ) {
                    ReportsDetailedModel rdm = new ReportsDetailedModel();
                    rdm.setId(el.getId());
                    rdm.setDate(UHelper.dateFormatymdhmsTOddmyyyy(el.getLoginDate()));

                    String in = UHelper.militoddmmyyyyhhmmss(el.getLoginTime());
                    String out = UHelper.militoddmmyyyyhhmmss(el.getLogoutTime());

                    rdm.setIn(UHelper.timeFormathmsTOhma(in.substring(19 - 8)));
                    rdm.setOut(UHelper.timeFormathmsTOhma(out.substring(19 - 8)));
                    int time[] = UHelper.convertMillis(el.getTimeDiff());

                    String d = UHelper.intLeadingZero(2, time[0]);
                    String h = UHelper.intLeadingZero(2, time[1]);
                    String m = UHelper.intLeadingZero(2, time[2]);

                    rdm.setHours(d + "d " + h + "H " + m + "m");
                    data.add(rdm);
                }
            }
            adapter.notifyDataSetChanged();
            setTotals();
        }
    }

    private void setTotals() {
        Employee e = (Employee) employeeSpinner.getSelectedItem();
        long timediff = dbh.totalHours(e.getId(), fromreverse + " 00:00:00", toreverse + " 23:59:59");
        TextView tv = findViewById(R.id.totalhours);
        tv.setText("Total Hours " + convertMillis(timediff));
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
                                    EditText fromdate = viewq.findViewById(R.id.etfromdate);
                                    fromdate.setText(date + "-" + month + "-" + year);
                                    fromreverse = (year + "-" + month + "-" + date);
                                    break;
                                case R.id.ettodate:
                                    EditText todate = viewq.findViewById(R.id.ettodate);
                                    todate.setText(date + "-" + month + "-" + year);
                                    toreverse = (year + "-" + month + "-" + date);
                                    break;
                                case R.id.etLoginDate:
                                    EditText etfromdate = viewq.findViewById(R.id.etLoginDate);
                                    etfromdate.setText(date + "-" + month + "-" + year);
                                    break;
                                case R.id.etLogoutDate:
                                    EditText ettodate = viewq.findViewById(R.id.etLogoutDate);
                                    ettodate.setText(date + "-" + month + "-" + year);
                                    break;
                            }
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    }

    public void timePicker(final View viewq) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        String hours = UHelper.intLeadingZero(2, hourOfDay);
                        String minutes = UHelper.intLeadingZero(2, minute);

                        switch (viewq.getId()) {
                            case R.id.etLoginTime:
                                EditText etin = viewq.findViewById(R.id.etLoginTime);
                                etin.setText(UHelper.timeFormathmsTOhma(hours + ":" + minutes + ":00"));
                                break;
                            case R.id.etLogoutTime:
                                EditText etout = viewq.findViewById(R.id.etLogoutTime);
                                etout.setText(UHelper.timeFormathmsTOhma(hours + ":" + minutes + ":00"));
                                break;
                        }
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public String convertMillis(long mili) {
        long days = 0;
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        long day = 8 * 60 * 60 * 1000;
        long hour = 60 * 60 * 1000;
        long minute = 60 * 1000;
        long second = 1000;

        if (mili > 0) {
            days = mili / day;
            hours = (mili % day) / hour;
            minutes = (mili % hour) / minute;
            seconds = (mili % minute) / second;
        }

        return days + "D " + hours + "H " + minutes + "m";
    }

}

