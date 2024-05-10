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

import com.example.noratelproject2024.References;
import com.example.noratelproject2024.databinding.FragmentLoginBinding;

import java.io.IOException;

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
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String body = response.body().string();
                            Log.d(TAG, "onResponse: " + body);
                            if (body.equals("1")){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity(), "Please Check Your Username or Password ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }else {
                                mListener.authSuccessful();
                            }
                        } else {
                            Log.d(TAG, "onResponse: " + response.code());
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
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String body = response.body().string();
                                Log.d(TAG, "onResponse: " + body);
                                if (body.equals("1")){
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "Please Check Your QR Code", Toast.LENGTH_SHORT).show();
                                            binding.editTextUserName.setText("");
                                            binding.editTextUserName.requestFocus();
                                        }
                                    });
                                }else {
                                    mListener.authSuccessful();
                                }

                            } else {
                                Log.d(TAG, "onResponse: " + response.code());
                            }
                        }
                    });

                }
                return false;
            }
        });
        binding.buttonTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Check C3 Home Page ! ", Toast.LENGTH_SHORT).show();
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
        void authSuccessful();


    }


}