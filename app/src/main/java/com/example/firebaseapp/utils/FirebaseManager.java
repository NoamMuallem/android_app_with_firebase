package com.example.firebaseapp.utils;

import android.content.Context;

import com.example.firebaseapp.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseManager {

    private static FirebaseManager instance;
    //firebase auth
    private FirebaseAuth mAuth;
    //firebase realtime database
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference userHistoryRefrence;
    //sign in with google
    private GoogleSignInClient mGoogleSignInClient;
    //request code for google sign in activity
    private static final int RC_SIGN_IN = 100;
    private GoogleSignInOptions gso;
    private Context context; //context for starting activity for google sign in

    //storage
    private StorageReference storageReference;
    private static final String storagePath = "Users_Profile_And_Cover_Images/";

    ////////////////////singleton
    public static FirebaseManager getInstance() {
        return instance;
    }

    private FirebaseManager(Context context) {
        //if we tak context a reference to context in some activity still exists
        //so to allow java garbage collector to delete unheeded activities
        //we take the application context from the context passed to here
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.database = FirebaseDatabase.getInstance();
        this.usersReference = this.database.getReference("Users");
        this.userHistoryRefrence = this.database.getReference("UserHistory");
        this.gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(this.context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        this.mGoogleSignInClient = GoogleSignIn.getClient(this.context, this.gso);
        this.storageReference = FirebaseStorage.getInstance().getReference();
    }

    public static void init(Context context) {
        //to allow init only for the first time - singleton design pattern
        //not because there is one instance but because we wont to be able to
        //instantiate only one and to use only that one instance
        if (instance == null) {
            //if we tak context a reference to context in some activity still exists
            //so to allow java garbage collector to delete unheeded activities
            //we take the application context from the context passed to here
            instance = new FirebaseManager(context);
        }
    }

    public FirebaseAuth getMAuth(){
        return this.mAuth;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public DatabaseReference getUsersReference() {
        return this.usersReference;
    }

    public DatabaseReference getUserHistoryRefrence() {
        return this.userHistoryRefrence;
    }

    public GoogleSignInClient getmGoogleSignInClient() {
        return mGoogleSignInClient;
    }

    public static int getRcSignIn() {
        return RC_SIGN_IN;
    }

    public static String getStoragePath() {
        return storagePath;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }
}
