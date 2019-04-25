package com.appdev.jayesh.ams;

public class Employee {

    private int id;
    private String employeeName;
    private double wage;


    public Employee(int id, String employeeName, double wage) {
        this.id = id;
        this.employeeName = employeeName;
        this.wage = wage;
    }

    public Employee(String employeeName, double wage) {
        this.employeeName = employeeName;
        this.wage = wage;
    }

    public Employee(int id, String employeeName) {
        this.employeeName = employeeName;
        this.id = id;
    }
    public Employee(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public double getWage() {
        return wage;
    }

    public void setWage(double wage) {
        this.wage = wage;
    }

    //to display object as a string in spinner
    @Override
    public String toString() {
        return employeeName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Employee) {
            Employee c = (Employee) obj;
            if (c.getEmployeeName().equals(employeeName) && c.getId() == id) return true;
        }

        return false;
    }
}
