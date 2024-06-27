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
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    private OkHttpClient client;

    String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        binding.editTextUserName.setText("test1");
        binding.editTextPassword.setText("test1");
        binding.editTextUserName.requestFocus();

        Date dates = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        date = formatter.format(dates);


        binding.textViewVersion.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getActivity(), "C3 Labs -LN", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

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
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                new Methods().saveToTextFile(getActivity(),"--------------------------------");
                new Methods().saveToTextFile(getActivity(),date);
                new Methods().saveToTextFile(getActivity(),"Login");
                new Methods().saveToTextFile(getActivity(),request.toString());
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        new Methods().saveToTextFile(getActivity(),e.getMessage());
                        new Methods().saveToTextFile(getActivity(),e.toString());
                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {

                            String body = response.body().string();
                            Log.d(TAG, "onResponse: login Response "+body);
                            new Methods().saveToTextFile(getActivity(),body);
                            new Methods().saveToTextFile(getActivity(),String.valueOf(response.code()));
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
                            new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
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

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    new Methods().saveToTextFile(getActivity(),"--------------------------------");
                    new Methods().saveToTextFile(getActivity(),date);
                    new Methods().saveToTextFile(getActivity(),"Login");
                    new Methods().saveToTextFile(getActivity(),request.toString());

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                            new Methods().saveToTextFile(getActivity(),e.getMessage());
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String body = response.body().string();
                                Log.d(TAG, "onResponse: " + body);
                                Gson gson = new Gson();
                                User user = gson.fromJson(body,User.class);
                                new Methods().saveToTextFile(getActivity(),body);
                                new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
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
                                new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
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