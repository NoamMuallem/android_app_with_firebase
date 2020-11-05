package com.example.firebaseapp;

import android.Manifest;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.jar.Pack200;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storageReference;
    //path here user profile picture and cover photo will be stored
    String storagePath = "Users_Profile_And_Cover_Images/";

    //views
    ImageView profile_imv_avatar, profile_imv_cover_photo;
    TextView profile_lbl_user_name, profile_lbl_user_email, profile_lbl_user_phone;
    FloatingActionButton profile_fab_edit_profile;

    //progress dialog
    ProgressDialog pd;

    //permission constance
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];
    //uri for picked image
    private Uri image_uri;
    //to indicate change in profile picture or cover photo so we can use one handler for both
    String profileOrCoverPhoto;

    //required public empty constructor
    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseInit();
        findViews(view);
        initViews();
        updateViewsWithProfileData();
        initPermissionsArray();
        return view;
    }

    //********************************initialization
    private void initPermissionsArray() {
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    private void findViews(View view) {
        profile_imv_avatar = view.findViewById(R.id.profile_imv_avatar);
        profile_lbl_user_name = view.findViewById(R.id.profile_lbl_user_name);
        profile_lbl_user_email = view.findViewById(R.id.profile_lbl_user_email);
        profile_lbl_user_phone = view.findViewById(R.id.profile_lbl_user_phone);
        profile_imv_cover_photo = view.findViewById(R.id.profile_imv_cover_photo);
        profile_fab_edit_profile = view.findViewById(R.id.profile_fab_edit_profile);
        pd = new ProgressDialog(getActivity());
    }

    private void initViews() {
        profile_fab_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
    }

    private void firebaseInit() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        //where in bucket users are stored
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();//firebase storage reference
    }

    //fetching info of current user by id
    private void updateViewsWithProfileData() {
        //using orderByChild query to get user that have uid that machs the current user uid
        Query query = databaseReference.orderByChild("uid").equalTo(firebaseUser.getUid());
        query.addValueEventListener(new ValueEventListener() {
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
    }

    //*********************************permissions
    //check if storage permissions is enabled or not
    private boolean checkStoragePermissions(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    //request runtime storage permission
    private void requestStoragePermission(){
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    //check if storage permissions is enabled or not
    private boolean checkCameraPermissions(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //request runtime storage permission
    private void requestCameraPermission(){
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
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
                    databaseReference.child(firebaseUser.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                        if (!checkCameraPermissions()) {
                            requestCameraPermission();
                            Log.d("pttt", "premission dinaied to take a photo");
                        } else {
                            Log.d("pttt", "premission granted to take a photo");
                            pickFromCamera();
                        }
                    }
                    break;
                    case 1: {
                        //Choose from gallery
                        if (!checkStoragePermissions()) {
                            requestStoragePermission();
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
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //image picked from gallery - get uri of image
                image_uri = data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //image picked from camera - get uri of image
                uploadProfileCoverPhoto(image_uri);
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
        String imagePathAndName = storagePath + "_" + profileOrCoverPhoto + "_" + firebaseUser.getUid();
        //creating new cluster in storage with the specified path
        StorageReference storageReference2nd = storageReference.child(imagePathAndName);
        //saving the image on to it
        storageReference2nd.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                    databaseReference.child(firebaseUser.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
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
            case CAMERA_REQUEST_CODE:{
                //TODO:fix permission issiues
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
            case STORAGE_REQUEST_CODE:{
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
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    //pick image from gallery
    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);
    }
}