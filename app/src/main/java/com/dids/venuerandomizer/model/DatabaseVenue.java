package com.dids.venuerandomizer.model;

public class DatabaseVenue {
    /**
     * This is used for Firebase User Data. Field conventions are not required
     */
    public String id;
    public String name;
    public String category;
    public String address;

    public DatabaseVenue() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public DatabaseVenue(String id, String name, String category, String address) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.address = address;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public String getAddress() {
        return this.address;
    }
}
