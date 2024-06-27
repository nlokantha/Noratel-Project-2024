package com.example.noratelproject2024.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.noratelproject2024.Adapters.SelectJobCardAdapter;
import com.example.noratelproject2024.Adapters.SelectLineAdapter;
import com.example.noratelproject2024.Adapters.SelectShiftAdapter;
import com.example.noratelproject2024.Controls.Methods;
import com.example.noratelproject2024.Database.AppDatabase;
import com.example.noratelproject2024.Models.Actions.Actions;
import com.example.noratelproject2024.Models.Detail;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    String datelog;

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
    ArrayList<Lines> linesArrayListFull = new ArrayList<>();
    ArrayList<Shift> shiftArrayList = new ArrayList<>();
    ArrayList<JobCard> jobCardArrayList = new ArrayList<>();
    ArrayList<JobCard> jobCardArrayListSearch = new ArrayList<>();
    ArrayList<ReasonCodes> reasonCodesArrayList = new ArrayList<>();
    ArrayList<Detail> detailArrayList = new ArrayList<>();
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
    String dateAndTime = "";
    AppDatabase db;

    //    Failed Massage
    String failedToSave;
    String failedToHold;
    String failedToComplete;
    //
    Detail mDetail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Date dates = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        datelog = formatter.format(dates);

        dateAndTime();
        db = Room.databaseBuilder(getActivity(), AppDatabase.class, "lines.db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        String jobCardName = binding.buttonJobCard.getText().toString();

        if (jobCardName.equals("-Not Selected-")) {
            binding.buttonClear.setEnabled(false);
            binding.buttonSave.setEnabled(false);
            binding.buttonHold.setEnabled(false);
            binding.buttonComplete.setEnabled(false);
        }
        try {
            if (mUser != null && mUser.getDetail() == null) {
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                if (sharedPref.contains("mLines")) {
                    String mLinesStr = sharedPref.getString("mLines", null);
                    if (mLinesStr == null) {
                        selectLineCustomDialog();
                    } else {
                        Gson gson = new Gson();
                        mLines = gson.fromJson(mLinesStr, Lines.class);
                        binding.textViewLine.setText(mLines.getSUB_UNINAME());
//                    selectShiftCustomDialog();
                        if (sharedPref.contains("mShift")) {
                            String mShiftStr = sharedPref.getString("mShift", null);
                            if (mShiftStr == null) {
                                selectShiftCustomDialog();
                            } else {
                                mShift = gson.fromJson(mShiftStr, Shift.class);
                                binding.textViewShift.setText(mShift.getRoster());
                                selectJobCardCustomDialog();
                            }
                        } else {
                            selectShiftCustomDialog();
                        }
                    }
                } else {
                    selectLineCustomDialog();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            new Methods().saveToTextFile(getActivity(), e.getMessage());
        }
        try {
            if (mUser != null && mUser.getDetail() != null) {
                for (Detail detail : mUser.getDetail()) {
                    Log.d(TAG, "onViewCreated: " + detail);
                    detailArrayList.add(detail);
                }
                if (detailArrayList.size() == 1) {
                    getLinesForUser();
                    mDetail = detailArrayList.get(0);
                } else {
                    getLinesForUser();
                    mDetail = detailArrayList.get(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Methods().saveToTextFile(getActivity(), e.getMessage());
        }
        binding.imageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsCustomDialog();
            }
        });
        binding.buttonDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
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
                if (!quantity.isEmpty() && !quantity.equals("0")) {
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

        GetReasonCodes();
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
        alert.getWindow().setGravity(Gravity.CENTER);
        alert.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_line, null);
        builder.setView(view);

        alertselectLine = builder.create();
        alertselectLine.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectLine.show();

        WindowManager.LayoutParams layoutParams = alertselectLine.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertselectLine.getWindow().setAttributes(layoutParams);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBarSelectLine = view.findViewById(R.id.progressBarSelectLine);
        progressBarSelectLine.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectLineAdapter = new SelectLineAdapter(linesArrayList, this);
        recyclerView.setAdapter(selectLineAdapter);
    }

    private void selectShiftCustomDialog() {
        getShift();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_shift, null);
        builder.setView(view);

        alertselectShift = builder.create();
        alertselectShift.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectShift.show();

        WindowManager.LayoutParams layoutParams = alertselectShift.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertselectShift.getWindow().setAttributes(layoutParams);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBarSelectShift = view.findViewById(R.id.progressBarSelectShift);
        progressBarSelectShift.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectShiftAdapter = new SelectShiftAdapter(shiftArrayList, this);
        recyclerView.setAdapter(selectShiftAdapter);
    }

    private void selectJobCardCustomDialog() {
        getJobCard();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomSearchJobCard);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_search_jobcard, null);
        builder.setView(view);

        alertselectJobCard = builder.create();
        alertselectJobCard.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectJobCard.show();

        // Center the dialog and set the width to 250dp
        WindowManager.LayoutParams layoutParams = alertselectJobCard.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertselectJobCard.getWindow().setAttributes(layoutParams);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        progressBarSearchJobCard = view.findViewById(R.id.progressBarSearchJobCard);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectJobCardAdapter = new SelectJobCardAdapter(jobCardArrayList, this);
        recyclerView.setAdapter(selectJobCardAdapter);
        EditText editTextSearch = view.findViewById(R.id.editTextSearch);
        ImageView imageViewSearch = view.findViewById(R.id.imageViewSearch);

        if (detailArrayList.size()>1){
            editTextSearch.setText(mUser.getUsername());
            search = editTextSearch.getText().toString().trim();
            handleSearch(search);
        }
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search = editTextSearch.getText().toString().trim();
                if (!search.isEmpty()){
                    handleSearch(search);
                }else {
                    progressBarSearchJobCard.setVisibility(View.VISIBLE);
                    getJobCard();
                }
            }
        });
    }

    private void handleSearch(String query) {
        if (!query.isEmpty() && query.length() <= 6) {
            SearchByEmpNo();
            progressBarSearchJobCard.setVisibility(View.VISIBLE);
        } else if (query.length() > 6) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    selectJobCardAdapter.filter(query);
                    selectJobCardAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void saveJobCardWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);


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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

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

                binding.buttonClear.setEnabled(false);
                binding.buttonSave.setEnabled(false);
                binding.buttonHold.setEnabled(false);
                binding.buttonComplete.setEnabled(false);

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

    private void SaveSuccessfulWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_save_successful_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        Button buttonClose = view.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void SaveFailedWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_save_failed_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        Button buttonClose = view.findViewById(R.id.buttonClose);
        TextView textViewMessage = view.findViewById(R.id.textViewMessage);

        if (!failedToSave.isEmpty()) {
            textViewMessage.setText(failedToSave);
        }
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void HoldSuccessfulWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_hold_successful_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        Button buttonClose = view.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void HoldFailedWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_hold_failed_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        Button buttonClose = view.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void SelectReasonCode() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_hold_dialog, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        AutoCompleteTextView autoCompleteTextView = view.findViewById(R.id.autoCompleteTextViewHold);
        ArrayAdapter<ReasonCodes> adapter = new ArrayAdapter<ReasonCodes>(getActivity(), R.layout.dropdown_item, reasonCodesArrayList);
        autoCompleteTextView.setAdapter(adapter);
        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonConfirm = view.findViewById(R.id.buttonActivate);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mReasonCodes = reasonCodesArrayList.get(position);
