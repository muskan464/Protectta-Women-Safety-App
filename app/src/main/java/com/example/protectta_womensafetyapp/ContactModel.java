package com.example.protectta_womensafetyapp;

public class ContactModel {
    public String contactName;
    public String contactNumber;

    // Firebase needs this no-arg constructor
    public ContactModel() { }

    public ContactModel(String contactName, String contactNumber) {
        this.contactName = contactName;
        this.contactNumber = contactNumber;
    }
}

