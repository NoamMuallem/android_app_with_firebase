package com.example.firebaseapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.Activitys.ActivityChat;
import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder> {

    Context context;
    List<ModelUser> userList;

    //constructor


    public AdapterUser(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String uid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage ();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();

        //setDate
        holder.row_user_lbl_name.setText(userName);
        holder.row_user_lbl_email.setText(userEmail);
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_image).into(holder.row_user_civ_avatar);
        }catch (Exception e){

        }
        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click on user will start a chat - start activity by putting
                //receiver uid, use that uid to identify the user we are chatting with
                Intent chatIntent = new Intent(context, ActivityChat.class);
                chatIntent.putExtra(ActivityChat.RECEIVER_UID, uid);
                context.startActivity(chatIntent);
            }
        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view Holder class
    class MyHolder extends RecyclerView.ViewHolder{

        ImageView row_user_civ_avatar;
        TextView row_user_lbl_name, row_user_lbl_email;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            row_user_civ_avatar = itemView.findViewById(R.id.row_user_civ_avatar);
            row_user_lbl_name = itemView.findViewById(R.id.row_user_lbl_name);
            row_user_lbl_email = itemView.findViewById(R.id.row_user_lbl_email);

        }
    }
}
