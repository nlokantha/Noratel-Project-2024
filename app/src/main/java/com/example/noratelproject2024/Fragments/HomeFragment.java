package com.example.noratelproject2024.Fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.noratelproject2024.Adapters.SelectLineAdapter;
import com.example.noratelproject2024.Adapters.SelectShiftAdapter;
import com.example.noratelproject2024.Models.Lines;
import com.example.noratelproject2024.Models.Shift;
import com.example.noratelproject2024.R;
import com.example.noratelproject2024.References;
import com.example.noratelproject2024.databinding.FragmentHomeBinding;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment implements SelectShiftAdapter.OnShiftSelectedListener, SelectLineAdapter.OnLineSelectedListener {
    public HomeFragment() {
        // Required empty public constructor
    }
    FragmentHomeBinding binding;
    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "demo";
    ArrayList<Lines> linesArrayList = new ArrayList<>();
    ArrayList<Shift> shiftArrayList = new ArrayList<>();
    SelectLineAdapter selectLineAdapter;
    SelectShiftAdapter selectShiftAdapter;
    AlertDialog alertselectLine,alertselectShift;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dateAndTime();
        String line = binding.textViewLine.getText().toString().trim();
        String shift = binding.textViewShift.getText().toString().trim();

        binding.imageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsCustomDialog();
            }
        });
        binding.buttonDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePick();
            }
        });

        if (line.equals("N/A") && shift.equals("N/A")){
            binding.buttonJobCard.setActivated(false);
        }else {
            binding.buttonJobCard.setActivated(true);
        }
        binding.buttonJobCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Hi", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void dateAndTime() {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    long date = System.currentTimeMillis();
                                    SimpleDateFormat time = new SimpleDateFormat("hh-mm-ss a");
                                    SimpleDateFormat d = new SimpleDateFormat("MM-dd-yyy");
                                    String timeString = time.format(date);
                                    String dateString = d.format(date);

                                    binding.textViewDate.setText(dateString);
                                    binding.textViewTime.setText(timeString);
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        t.start();
    }
    private void settingsCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_setting_dialog, null);
        builder.setView(view);

        AlertDialog alert = builder.create();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.show();

        Button buttonLogout = view.findViewById(R.id.buttonLogout);
        Button buttonChangeLine = view.findViewById(R.id.buttonChangeLine);
        Button buttonChangeShift = view.findViewById(R.id.buttonChangeShift);

        buttonChangeShift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectShiftCustomDialog();
                alert.dismiss();
            }
        });
        buttonChangeLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectLineCustomDialog();
                alert.dismiss();
            }
        });
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.logout();
                alert.dismiss();
            }
        });

    }
    private void selectLineCustomDialog(){
        getLines();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_line,null);
        builder.setView(view);

        alertselectLine = builder.create();
        alertselectLine.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectLine.show();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectLineAdapter = new SelectLineAdapter(linesArrayList,this);
        recyclerView.setAdapter(selectLineAdapter);
    }
    private void selectShiftCustomDialog(){
        getShift();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_shift,null);
        builder.setView(view);

        alertselectShift = builder.create();
        alertselectShift.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectShift.show();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectShiftAdapter = new SelectShiftAdapter(shiftArrayList,this);
        recyclerView.setAdapter(selectShiftAdapter);
    }

    private void getLines() {
        String url = References.GetLines.methodName;
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
                    Gson gson = new Gson();
                    Lines[] lines = gson.fromJson(body,Lines[].class);
                    linesArrayList.addAll(Arrays.asList(lines));

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            selectLineAdapter.notifyDataSetChanged();
                        }
                    });
                } else {

                }
            }
        });
    }

    private void getShift() {
        String url = References.GetShift.methodName;
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
                    Gson gson = new Gson();
                    Shift[] shifts = gson.fromJson(body,Shift[].class);
                    shiftArrayList.addAll(Arrays.asList(shifts));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            selectShiftAdapter.notifyDataSetChanged();
                        }
                    });
                } else {

                }
            }
        });
    }

    private void getJobCard(){
        String url = References.GetShift.methodName;
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
                if (response.isSuccessful()){
                    String body = response.body().string();

                }else {

                }
            }
        });
    }

    private void datePick(){
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = year+"-"+month+"-"+dayOfMonth;
                binding.buttonDatePicker.setText(date);
            }
        },YEAR,MONTH,DATE);
        datePickerDialog.show();

    }

    HomeFragmentListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (HomeFragmentListener) context;
    }

    @Override
    public void onShiftSelected(Shift selectedShift) {
        binding.textViewShift.setText(selectedShift.getRoster());
        alertselectShift.dismiss();
    }

    @Override
    public void onLineSelected(Lines selectedLine) {
        binding.textViewLine.setText(selectedLine.getSUB_UNINAME());
        alertselectLine.dismiss();

    }

    public interface HomeFragmentListener {
        void logout();

    }
}