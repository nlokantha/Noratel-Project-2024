package com.example.noratelproject2024;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.noratelproject2024.Fragments.HomeFragment;
import com.example.noratelproject2024.Fragments.LoginFragment;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener, HomeFragment.HomeFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView, new LoginFragment())
                .commit();
    }
    @Override
    public void authSuccessful() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new HomeFragment())
                .commit();
    }

    @Override
    public void logout() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new LoginFragment())
                .commit();
    }
}