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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noratelproject2024.Adapters.SelectJobCardAdapter;
import com.example.noratelproject2024.Adapters.SelectLineAdapter;
import com.example.noratelproject2024.Adapters.SelectShiftAdapter;
import com.example.noratelproject2024.Models.JobCard;
import com.example.noratelproject2024.Models.JobCardDetails;
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

public class HomeFragment extends Fragment implements SelectShiftAdapter.OnShiftSelectedListener, SelectLineAdapter.OnLineSelectedListener, SelectJobCardAdapter.OnJobCardSelectedListener {
    public HomeFragment() {
        // Required empty public constructor
    }
    FragmentHomeBinding binding;
    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "demo";
    ArrayList<Lines> linesArrayList = new ArrayList<>();
    ArrayList<Shift> shiftArrayList = new ArrayList<>();
    ArrayList<JobCard> jobCardArrayList = new ArrayList<>();
    SelectLineAdapter selectLineAdapter;
    SelectShiftAdapter selectShiftAdapter;
    SelectJobCardAdapter selectJobCardAdapter;
    AlertDialog alertselectLine,alertselectShift,alertselectJobCard;

    Shift mShift;
    Lines mLines;
    JobCard mJobCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dateAndTime();
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
        binding.buttonJobCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String line = binding.textViewLine.getText().toString().trim();
                String shift = binding.textViewShift.getText().toString().trim();
                if (line.equals("N/A")){
                    Toast.makeText(getActivity(), "Please Select Line", Toast.LENGTH_SHORT).show();
                } else if (shift.equals("N/A")) {
                    Toast.makeText(getActivity(), "Please Select Shift", Toast.LENGTH_SHORT).show();
                }else {
                    selectJobCardCustomDialog();
                }
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
        TextView textViewSelectedLine = view.findViewById(R.id.textViewSelectedLine);
        TextView textViewSelectedShift = view.findViewById(R.id.textViewSelectedShift);

        if (mLines != null){
            textViewSelectedLine.setText(mLines.getSUB_UNINAME());
        }else {
            textViewSelectedLine.setText("N/A");
        }

        if (mShift != null){
            textViewSelectedShift.setText(mShift.getRoster());
        }else {
            textViewSelectedShift.setText("N/A");
        }

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

    private void selectJobCardCustomDialog(){
        getJobCard();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_search_jobcard,null);
        builder.setView(view);

        alertselectJobCard = builder.create();
        alertselectJobCard.getWindow().setGravity(Gravity.BOTTOM);
        alertselectJobCard.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        alertselectJobCard.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectJobCard.show();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectJobCardAdapter = new SelectJobCardAdapter(jobCardArrayList,this);
        recyclerView.setAdapter(selectJobCardAdapter);
    }

    private void getLines() {
        linesArrayList.clear();
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
        shiftArrayList.clear();
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
        jobCardArrayList.clear();
        String date = binding.buttonDatePicker.getText().toString().trim();
        if (mLines != null && mShift != null){
            String url = References.GetJobCard.methodName+
                    "?date="+date +
                    "&line="+mLines.getSUB_UNICODE() +
                    "&shift="+mShift.getRoster();

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
                        Log.d(TAG, "onResponse: getjob"+body);
                        Gson gson = new Gson();
                        JobCard[] jobCards = gson.fromJson(body,JobCard[].class);
                        jobCardArrayList.addAll(Arrays.asList(jobCards));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                selectJobCardAdapter.notifyDataSetChanged();
                            }
                        });

                    }else {

                    }
                }
            });
        }
    }

    private void GetJobCardDetail(){
        if (mJobCard != null){
            String url = References.GetJobCardDetail.methodName+mJobCard.getJC_Serial_No();

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
                        Log.d(TAG, "onResponse: jobcard details "+body);
                        Gson gson = new Gson();
                        JobCardDetails jobCardDetails = gson.fromJson(body, JobCardDetails.class);
                        Log.d(TAG, "onResponse: ggrgrrrrrrr"+jobCardDetails);

                    }
                }
            });
        }
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
        this.mShift = selectedShift;
        binding.textViewShift.setText(selectedShift.getRoster());
        alertselectShift.dismiss();
    }

    @Override
    public void onLineSelected(Lines selectedLine) {
        this.mLines = selectedLine;
        binding.textViewLine.setText(selectedLine.getSUB_UNINAME());
        alertselectLine.dismiss();

    }

    @Override
    public void onJobCardSelected(JobCard jobCard) {
        this.mJobCard=jobCard;
        binding.buttonJobCard.setText(jobCard.getJObCardNo());
        GetJobCardDetail();
        alertselectJobCard.dismiss();

    }

    public interface HomeFragmentListener {
        void logout();

    }
}