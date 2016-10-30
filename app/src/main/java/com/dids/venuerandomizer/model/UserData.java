package com.dids.venuerandomizer.model;

public class UserData {
    /** This is used for Firebase User Data. Field conventions are not required */
    public String username;
    public String email;

    public UserData() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public UserData(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
