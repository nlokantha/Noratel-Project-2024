package com.example.noratelproject2024;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.noratelproject2024.Fragments.HomeFragment;
import com.example.noratelproject2024.Fragments.LoginFragment;
import com.example.noratelproject2024.Models.User;

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
    public void authSuccessful(User user) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,HomeFragment.newInstance(user))
                .commit();
    }

    @Override
    public void logout() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new LoginFragment())
                .commit();
    }
}