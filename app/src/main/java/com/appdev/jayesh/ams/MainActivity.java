package com.appdev.jayesh.ams;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.appdev.jayesh.ams.ExpandableMenu.ExpandableListAdapter;
import com.appdev.jayesh.ams.ExpandableMenu.ExpandedMenuModel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.appdev.jayesh.ams.DatabaseHelper.DATABASE_NAME;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;

    ArrayList<Employee> employeeArrayList;

    Spinner employeeSpinner;
    ArrayAdapter<Employee> empAdapter;


    private ProgressDialog pDialog;
    private DatabaseReference.CompletionListener completionListener;
    private DatabaseHelper dbh;

    private int mYear, mMonth, mDay, mHour, mMinute;

    FirebaseStorage firebaseStorage;
    FirebaseDatabase firebaseDatabase;
    ProgressDialog progressDialog;
    StorageReference mStorageReference;

    FirebaseAuth mAuth;
    FirebaseUser user;

    ExpandableListAdapter mMenuAdapter;
    ExpandableListView expandableList;
    List<ExpandedMenuModel> listDataHeader;
    HashMap<ExpandedMenuModel, List<String>> listDataChild;
    private int lastExpandedPosition = -1;

    TextView loggedInEmployeeNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbh = DatabaseHelper.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        loggedInEmployeeNames = findViewById(R.id.logedInEmployee);


        progressDialog = new ProgressDialog(MainActivity.this);
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        setupEmployeeSpinner();
        setupNavigationDrawer();
        PrepareMenu();
        populateExpandableList();
        setEmployeeData();
        getEmployeesLogedIn();
    }

    private void setupEmployeeSpinner() {
        employeeSpinner = findViewById(R.id.employeeSpinner);
        employeeArrayList = new ArrayList<>();
        employeeArrayList = dbh.getAllEmployee();
        empAdapter = new ArrayAdapter<Employee>(this, android.R.layout.simple_spinner_dropdown_item, employeeArrayList);
        empAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        employeeSpinner.setAdapter(empAdapter);
        employeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setEmployeeData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "Permission provided, backup data again.", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(MainActivity.this, "Please provide Storgae permission", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            System.out.println("toggled");

            //to collapse all the menu item on toggle
            int count = mMenuAdapter.getGroupCount();
            for (int i = 0; i < count; i++)
                expandableList.collapseGroup(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d("CDA", "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public void loginButton(View view) {
        login(System.currentTimeMillis());
        setEmployeeData();
        getEmployeesLogedIn();
    }

    public void logoutButton(View view) {
        logout(System.currentTimeMillis());
        setEmployeeData();
        getEmployeesLogedIn();
    }


    private void showProgressBar(final boolean visibility, final String message) {

        runOnUiThread(new Runnable() {
            public void run() {
                pDialog.setMessage(message);
                if (visibility)
                    showpDialog();
                else hidepDialog();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            mDrawerLayout.closeDrawers();
            employeeArrayList.clear();
            empAdapter.clear();
            for (Employee e : dbh.getAllEmployee()) {
                empAdapter.add(e);
            }
            empAdapter.notifyDataSetChanged();
            System.out.println("hurrayyyy");
        }

    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void showProgressBar(final boolean visibility) {

        runOnUiThread(new Runnable() {
            public void run() {
                if (visibility)
                    showpDialog();
                else hidepDialog();
            }
        });
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
                                case R.id.etLoginDate:
                                    EditText etin = findViewById(R.id.etLoginDate);
                                    etin.setText(date + "-" + month + "-" + year);
                                    break;
                                case R.id.etLogoutDate:
                                    EditText etout = findViewById(R.id.etLogoutDate);
                                    etout.setText(date + "-" + month + "-" + year);
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
                                EditText etin = findViewById(R.id.etLoginTime);
                                etin.setText(UHelper.timeFormathmsTOhma(hours + ":" + minutes + ":00"));
                                break;
                            case R.id.etLogoutTime:
                                EditText etout = findViewById(R.id.etLogoutTime);
                                etout.setText(UHelper.timeFormathmsTOhma(hours + ":" + minutes + ":00"));
                                break;
                        }
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    public void buttonManualLogin(View view) {
        EditText loginDate = findViewById(R.id.etLoginDate);
        EditText loginTime = findViewById(R.id.etLoginTime);
        String lindate = loginDate.getText().toString();
        String lintime = loginTime.getText().toString();
        if (lindate.length() > 0 && lintime.length() > 0) {
            login(UHelper.ddmmyyyyhmaTomili(lindate + " " + lintime));
            loginDate.setText(null);
            loginTime.setText(null);
        } else
            Toast.makeText(this, "Enter Login date and time", Toast.LENGTH_SHORT).show();
        setEmployeeData();
        getEmployeesLogedIn();
    }

    public void buttonManualLogout(View view) {
        EditText logoutDate = findViewById(R.id.etLogoutDate);
        EditText logoutTime = findViewById(R.id.etLogoutTime);
        String loutdate = logoutDate.getText().toString();
        String louttime = logoutTime.getText().toString();
        if (loutdate.length() > 0 && louttime.length() > 0) {
            logout(UHelper.ddmmyyyyhmaTomili(loutdate + " " + louttime));
            logoutDate.setText(null);
            logoutTime.setText(null);
        } else
            Toast.makeText(this, "Enter Logout date and time", Toast.LENGTH_SHORT).show();
        setEmployeeData();
        getEmployeesLogedIn();
    }

    public void login(long milisecond) {

        System.out.println("Before : " + milisecond);
        milisecond = UHelper.ymdhmTomili(UHelper.militoyyyymmddhm(milisecond));
        System.out.println("Before : " + milisecond);


        Employee employee = (Employee) employeeSpinner.getSelectedItem();
        if (employee != null) {
            EmployeeLog employeeLog = dbh.getLastEmployeeLog(employee.getId());
            if (employeeLog == null) {
                EmployeeLog templog = new EmployeeLog();
                templog.setEid(employee.getId());
                templog.setLoginTime(milisecond);
                templog.setLoginDate(UHelper.militoyyyymmdd(milisecond) + " 00:00:00");
                templog.setText(getEmployeeLogText());
                System.out.println(dbh.addEmployeeLog(templog));
            } else if (employeeLog.getLoginTime() > 0 && employeeLog.getLogoutTime() == 0) {
                Toast.makeText(this, "Employee already logged in, cant login again", Toast.LENGTH_SHORT).show();
            } else {
                EmployeeLog templog = new EmployeeLog();
                templog.setEid(employee.getId());
                templog.setLoginTime(milisecond);
                templog.setLoginDate(UHelper.militoyyyymmdd(milisecond) + " 00:00:00");
                templog.setText(getEmployeeLogText());
                System.out.println(dbh.addEmployeeLog(templog));
            }
        }
    }


    public void setEmployeeData() {
        TextView etLastLogin = findViewById(R.id.lastlogin);
        TextView etLastLogout = findViewById(R.id.lastlogout);
        TextView etTotalHours = findViewById(R.id.totalHours);
        TextView etcommentText = findViewById(R.id.comment);


        Employee e = (Employee) employeeSpinner.getSelectedItem();
        if (e != null) {
            EmployeeLog employeeLog = dbh.getLastEmployeeLog(e.getId());

            if (employeeLog.getLoginTime() != 0)
                etLastLogin.setText(UHelper.militoddmmyyyyhhmma(employeeLog.getLoginTime()));
            else etLastLogin.setText(null);

            if (employeeLog.getLogoutTime() != 0)
                etLastLogout.setText(UHelper.militoddmmyyyyhhmma(employeeLog.getLogoutTime()));
            else etLastLogout.setText(null);

            long timediff = dbh.totalHours(e.getId(), UHelper.setPresentDateyyyyMMdd() + " 00:00:00", UHelper.setPresentDateyyyyMMdd() + " 23:59:59");
            etTotalHours.setText(convertMillis(timediff));

            etcommentText.setText(employeeLog.getText());
        }

    }

    private String getEmployeeLogText() {
        EditText editText = findViewById(R.id.text);
        String t = editText.getText().toString();
        if (t.length() > 0) {
            editText.setText(null);
            return t;
        } else return null;
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

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

    private void populateExpandableList() {
        expandableList = findViewById(R.id.navigationmenu);
        mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, expandableList);

        // setting list adapter
        expandableList.setAdapter(mMenuAdapter);

        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {

                if (listDataChild.get(listDataHeader.get(groupPosition)) != null) {
                    String subMenu = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                    actOnMenuClick(subMenu);
                }
                return false;
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                actOnMenuClick(listDataHeader.get(i).getMainMenu());
                return false;
            }
        });
        expandableList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expandableList.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

    }

    private void actOnMenuClick(String subMenu) {
        switch (subMenu) {
            case "Employee":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivityForResult(new Intent(MainActivity.this, ManageEmployee.class), 2);
                break;
            case "Day Report":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, Reports.class));
                break;
            case "Detailed Report":
                mDrawerLayout.closeDrawer(Gravity.START);
                startActivity(new Intent(MainActivity.this, ReportsDetailed.class));
                break;

            case "Settings":
                mDrawerLayout.closeDrawer(Gravity.START);
                break;
            case "Online Backup":
                mDrawerLayout.closeDrawer(Gravity.START);
                onlineBackup();
                break;
            case "Online Restore":
                mDrawerLayout.closeDrawer(Gravity.START);
                onlineRestore();
                break;
            case "Selective Restore":
                mDrawerLayout.closeDrawer(Gravity.START);
                selectiveRestore();
                break;
            case "Logout":
                mDrawerLayout.closeDrawer(Gravity.START);
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, Login.class));
                break;
        }
    }

    private void PrepareMenu() {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        listDataChild = new HashMap<ExpandedMenuModel, List<String>>();

        ExpandedMenuModel employee = new ExpandedMenuModel("Employee", R.drawable.ic_account_circle, false);
        // Adding data header
        listDataHeader.add(employee);

        ExpandedMenuModel report = new ExpandedMenuModel("Reports", R.drawable.ic_report, true);
        listDataHeader.add(report);
        List<String> reports = new ArrayList<String>();
        reports.add("Day Report");
        reports.add("Detailed Report");

        ExpandedMenuModel settings = new ExpandedMenuModel("Settings", R.drawable.ic_settings, false);
        listDataHeader.add(settings);

        ExpandedMenuModel backup = new ExpandedMenuModel("Backup", R.drawable.ic_backup, true);
        listDataHeader.add(backup);
        List<String> backupChildren = new ArrayList<String>();
        backupChildren.add("Online Backup");
        backupChildren.add("Online Restore");
        backupChildren.add("Selective Restore");

        ExpandedMenuModel logout = new ExpandedMenuModel("Logout", R.drawable.ic_logout, false);
        listDataHeader.add(logout);


        listDataChild.put(listDataHeader.get(1), reports);
        listDataChild.put(listDataHeader.get(3), backupChildren);

    }

    private void setupNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mNavigationView = findViewById(R.id.navigation_view);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               /* int id = item.getItemId();
                switch (id) {
                    case R.id.manageEmployee:
                        mDrawerLayout.closeDrawer(Gravity.START);
                        startActivityForResult(new Intent(MainActivity.this, ManageEmployee.class), 2);
                        break;
                    case R.id.Report:
                        mDrawerLayout.closeDrawer(Gravity.START);
                        startActivity(new Intent(MainActivity.this, Reports.class));
                        break;
                    case R.id.DetailedReport:
                        mDrawerLayout.closeDrawer(Gravity.START);
                        startActivity(new Intent(MainActivity.this, ReportsDetailed.class));
                        break;

                    case R.id.Settings:
                        mDrawerLayout.closeDrawer(Gravity.START);
                        break;
                    case R.id.Backup:
                        mDrawerLayout.closeDrawer(Gravity.START);
                        popupMenu();
                        break;
                    case R.id.Logout:
                        mDrawerLayout.closeDrawer(Gravity.START);
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, Login.class));
                        break;
                }*/
                return false;
            }
        });

    }

    private void onlineBackup() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            firebaseStorage = FirebaseStorage.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();
            final long filename;

            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setProgress(0);
            progressDialog.setTitle("Data Uploading");
            progressDialog.setCancelable(false);
            progressDialog.show();

            UploadTask uploadTask;

            String databaseFilePath = "/data/data/" + getPackageName() + "/databases/" + DATABASE_NAME;
            InputStream data = null;
            try {
                data = new FileInputStream(new File(databaseFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Uri file = Uri.fromFile(new File(databaseFilePath));
            filename = System.currentTimeMillis();
            mStorageReference = firebaseStorage.getReference().child("user/" + user.getUid()).child("Databases").child(filename + "");

            uploadTask = mStorageReference.putFile(file);
            // Listen for state changes, errors, and completion of the upload.
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    int currentProgress = (int) ((100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()));
                    progressDialog.setProgress(currentProgress);
                    System.out.println("Upload is " + progress + "% done");
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload is paused");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(getApplicationContext(), "Upload Failed" + exception, Toast.LENGTH_SHORT).show();

                    progressDialog.setCancelable(true);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    toast("Upload is Successful");
                    progressDialog.setCancelable(true);
                    progressDialog.hide();
                }
            });

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    System.out.println("URL : " + mStorageReference.getDownloadUrl());

                    // Continue with the task to get the download URL
                    return mStorageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference().child("users/" + user.getUid() + "/Databases");
                        mDatabaseReference.child(String.valueOf(filename)).setValue(downloadUri.toString());
                        System.out.println("URI :" + downloadUri.toString());
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

        } else
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);
    }

    private void onlineRestore() {
        System.out.println("Restore clicked");
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference().child("users/" + user.getUid() + "/Databases");
        Query query = mDatabaseReference.limitToLast(1);

        showProgressBar(true, "Getting recent backup file name");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showProgressBar(false);
                for (DataSnapshot d : dataSnapshot.getChildren()
                ) {
                    restoreFromByte(d.getKey().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgressBar(false);
                toast("Unable to get recent file name, try again ");
            }
        });
    }

    private void restoreFromByte(String filename) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            firebaseStorage = FirebaseStorage.getInstance();
            firebaseDatabase = FirebaseDatabase.getInstance();

            final String databaseFile = "/data/data/" + getPackageName() + "/databases/" + DATABASE_NAME;
            mStorageReference = firebaseStorage.getReference().child("user/" + user.getUid()).child("Databases").child(filename + "");

            final long ONE_MEGABYTE = 1024 * 1024;
            showProgressBar(true, "Downloading Backup File");
            mStorageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    showProgressBar(false);
                    System.out.println("Data Downlaod Success " + bytes.length);
                    File file = new File(databaseFile);
                    try {

                        OutputStream os = new FileOutputStream(file);
                        os.write(bytes);
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        toast("Unable to restore data to Appa" + e);
                    }
                    toast("Data Restored Successfully");
                    setEmployeeData();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    showProgressBar(false);
                    toast("Unable to download file online" + exception.getMessage());
                }
            });


        } else
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 9);

    }

    private void selectiveRestore() {
        final List<BackupFile> data = new ArrayList<>();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference().child("users/" + user.getUid() + "/Databases");
        Query query = mDatabaseReference.limitToLast(10);

        showProgressBar(true, "Getting recent backup file name");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showProgressBar(false);
                for (DataSnapshot d : dataSnapshot.getChildren()
                ) {
                    Long milli = Long.parseLong(d.getKey());
                    data.add(new BackupFile(d.getKey().toString(), UHelper.militoddmmyyyyhhmmss(milli)));
                }
                if (data.size() > 0)
                    selectiveRestoreFileList(data);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgressBar(false);
                toast("Unable to get recent file name, try again ");
            }
        });
    }

    public void selectiveRestoreFileList(final List<BackupFile> data) {
        final Dialog dialog = new Dialog(this);
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 300);
        dialog.setContentView(R.layout.recycler_popup_window);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.show();

        final BackupFile[] backupFile = new BackupFile[1];
        final RecyclerView recyclerView = dialog.findViewById(R.id.bf_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        FileRecyclerViewAdapter adapter = new FileRecyclerViewAdapter(data);
        recyclerView.setAdapter(adapter);

        Button restore = dialog.findViewById(R.id.restore);
        final TextView selectedFile = dialog.findViewById(R.id.selectedfile);

        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedFile.getText().length() > 0) {
                    dialog.dismiss();
                    restoreFromByte(selectedFile.getText().toString());
                }
            }
        });

        adapter.notifyDataSetChanged();
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                backupFile[0] = data.get(position);
                selectedFile.setText(backupFile[0].getFileName());
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    public void logout(long milisecond) {
        System.out.println("Before : " + milisecond);
        milisecond = UHelper.ymdhmTomili(UHelper.militoyyyymmddhm(milisecond));
        System.out.println("After : " + milisecond);

        Employee employee = (Employee) employeeSpinner.getSelectedItem();
        if (employee != null) {
            EmployeeLog employeeLog = dbh.getLastEmployeeLog(employee.getId());
            if (employeeLog == null || employee == null) {
                Toast.makeText(this, "Employee not logged in anytime", Toast.LENGTH_SHORT).show();
            } else if (employeeLog.getLoginTime() > 0 && employeeLog.getLogoutTime() == 0) {
                if (employeeLog.getLoginTime() < milisecond) {
                    employeeLog.setEid(employee.getId());
                    employeeLog.setLogoutTime(milisecond);
                    employeeLog.setTimeDiff(milisecond - employeeLog.getLoginTime());
                    if (getEmployeeLogText() != null)
                        employeeLog.setText(getEmployeeLogText());
                    System.out.println(dbh.updateEmployeeLog(employeeLog));
                } else {
                    Toast.makeText(this, "Incorrect Time", Toast.LENGTH_SHORT).show();

                }

            } else {
                Toast.makeText(this, "Employee already logged out", Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected void getEmployeesLogedIn() {
        String names = "";
        ArrayList<EmployeeLog> loggedInEmployee = dbh.geEmployeesLogedIn();

        for (EmployeeLog e : loggedInEmployee) {
            names = names + (dbh.getEmployeeNameById(e.getId()) + "\n");
        }
        loggedInEmployeeNames.setText(names);
    }

}