//                Toast.makeText(getActivity(), mReasonCodes.getCategory(), Toast.LENGTH_SHORT).show();
            }
        });
        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mReasonCodes != null) {
                        HoldJobCard();
                        alertDialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Please Select Reason", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

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

    private void FailedToCompleteWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_complete_failed_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        Button buttonClose = view.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    private void SelectNextWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_selectnext, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonSelectNext = view.findViewById(R.id.buttonSelectNext);

        buttonSelectNext.setOnClickListener(new View.OnClickListener() {
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

                binding.buttonClear.setEnabled(false);
                binding.buttonSave.setEnabled(false);
                binding.buttonHold.setEnabled(false);
                binding.buttonComplete.setEnabled(false);

                selectJobCardCustomDialog();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_job_hold_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonActivate = view.findViewById(R.id.buttonActivate);

        buttonActivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    HoldToOpen();
                    binding.buttonJobCard.setText(mJobCard.getJObCardNo());
                    binding.editTextJobNo.setText(mJobCard.getJob_No());
                    if (jobCardDetails != null) {
                        binding.editTextOperations.setText(jobCardDetails.getOperations().toString());
                        binding.editTextEmployees.setText(jobCardDetails.getEmployees().toString());
                        binding.editTextLastRecorded.setText(jobCardDetails.getLastRec());
                        binding.editTextTarg.setText(jobCardDetails.getTarget());
                        binding.editTextComp.setText(jobCardDetails.getCompleted());
                    }
                    binding.buttonClear.setEnabled(true);
                    binding.buttonSave.setEnabled(true);
                    binding.buttonHold.setEnabled(true);
                    binding.buttonComplete.setEnabled(true);


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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_number_plate, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        // Center the dialog and set the width to 250dp
        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);


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
        linesArrayListFull.clear();
        String url = References.GetLines.methodName;
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Get Lines");
        new Methods().saveToTextFile(getActivity(), request.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {

                    String body = response.body().string();
                    Log.d(TAG, "onResponse: " + body);
                    Gson gson = new Gson();
                    Lines[] lines = gson.fromJson(body, Lines[].class);

//                    linesArrayList.addAll(db.linesDao().getAllFromUser(mUser.getUsername()));

                    List<Lines> linesFromDb = db.linesDao().getAllFromUser(mUser.getUsername());
                    Set<String> namesSet = new HashSet<>();
                    List<Lines> uniqueLinesList = new ArrayList<>();

                    for (Lines line : linesFromDb) {
                        if (namesSet.add(line.getSUB_UNINAME())) {
                            uniqueLinesList.add(line);
                        }
                    }
                    linesArrayList.addAll(uniqueLinesList);


                    Log.d(TAG, "onResponse: Database list" + db.linesDao().getAll().size());
                    Log.d(TAG, "onResponse: Database list" + db.linesDao().getAll().toString());

                    linesArrayListFull.addAll(Arrays.asList(lines));
                    linesArrayListFull.removeAll(linesArrayList);

                    Iterator<Lines> iterator = linesArrayListFull.iterator();
                    while (iterator.hasNext()) {
                        Lines lines1 = iterator.next();
                        for (Lines lines2 : linesArrayList) {
                            if (lines1.getSUB_UNINAME().equals(lines2.getSUB_UNINAME())) {
                                iterator.remove();
                                break;
                            }
                        }
                    }

                    linesArrayList.addAll(linesArrayListFull);
                    Log.d(TAG, "onResponse: arraysize of lines " + linesArrayList.size());
                    new Methods().saveToTextFile(getActivity(), body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSelectLine.setVisibility(View.GONE);
                            selectLineAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                }
            }
        });
    }

    private void getLinesForUser() {
        linesArrayList.clear();
        String url = References.GetLines.methodName;
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Get Line For User");
        new Methods().saveToTextFile(getActivity(), request.toString());


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Methods().saveToTextFile(getActivity(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Gson gson = new Gson();
                    Lines[] lines = gson.fromJson(body, Lines[].class);
                    linesArrayList.addAll(Arrays.asList(lines));
                    String SUB_UNICODE = mDetail.getLine();
                    for (Lines lines1 : linesArrayList) {
                        if (lines1.getSUB_UNICODE().equals(SUB_UNICODE)) {
                            mLines = lines1;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.textViewLine.setText(mLines.getSUB_UNINAME());
                                    getShiftForUser();
                                }
                            });

                        }
                    }
