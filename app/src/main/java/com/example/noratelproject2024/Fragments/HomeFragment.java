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

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.noratelproject2024.Adapters.SelectJobCardAdapter;
import com.example.noratelproject2024.Adapters.SelectLineAdapter;
import com.example.noratelproject2024.Adapters.SelectShiftAdapter;
import com.example.noratelproject2024.Controls.Methods;
import com.example.noratelproject2024.Models.JobCard;
import com.example.noratelproject2024.Models.JobCardDetails;
import com.example.noratelproject2024.Models.Lines;
import com.example.noratelproject2024.Models.ReasonCodes;
import com.example.noratelproject2024.Models.Shift;
import com.example.noratelproject2024.Models.User;
import com.example.noratelproject2024.R;
import com.example.noratelproject2024.References;
import com.example.noratelproject2024.databinding.FragmentHomeBinding;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment implements SelectShiftAdapter.OnShiftSelectedListener, SelectLineAdapter.OnLineSelectedListener, SelectJobCardAdapter.OnJobCardSelectedListener {
    private static final String ARG_PARAM_USER = "ARG_PARAM_USER";
    private User mUser;
    String date = String.valueOf(new java.util.Date());

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(User mUser) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_USER, mUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(ARG_PARAM_USER);
        }
    }

    FragmentHomeBinding binding;
    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "demo";
    ArrayList<Lines> linesArrayList = new ArrayList<>();
    ArrayList<Shift> shiftArrayList = new ArrayList<>();
    ArrayList<JobCard> jobCardArrayList = new ArrayList<>();
    ArrayList<JobCard> jobCardArrayListSearch = new ArrayList<>();
    ArrayList<ReasonCodes> reasonCodesArrayList = new ArrayList<>();
    SelectLineAdapter selectLineAdapter;
    SelectShiftAdapter selectShiftAdapter;
    SelectJobCardAdapter selectJobCardAdapter;
    AlertDialog alertselectLine, alertselectShift, alertselectJobCard;
    ProgressBar progressBarSelectShift, progressBarSearchJobCard, progressBarSelectLine;
    Shift mShift;
    Lines mLines;
    JobCard mJobCard;
    JobCardDetails jobCardDetails;
    ReasonCodes mReasonCodes;
    int count = 0;
    String search;

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
        binding.buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = binding.editTextQuantity.getText().toString();
                if (!quantity.isEmpty()) {
                    count = Integer.valueOf(quantity);
                    count++;
                    binding.editTextQuantity.setText(String.valueOf(count));
                }
            }
        });
        binding.buttonMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = binding.editTextQuantity.getText().toString();
                if (!quantity.isEmpty()) {
                    count = Integer.valueOf(quantity);
                    count--;
                    binding.editTextQuantity.setText(String.valueOf(count));
                }
            }
        });
        binding.buttonJobCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String line = binding.textViewLine.getText().toString().trim();
                String shift = binding.textViewShift.getText().toString().trim();
                if (line.equals("N/A")) {
                    Toast.makeText(getActivity(), "Please Select Line", Toast.LENGTH_SHORT).show();
                } else if (shift.equals("N/A")) {
                    Toast.makeText(getActivity(), "Please Select Shift", Toast.LENGTH_SHORT).show();
                } else {
                    selectJobCardCustomDialog();
                }
            }
        });
        binding.buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEverything();
            }
        });
        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveJobCardWarning();
            }
        });
        binding.buttonHold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectReasonCode();
            }
        });
        binding.buttonComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CompleteWarning();
            }
        });
        binding.editTextQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPlates();
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
        TextView textViewUserName = view.findViewById(R.id.textViewUserName);
        if (mUser != null) {
            textViewUserName.setText(mUser.getUsername());
        } else {
            textViewUserName.setText("N/A");
        }

        if (mLines != null) {
            textViewSelectedLine.setText(mLines.getSUB_UNINAME());
        } else {
            textViewSelectedLine.setText("N/A");
        }

        if (mShift != null) {
            textViewSelectedShift.setText(mShift.getRoster());
        } else {
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

    private void selectLineCustomDialog() {
        getLines();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_line, null);
        builder.setView(view);

        alertselectLine = builder.create();
        alertselectLine.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectLine.show();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBarSelectLine = view.findViewById(R.id.progressBarSelectLine);
        progressBarSelectLine.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectLineAdapter = new SelectLineAdapter(linesArrayList, this);
        recyclerView.setAdapter(selectLineAdapter);
    }

    private void selectShiftCustomDialog() {
        getShift();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_shift, null);
        builder.setView(view);

        alertselectShift = builder.create();
        alertselectShift.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectShift.show();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBarSelectShift = view.findViewById(R.id.progressBarSelectShift);
        progressBarSelectShift.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectShiftAdapter = new SelectShiftAdapter(shiftArrayList, this);
        recyclerView.setAdapter(selectShiftAdapter);
    }

    private void selectJobCardCustomDialog() {
        getJobCard();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_search_jobcard, null);
        builder.setView(view);

        alertselectJobCard = builder.create();
        alertselectJobCard.getWindow().setGravity(Gravity.BOTTOM);
        alertselectJobCard.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alertselectJobCard.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectJobCard.show();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBarSearchJobCard = view.findViewById(R.id.progressBarSearchJobCard);
        progressBarSearchJobCard.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectJobCardAdapter = new SelectJobCardAdapter(jobCardArrayList, this);
        recyclerView.setAdapter(selectJobCardAdapter);
        EditText editTextSearch = view.findViewById(R.id.editTextSearch);
        ImageView imageViewSearch = view.findViewById(R.id.imageViewSearch);
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search = editTextSearch.getText().toString().trim();
//                selectJobCardAdapter.filter(search);
                handleSearch(search);

            }
        });
    }

    private void handleSearch(String query) {
        if (!query.isEmpty() && query.length() <= 6) {
            SearchByEmpNo();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectJobCardAdapter.updateList(jobCardArrayListSearch);
                }
            },2000);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    progressBarSearchJobCard.setVisibility(View.GONE);
                    selectJobCardAdapter.filter(query);
                }
            });
        }
    }

    private void saveJobCardWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonConfirm = view.findViewById(R.id.buttonActivate);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SaveJobCard();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                alertDialog.dismiss();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void clearEverything() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonConfirm = view.findViewById(R.id.buttonActivate);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.buttonJobCard.setText("-Not Selected-");
                binding.buttonDatePicker.setText("");
                binding.editTextJobNo.setText("");
                binding.editTextOperations.setText("");
                binding.editTextEmployees.setText("");
                binding.editTextLastRecorded.setText("");
                binding.editTextTarg.setText("");
                binding.editTextComp.setText("");
                binding.editTextQuantity.setText("0");
                binding.editTextSerialNumber.setText("");
                alertDialog.dismiss();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void SelectReasonCode() {
        GetReasonCodes();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_hold_dialog, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.autoCompleteTextViewHold);
        ArrayAdapter<ReasonCodes> adapter = new ArrayAdapter<ReasonCodes>(getActivity(), R.layout.dropdown_item, reasonCodesArrayList);
        autoCompleteTextView.setAdapter(adapter);
        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonConfirm = view.findViewById(R.id.buttonActivate);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mReasonCodes = reasonCodesArrayList.get(position);
                Toast.makeText(getActivity(), mReasonCodes.getCategory(), Toast.LENGTH_SHORT).show();
            }
        });
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    HoldJobCard();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                alertDialog.dismiss();
            }
        });
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void CompleteWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonConfirm = view.findViewById(R.id.buttonActivate);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    CompleteJobCard();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                alertDialog.dismiss();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void JobHoldToOpenWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_job_hold_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonActivate = view.findViewById(R.id.buttonActivate);

        buttonActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    HoldToOpen();
                    binding.buttonJobCard.setText(mJobCard.getJObCardNo());
                    binding.editTextJobNo.setText(mJobCard.getJob_No());
                    binding.editTextOperations.setText(jobCardDetails.getOperations().toString());
                    binding.editTextEmployees.setText(jobCardDetails.getEmployees().toString());
                    binding.editTextLastRecorded.setText(jobCardDetails.getLastRec());
                    binding.editTextTarg.setText(jobCardDetails.getTarget());
                    binding.editTextComp.setText(jobCardDetails.getCompleted());

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                alertDialog.dismiss();
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void NumberPlates() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_number_plate, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        EditText editTextQ = view.findViewById(R.id.editTextQ);
        Button button1, button2, button3, button4, button5, button6, button7, button8, button9, button0;
        button0 = view.findViewById(R.id.button0);
        button1 = view.findViewById(R.id.button1);
        button2 = view.findViewById(R.id.button2);
        button3 = view.findViewById(R.id.button3);
        button4 = view.findViewById(R.id.button4);
        button5 = view.findViewById(R.id.button5);
        button6 = view.findViewById(R.id.button6);
        button7 = view.findViewById(R.id.button7);
        button8 = view.findViewById(R.id.button8);
        button9 = view.findViewById(R.id.button9);
        Button buttonClear = view.findViewById(R.id.buttonClear);
        Button buttonSet = view.findViewById(R.id.buttonSet);


        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 0);
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 1);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 2);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 3);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 4);
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 5);
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 6);
            }
        });
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 7);
            }
        });
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 8);
            }
        });
        button9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString();
                editTextQ.setText(quantity + 9);
            }
        });
        buttonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editTextQ.getText().toString().trim();
                binding.editTextQuantity.setText(quantity);
                alertDialog.dismiss();
            }
        });

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextQ.setText("");
            }
        });

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
                    Lines[] lines = gson.fromJson(body, Lines[].class);
                    linesArrayList.addAll(Arrays.asList(lines));

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSelectLine.setVisibility(View.GONE);
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
        new Methods().saveToTextFile(getActivity(),date + "\n", "/getShift.txt");
        new Methods().saveToTextFile(getActivity(),url + "\n", "/getShift.txt");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/getShift.txt");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {

                    String body = response.body().string();
                    Log.d(TAG, "onResponse: " + body);
                    Gson gson = new Gson();
                    Shift[] shifts = gson.fromJson(body, Shift[].class);
                    shiftArrayList.addAll(Arrays.asList(shifts));
                    new Methods().saveToTextFile(getActivity(),body + "\n", "/getShift.txt");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/getShift.txt");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSelectShift.setVisibility(View.GONE);
                            selectShiftAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/getShift.txt");
                }
            }
        });
    }

    private void getJobCard() {
        jobCardArrayList.clear();
        String date = binding.buttonDatePicker.getText().toString().trim();
        if (mLines != null && mShift != null) {
            String url = References.GetJobCard.methodName +
                    "?date=" + date +
                    "&line=" + mLines.getSUB_UNICODE() +
                    "&shift=" + mShift.getRoster();
            new Methods().saveToTextFile(getActivity(),url + "\n", "/getJobCard.txt");

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/getJobCard.txt");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {

                        String body = response.body().string();
                        Log.d(TAG, "onResponse: get job" + body);
                        Gson gson = new Gson();
                        JobCard[] jobCards = gson.fromJson(body, JobCard[].class);
                        jobCardArrayList.addAll(Arrays.asList(jobCards));
                        Log.d(TAG, "onResponse: job card list = "+jobCardArrayList.size());
                        new Methods().saveToTextFile(getActivity(),body + "\n", "/getJobCard.txt");
                        new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/getJobCard.txt");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBarSearchJobCard.setVisibility(View.GONE);
                                selectJobCardAdapter.notifyDataSetChanged();
                            }
                        });

                    } else {
                        new Methods().saveToTextFile(getActivity(),response.code()+ "\n", "/getJobCard.txt");

                    }
                }
            });
        }
    }

    private void GetJobCardDetail() {
        if (mJobCard != null) {
            String url = References.GetJobCardDetail.methodName + mJobCard.getJC_Serial_No();
            new Methods().saveToTextFile(getActivity(),url + "\n", "/getJobCardDetail.txt");

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/getJobCardDetail.txt");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        Log.d(TAG, "onResponse: job Card details " + body);
                        Gson gson = new Gson();
                        jobCardDetails = gson.fromJson(body, JobCardDetails.class);
                        Log.d(TAG, "onResponse: ggrgrrrrrrr" + jobCardDetails);
                        new Methods().saveToTextFile(getActivity(),body + "\n", "/getJobCardDetail.txt");
                        new Methods().saveToTextFile(getActivity(),String.valueOf(response.code()) + "\n", "/getJobCardDetail.txt");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!mJobCard.getStatus().equals("Hold")) {
                                    binding.buttonJobCard.setText(mJobCard.getJObCardNo());
                                    binding.editTextJobNo.setText(mJobCard.getJob_No());
                                    binding.editTextOperations.setText(jobCardDetails.getOperations().toString());
                                    binding.editTextEmployees.setText(jobCardDetails.getEmployees().toString());
                                    binding.editTextLastRecorded.setText(jobCardDetails.getLastRec());
                                    binding.editTextTarg.setText(jobCardDetails.getTarget());
                                    binding.editTextComp.setText(jobCardDetails.getCompleted());
                                }

                            }
                        });

                    }else {
                        new Methods().saveToTextFile(getActivity(),String.valueOf(response.code()) + "\n", "/getJobCardDetail.txt");
                    }
                }
            });
        }
    }

    private void GetReasonCodes() {
        Request request = new Request.Builder()
                .url(References.GetReasonCodes.methodName)
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
                    Log.d(TAG, "onResponse: Get Reason Codes " + body);
                    Gson gson = new Gson();
                    ReasonCodes[] reasonCodes = gson.fromJson(body, ReasonCodes[].class);
                    reasonCodesArrayList.addAll(Arrays.asList(reasonCodes));
                } else {

                }
            }
        });
    }

    private void SearchByEmpNo() {
        jobCardArrayListSearch.clear();

        String url = References.SearchByEmpNo.methodName + search;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBarSearchJobCard.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Gson gson = new Gson();
                    JobCard[] jobCards = gson.fromJson(body, JobCard[].class);
                    jobCardArrayListSearch.addAll(Arrays.asList(jobCards));
                    Log.d(TAG, "onResponse: SearchByEmpNo" + body);
                    Log.d(TAG, "onResponse: SearchByEmpNo array size " + jobCardArrayListSearch.size());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSearchJobCard.setVisibility(View.GONE);
                            selectJobCardAdapter.updateList(jobCardArrayListSearch);
                            selectJobCardAdapter.notifyDataSetChanged();
                        }
                    });

                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "Unsuccessful response", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void SaveJobCard() throws JSONException {
        String quantity = binding.editTextQuantity.getText().toString();
        String url = References.SaveJobCard.methodName;

        JSONObject jsonBody = new JSONObject();

        jsonBody.put("JobSrNo", mJobCard.getJC_Serial_No());
        jsonBody.put("QtyCompleted", quantity);
        jsonBody.put("Status", mJobCard.getStatus());
        jsonBody.put("Username", mUser.getUsername());

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
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
                    Log.d(TAG, "onResponse: POST " + body);

                } else {

                }
            }
        });


    }

    private void HoldJobCard() throws JSONException {

        String quantity = binding.editTextQuantity.getText().toString();
        JSONObject jsonBody = new JSONObject();

        jsonBody.put("JobSrNo", mJobCard.getJC_Serial_No());
        jsonBody.put("QtyCompleted", quantity);
        jsonBody.put("Status", mJobCard.getStatus());
        jsonBody.put("Username", mUser.getUsername());
        jsonBody.put("ReasonCode", mReasonCodes.getSr_No());

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(References.HoldJobCard.methodName)
                .post(requestBody)
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
                    Log.d(TAG, "onResponse: HoldJobCard " + body);
                    Log.d(TAG, "onResponse: HoldJobCard " + jsonBody.toString());
                } else {

                }
            }
        });
    }

    private void CompleteJobCard() throws JSONException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("JobSrNo", mJobCard.getJC_Serial_No());
        jsonBody.put("Status", mJobCard.getStatus());
        jsonBody.put("Username", mUser.getUsername());

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(References.CompleteJobCard.methodName)
                .post(requestBody)
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
                    Log.d(TAG, "onResponse: CompleteJobCard " + body);
                } else {
                    Log.d(TAG, "onResponse: CompleteJobCard ERROR!!!!!");
                }
            }
        });
    }

    private void HoldToOpen() throws JSONException {

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("JobSrNo", mJobCard.getJC_Serial_No());
        jsonBody.put("Username", mUser.getUsername());

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(References.HoldToOpen.methodName)
                .post(requestBody)
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
                    Log.d(TAG, "onResponse: HoldToOpen " + body);
                } else {
                    Log.d(TAG, "onResponse: HoldToOpen Error!!!!!!");

                }
            }
        });
    }


    private void datePick() {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = year + "-" + month + "-" + dayOfMonth;
                binding.buttonDatePicker.setText(date);
            }
        }, YEAR, MONTH, DATE);
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
        this.mJobCard = jobCard;
        if (jobCard.getStatus().equals("Hold")) {
            JobHoldToOpenWarning();
        }
        GetJobCardDetail();
        alertselectJobCard.dismiss();

    }

    public interface HomeFragmentListener {
        void logout();

    }
}