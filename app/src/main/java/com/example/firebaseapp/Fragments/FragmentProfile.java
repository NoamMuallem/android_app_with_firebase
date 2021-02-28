package com.example.firebaseapp.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebaseapp.Activitys.ActivityMain;
import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelShift;
import com.example.firebaseapp.utils.FirebaseManager;
import com.example.firebaseapp.utils.PermissionManager;
import com.example.firebaseapp.utils.SP;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class FragmentProfile extends Fragment {
    //views
    ImageView profile_imv_avatar, profile_imv_cover_photo;
    TextView profile_lbl_user_name, profile_lbl_user_email, profile_lbl_user_phone,profile_txv_this_date, profile_txv_total_hours;
    FloatingActionButton profile_fab_edit_profile;
    LinearLayout profile_lil_shift_list;

    //progress dialog
    ProgressDialog pd;

    //to indicate change in profile picture or cover photo so we can use one handler for both
    private String profileOrCoverPhoto;

    //required public empty constructor
    public FragmentProfile() {
    }

    //today shift array
    ArrayList<ModelShift> shifts;

    //view for adding text views problematically for each entry for today
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        findViews(view);
        initViews();
        updateViewsWithProfileData();
        this.view = view;
        return view;
    }

    private void findViews(View view) {
        this.profile_imv_avatar = view.findViewById(R.id.profile_imv_avatar);
        this.profile_lbl_user_name = view.findViewById(R.id.profile_lbl_user_name);
        this.profile_lbl_user_email = view.findViewById(R.id.profile_lbl_user_email);
        this.profile_lbl_user_phone = view.findViewById(R.id.profile_lbl_user_phone);
        this.profile_imv_cover_photo = view.findViewById(R.id.profile_imv_cover_photo);
        this.profile_fab_edit_profile = view.findViewById(R.id.profile_fab_edit_profile);
        this.profile_txv_this_date = view.findViewById(R.id.profile_txv_this_date);
        this.profile_txv_total_hours = view.findViewById(R.id.profile_txv_total_hours);
        this.profile_lil_shift_list = view.findViewById(R.id.profile_lil_shift_list);
        this.pd = new ProgressDialog(getActivity());
    }

    private void initViews() {
        profile_fab_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        this.profile_txv_this_date.setText(new SimpleDateFormat("dd/MM/yy").format(Calendar.getInstance().getTime()));
    }

    //fetching info of current user and all day shifts by id
    private void updateViewsWithProfileData() {
        FirebaseManager.getInstance().getUsersReference().orderByChild("uid").equalTo(FirebaseManager.getInstance().getMAuth().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //checks until required data is back
                for(DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();
                    //saving to SP the check in value
                    SP.getInstance().putString("checkin",(String)ds.child("openClock").getValue().toString());
                    //set data
                    profile_lbl_user_name.setText(name);
                    profile_lbl_user_email.setText(email);
                    profile_lbl_user_phone.setText(phone);

                    //to set profile image
                    try{
                        //if image is received
                        Picasso.get().load(image).into(profile_imv_avatar);
                    }catch(Exception e){
                        //if there are any exceptions show default pic
                        Picasso.get().load(R.drawable.ic_default_image_white).into(profile_imv_avatar);
                    }

                    //for setting cover image
                    try{
                        //if cover image is received
                        Picasso.get().load(cover).into(profile_imv_cover_photo);
                    }catch(Exception e){
                        //if there are any exceptions show default pic
                        Picasso.get().load(R.drawable.ic_default_image_white).into(profile_imv_cover_photo);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //fetching all timestamp keys in "userHistory" that are bigger from last midnight
        //get last midnight timestamp
        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date d1 = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        FirebaseManager.getInstance().getUserHistoryRefrence().child(FirebaseManager.getInstance().getMAuth().getUid()).orderByKey().startAt(d1.getTime()+"").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shifts = new ArrayList<>();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    //create list of objects of starting date, end date
                    if(!ds.getKey().isEmpty() || !ds.getValue().toString().isEmpty()){
                        ModelShift shift = new ModelShift(ds.getKey(),ds.getValue().toString());
                        shifts.add(shift);
                        TextView tv = new TextView(view.getContext());
                        tv.setTextSize(16);
                        tv.setText(sdf.format(shift.getStart()) + " - " + sdf.format(shift.getEnds()) + ": " + shift.getTotal());
                        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        profile_lil_shift_list.addView(tv);
                    }
                }
                //formatting the total hours to string format with starting 0 if the time is lower then 10
                String HoursStr = ModelShift.getTotalHours() < 10 ? "0"+ModelShift.getTotalHours() : ModelShift.getTotalHours()+"";
                String MinutesStr = ModelShift.getTotalMinutes() < 10 ? "0"+ModelShift.getTotalMinutes() : ModelShift.getTotalMinutes()+"";
                String SecondsStr = ModelShift.getTotalSeconds() < 10 ? "0"+ModelShift.getTotalSeconds() : ModelShift.getTotalSeconds()+"";
                profile_txv_total_hours.setText("Total: " + HoursStr+":"+MinutesStr+":"+SecondsStr);
                //reset total counter
                ModelShift.resetTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //********************************profile editing
    //editing menu
    private void showEditProfileDialog() {
        //options to show in dialog
        String options[] = {"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Edit Profile");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialog items click
                switch(which){
                    case 0: {
                        //edit profile picture clicked
                        pd.setMessage("Updating Profile Image");
                        profileOrCoverPhoto = "image"; //indicate profile picture change
                        showImagePicDialog();
                    }
                    break;
                    case 1: {
                        //edit cover photo clicked
                        pd.setMessage("Updating Profile Cover Photo");
                        profileOrCoverPhoto = "cover"; //indicate cover photo change
                        showImagePicDialog();
                    }
                    break;
                    case 2: {
                        //edit profile name clicked
                        pd.setMessage("Updating Profile Name");
                        showNamePhoneUpdateDialog("Name");
                    }
                    break;
                    case 3: {
                        //edit phone clicked
                        pd.setMessage("Updating Profile Phone");
                        showNamePhoneUpdateDialog("Phone");
                    }
                    break;
                }
            }
        });
        //create dialog
        builder.create().show();
    }

    //updating text values menu
    private void showNamePhoneUpdateDialog(String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Update " + key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);
        linearLayout.addView(editText);
        alertDialog.setView(linearLayout);

        //add button in dialog
        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //validate that a user has entered something
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key.toLowerCase(), value);
                    FirebaseManager.getInstance().getUsersReference().child(FirebaseManager.getInstance().getMAuth().getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Updated...",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(getActivity(),"Please Enter "+key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //create and show dialog
        alertDialog.create().show();
    }

    //show dialog to pick an image from gallery or take a new photo
    private void showImagePicDialog() {
        //options to show in dialog
        String options[] = {"Camera","Gallery"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Image Source");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("pttt","entered");
                //handle dialog items click
                switch(which){
                    case 0: {
                        //Take a photo
                        if (!PermissionManager.getInstance().checkCameraPermissions()) {
                            PermissionManager.getInstance().requestCameraPermission(getActivity());
                            Log.d("pttt", "premission dinaied to take a photo");
                        } else {
                            Log.d("pttt", "premission granted to take a photo");
                            pickFromCamera();
                        }
                    }
                    break;
                    case 1: {
                        //Choose from gallery
                        if (!PermissionManager.getInstance().checkStoragePermissions()) {
                            PermissionManager.getInstance().requestStoragePermission(getActivity());
                            Log.d("pttt", "premission denaied to choose from storage");
                        } else {
                            Log.d("pttt", "premission granted to choose from storage");
                            pickFromGallery();
                        }
                    }
                    break;

                }
            }
        });
        //create dialog
        builder.create().show();
    }

    //this method will run after picking image from gallery or camera
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == PermissionManager.getInstance().getImagePickGalleryCode()){
                //image picked from gallery - get uri of image
                PermissionManager.getInstance().setImage_uri(data.getData());
                uploadProfileCoverPhoto(PermissionManager.getInstance().getImage_uri());
            }
            if(requestCode == PermissionManager.getInstance().getImagePickCameraCode()){
                //image picked from camera - get uri of image
                uploadProfileCoverPhoto(PermissionManager.getInstance().getImage_uri());
            }
        }
    }

    //one function to handle cover photo and profile picture
    private void uploadProfileCoverPhoto(Uri image_uri) {
        //show progress
        pd.show();

        //path and name of image that will be stored in firebase
        //examples for imagePathAndName:
        //Users_Profile_And_Cover_Images_image_052982309840
        //Users_Profile_And_Cover_Images_cover_052982309840
        String imagePathAndName = FirebaseManager.getInstance().getStoragePath() + "_" + profileOrCoverPhoto + "_" + FirebaseManager.getInstance().getMAuth().getUid();
        //creating new cluster in storage with the specified path
        FirebaseManager.getInstance().getStorageReference().child(imagePathAndName).putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image was uploaded to storage - now get url and store it in Users database
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                Uri uri = uriTask.getResult();

                //check if image was uploaded or not and that a url was received
                if(uriTask.isSuccessful()){
                    //image upload - add / update image in Users database
                    HashMap<String, Object> result = new HashMap<>();
                    //first parameter is profileOrCoverPhoto that can be "image" or "cover"
                    //which are keys in Users database and url of image will be saved in one of theme
                    //second parameter is the url string
                    result.put(profileOrCoverPhoto,uri.toString());
                    //the first reference that points to Users - get user that matches firebaseUser uid
                    FirebaseManager.getInstance().getUsersReference().child(FirebaseManager.getInstance().getMAuth().getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //url of image stored in Users realtime database successfully - dismiss progress bar
                            pd.dismiss();
                            Toast.makeText(getActivity(),"image updated...",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //error adding url to realtime database for user - dismiss progressbar
                            pd.dismiss();
                            Toast.makeText(getActivity(),"error updating image...",Toast.LENGTH_SHORT).show();

                        }
                    });

                }else{
                    //error in uploading to storage
                    pd.dismiss();
                    Toast.makeText(getActivity(),"error in image uploading",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //there was an error(s), get and show error, dismiss progress dialog
                pd.dismiss();
                Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }

    //this methods runs when permission dialog closes with granted or denial access
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int  i = 0 ; i < grantResults.length ; i++){
            Log.d("pttt",""+grantResults[i]);
        }
        switch(requestCode){
            case PermissionManager.CAMERA_REQUEST_CODE:{
                //picking from camera - check we have permissions
                if(grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        //permission granted
                        pickFromCamera();
                    }else{
                        //permission denied
                        Toast.makeText(getActivity(),"please enable camera & storage permissions",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case PermissionManager.STORAGE_REQUEST_CODE:{
                //picking from gallery - check we have permissions
                if(grantResults.length > 0){
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        //permission granted
                        pickFromGallery();
                    }else{
                        //permission denied
                        Toast.makeText(getActivity(),"please enable storage permissions",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    //intent for picking image from camera
    private void pickFromCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        PermissionManager.getInstance().setImage_uri(getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values));

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, PermissionManager.getInstance().getImage_uri());
        startActivityForResult(cameraIntent, PermissionManager.IMAGE_PICK_CAMERA_CODE);
    }

    //pick image from gallery
    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PermissionManager.IMAGE_PICK_GALLERY_CODE);
    }

    //to show menu option in fragment
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    //redirect user if he is here and not sign in
    private void checkUserStatus(){
        //get current user
        FirebaseUser user = FirebaseManager.getInstance().getMAuth().getCurrentUser();
        if(user != null){
            //user sign in - stay here
        }else{
            //user is not sign in, go to main activity
            startActivity(new Intent(getActivity(), ActivityMain.class));
            getActivity().finish();
        }
    }

    //inflate options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu items clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.menue_item_logout){
            FirebaseManager.getInstance().getMAuth().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}