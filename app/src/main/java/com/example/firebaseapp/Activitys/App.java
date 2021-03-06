package com.example.firebaseapp.Activitys;

import android.app.Application;

import com.example.firebaseapp.utils.FirebaseManager;
import com.example.firebaseapp.utils.PermissionManager;
import com.example.firebaseapp.utils.SP;


//this class will be instantiated when app starts
//it will initialize all utils and pass context to them
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SP.init(this);
        PermissionManager.init(this);
        FirebaseManager.init(this);
    }

}