package com.dids.venuerandomizer.model;

@SuppressWarnings("WeakerAccess")
public class DatabaseVenue {
    /**
     * This is used for Firebase User Data. Field conventions are not required
     */
    public String id;
    public String name;
    public String category;
    public String address;
    public String telephone;
    public int variant;

    public DatabaseVenue() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public DatabaseVenue(String id, String name, String category, String address, String telephone,
                         int variant) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.address = address;
        this.telephone = telephone;
        this.variant = variant;
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

    public String getTelephone() {
        return this.telephone;
    }

    public int getVariant() {
        return this.variant;
    }
}
