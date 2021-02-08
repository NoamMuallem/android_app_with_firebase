package com.example.firebaseapp.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.firebaseapp.Fragments.FragmentChatList;
import com.example.firebaseapp.Fragments.FragmentHome;
import com.example.firebaseapp.Fragments.FragmentProfile;
import com.example.firebaseapp.Fragments.FragmentUsers;
import com.example.firebaseapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActivityDashboard extends AppCompatActivity {

    //views


    //firebase auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //action bar and its action
        actionBar = getSupportActionBar();
        actionBar.setTitle("profile");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //bottom navigation
        BottomNavigationView navigationView = findViewById(R.id.dashboard_bom_navigation);

        //home fragment transaction (default fragment)
        actionBar.setTitle("Home");
        FragmentHome fragmentHome = new FragmentHome();
        FragmentTransaction homeFragmentTransaction = getSupportFragmentManager().beginTransaction();
        homeFragmentTransaction.replace(R.id.dashboard_frl_content, fragmentHome,"");
        homeFragmentTransaction.commit();

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //handle items clicks
                switch(item.getItemId()){
                    case R.id.navigation_home:
                        //home fragment transaction
                        actionBar.setTitle("Home");
                        FragmentHome fragmentHome = new FragmentHome();
                        FragmentTransaction homeFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        homeFragmentTransaction.replace(R.id.dashboard_frl_content, fragmentHome,"");
                        homeFragmentTransaction.commit();
                        return true;
                    case R.id.navigation_profile:
                        //profile fragment transaction
                        actionBar.setTitle("Profile");
                        FragmentProfile fragmentProfile = new FragmentProfile();
                        FragmentTransaction profileFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        profileFragmentTransaction.replace(R.id.dashboard_frl_content, fragmentProfile,"");
                        profileFragmentTransaction.commit();
                        return true;
                    case R.id.navigation_users:
                        //users fragment transaction
                        actionBar.setTitle("Users");
                        FragmentUsers fragmentUsers = new FragmentUsers();
                        FragmentTransaction usersFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        usersFragmentTransaction.replace(R.id.dashboard_frl_content, fragmentUsers,"");
                        usersFragmentTransaction.commit();
                        return true;
                    case R.id.navigation_chat:
                        //chat fragment transaction
                        actionBar.setTitle("Chat");
                        FragmentChatList fragmentChat = new FragmentChatList();
                        FragmentTransaction chatFragmentTransaction = getSupportFragmentManager().beginTransaction();
                        chatFragmentTransaction.replace(R.id.dashboard_frl_content, fragmentChat,"");
                        chatFragmentTransaction.commit();
                        return true;
                }
                return false;
            }
        });

    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //user sign in - stay here


        }else{
            //user is not sign in, go to main activity
            startActivity(new Intent(ActivityDashboard.this, ActivityMain.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();

    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}