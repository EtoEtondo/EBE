package com.example.myapplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

//User can be setuped on login
@IgnoreExtraProperties
public class User {
    public String username;
    public String userID;
    public Boolean state; //false = no incident, true = incident that need to be reported
    public String dataURL;

    public User() { //default test user cause of ai
        this.username = "ethem";
        this.userID = "0000";
        this.state = false;
        this.dataURL = "";
    }

    public User(String username, String userID) {
        this.username = username;
        this.userID = userID;
        this.state = false;
        this.dataURL = "";
    }

    public void writeUser() {
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance("https://schoolio2-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Lobby");
        mDatabase.child("Students").child(this.userID).child("user").setValue(this.username); //key:ID, value:Name -- any new ID creates a new user in the DB
        mDatabase.child("Students").child(this.userID).child("incident").setValue(this.state);
        mDatabase.child("Students").child(this.userID).child("dataURL").setValue(this.dataURL);
    }

    public void reportIncident() {
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance("https://schoolio2-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Lobby");
        mDatabase.child("Students").child(this.userID).child("incident").setValue(this.state);
    }

    public void setDataURL(String url) {
        this.dataURL = url;
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance("https://schoolio2-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Lobby");
        mDatabase.child("Students").child(this.userID).child("dataURL").setValue(this.dataURL);
    }
}
