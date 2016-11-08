package com.dids.findmeaplace.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.dids.findmeaplace.R;
import com.dids.findmeaplace.controller.PauseableHandler;
import com.dids.findmeaplace.controller.utility.Utilities;
import com.dids.findmeaplace.view.base.BaseActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends BaseActivity implements FacebookCallback<LoginResult>,
        GraphRequest.GraphJSONObjectCallback, FirebaseAuth.AuthStateListener,
        PauseableHandler.PauseableHandlerCallback {
    private static final String TAG = "LoginActivity";
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private ViewSwitcher mSwitcher;
    private PauseableHandler mHandler;
    private AuthCredential mCredentials;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.VenueDetailTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwitcher.showNext();
            }
        });
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.registerCallback(mCallbackManager, this);

        mHandler = new PauseableHandler(this);
        mAuth = FirebaseAuth.getInstance();
        mSwitcher = (ViewSwitcher) findViewById(R.id.switcher);
        if (isLoggedIn()) {
            mSwitcher.showNext();
            handleFacebookAccessToken(AccessToken.getCurrentAccessToken());
        }

        ImageView view = (ImageView) findViewById(R.id.background);
        view.setImageDrawable(Utilities.getDrawableFromAsset(this, "bg.jpg"));
    }

    private boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            Log.d(TAG, "User is logged in");
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.pause();
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        Log.d(TAG, "success");
        handleFacebookAccessToken(loginResult.getAccessToken());
    }

    @Override
    public void onCancel() {
        Log.d(TAG, "cancel");
        mSwitcher.showPrevious();
    }

    @Override
    public void onError(FacebookException error) {
        Log.d(TAG, "error");
        mSwitcher.showPrevious();
        showNetworkError();
    }

    private void showNetworkError() {
        SuperActivityToast.cancelAllSuperToasts();
        SuperActivityToast.create(this, new Style(), Style.TYPE_STANDARD)
                .setProgressBarColor(Color.WHITE)
                .setText(getString(R.string.random_no_internet))
                .setDuration(Style.DURATION_LONG)
                .setFrame(Style.FRAME_LOLLIPOP)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setAnimations(Style.ANIMATIONS_POP).show();
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

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        mCredentials = FacebookAuthProvider.getCredential(token.getToken());
        Message newMessage = Message.obtain(mHandler, 0);
        mHandler.sendMessage(newMessage);
    }

    @Override
    public boolean storeMessage(Message message) {
        return true;
    }

    @Override
    public void processMessage(Message message) {
        mAuth.signInWithCredential(mCredentials)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "task failed");
                            LoginManager.getInstance().logOut();
                            mSwitcher.showPrevious();
                            showNetworkError();
                            return;
                        }
                        Log.d(TAG, "starting main activity");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(MainActivity.SKIP_LOGIN, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
    }
}