//                    new Methods().saveToTextFile(getActivity(),body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                } else {
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
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
        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Get Shift");
        new Methods().saveToTextFile(getActivity(), request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(), e.getMessage() + "\n");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {

                    String body = response.body().string();
                    Log.d(TAG, "onResponse: " + body);
                    Gson gson = new Gson();
                    Shift[] shifts = gson.fromJson(body, Shift[].class);
                    shiftArrayList.addAll(Arrays.asList(shifts));
                    new Methods().saveToTextFile(getActivity(), body + "\n");
                    new Methods().saveToTextFile(getActivity(), response.code() + "\n");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSelectShift.setVisibility(View.GONE);
                            selectShiftAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    new Methods().saveToTextFile(getActivity(), response.code() + "\n");
                }
            }
        });
    }

    private void getShiftForUser() {
        shiftArrayList.clear();
        String url = References.GetShift.methodName;
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Get Shift For User");
        new Methods().saveToTextFile(getActivity(), request.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Methods().saveToTextFile(getActivity(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: " + body);
                    Gson gson = new Gson();
                    Shift[] shifts = gson.fromJson(body, Shift[].class);
                    shiftArrayList.addAll(Arrays.asList(shifts));

                    for (Shift shift : shiftArrayList) {
                        if (shift.getRoster().equals(mDetail.getShift())) {
                            mShift = shift;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.textViewShift.setText(mShift.getRoster());
                                    if (detailArrayList.size()==1){
                                        getJobCardForUser();
                                    }
                                }
                            });

                        }
                    }
//                    new Methods().saveToTextFile(getActivity(), body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));

                } else {
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
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

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            new Methods().saveToTextFile(getActivity(), "--------------------------------");
            new Methods().saveToTextFile(getActivity(), datelog);
            new Methods().saveToTextFile(getActivity(), "Get JobCard");
            new Methods().saveToTextFile(getActivity(), request.toString());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    new Methods().saveToTextFile(getActivity(), e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {

                        String body = response.body().string();
                        Log.d(TAG, "onResponse: get job" + body);
                        Gson gson = new Gson();
                        JobCard[] jobCards = gson.fromJson(body, JobCard[].class);
                        jobCardArrayList.addAll(Arrays.asList(jobCards));
                        Log.d(TAG, "onResponse: job card list = " + jobCardArrayList.size());
                        new Methods().saveToTextFile(getActivity(), body);
                        new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBarSearchJobCard.setVisibility(View.GONE);
                                selectJobCardAdapter.notifyDataSetChanged();


                            }
                        });

                    } else {
                        new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));

                    }
                }
            });
        }
    }

    private void getJobCardForUser() {
        jobCardArrayList.clear();
        String date = binding.buttonDatePicker.getText().toString().trim();

        String url = References.GetJobCard.methodName +
                "?date=" + date +
                "&line=" + mLines.getSUB_UNICODE() +
                "&shift=" + mShift.getRoster();
        Request request = new Request.Builder()
                .url(url)
                .build();

        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Get JobCard For User");
        new Methods().saveToTextFile(getActivity(), request.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Methods().saveToTextFile(getActivity(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: get job" + body);
                    Gson gson = new Gson();
                    JobCard[] jobCards = gson.fromJson(body, JobCard[].class);
                    jobCardArrayList.addAll(Arrays.asList(jobCards));
                    for (JobCard jobCard : jobCardArrayList) {
                        if (jobCard.getJObCardNo().equals(mDetail.getJObCardNo())) {
                            mJobCard = jobCard;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.buttonJobCard.setText(mJobCard.getJObCardNo());
                                    GetJobCardDetail();
                                }
                            });
                        }
                    }
//                    new Methods().saveToTextFile(getActivity(), body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                } else {
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                }

            }
        });

    }

    private void GetJobCardDetail() {
        if (mJobCard != null) {
            String url = References.GetJobCardDetail.methodName + mJobCard.getJC_Serial_No();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            new Methods().saveToTextFile(getActivity(), "--------------------------------");
            new Methods().saveToTextFile(getActivity(), datelog);
            new Methods().saveToTextFile(getActivity(), "Get Job Card Details");
            new Methods().saveToTextFile(getActivity(), request.toString());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    new Methods().saveToTextFile(getActivity(), e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        Log.d(TAG, "onResponse: job Card details " + body);
                        Gson gson = new Gson();
                        jobCardDetails = gson.fromJson(body, JobCardDetails.class);
                        Log.d(TAG, "onResponse: jobCardDetails" + jobCardDetails);
//                        new Methods().saveToTextFile(getActivity(), body);
                        new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
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

                                    binding.buttonClear.setEnabled(true);
                                    binding.buttonSave.setEnabled(true);
                                    binding.buttonHold.setEnabled(true);
                                    binding.buttonComplete.setEnabled(true);
                                } else {
                                    JobHoldToOpenWarning();
                                }
                            }
                        });

                    } else {
                        new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                    }
                }
            });
        }
    }

    private void GetJobCardDetailAfterSave() {
        if (mJobCard != null) {
            String url = References.GetJobCardDetail.methodName + mJobCard.getJC_Serial_No();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            new Methods().saveToTextFile(getActivity(), "--------------------------------");
            new Methods().saveToTextFile(getActivity(), datelog);
            new Methods().saveToTextFile(getActivity(), "Get Job Card Detail After Save");
            new Methods().saveToTextFile(getActivity(), request.toString());
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        Log.d(TAG, "onResponse: GetJobCardDetailAfterSave " + body);
                        Gson gson = new Gson();
                        jobCardDetails = gson.fromJson(body, JobCardDetails.class);
                        Log.d(TAG, "onResponse: jobCardDetails" + jobCardDetails);
                        new Methods().saveToTextFile(getActivity(), body);
                        new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.editTextLastRecorded.setText(jobCardDetails.getLastRec());
                                binding.editTextTarg.setText(jobCardDetails.getTarget());
                                binding.editTextComp.setText(jobCardDetails.getCompleted());
                                binding.editTextQuantity.setText("0");
                                binding.editTextSerialNumber.setText("");
                            }
                        });
                    } else {
                        new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                    }
                }
            });
        }


    }

    private void GetReasonCodes() {
        String url = References.GetReasonCodes.methodName;
        Request request = new Request.Builder()
                .url(url)
                .build();
        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Get Reason Codes");
        new Methods().saveToTextFile(getActivity(), request.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
//                    Log.d(TAG, "onResponse: Get Reason Codes " + body);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            ReasonCodes[] reasonCodes = gson.fromJson(body, ReasonCodes[].class);
                            reasonCodesArrayList.addAll(Arrays.asList(reasonCodes));
                        }
                    });
                    Gson gson = new Gson();
                    ReasonCodes[] reasonCodes = gson.fromJson(body, ReasonCodes[].class);
                    reasonCodesArrayList.addAll(Arrays.asList(reasonCodes));
