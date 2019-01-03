package com.ads2dios.adrieladtan.letstalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    TextView catNameTV;
    TextView questionTV;
    TextView catDetailsTV;
    ImageButton prevButt;
    ImageButton nextButt;

    EditText messageET;
    ImageButton sendButt;
    ArrayList<MyMessage> chatAL = new ArrayList<>();
    ListView chatLV;
    ChatAdapter chatAdapter;

    String chatroomID;
    String currentUserID;
    String category;
    Integer currentIndex;
    ArrayList<Integer> questionOrder;

    DatabaseReference mDatabase;
    DatabaseReference chatroomDB;

    private InterstitialAd mInterstitialAd;
    int adCount;
    long adTimer;

    boolean hasDeleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.popup_admob_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
        adCount = 0;
        adTimer = System.currentTimeMillis();

        Intent intent = getIntent();
        chatroomID = intent.getStringExtra(MatchmakerActivity.INTENT_CHATROOOM_ID);
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        category = intent.getStringExtra(MainActivity.CATEGORY_NAME);
        questionOrder = intent.getIntegerArrayListExtra(MatchmakerActivity.INTENT_QUESTION_ORDER);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        chatroomDB = mDatabase.child("Chatrooms").child(chatroomID);

        catNameTV = findViewById(R.id.catNameTV);
        catNameTV.setText(category);
        catDetailsTV = findViewById(R.id.catDetailsTV);
        catDetailsTV.setText(intent.getStringExtra(MainActivity.CATEGORY_DETAILS));
        questionTV = findViewById(R.id.questionTV);

        messageET = findViewById(R.id.messageET);
        sendButt = findViewById(R.id.sendButt);
        chatLV = findViewById(R.id.chat_lv);
        chatAdapter = new ChatAdapter(ChatActivity.this, R.layout.chat_lv_item, chatAL);
        chatLV.setAdapter(chatAdapter);
        sendButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send();
            }
        });
        messageET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    send();
                }
                return false;
            }
        });
        // listen for new messages
        chatroomDB.child("messages").child("sent").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // finish();
                    return;
                }
                for (DataSnapshot snap : dataSnapshot.getChildren()){
                    if (!snap.child("senderID").getValue(String.class).equals(currentUserID)){
                        MyMessage newMessage = snap.getValue(MyMessage.class);
                        // chatroomDB.child("messages").child("received").child(newMessage.getId()).setValue(newMessage);
                        chatroomDB.child("messages").child("sent").child(newMessage.getId()).removeValue();
                        updateLV(newMessage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        nextButt = findViewById(R.id.nextButt);
        nextButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentIndex += 1;
                if (currentIndex >= questionOrder.size()) currentIndex = 0;
                chatroomDB.child("currentIndex").setValue(currentIndex);
            }
        });

        prevButt = findViewById(R.id.prevButt);
        prevButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentIndex -= 1;
                if (currentIndex < 0) currentIndex = questionOrder.size()-1;
                chatroomDB.child("currentIndex").setValue(currentIndex);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        // listening if both users are still present
        chatroomDB.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    //finish();
                    return;
                }
                for (DataSnapshot snap : dataSnapshot.getChildren()){
                    if (snap.getValue(String.class).equals("offline") && !hasDeleted){
                        // the other guy quit so let's destroy the chatroom and quit too
                        chatroomDB.removeValue();
                        hasDeleted = true;
                        finish();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // listening if current index has changed
        chatroomDB.child("currentIndex").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null) {
                    // finish();
                    return;
                }
                currentIndex = dataSnapshot.getValue(Integer.class);
                updateQuestion();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateQuestion(){
        String questionString = String.valueOf(questionOrder.get(currentIndex));
        if (questionString.length()==1) questionString = "0" + questionString;
        questionString = category + questionString;
        mDatabase.child("Database").child("allQns").child(questionString).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                questionTV.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        adCount += 1;
        if (mInterstitialAd.isLoaded() && (adCount>=10 || (adCount>=5 && (System.currentTimeMillis() - adTimer)>60000))){
            //every 5th count, if ad is loaded and 1 min (60s * 1000) has elapsed, ad shows. Or every 10 if u spam.
            mInterstitialAd.show();
            adTimer = System.currentTimeMillis();
            adCount = 0;
        }
    }

    private void send(){
        String messageString = messageET.getText().toString();
        messageString = messageString.trim();
        if (messageString.length()>0) {
            String id = chatroomDB.child("messages").child("sent").push().getKey();
            MyMessage msg = new MyMessage(id, currentUserID, messageString);
            messageET.setText("");
            chatroomDB.child("messages").child("sent").child(id).setValue(msg);
            updateLV(msg);
        }
    }

    private void updateLV(MyMessage msg){
        chatAL.add(msg);
//        chatAdapter.refreshData(chatAL);
        chatAdapter.notifyDataSetChanged();
        // chatLV.setAdapter(chatAdapter);
        // restore index and position
        chatLV.setSelectionFromTop(chatAL.size()-1, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!hasDeleted) chatroomDB.child("users").child(currentUserID).setValue("offline");
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
            .setTitle("Really Exit?")
            .setMessage("Are you sure you want to exit?")
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface arg0, int arg1) {
                    ChatActivity.super.onBackPressed();
                }
            }).create().show();
    }

    public class ChatAdapter extends ArrayAdapter<MyMessage> {
        private int mResource;
        private ArrayList<MyMessage> myMessages;

        public ChatAdapter(Context context, int resource, ArrayList<MyMessage> messagesAL){
            super(context, resource, messagesAL);
            this.mResource = resource;
            this.myMessages = messagesAL;
        }

        @Override
        public View getView(final int position, View row, ViewGroup parent){
            if (row==null){
                row = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
            }

            MyMessage msg = myMessages.get(position);
            TextView receiveTV = row.findViewById(R.id.receive_chat_bubble);
            TextView sendTV = row.findViewById(R.id.send_chat_bubble);

            if (msg.getSenderID().equals(currentUserID)){
                receiveTV.setVisibility(View.GONE);
                sendTV.setVisibility(View.VISIBLE);
                sendTV.setText(msg.getMessageString());
            }
            else{
                sendTV.setVisibility(View.GONE);
                receiveTV.setVisibility(View.VISIBLE);
                receiveTV.setText(msg.getMessageString());
            }

//            row.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });

            return row;
        }
    }
}
