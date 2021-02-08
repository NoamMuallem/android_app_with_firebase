package com.example.firebaseapp.Activitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.firebaseapp.R;
import com.example.firebaseapp.adapters.AdapterChat;
import com.example.firebaseapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ActivityChat extends AppCompatActivity {

    //constants for extras
    public static final String RECEIVER_UID = "RECEIVER_UID";

    //views
    private Toolbar chat_toolbar;
    private RecyclerView chat_recycler_view;
    private ImageView char_imv_receiver_avatar;
    private TextView chat_lbl_receiver_name, chat_lbl_receiver_status;
    private EditText chat_edt_msg;
    private ImageButton chat_btn_send_msg;

    //firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser fUser; //will contain current user
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userDBRef; //use to indicate msg is seen
    private ValueEventListener seenListener; //for checking is user seen msg

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    //receiver uid
    private String receiverUid;
    private String myUid;
    private String receiverImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        findViews();
        init();
    }

    private void init() {
        //setup vies
        setSupportActionBar(chat_toolbar);
        chat_toolbar.setTitle("");
        //set up firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        fUser = firebaseAuth.getCurrentUser();
        userDBRef = firebaseDatabase.getReference("Users");
        //get uid of user and receiver from firebase and extras
        myUid = fUser.getUid();
        receiverUid = getIntent().getStringExtra(RECEIVER_UID);
        //search the db for the info by uid
        Query userQuery = userDBRef.orderByChild("uid").equalTo(receiverUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required info is received
                //the snapshot is empty when data is not received yet
                for(DataSnapshot ds : snapshot.getChildren()){
                    //get data
                    String name = ""+ds.child("name").getValue();
                    receiverImage = ""+ds.child("image").getValue();
                    //get value of online status
                    String onlineStatus = ""+ds.child("onlineStatus").getValue();

                    //set data
                    if(onlineStatus.equals("online")){
                        chat_lbl_receiver_status.setText(onlineStatus);
                    }else{
                        //convert timestamp to proper time date
                        //convert timestamp to dd/mm/yyyy hh:mm am/pm
                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(Long.parseLong(onlineStatus));
                        String dateTime = DateFormat.format("dd/mm/yyyy hh:mm aa", cal).toString();
                        chat_lbl_receiver_status.setText("Last seen at: " + dateTime);
                    }
                    chat_lbl_receiver_name.setText(name);
                    try{
                        //image received set it to image view in toolbar
                        Picasso.get().load(receiverImage).placeholder(R.drawable.ic_default_image_white).into(char_imv_receiver_avatar);
                    }catch(Exception e){
                        //there was an exception - set default image
                        Picasso.get().load(R.drawable.ic_default_image_white).into(char_imv_receiver_avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //click button to sent msg
        chat_btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get text from edit text
                String msg = chat_edt_msg.getText().toString().trim();
                //check if text is empty or not
                if(TextUtils.isEmpty(msg)){
                    //text empty
                    Toast.makeText(ActivityChat.this, "cannot send an empty message...", Toast.LENGTH_SHORT).show();
                }else{
                    //text is not empty
                    sendMsg(msg);
                }
            }
        });

        readMsgs();

        seenMsg();

        //Layout(Linear Layout) for recycler view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recycler properties
        chat_recycler_view.setHasFixedSize(true);
        chat_recycler_view.setLayoutManager(linearLayoutManager);

    }

    private void seenMsg() {
        userDBRef = firebaseDatabase.getInstance().getReference("Chats");
        seenListener = userDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    //if the user is the receiver and he is here - he seeing the msg
                    if(chat.getReceiver().equals(myUid) && chat.getSender().equals(receiverUid)){
                        HashMap<String,Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        //set offline with last seen timestamp
        checkOnlineStatus(timestamp);

        //is the user is not currently in the app, don't update
        //seen on sent msg
        userDBRef.removeEventListener(seenListener);
    }

    private void readMsgs() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot ds : snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if((chat.getReceiver().equals(myUid) && chat.getSender().equals(receiverUid)) ||
                            chat.getReceiver().equals(receiverUid) && chat.getSender().equals(myUid)){
                        //add to chat list all chats where user and other user take part anyone
                        //of theme can be the sender or the receiver depends who start the chat
                        chatList.add(chat);
                    }

                    //adapter
                    adapterChat = new AdapterChat(ActivityChat.this, chatList, receiverImage);
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recycler view
                    chat_recycler_view.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMsg(String msg) {
        //new node in db, "Chats" - will contain every chat msg in app
        //will have 3 keys - sender, receiver, msg
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", receiverUid);
        hashMap.put("msg",msg);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);
        databaseReference.child("Chats").push().setValue(hashMap);

        //reset editing after sending text
        chat_edt_msg.setText("");
    }

    private void findViews() {
        chat_toolbar = findViewById(R.id.chat_toolbar);
        chat_recycler_view = findViewById(R.id.chat_recycler_view);
        char_imv_receiver_avatar = findViewById(R.id.char_imv_receiver_avatar);
        chat_lbl_receiver_name = findViewById(R.id.chat_lbl_receiver_name);
        chat_lbl_receiver_status = findViewById(R.id.chat_lbl_receiver_status);
        chat_edt_msg = findViewById(R.id.chat_edt_msg);
        chat_btn_send_msg = findViewById(R.id.chat_btn_send_msg);
        chat_lbl_receiver_status = findViewById(R.id.chat_lbl_receiver_status);
    }

    private void checkOnlineStatus(String status){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus", status);
        //update value of online in current user
        dbRef.updateChildren(hashMap);
    }

    private void checkUserStatus(){
        //get current user
        if(fUser != null){
            //user sign in - stay here
        }else{
            //user is not sign in, go to main activity
            startActivity(new Intent(this, ActivityMain.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set user online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide search view as we don't need it here
        menu.findItem(R.id.menue_item_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

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

    @Override
    protected void onResume() {
        //set user online
        checkOnlineStatus("online");
        super.onResume();
    }
}