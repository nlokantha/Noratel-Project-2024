package com.example.noratelproject2024.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import java.util.Iterator;

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
    String datelog = String.valueOf(new java.util.Date());

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
    String dateAndTime;
    AppDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dateAndTime();

        db = Room.databaseBuilder(getActivity(), AppDatabase.class,"lines.db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        /*
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
        AppDatabase.class, "database-name").build();
         */

        String jobcardName = binding.buttonJobCard.getText().toString();
        String quantity = binding.editTextQuantity.getText().toString();
        if (jobcardName.equals("-Not Selected-")){
            binding.buttonClear.setEnabled(false);
            binding.buttonSave.setEnabled(false);
            binding.buttonHold.setEnabled(false);
            binding.buttonComplete.setEnabled(false);
        }
        if (mUser != null){
            selectLineCustomDialog();
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
                    reasonCodesArrayList.clear();
                    GetReasonCodes();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_line, null);
        builder.setView(view);

        alertselectLine = builder.create();
        alertselectLine.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectLine.show();

        WindowManager.LayoutParams layoutParams = alertselectLine.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,getResources().getDisplayMetrics());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_select_shift, null);
        builder.setView(view);

        alertselectShift = builder.create();
        alertselectShift.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertselectShift.show();

        WindowManager.LayoutParams layoutParams = alertselectShift.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,getResources().getDisplayMetrics());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomSearchJobCard);
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
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search = editTextSearch.getText().toString().trim();

                handleSearch(search);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,getResources().getDisplayMetrics());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,getResources().getDisplayMetrics());
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

    private void SelectReasonCode() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_hold_dialog, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,getResources().getDisplayMetrics());
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
                    if (mReasonCodes != null){
                        HoldJobCard();
                        alertDialog.dismiss();
                    }else {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,getResources().getDisplayMetrics());
        layoutParams.gravity = Gravity.CENTER;
        alertDialog.getWindow().setAttributes(layoutParams);

        Button buttonClose = view.findViewById(R.id.buttonClose);
        Button buttonConfirm = view.findViewById(R.id.buttonActivate);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    CompleteJobCard();
                    SelectNextWarning();
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

    private void SelectNextWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_selectnext, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,getResources().getDisplayMetrics());
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_job_hold_warning, null);
        builder.setView(view);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,300,getResources().getDisplayMetrics());
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
                    if (jobCardDetails != null){
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
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
        String url = References.GetLines.methodName;
        new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/getLines.txt");
        new Methods().saveToTextFile(getActivity(),datelog + "\n", "/getLines.txt");
        new Methods().saveToTextFile(getActivity(),url + "\n", "/getLines.txt");
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/getLines.txt");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {

                    String body = response.body().string();
                    Log.d(TAG, "onResponse: " + body);
                    Gson gson = new Gson();
                    Lines[] lines = gson.fromJson(body, Lines[].class);

                    linesArrayList.addAll(db.linesDao().getAllFromUser(mUser.getUsername()));
                    Log.d(TAG, "onResponse: Database list"+db.linesDao().getAll().size());
                    Log.d(TAG, "onResponse: Database list"+db.linesDao().getAll().toString());

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
                    Log.d(TAG, "onResponse: arraysize of lines "+linesArrayList.size());
                    new Methods().saveToTextFile(getActivity(),body + "\n", "/getLines.txt");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/getLines.txt");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSelectLine.setVisibility(View.GONE);
                            selectLineAdapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/getLines.txt");
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
        new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/getShift.txt");
        new Methods().saveToTextFile(getActivity(),datelog + "\n", "/getShift.txt");
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
            new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/getJobCard.txt");
            new Methods().saveToTextFile(getActivity(),datelog + "\n", "/getJobCard.txt");
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
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/getJobCardDetail.txt");
            new Methods().saveToTextFile(getActivity(),datelog + "\n", "/getJobCardDetail.txt");
            new Methods().saveToTextFile(getActivity(),request.toString() + "\n", "/getJobCardDetail.txt");

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
                        Log.d(TAG, "onResponse: jobCardDetails" + jobCardDetails);
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

                                    binding.buttonClear.setEnabled(true);
                                    binding.buttonSave.setEnabled(true);
                                    binding.buttonHold.setEnabled(true);
                                    binding.buttonComplete.setEnabled(true);
                                }else {
                                    JobHoldToOpenWarning();
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
        String url = References.GetReasonCodes.methodName;
        Request request = new Request.Builder()
                .url(url)
                .build();
        new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/getReasonCodes.txt");
        new Methods().saveToTextFile(getActivity(),datelog + "\n", "/getReasonCodes.txt");
        new Methods().saveToTextFile(getActivity(),request.toString() + "\n", "/getReasonCodes.txt");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/getReasonCodes.txt");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: Get Reason Codes " + body);
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
                    new Methods().saveToTextFile(getActivity(),body + "\n", "/getReasonCodes.txt");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/getReasonCodes.txt");
                } else {
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/getReasonCodes.txt");
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
        new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/searchByEmpNo.txt");
        new Methods().saveToTextFile(getActivity(),datelog + "\n", "/searchByEmpNo.txt");
        new Methods().saveToTextFile(getActivity(),request.toString() + "\n", "/searchByEmpNo.txt");
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
                new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/searchByEmpNo.txt");
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
                    new Methods().saveToTextFile(getActivity(),body + "\n", "/searchByEmpNo.txt");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/searchByEmpNo.txt");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBarSearchJobCard.setVisibility(View.GONE);
                            selectJobCardAdapter.updateList(jobCardArrayListSearch);
                            selectJobCardAdapter.notifyDataSetChanged();
                        }
                    });

                } else {
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/searchByEmpNo.txt");
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
        jsonBody.put("JobNo", mJobCard.getJob_No());
        jsonBody.put("Line", mLines.getSUB_UNINAME());
        jsonBody.put("Operations",jobCardDetails.getOperations());


        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/saveJobCard.txt");
        new Methods().saveToTextFile(getActivity(),datelog + "\n", "/saveJobCard.txt");
        new Methods().saveToTextFile(getActivity(),request.toString() + "\n", "/saveJobCard.txt");
        new Methods().saveToTextFile(getActivity(),jsonBody.toString() + "\n", "/saveJobCard.txt");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/saveJobCard.txt");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: Save Job Card Response  " + body);
                    new Methods().saveToTextFile(getActivity(),body + "\n", "/saveJobCard.txt");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/saveJobCard.txt");

                } else {
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/saveJobCard.txt");

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

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/HoldJobCard.txt");
        new Methods().saveToTextFile(getActivity(),datelog + "\n", "/HoldJobCard.txt");
        new Methods().saveToTextFile(getActivity(),request.toString() + "\n", "/HoldJobCard.txt");
        new Methods().saveToTextFile(getActivity(),jsonBody.toString() + "\n", "/HoldJobCard.txt");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/HoldJobCard.txt");

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: HoldJobCard " + body);
                    Log.d(TAG, "onResponse: HoldJobCard " + jsonBody.toString());
                    new Methods().saveToTextFile(getActivity(),body + "\n", "/HoldJobCard.txt");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/HoldJobCard.txt");
                } else {
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/HoldJobCard.txt");

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

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/CompleteJobCard.txt");
        new Methods().saveToTextFile(getActivity(),datelog + "\n", "/CompleteJobCard.txt");
        new Methods().saveToTextFile(getActivity(),request.toString() + "\n", "/CompleteJobCard.txt");
        new Methods().saveToTextFile(getActivity(),jsonBody.toString() + "\n", "/CompleteJobCard.txt");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/CompleteJobCard.txt");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: CompleteJobCard " + body);
                    new Methods().saveToTextFile(getActivity(),body + "\n", "/CompleteJobCard.txt");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/CompleteJobCard.txt");
                } else {
                    Log.d(TAG, "onResponse: CompleteJobCard ERROR!!!!!");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/CompleteJobCard.txt");
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
        new Methods().saveToTextFile(getActivity(),"--------------------------------" + "\n", "/HoldToOpen.txt");
        new Methods().saveToTextFile(getActivity(),datelog + "\n", "/HoldToOpen.txt");
        new Methods().saveToTextFile(getActivity(),request.toString() + "\n", "/HoldToOpen.txt");
        new Methods().saveToTextFile(getActivity(),jsonBody.toString() + "\n", "/HoldToOpen.txt");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                new Methods().saveToTextFile(getActivity(),e.getMessage() + "\n", "/HoldToOpen.txt");
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "onResponse: HoldToOpen " + body);
                    new Methods().saveToTextFile(getActivity(),body + "\n", "/HoldToOpen.txt");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/HoldToOpen.txt");
                } else {
                    Log.d(TAG, "onResponse: HoldToOpen Error!!!!!!");
                    new Methods().saveToTextFile(getActivity(),response.code() + "\n", "/HoldToOpen.txt");

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
                dateAndTime = year + "-" + (month + 1) + "-" + dayOfMonth;
                showTimePicker();
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
                boolean isPM = (hourOfDay >= 12);
                int hourIn12Format = hourOfDay % 12;
                if (hourIn12Format == 0) hourIn12Format = 12;
                String amPm = isPM ? "PM" : "AM";
                dateAndTime += " " + String.format("%02d:%02d %s", hourIn12Format, minute, amPm);
                binding.buttonDatePicker.setText(dateAndTime);
            }
        }, hour, minute, false);
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
        binding.textViewShift.setText(selectedShift.getRoster());
        alertselectShift.dismiss();
        selectJobCardCustomDialog();
    }

    @Override
    public void onLineSelected(Lines selectedLine) {
        this.mLines = selectedLine;

        if (mLines != null && mUser != null){
            Lines lines = new Lines(mLines.getSUB_UNICODE(),mLines.getSUB_UNINAME(),mUser.getUsername());
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
//        if (jobCard.getStatus().equals("Hold")) {
//            JobHoldToOpenWarning();
//        }
        alertselectJobCard.dismiss();

    }

    public interface HomeFragmentListener {
        void logout();

    }
}