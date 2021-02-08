package com.example.firebaseapp.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.firebaseapp.Activitys.ActivityMain;
import com.example.firebaseapp.R;
import com.example.firebaseapp.adapters.AdapterUser;
import com.example.firebaseapp.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentUsers extends Fragment {

    //Firebase auth
    FirebaseAuth firebaseAuth;

    RecyclerView users_rv;
    AdapterUser adapterUser;
    List<ModelUser> userList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        findViews(view);
        init();
        //to show menu option in fragment
        setHasOptionsMenu(true);
        return view;
    }

    private void init() {
        users_rv.setHasFixedSize(true);
        users_rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        userList = new ArrayList<ModelUser>();
        firebaseAuth = FirebaseAuth.getInstance();
        getAllUsers();
    }

    private void getAllUsers() {
        //get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database names "Users" containing user info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //get all user except for currently sign in user
                    if(!modelUser.getUid().equals(fUser.getUid())){
                        userList.add(modelUser);
                    }

                    //adapter
                    adapterUser = new AdapterUser(getActivity(), userList);

                    //set adapter to recycler view
                    users_rv.setAdapter(adapterUser);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void findViews(View view) {
        users_rv = view.findViewById(R.id.users_rv);
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
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
        Log.d("pttt", "in on create optins menu");
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);
        Log.d("pttt", "1");
        //search view
        MenuItem item = menu.findItem(R.id.menue_item_search);
        Log.d("pttt", "1.5");
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        Log.d("pttt", "2");
        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String str) {
                Log.d("pttt", "3");
                //called when user press search in keyboard
                //if search query is not empty then search
                if(!TextUtils.isEmpty(str.trim())){
                    //search text contains newText
                    searchUser(str);
                }else{
                    //search text empty - sow all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String str) {
                Log.d("pttt", "4");
                //called on every single latter key press
                //if search query is not empty then search
                if(!TextUtils.isEmpty(str.trim())){
                    //search text contains newText
                    searchUser(str);
                }else{
                    //search text empty - sow all users
                    getAllUsers();
                }
                return false;
            }
        });
        Log.d("pttt", "5");
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchUser(String query) {
        Log.d("pttt", "in search user");
        //get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database names "Users" containing user info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    //user is showed if name or email contains query

                    //get all searched users except for currently sign in user
                    if(!modelUser.getUid().equals(fUser.getUid())){
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }
                    }

                    //adapter
                    adapterUser = new AdapterUser(getActivity(), userList);

                    //refresh adaptor
                    adapterUser.notifyDataSetChanged();

                    //set adapter to recycler view
                    users_rv.setAdapter(adapterUser);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //handle menu items clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.menue_item_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}