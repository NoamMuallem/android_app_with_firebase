package com.example.firebaseapp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.R;
import com.example.firebaseapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder>{

    //constance so we can differ sender and receiver msg
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    //firebase
    FirebaseUser fUser;

    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        String msg = chatList.get(position).getMsg();
        String timestamp = chatList.get(position).getTimestamp();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

        //set date
        holder.chat_lbl_msg.setText(msg);
        holder.chat_lbl_date.setText(dateTime);
        try{
            Picasso.get().load(imageUrl).into(holder.chat_imv_receiver_avatar);
        }catch(Exception e){

        }

        //set is seen
        if(position == chatList.size()-1){
            if(chatList.get(position).isSeen()){
                holder.chat_lbl_is_seen.setText("Seen");
            }else{
                holder.chat_lbl_is_seen.setText("Delivered");
            }
        }else{
            holder.chat_lbl_is_seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }


    @Override
    public int getItemViewType(int position) {
        //get currently sign in user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{
        //views
        ImageView chat_imv_receiver_avatar;
        TextView chat_lbl_msg, chat_lbl_date, chat_lbl_is_seen;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            //find views
            chat_imv_receiver_avatar = itemView.findViewById(R.id.chat_imv_receiver_avatar);
            chat_lbl_msg = itemView.findViewById(R.id.chat_lbl_msg);
            chat_lbl_date = itemView.findViewById(R.id.chat_lbl_date);
            chat_lbl_is_seen = itemView.findViewById(R.id.chat_lbl_is_seen);
        }
    }
}
