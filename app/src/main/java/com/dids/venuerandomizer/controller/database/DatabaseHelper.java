package com.dids.venuerandomizer.controller.database;

import com.dids.venuerandomizer.model.DatabaseVenue;
import com.dids.venuerandomizer.model.UserData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class DatabaseHelper {
    private static final String CHILD_EMAIL = "email";
    private static final String CHILD_ID = "id";
    private static final String CHILD_FAVORITES = "favorites";
    private static final String CHILD_USERS = "users";
    private static final String DATABASE_REFERENCE = "findmeaplace";
    private static DatabaseHelper mSingleton;

    private final DatabaseReference mDatabase;

    private DatabaseHelper() {
        mDatabase = FirebaseDatabase.getInstance().getReference(DATABASE_REFERENCE);
    }

    public static synchronized DatabaseHelper getInstance() {
        if (mSingleton == null) {
            mSingleton = new DatabaseHelper();
        }
        return mSingleton;
    }

    public Query createUserQuery(String email) {
        return mDatabase.child(CHILD_USERS).orderByChild(CHILD_EMAIL).equalTo(email);
    }

    public void addUser(String uid, UserData userData) {
        mDatabase.child(CHILD_USERS).child(uid).setValue(userData);
    }

    public Query createFavoriteQuery(String uid, String id) {
        return mDatabase.child(CHILD_USERS).child(uid).child(CHILD_FAVORITES).
                orderByChild("id").equalTo(id);
    }

    public void addFavorite(String uid, DatabaseVenue venue) {
        DatabaseReference ref = mDatabase.child(CHILD_USERS).child(uid).child(CHILD_FAVORITES).push();
        ref.setValue(venue);
    }
}
