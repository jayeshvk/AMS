package com.appdev.jayesh.ams;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextPaint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cie.btp.CieBluetoothPrinter;
import com.cie.btp.DebugLog;
import com.cie.btp.PrintDensity;
import com.cie.btp.PrinterWidth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import static com.cie.btp.BtpConsts.PRINTER_DISCONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_DEVICE_NAME;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_CONNECTING;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_LISTEN;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_CONN_STATE_NONE;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MESSAGES;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOTIFICATION_MSG;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_CONNECTED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_NOT_FOUND;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_SAVED;
import static com.cie.btp.BtpConsts.RECEIPT_PRINTER_STATUS;

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

    //printer variables
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public CieBluetoothPrinter mPrinter = CieBluetoothPrinter.INSTANCE;
    private static final int LEFT = 1;
    private static final int RIGHT = -1;
    private static final int CENTER = 0;
    public static final int REQUEST_ENABLE_BT = 9;
    boolean printerConnected;

    double hours, amt;
    String employeeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports);

        dbh = DatabaseHelper.getInstance(this);
        employeeSpinner = findViewById(R.id.employeeSpinner);
        employeeArrayList = new ArrayList<>();
        employeeArrayList = dbh.getAllEmployee();
        Collections.sort(employeeArrayList, Employee.empNameCompare);
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
        employeeName = e.getEmployeeName();
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
            hours = totalhours;
            amt = totals;
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

    public void buttonPrint(View view) {
        if (employeeReports.size() <= 0)
            return;
        if (printerConnected)
            print();
        else connectPrinter();
    }

    private void connectPrinter() {

        if (!isBluetoothEnabled()) {
            toast("Bluetooth is not switched on");

        } else {
            if (!printerConnected)
                connect();
        }
    }

    private boolean isBluetoothEnabled() {
        if (mBluetoothAdapter == null) {
            toast("Device does not support bluetooth");
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                toast("Switch on Bluetooth to use printer");
                return false;
            } else {
                return true;
            }
        }
    }

    void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    void connect() {
        toast("Connecting to Printer");
        try {
            mPrinter.initService(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPrinter.connectToPrinter("D8:80:39:F8:37:A5");
    }

    private final BroadcastReceiver ReceiptPrinterMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DebugLog.logTrace("Printer Message Received");
            Bundle b = intent.getExtras();
            switch (b.getInt(RECEIPT_PRINTER_STATUS)) {
                case RECEIPT_PRINTER_CONN_STATE_NONE:
                    //toast("Printer Not Connected");
                    printerConnected = false;
                    break;
                case RECEIPT_PRINTER_CONN_STATE_LISTEN:
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTING:
                    toast("Connecting to Printer, please wait");
                    break;
                case RECEIPT_PRINTER_CONN_STATE_CONNECTED:
                    printerConnected = true;
                    toast("Printer Connected");
                    print();
                    break;
                case RECEIPT_PRINTER_CONN_DEVICE_NAME:
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_ERROR_MSG:
                    String n = b.getString(RECEIPT_PRINTER_MSG);
                    break;
                case RECEIPT_PRINTER_NOTIFICATION_MSG:
                    String m = b.getString(RECEIPT_PRINTER_MSG);
                    //toast(m);
                    break;
                case RECEIPT_PRINTER_NOT_CONNECTED:
                    //toast("Status : Printer Not Connected");
                    printerConnected = false;
                    break;
                case RECEIPT_PRINTER_NOT_FOUND:
                    toast("Printer Not Found");
                    printerConnected = false;
                    break;
                case RECEIPT_PRINTER_SAVED:
                    //toast("Printer Saved as Favourite");
                    break;
                case PRINTER_DISCONNECTED:
                    toast("Printer Disconnected");
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPrinter.onActivityRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(requestCode + "*" + resultCode + "*" + data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    connect();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    toast("Bluetooth not switched on");
                    //checkFinish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onResume() {
        DebugLog.logTrace();
        mPrinter.onActivityResume();
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RECEIPT_PRINTER_MESSAGES);
        LocalBroadcastManager.getInstance(this).registerReceiver(ReceiptPrinterMessageReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(ReceiptPrinterMessageReceiver);
        } catch (Exception e) {
            DebugLog.logException(e);
        }
    }

    @Override
    protected void onPause() {
        mPrinter.onActivityPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        DebugLog.logTrace("onDestroy");
        mPrinter.onActivityDestroy();
        super.onDestroy();
    }

    private void mPrintUnicodeText(String text, int size, int almnt, int typefaceType) {
        Layout.Alignment alignment = null;
        switch (almnt) {
            case 0:
                alignment = Layout.Alignment.ALIGN_CENTER;
                break;
            case 1:
                alignment = Layout.Alignment.ALIGN_NORMAL;
                break;
            case -1:
                alignment = Layout.Alignment.ALIGN_OPPOSITE;
                break;
        }
        Typeface plain = Typeface.createFromAsset(getAssets(), "fonts/Cousine-Regular.ttf");
        Typeface typeface = Typeface.create(plain, typefaceType);
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(size);
        textPaint.setTypeface(typeface);
        System.out.println("Print status **" + mPrinter.printUnicodeText(text, alignment, textPaint));
        System.out.println("Print status Stat**" + mPrinter.getPrinterStatus());
    }

    private String rightpad(String text, int length) {
        return String.format("%-" + length + "." + length + "s", text);
    }

    private String leftpad(String text, int length) {
        return String.format("%" + length + "." + length + "s", text);
    }

    private void print() {

        mPrinter.setPrinterWidth(PrinterWidth.PRINT_WIDTH_48MM);
        mPrinter.setAlignmentCenter();
        int textSize = 22;
        mPrinter.setPrintDensity(PrintDensity.FADE);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/ErasBoldITC.ttf");
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40);
        textPaint.setTypeface(custom_font);
        mPrinter.printUnicodeText("Payments", Layout.Alignment.ALIGN_CENTER, textPaint);
        mPrinter.resetPrinter();
        mPrinter.setPrintDensity(PrintDensity.NORMAL);
        mPrintUnicodeText(UHelper.setPresentDateddMMyyyy(), 30, CENTER, Typeface.NORMAL);
        mPrintUnicodeText("Name : " + employeeName, textSize, LEFT, Typeface.BOLD_ITALIC);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", textSize, LEFT, Typeface.NORMAL);
        mPrintUnicodeText(rightpad("Date", 10) + " " + leftpad("Hours", 8) + " " + leftpad("Amount", 9), textSize, LEFT, Typeface.NORMAL);
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", textSize, LEFT, Typeface.NORMAL);
        for (EmployeeReport e : employeeReports) {
            String date = rightpad(e.getDate(), 10);
            String hours = leftpad(e.getHours(), 8);
            String amt = leftpad(UHelper.stringDouble(e.getAmount()), 9);
            ;

            mPrintUnicodeText(date + " " + hours + " " + amt, textSize, LEFT, Typeface.NORMAL);
        }
        mPrintUnicodeText("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", textSize, LEFT, Typeface.NORMAL);
        mPrintUnicodeText(rightpad("Total", 10) + " " + leftpad(UHelper.stringDouble(hours + ""), 8) + " " + leftpad(UHelper.stringDouble(amt + ""), 9), textSize, LEFT, Typeface.BOLD_ITALIC);

        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.printLineFeed();
        mPrinter.resetPrinter();
    }

}
