package com.example.firebaseapp.Fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelShift;
import com.example.firebaseapp.utils.FirebaseManager;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


@RequiresApi(api = Build.VERSION_CODES.N)
public class FragmentReports extends Fragment {

    MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.dateRangePicker();
    private Button reports_btn_pick_date;
    private LinearLayout reports_lil_logs;
    private TextView reports_tev_total;
    //to problematically add to reports
    private View view;

    public FragmentReports() {
        // Required empty public constructor
    }

    public static FragmentReports newInstance() {
        FragmentReports fragment = new FragmentReports();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reports, container, false);
        this.view = view;
        findViews(view);
        initialize();
        return view;
    }

    private void initialize() {
        //click on pick a date
        this.reports_btn_pick_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker picker = builder.build();
                picker.show(getFragmentManager(), picker.toString());
                //in time picker if picked cancel
                picker.addOnNegativeButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        picker.dismiss();
                    }
                });
                //in the date picker if picked save
                picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long,Long>>(){
                    @Override
                    public void onPositiveButtonClick(Pair<Long, Long> selection) {
                        //extract date picked by user
                        long first = selection.first;
                        long second = selection.second;
                        //array for shifts and time formats to display
                        ArrayList<ModelShift> shifts = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        SimpleDateFormat sdfd = new SimpleDateFormat("dd/MM");
                        //query the user history database from start date until end date
                        FirebaseManager.getInstance().getUserHistoryRefrence().child(FirebaseManager.getInstance().getMAuth().getUid()).orderByKey().startAt(first+"").endAt(second+"").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                //clean linear layout from privies TextViews if not first query
                                if(reports_lil_logs.getChildCount() > 0){
                                    reports_lil_logs.removeAllViews();
                                }
                                //iterate over all snapshot data
                                for(DataSnapshot ds : snapshot.getChildren()) {
                                    //create list of objects of starting date, end date
                                    if(!ds.getKey().isEmpty() || !ds.getValue().toString().isEmpty()){
                                        ModelShift shift = new ModelShift(ds.getKey(),ds.getValue().toString());
                                        shifts.add(shift);
                                        TextView tv = new TextView(view.getContext());
                                        tv.setTextSize(16);
                                        tv.setText(sdfd.format(shift.getStart()) + ": " + sdf.format(shift.getStart()) + " - " + sdf.format(shift.getEnds()) + "  " + shift.getTotal());
                                        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                        reports_lil_logs.addView(tv);
                                    }
                                }
                                //formatting the total hours to string format with starting 0 if the time is lower then 10
                                String HoursStr = ModelShift.getTotalHours() < 10 ? "0"+ModelShift.getTotalHours() : ModelShift.getTotalHours()+"";
                                String MinutesStr = ModelShift.getTotalMinutes() < 10 ? "0"+ModelShift.getTotalMinutes() : ModelShift.getTotalMinutes()+"";
                                reports_tev_total.setText("Total: " + HoursStr+":"+MinutesStr);
                                ModelShift.resetTotal();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                TextView tv = new TextView(view.getContext());
                                tv.setTextSize(16);
                                tv.setTextColor(getResources().getColor(R.color.error));
                                tv.setText("an error has occurred: "+error);
                                tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                reports_lil_logs.addView(tv);
                            }
                        });

                    }
                });
            }
        });
    }

    private void findViews(View view) {
        this.reports_btn_pick_date = view.findViewById(R.id.reports_btn_pick_date);
        this.reports_lil_logs = view.findViewById(R.id.reports_lil_logs);
        this.reports_btn_pick_date = view.findViewById(R.id.reports_btn_pick_date);
        this.reports_tev_total = view.findViewById(R.id.reports_tev_total);
    }
}