package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.TaskStackBuilder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class ActivityRegister extends AppCompatActivity {

    //views
    EditText register_edt_email, register_edt_password;
    Button register_btn_submit;
    TextView register_lbl_haveAccount;

    //progressbar toto display while registering a user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //action bar and its action
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("create a new account");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        register_edt_email = findViewById(R.id.register_edt_email);
        register_edt_password = findViewById(R.id.register_edt_password);
        register_btn_submit = findViewById(R.id.register_btn_submit);
        register_lbl_haveAccount = findViewById(R.id.register_lbl_haveAccount);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering new user...");

        //initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        //handle register button click
        register_btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email password
                String email = register_edt_email.getText().toString().trim();
                String password = register_edt_password.getText().toString().trim();
                //validate
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus on edit email text
                    register_edt_email.setError("Invalid Email Address");
                    register_edt_email.setFocusable(true);
                }else if(password.length()<6){
                    //set error and focus on password
                    register_edt_password.setError("Password length must be greater then 6");
                    register_edt_password.setFocusable(true);
                }else{
                    registerUser(email, password);
                }
            }
        });

        //handle click on "have an account already"
        register_lbl_haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityRegister.this, ActivityLogin.class));
                finish();
            }
        });
    }

    private void registerUser(String email, String password) {
        //email and password are valis show progressDialog and start register user
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and tart register activity
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUserDataToDatabase(user);
                            Toast.makeText(ActivityRegister.this,"Registered...\n" + user.getEmail(),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ActivityRegister.this, ActivityDashboard.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(ActivityRegister.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener(){
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progress dialog and get and show the error massage
                progressDialog.dismiss();
                Toast.makeText(ActivityRegister.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserDataToDatabase(FirebaseUser user) {
        //get user email and uid from auth
        String email = user.getEmail();
        String uid = user.getUid();
        //when user register tore user info in firebase realtime database too
        //sing HashMap
        HashMap <Object, String> hashMap = new HashMap<>();
        //put info in hash map
        hashMap.put("email", email);
        hashMap.put("uid", uid);
        hashMap.put("name", ""); //will add later (e.g. edit profile)
        hashMap.put("phone", ""); //will add later (e.g. edit profile)
        hashMap.put("image", ""); //will add later (e.g. edit profile)
        hashMap.put("cover", ""); //will add later (e.g. edit profile)
        //firebase database instance
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //path to store user data named "Users"
        DatabaseReference reference = database.getReference("Users");
        //put data within hashMap in database
        reference.child(uid).setValue(hashMap);
    }

    /**
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar.
     *
     * <p>If a parent was specified in the manifest for this activity or an activity-alias to it,
     * default Up navigation will be handled automatically. See
     * {@link #getSupportParentActivityIntent()} for how to specify the parent. If any activity
     * along the parent chain requires extra Intent arguments, the Activity subclass
     * should override the method {@link #onPrepareSupportNavigateUpTaskStack(TaskStackBuilder)}
     * to supply those arguments.</p>
     *
     * <p>See <a href="{@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html">Tasks and
     * Back Stack</a> from the developer guide and
     * <a href="{@docRoot}design/patterns/navigation.html">Navigation</a> from the design guide
     * for more information about navigating within your app.</p>
     *
     * <p>See the {@link TaskStackBuilder} class and the Activity methods
     * {@link #getSupportParentActivityIntent()}, {@link #supportShouldUpRecreateTask(Intent)}, and
     * {@link #supportNavigateUpTo(Intent)} for help implementing custom Up navigation.</p>
     *
     * @return true if Up navigation completed successfully and this Activity was finished,
     * false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to privies activity
        return super.onSupportNavigateUp();
    }
}