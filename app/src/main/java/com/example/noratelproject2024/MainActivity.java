package com.example.noratelproject2024;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.noratelproject2024.Fragments.HomeFragment;
import com.example.noratelproject2024.Fragments.LoginFragment;
import com.example.noratelproject2024.Models.User;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginFragmentListener, HomeFragment.HomeFragmentListener {
    User mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.contains("auth")){

            String authStr = sharedPref.getString("auth",null);
            if (authStr == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.rootView, new LoginFragment())
                        .commit();
            }else {
                Gson gson = new Gson();
                mUser = gson.fromJson(authStr, User.class);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.rootView,HomeFragment.newInstance(mUser))
                        .commit();
            }
        }else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rootView, new LoginFragment())
                    .commit();
        }


    }
    @Override
    public void authSuccessful(User user) {

        this.mUser = user;

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        editor.putString("auth",gson.toJson(user));
        editor.apply();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,HomeFragment.newInstance(user))
                .commit();

    }

    @Override
    public void logout() {
        mUser = null;
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("auth");
        editor.apply();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.rootView,new LoginFragment())
                .commit();
    }
}