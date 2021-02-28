package com.example.firebaseapp.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.R;
import com.example.firebaseapp.utils.FirebaseManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ActivityLogin extends AppCompatActivity {

    //views
    EditText login_edt_email, login_edt_password;
    Button login_btn_submit;
    TextView login_lbl_noAccount, login_lbl_password_recovery;
    SignInButton login_btn_google_login;

    //progress dialog for loading
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //action bar and its action
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Sign In");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        login_edt_email = findViewById(R.id.login_edt_email);
        login_edt_password = findViewById(R.id.login_edt_password);
        login_btn_submit = findViewById(R.id.login_btn_submit);
        login_lbl_noAccount = findViewById(R.id.login_lbl_noAccount);
        login_lbl_password_recovery = findViewById(R.id.login_lbl_password_recovery);
        login_btn_google_login = findViewById(R.id.login_btn_google_login);

        //init progress dialog
        pd = new ProgressDialog(this);

        //init recover password click handler
        login_lbl_password_recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //handle google login button click
        login_btn_google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin google sign in
                Intent signInIntent = FirebaseManager.getInstance().getmGoogleSignInClient().getSignInIntent();
                startActivityForResult(signInIntent, FirebaseManager.getInstance().getRcSignIn());
            }
        });

        //handle login button click
        login_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //data inputs
                String email = login_edt_email.getText().toString().trim();
                String password = login_edt_password.getText().toString().trim();
                //validation
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //email adress not valid
                    login_edt_email.setError("Email adress not valid");
                    login_edt_email.setFocusable(true);
                }else if(password.length()<6){
                    //password to short
                    login_edt_password.setError("Password to short");
                    login_edt_password.setFocusable(true);
                }else{
                    loginUser(email, password);
                }
            }
        });

        //don't have an account TextView click handler
        login_lbl_noAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this, ActivityRegister.class));
                finish();
            }
        });
    }

    private void showRecoverPasswordDialog() {
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //set layout linear
        LinearLayout linearLayout = new LinearLayout(this);

        //views to set in dialog
        EditText dialog_edt_email = new EditText(this);
        dialog_edt_email.setHint("Email");
        dialog_edt_email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        dialog_edt_email.setMinEms(16);

        //add dialog_edt_email to linear layout
        linearLayout.addView(dialog_edt_email);
        linearLayout.setPadding(10,10,10,10);

        //add linear layout to dialog
        builder.setView(linearLayout);

        //button recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input data
                String email = dialog_edt_email.getText().toString().trim();
                if(Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    beginRecovery(email);
                }
            }
        });

        //button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        //show progress dialog
        pd.setMessage("Sending email...");
        pd.show();
        FirebaseManager.getInstance().getMAuth().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(ActivityLogin.this,"Email sent",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(ActivityLogin.this,"Failed to send Email",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //get and show proper error massage
                Toast.makeText(ActivityLogin.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String password) {
        //show progress dialog
        pd.setMessage("Logging In...");
        pd.show();
        FirebaseManager.getInstance().getMAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismiss progress dialog
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = FirebaseManager.getInstance().getMAuth().getCurrentUser();
                            startActivity(new Intent(ActivityLogin.this, ActivityDashboard.class));
                            finish();
                        } else {
                            //dismiss progress dialog
                            pd.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(ActivityLogin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismiss progress dialog
                pd.dismiss();
                //error, get error and display it
                Toast.makeText(ActivityLogin.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseManager.getInstance().getMAuth().getCurrentUser();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to privies activity
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == FirebaseManager.getInstance().getRcSignIn()) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseManager.getInstance().getMAuth().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = FirebaseManager.getInstance().getMAuth().getCurrentUser();
                            //if signning in for the first time - save user data
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                saveUserDataToDatabase(user);
                            }
                            //go to profile account
                            startActivity(new Intent(ActivityLogin.this, ActivityDashboard.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(ActivityLogin.this,"Login Failed...",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show error
                Toast.makeText(ActivityLogin.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDataToDatabase(FirebaseUser user) {
        //get user email and uid from auth
        String email = user.getEmail();
        String uid = user.getUid();
        //when user register tore user info in firebase realtime database too
        //sing HashMap
        HashMap<Object, String> hashMap = new HashMap<>();
        //put info in hash map
        hashMap.put("email", email);
        hashMap.put("uid", uid);
        hashMap.put("name", ""); //will add later (e.g. edit profile)
        hashMap.put("phone", user.getPhoneNumber()+""); //will add later (e.g. edit profile)
        hashMap.put("image", user.getPhotoUrl()+""); //will add later (e.g. edit profile)
        hashMap.put("cover", ""); //will add later (e.g. edit profile)
        hashMap.put("openClock", ""); //will add later (e.g. edit profile)

        //put data within hashMap in database
        FirebaseManager.getInstance().getUsersReference().child(uid).setValue(hashMap);
    }
}