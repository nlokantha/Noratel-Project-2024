package com.example.noratelproject2024.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.noratelproject2024.Controls.Methods;
import com.example.noratelproject2024.Models.User;
import com.example.noratelproject2024.References;
import com.example.noratelproject2024.databinding.FragmentLoginBinding;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Time;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }
    FragmentLoginBinding binding;
    private static final String TAG = "demo";
    private final OkHttpClient client = new OkHttpClient();
    String date = String.valueOf(new java.util.Date());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.editTextUserName.setText("test");
        binding.editTextPassword.setText("test123");
        binding.editTextUserName.requestFocus();

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = binding.editTextUserName.getText().toString();
                String password = binding.editTextPassword.getText().toString();
                String userKey = "";

                String url = References.Login.methodName +
                        "?userID=" + userName +
                        "&password=" + password +
                        "&userKey=" + userKey;
                new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/login.txt");
                new Methods().saveToTextFile(getActivity(),date + "\n", "/login.txt");
                new Methods().saveToTextFile(getActivity(),url + "\n", "/login.txt");

                Request request = new Request.Builder()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/login.txt");
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String body = response.body().string();
                            Log.d(TAG, "onResponse: login Response "+body);
                            new Methods().saveToTextFile(getActivity(),body + "\n", "/login.txt");
                            new Methods().saveToTextFile(getActivity(),String.valueOf(response.code()) + "\n", "/login.txt");
                            Gson gson = new Gson();
                            User user = gson.fromJson(body,User.class);
                            if (user.getStatus().equals("1")){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Please Check Your Username or Password ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mListener.authSuccessful(user);
                                    }
                                });

                            }
                        } else {
                            Log.d(TAG, "onResponse: " + response.code());
                            new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/login.txt");
                        }
                    }
                });
            }
        });
        binding.editTextUserName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_TAB && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String userName = "";
                    String password = "";
                    String userKey = binding.editTextUserName.getText().toString();

                    String url = References.Login.methodName +
                            "?userID=" + userName +
                            "&password=" + password +
                            "&userKey=" + userKey;
                    new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/login.txt");
                    new Methods().saveToTextFile(getActivity(),date + "\n", "/login.txt");
                    new Methods().saveToTextFile(getActivity(),url + "\n", "/login.txt");

                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                            new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/login.txt");
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String body = response.body().string();
                                Log.d(TAG, "onResponse: " + body);
                                Gson gson = new Gson();
                                User user = gson.fromJson(body,User.class);
                                new Methods().saveToTextFile(getActivity(),body + "\n", "/login.txt");
                                new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/login.txt");
                                if (user.getStatus().equals("1")){
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "Please Check Your QR Code", Toast.LENGTH_SHORT).show();
                                            binding.editTextUserName.setText("");
                                            binding.editTextUserName.requestFocus();
                                        }
                                    });
                                }else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mListener.authSuccessful(user);
                                        }
                                    });
                                }

                            } else {
                                Log.d(TAG, "onResponse: " + response.code());
                                new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/login.txt");
                            }
                        }
                    });

                }
                return false;
            }
        });
    }


    LoginFragmentListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (LoginFragmentListener) context;
    }

    public interface LoginFragmentListener {
        void authSuccessful(User user);
    }


}