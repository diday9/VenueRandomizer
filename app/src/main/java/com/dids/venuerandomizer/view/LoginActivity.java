package com.dids.venuerandomizer.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dids.venuerandomizer.R;
import com.dids.venuerandomizer.view.base.BaseActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends BaseActivity implements FacebookCallback<LoginResult>,
        GraphRequest.GraphJSONObjectCallback, FirebaseAuth.AuthStateListener {
    private static final String TAG = "LoginActivity";
    private CallbackManager mCallbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.registerCallback(mCallbackManager, this);
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCancel() {
    }

    @Override
    public void onError(FacebookException error) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCompleted(JSONObject object, GraphResponse response) {
        try {
            Log.d(TAG, "name: " + object.getString("name"));
            Log.d(TAG, "mail: " + object.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
    }

}
