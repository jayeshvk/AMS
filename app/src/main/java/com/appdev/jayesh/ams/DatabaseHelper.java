package com.appdev.jayesh.ams;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    static final String DATABASE_NAME = "AMSDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_EMPLOYEE = "employee";
    private static final String TABLE_EMPLOYEELOG = "employeelog";

    // TABLE_EMPLOYEE Table Columns
    private static final String KEY_EMPLOYEE_ID = "id";
    private static final String KEY_EMPLOYEE_NAME = "name";
    private static final String KEY_EMPLOYEE_WAGE = "wage";

    // TABLE_EMPLOYEELOG  Table Columns
    private static final String KEY_EMPLOYEELOG_ID = "id";
    private static final String KEY_EMPLOYEELOG_EID = "empid";
    private static final String KEY_EMPLOYEELOG_CREATEDATE = "createdate";
    private static final String KEY_EMPLOYEELOG_LOGINTIME = "logintime";
    private static final String KEY_EMPLOYEELOG_LOGOUTTIME = "logouttime";
    private static final String KEY_EMPLOYEELOG_TIMEDIFF = "timediff";
    private static final String KEY_EMPLOYEELOG_LOGINDATE = "logindate";
    private static final String KEY_EMPLOYEELOG_TEXT = "text";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign in support, write-ahead logging, etc.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EMPLOYEE_TABLE = "CREATE TABLE " + TABLE_EMPLOYEE +
                "(" +
                KEY_EMPLOYEE_ID + " INTEGER PRIMARY KEY," + // Define a primary in
                KEY_EMPLOYEE_NAME + " TEXT," +
                KEY_EMPLOYEE_WAGE + " REAL NOT NULL DEFAULT 0" +
                ")";

        String CREATE_EMPLOYEELOG_TABLE = "CREATE TABLE " + TABLE_EMPLOYEELOG +
                "(" +
                KEY_EMPLOYEELOG_ID + " INTEGER PRIMARY KEY," +
                KEY_EMPLOYEELOG_EID + " INTEGER," +
                KEY_EMPLOYEELOG_CREATEDATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                KEY_EMPLOYEELOG_LOGINTIME + " INTEGER DEFAULT 0," +
                KEY_EMPLOYEELOG_LOGOUTTIME + " INTEGER DEFAULT 0," +
                KEY_EMPLOYEELOG_TIMEDIFF + " INTEGER DEFAULT 0," +
                KEY_EMPLOYEELOG_LOGINDATE + " DATETIME," +
                KEY_EMPLOYEELOG_TEXT + " TEXT," +
                "FOREIGN KEY(" + KEY_EMPLOYEELOG_EID + ") REFERENCES " + TABLE_EMPLOYEE + "(" + KEY_EMPLOYEE_ID + ")" +
                ")";


        db.execSQL(CREATE_EMPLOYEE_TABLE);
        db.execSQL(CREATE_EMPLOYEELOG_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEELOG);
        }
    }

    long addEmployee(Employee employee) {
        long returnid = 0;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_EMPLOYEE_NAME, employee.getEmployeeName());
            values.put(KEY_EMPLOYEE_WAGE, employee.getWage());

            returnid = db.insertOrThrow(TABLE_EMPLOYEE, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Employee to database");
        } finally {
            db.endTransaction();
            Log.d(TAG, "Record Added");
        }
        return returnid;
    }

    ArrayList<Employee> getAllEmployee() {
        ArrayList<Employee> employees = new ArrayList<>();
        String EMPLOYEES_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_EMPLOYEE
                );

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EMPLOYEES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Employee newEmployee = new Employee();
                    newEmployee.setEmployeeName(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEE_NAME)));
                    newEmployee.setWage(cursor.getDouble(cursor.getColumnIndex(KEY_EMPLOYEE_WAGE)));
                    newEmployee.setId(cursor.getInt(cursor.getColumnIndex(KEY_EMPLOYEE_ID)));
                    employees.add(newEmployee);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Employees from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return employees;
    }

    int updateEmployee(Employee employee) {
        SQLiteDatabase db = this.getWritableDatabase();
        Integer retID = 0;
        ContentValues values = new ContentValues();
        values.put(KEY_EMPLOYEE_NAME, employee.getEmployeeName());
        values.put(KEY_EMPLOYEE_WAGE, employee.getWage());
        try {
            retID = db.update(TABLE_EMPLOYEE, values, KEY_EMPLOYEE_ID + " = ?",
                    new String[]{String.valueOf(employee.getId())});
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error while trying to update Employees from database");

        }
        return retID;
    }

    int deleteEmployee(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Integer retID = 0;
        try {

            retID = db.delete(TABLE_EMPLOYEE, KEY_EMPLOYEE_ID + " = ?",
                    new String[]{String.valueOf(id)});

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error while trying to delete Employees from database " + e);
        }
        return retID;
    }

    long addEmployeeLog(EmployeeLog employeeLog) {
        long result = 0;
        // Create and/or open the database for writing\=
        SQLiteDatabase db = getWritableDatabase();

        // consistency of the database.
        db.beginTransaction();
        try {

            ContentValues values = new ContentValues();
            values.put(KEY_EMPLOYEELOG_EID, employeeLog.getEid());
            values.put(KEY_EMPLOYEELOG_LOGINTIME, employeeLog.getLoginTime());
            values.put(KEY_EMPLOYEELOG_LOGINDATE, employeeLog.getLoginDate());
            values.put(KEY_EMPLOYEELOG_TEXT, employeeLog.getText());

            // Notice how we haven't specified the primary in. SQLite auto increments the primary in column.
            result = db.insertWithOnConflict(TABLE_EMPLOYEELOG, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add Employee Log to database" + e);
        } finally {
            db.endTransaction();
            Log.d(TAG, "Sales Record Added");
        }
        return result;
    }

    EmployeeLog getLastEmployeeLog(int eid) {
        EmployeeLog employeeLog = new EmployeeLog();
        String EMPLOYEELOG_SELECT_QUERY =
                String.format("SELECT * FROM %s  where %s = %s ORDER BY %s DESC LIMIT 1",
                        TABLE_EMPLOYEELOG, KEY_EMPLOYEELOG_EID, eid, KEY_EMPLOYEELOG_ID
                );

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EMPLOYEELOG_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    EmployeeLog el = new EmployeeLog();
                    el.setCreateDate(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEELOG_CREATEDATE)));
                    el.setText(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEELOG_TEXT)));
                    el.setLoginTime(cursor.getLong(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGINTIME)));
                    el.setLogoutTime(cursor.getLong(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGOUTTIME)));
                    el.setId(cursor.getInt(cursor.getColumnIndex(KEY_EMPLOYEELOG_ID)));
                    employeeLog = el;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get Employees from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return employeeLog;
    }

    long totalHours(int eid, String fromDate, String toDate) {
        long totalHours = 0;
        String EMPLOYEELOG_SELECT_QUERY =
                String.format("SELECT sum(%s) as %s FROM %s  where %s = %s and %s between Datetime('%s') and Datetime('%s')",
                        KEY_EMPLOYEELOG_TIMEDIFF, KEY_EMPLOYEELOG_TIMEDIFF, TABLE_EMPLOYEELOG, KEY_EMPLOYEELOG_EID, eid, KEY_EMPLOYEELOG_LOGINDATE, fromDate, toDate
                );
        System.out.println(EMPLOYEELOG_SELECT_QUERY);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EMPLOYEELOG_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    totalHours = cursor.getInt(cursor.getColumnIndex(KEY_EMPLOYEELOG_TIMEDIFF));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get total hours from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return totalHours;
    }

    ArrayList<EmployeeLog> getDetailedEmployeeLogList(int eid, String fromDate, String toDate) {
        ArrayList<EmployeeLog> employeeLogs = new ArrayList<>();
        String EMPLOYEELOG_SELECT_QUERY =
                String.format("SELECT * FROM %s  where %s = %s and %s between Datetime('%s') and Datetime('%s') order by %s",
                        TABLE_EMPLOYEELOG, KEY_EMPLOYEELOG_EID, eid, KEY_EMPLOYEELOG_LOGINDATE, fromDate, toDate, KEY_EMPLOYEELOG_LOGINDATE);
        System.out.println(EMPLOYEELOG_SELECT_QUERY);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EMPLOYEELOG_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    EmployeeLog empl = new EmployeeLog();
                    empl.setLoginTime(cursor.getLong(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGINTIME)));
                    empl.setId(cursor.getInt(cursor.getColumnIndex(KEY_EMPLOYEELOG_ID)));
                    empl.setLogoutTime(cursor.getLong(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGOUTTIME)));
                    empl.setTimeDiff(cursor.getLong(cursor.getColumnIndex(KEY_EMPLOYEELOG_TIMEDIFF)));
                    empl.setText(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEELOG_TEXT)));
                    empl.setLoginDate(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGINDATE)));
                    employeeLogs.add(empl);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get data from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return employeeLogs;
    }

    ArrayList<EmployeeReport> employeeReport(int eid, String fromDate, String toDate) {
        ArrayList<EmployeeReport> employeeReports = new ArrayList<>();
        String EMPLOYEELOG_SELECT_QUERY =
                String.format("SELECT %s, sum(%s) as %s FROM %s  where %s = %s and %s between Datetime('%s') and Datetime('%s') group by %s",
                        KEY_EMPLOYEELOG_LOGINDATE, KEY_EMPLOYEELOG_TIMEDIFF, KEY_EMPLOYEELOG_TIMEDIFF, TABLE_EMPLOYEELOG, KEY_EMPLOYEELOG_EID, eid, KEY_EMPLOYEELOG_LOGINDATE, fromDate, toDate, KEY_EMPLOYEELOG_LOGINDATE);
        System.out.println(EMPLOYEELOG_SELECT_QUERY);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EMPLOYEELOG_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    EmployeeReport empr = new EmployeeReport();
                    empr.setDate(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGINDATE)));
                    empr.setHours(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEELOG_TIMEDIFF)));
                    employeeReports.add(empr);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get days from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return employeeReports;
    }

    EmployeeLog getEmployeeLogById(int id) {
        EmployeeLog employeeLog = new EmployeeLog();
        String EMPLOYEELOG_SELECT_QUERY =
                String.format("SELECT * FROM %s where %s = %s",
                        TABLE_EMPLOYEELOG, KEY_EMPLOYEELOG_ID, id);
        System.out.println(EMPLOYEELOG_SELECT_QUERY);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(EMPLOYEELOG_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    EmployeeLog empl = new EmployeeLog();
                    empl.setLoginTime(cursor.getLong(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGINTIME)));
                    empl.setId(cursor.getInt(cursor.getColumnIndex(KEY_EMPLOYEELOG_ID)));
                    empl.setLogoutTime(cursor.getLong(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGOUTTIME)));
                    empl.setTimeDiff(cursor.getLong(cursor.getColumnIndex(KEY_EMPLOYEELOG_TIMEDIFF)));
                    empl.setText(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEELOG_TEXT)));
                    empl.setLoginDate(cursor.getString(cursor.getColumnIndex(KEY_EMPLOYEELOG_LOGINDATE)));
                    empl.setEid(cursor.getInt(cursor.getColumnIndex(KEY_EMPLOYEELOG_EID)));
                    employeeLog = empl;
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get data from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return employeeLog;
    }

    // Delete Customer
    int deleteEmployeeLogbyId(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;
        try {

            retID = db.delete(TABLE_EMPLOYEELOG, KEY_EMPLOYEELOG_ID + " = ?",
                    new String[]{String.valueOf(id)});

        } catch (Exception e) {
            e.printStackTrace();
        }

        return retID;
    }

    int updateEmployeeLog(EmployeeLog emp) {
        SQLiteDatabase db = this.getWritableDatabase();
        int retID = 0;

        ContentValues values = new ContentValues();
        if (emp.getText() != null)
            values.put(KEY_EMPLOYEELOG_TEXT, emp.getText());
        values.put(KEY_EMPLOYEELOG_LOGINTIME, emp.getLoginTime());
        values.put(KEY_EMPLOYEELOG_LOGOUTTIME, emp.getLogoutTime());
        if (emp.getLoginDate() != null)
            values.put(KEY_EMPLOYEELOG_LOGINDATE, emp.getLoginDate());
        values.put(KEY_EMPLOYEELOG_TIMEDIFF, emp.getTimeDiff());

        try {
            retID = db.update(TABLE_EMPLOYEELOG, values, KEY_EMPLOYEELOG_ID + " = ?",
                    new String[]{String.valueOf(emp.getId())});

        } catch (Exception e) {
            e.printStackTrace();
        }


        return retID;
    }
}
