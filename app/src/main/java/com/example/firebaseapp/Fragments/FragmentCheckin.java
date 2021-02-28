package com.example.firebaseapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.firebaseapp.Callbacks.OnDataReceiveCallback;
import com.example.firebaseapp.R;
import com.example.firebaseapp.utils.FirebaseManager;
import com.example.firebaseapp.utils.SP;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class FragmentCheckin extends Fragment implements OnDataReceiveCallback {

    private Button checkin_btn_checkin;
    private TextView checkin_txv_start_time, checkin_txv_timer;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if(!(SP.getInstance().getString("checkin","").equals(""))) {
                long millis = System.currentTimeMillis() - new Date(Long.parseLong(SP.getInstance().getString("checkin", ""))).getTime();
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                checkin_txv_timer.setText(String.format("%d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 500);
            }
        }
    };


    public FragmentCheckin() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static FragmentCheckin newInstance(String param1, String param2) {
        FragmentCheckin fragment = new FragmentCheckin();
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
        View view = inflater.inflate(R.layout.fragment_checkin, container, false);
        //listen for changes in start shift pass custom callback
        FirebaseManager.getInstance().getUsersReference().child(FirebaseManager.getInstance().getMAuth().getUid()).child("openClock").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                onDataReceived(snapshot.getValue()+"");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        findViews(view);
        initialListeners();
        return view;
    }

    private void initialListeners() {
        this.checkin_btn_checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date now = new Date();
                if(SP.getInstance().getString("checkin","").equals("")){
                    //set new time stamp in user profile on firebase
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("openClock",now.getTime());
                    FirebaseManager.getInstance().getUsersReference().child(FirebaseManager.getInstance().getMAuth().getUid()).updateChildren(hashMap);
                }else{
                    //add start timestamp and end timestamp to "userHistory in firebase"
                    //start timestamp : end timestamp
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(SP.getInstance().getString("checkin",""),now.getTime());
                    FirebaseManager.getInstance().getUserHistoryRefrence().child(FirebaseManager.getInstance().getMAuth().getUid()).updateChildren(hashMap);
                    //clear time stamp in user profile - openClock:""
                    HashMap<String, Object> hashMapForUserProfile = new HashMap<>();
                    hashMapForUserProfile.put("openClock","");
                    FirebaseManager.getInstance().getUsersReference().child(FirebaseManager.getInstance().getMAuth().getUid()).updateChildren(hashMapForUserProfile);
                    //set time passed string to ""
                    checkin_txv_timer.setText("");
                }
            }
        });
    }

    private void findViews(View view) {
        this.checkin_btn_checkin = view.findViewById(R.id.checkin_btn_checkin);
        this.checkin_txv_start_time = view.findViewById(R.id.checkin_txv_start_time);
        this.checkin_txv_timer = view.findViewById(R.id.checkin_txv_timer);
    }

    @Override
    public void onPause() {
        super.onPause();
        //if there is a starting timestamp remove timer
        if(!(SP.getInstance().getString("checkin","").equals(""))){
            this.timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //if there is a starting timestamp add timer
        this.timerHandler.postDelayed(this.timerRunnable, 0);
    }

    @Override
    public void onDataReceived(String date) {
        //received empty timestamp - did not started shift
        if(date.equals("")){
            this.checkin_txv_start_time.setText("");
            this.checkin_btn_checkin.setText("clock-in");
            //remove timer
            this.timerHandler.removeCallbacks(timerRunnable);
        }else{
            //received timestamp - started shift, display start date
            SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/YY HH:mm:ss");
            String strDate = sdfDate.format(new Date(Long.parseLong(date)));
            this.checkin_txv_start_time.setText(strDate);
            this.checkin_btn_checkin.setText("clock-out");
            //set timer
            this.timerHandler.postDelayed(this.timerRunnable, 0);
        }
        SP.getInstance().putString("checkin",date);
    }
}