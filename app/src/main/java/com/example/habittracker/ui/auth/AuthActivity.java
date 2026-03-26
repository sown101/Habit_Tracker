package com.example.habittracker.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habittracker.R;
import com.example.habittracker.ui.main.MainActivity;
import com.example.habittracker.utils.SessionManager;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.util.Log.d("AUTH_FLOW", "AuthActivity onCreate");

        if (SessionManager.isLoggedIn(this)) {
            android.util.Log.d("AUTH_FLOW", "User already logged in -> open MainActivity");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.authFragmentContainer, new LoginFragment())
                    .commit();
        }
    }

    public void openRegisterFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.authFragmentContainer, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }

    public void openLoginFragment() {
        getSupportFragmentManager().popBackStack();
    }
}
