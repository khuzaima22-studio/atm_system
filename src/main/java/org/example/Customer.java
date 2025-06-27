package org.example;

public class Customer {
    private int id;
    private String name;
    private int pin;
    private String role;

    public Customer(int id, String name, String role, int pin) {
        this.id = id;
        this.name = name;
        this.pin = pin;
        this.role = role;
//        this.account = new Account(this);  // This will pass the customer object to Account Constructor
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


//    public Account getAccount() {
//        return
//    }
}