//                    new Methods().saveToTextFile(getActivity(), body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                } else {
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
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
        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Search By EmpNo");
        new Methods().saveToTextFile(getActivity(), request.toString());
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
                new Methods().saveToTextFile(getActivity(), e.getMessage());
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
                    new Methods().saveToTextFile(getActivity(), body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSearchJobCard.setVisibility(View.GONE);
                            selectJobCardAdapter.updateList(jobCardArrayListSearch);
                            selectJobCardAdapter.notifyDataSetChanged();
                        }
                    });

                } else {
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
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
        String serialNumber = binding.editTextSerialNumber.getText().toString();

        JSONObject jsonBody = new JSONObject();

        jsonBody.put("JobSrNo", mJobCard.getJC_Serial_No());
        jsonBody.put("QtyCompleted", quantity);
        jsonBody.put("Status", mJobCard.getStatus());
        jsonBody.put("Username", mUser.getUsername());
        jsonBody.put("JobNo", mJobCard.getJob_No());
        jsonBody.put("Line", mLines.getSUB_UNINAME());

        List<String> operations = jobCardDetails.getOperations();
        JSONArray operationsJsonArray = new JSONArray(operations);

        jsonBody.put("Operations", operationsJsonArray);
        if (serialNumber.isEmpty()) {
            serialNumber = "";
        }
        jsonBody.put("SerialTrackingText", serialNumber);

        jsonBody.put("RecordingTimeGiven", dateAndTime);

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Save Job Card");
        new Methods().saveToTextFile(getActivity(), request.toString());
        new Methods().saveToTextFile(getActivity(), jsonBody.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: Save Job Card Response  " + body);
                    new Methods().saveToTextFile(getActivity(), body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                    Gson gson = new Gson();
                    Actions actions = gson.fromJson(body, Actions.class);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (actions.getStatus().equals("0")) {
                                SaveSuccessfulWarning();
                                binding.buttonDatePicker.setText("");
                                GetJobCardDetailAfterSave();
                            } else {
                                failedToSave = actions.getMessage().toString();
//                                Toast.makeText(getActivity(), "Failed to Save", Toast.LENGTH_SHORT).show();
                                SaveFailedWarning();
                            }
                        }
                    });


                } else {
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SaveFailedWarning();
                        }
                    });

                }
            }
        });
    }

    private void HoldJobCard() throws JSONException {

        String quantity = binding.editTextQuantity.getText().toString();
        String url = References.HoldJobCard.methodName;
        JSONObject jsonBody = new JSONObject();

        jsonBody.put("JobSrNo", mJobCard.getJC_Serial_No());
        jsonBody.put("QtyCompleted", quantity);
        jsonBody.put("Status", mJobCard.getStatus());
        jsonBody.put("Username", mUser.getUsername());
        jsonBody.put("ReasonCode", mReasonCodes.getSr_No());
        jsonBody.put("RecordingTimeGiven", dateAndTime);

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Hold Job Card");
        new Methods().saveToTextFile(getActivity(), request.toString());
        new Methods().saveToTextFile(getActivity(), jsonBody.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(), e.getMessage() + "\n");

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: HoldJobCard " + body);
                    Log.d(TAG, "onResponse: HoldJobCard " + jsonBody.toString());
                    new Methods().saveToTextFile(getActivity(), body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));
                    Gson gson = new Gson();
                    Actions actions = gson.fromJson(body, Actions.class);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (actions.getStatus().equals("0")) {
//                                Toast.makeText(getActivity(), "Hold Successfully", Toast.LENGTH_SHORT).show();
                                HoldSuccessfulWarning();

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


                                binding.buttonClear.setEnabled(false);
                                binding.buttonSave.setEnabled(false);
                                binding.buttonHold.setEnabled(false);
                                binding.buttonComplete.setEnabled(false);

                            } else {
//                                Toast.makeText(getActivity(), "Failed to Hold", Toast.LENGTH_SHORT).show();
                                failedToHold = actions.getMessage().toString();
                                HoldFailedWarning();

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

                                binding.buttonClear.setEnabled(false);
                                binding.buttonSave.setEnabled(false);
                                binding.buttonHold.setEnabled(false);
                                binding.buttonComplete.setEnabled(false);
                            }

                        }
                    });

                } else {
                    new Methods().saveToTextFile(getActivity(), response.code() + "\n");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            HoldFailedWarning();

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

                            binding.buttonClear.setEnabled(false);
                            binding.buttonSave.setEnabled(false);
                            binding.buttonHold.setEnabled(false);
                            binding.buttonComplete.setEnabled(false);
                        }
                    });

                }
            }
        });
    }

    private void CompleteJobCard() throws JSONException {
        String url = References.CompleteJobCard.methodName;
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("JobSrNo", mJobCard.getJC_Serial_No());
        jsonBody.put("Status", mJobCard.getStatus());
        jsonBody.put("Username", mUser.getUsername());
        jsonBody.put("RecordingTimeGiven", dateAndTime);

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        new Methods().saveToTextFile(getActivity(), "--------------------------------" + "\n");
        new Methods().saveToTextFile(getActivity(), datelog + "\n");
        new Methods().saveToTextFile(getActivity(), "Complete Job Card" + "\n");
        new Methods().saveToTextFile(getActivity(), request.toString() + "\n");
        new Methods().saveToTextFile(getActivity(), jsonBody.toString() + "\n");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(), e.getMessage() + "\n");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: CompleteJobCard " + body);
                    new Methods().saveToTextFile(getActivity(), body + "\n");
                    new Methods().saveToTextFile(getActivity(), response.code() + "\n");
                    Gson gson = new Gson();
                    Actions actions = gson.fromJson(body, Actions.class);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (actions.getStatus().equals("0")) {
                                SelectNextWarning();
                                binding.buttonDatePicker.setText("");
                            } else {
                                failedToComplete = actions.getMessage().toString();
                                FailedToCompleteWarning();
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "onResponse: CompleteJobCard ERROR!!!!!");
                    new Methods().saveToTextFile(getActivity(), response.code() + "\n");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FailedToCompleteWarning();
                        }
                    });
                }
            }
        });
    }

    private void HoldToOpen() throws JSONException {
        String url = References.HoldToOpen.methodName;

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("JobSrNo", mJobCard.getJC_Serial_No());
        jsonBody.put("Username", mUser.getUsername());

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(References.HoldToOpen.methodName)
                .post(requestBody)
                .build();
        new Methods().saveToTextFile(getActivity(), "--------------------------------");
        new Methods().saveToTextFile(getActivity(), datelog);
        new Methods().saveToTextFile(getActivity(), "Hold To Open");
        new Methods().saveToTextFile(getActivity(), request.toString());
        new Methods().saveToTextFile(getActivity(), jsonBody.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: HoldToOpen " + body);
                    new Methods().saveToTextFile(getActivity(), body);
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));

                } else {
                    Log.d(TAG, "onResponse: HoldToOpen Error!!!!!!");
                    new Methods().saveToTextFile(getActivity(), String.valueOf(response.code()));

                }
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int YEAR = calendar.get(Calendar.YEAR);
        int MONTH = calendar.get(Calendar.MONTH);
        int DATE = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // Format the date as "YYYY-MM-DD"
                dateAndTime = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
//                showTimePicker();
                binding.buttonDatePicker.setText(dateAndTime);
            }
        }, YEAR, MONTH, DATE);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // Format the time as "HH:mm"
                dateAndTime += String.format(" %02d:%02d", hourOfDay, minute);
                binding.buttonDatePicker.setText(dateAndTime);
            }
        }, hour, minute, false); // 'true' for 24-hour format
        timePickerDialog.show();
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

        new Methods().saveToTextFile(getActivity(), "Selected Shift = " + selectedShift);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        editor.putString("mShift", gson.toJson(mShift));
        editor.apply();


        binding.textViewShift.setText(selectedShift.getRoster());
        alertselectShift.dismiss();
        selectJobCardCustomDialog();
    }

    @Override
    public void onLineSelected(Lines selectedLine) {
        this.mLines = selectedLine;

        new Methods().saveToTextFile(getActivity(), "Selected Line = " + selectedLine);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        editor.putString("mLines", gson.toJson(mLines));
        editor.apply();


        if (mLines != null && mUser != null) {
            Lines lines = new Lines(mLines.getSUB_UNICODE(), mLines.getSUB_UNINAME(), mUser.getUsername());
            db.linesDao().upsert(lines);
        }

        binding.textViewLine.setText(selectedLine.getSUB_UNINAME());
        alertselectLine.dismiss();
        selectShiftCustomDialog();
    }

    @Override
    public void onJobCardSelected(JobCard jobCard) {
        this.mJobCard = jobCard;
        GetJobCardDetail();
        alertselectJobCard.dismiss();
        binding.editTextQuantity.setText("0");
        binding.buttonDatePicker.setText("");
        binding.editTextSerialNumber.setText("");
        new Methods().saveToTextFile(getActivity(), "Selected JobCard = " + jobCard);

    }

    public interface HomeFragmentListener {
        void logout();

    }
}