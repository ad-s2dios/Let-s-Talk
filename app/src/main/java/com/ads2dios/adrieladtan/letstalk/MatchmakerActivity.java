package com.ads2dios.adrieladtan.letstalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MatchmakerActivity extends AppCompatActivity {

    public static final String INTENT_CHATROOOM_ID = "chatroomID";
    public static final String INTENT_QUESTION_ORDER = "questionOrder";

    public static final int REQUEST_CHAT = 3;

    TextView adviceTV;
    TextView catNameTV;
    TextView catDetailsTV;
    String category;
    String catDetails;

    DatabaseReference matchingDB;
    DatabaseReference mDatabase;
    String currentUserId;

    boolean didAdd;
    ArrayList<DatabaseReference> listenerReferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matchmaker);

        Intent intent = getIntent();
        category = intent.getStringExtra(MainActivity.CATEGORY_NAME);
        catDetails = intent.getStringExtra(MainActivity.CATEGORY_DETAILS);

        catNameTV = findViewById(R.id.catNameTV);
        catDetailsTV = findViewById(R.id.catDetailsTV);
        catNameTV.setText(category);
        catDetailsTV.setText(catDetails);
        adviceTV = findViewById(R.id.adviceTV);

        listenerReferences = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        matchingDB = mDatabase.child("Matchmaking").child(category);

        try {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        catch (NullPointerException e){
            print("Error");
            return;
        }

        listenerReferences.add(matchingDB);
        matchingDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Iterable<DataSnapshot> usernames = dataSnapshot.child("users").getChildren();

                matchingDB.child("Available").runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull final MutableData mutableData) {

                        Integer avail = mutableData.getValue(Integer.class);

                        if (avail==null) return Transaction.success(mutableData);

                        if (avail>0) {
                            mutableData.setValue(avail - 1);
                            didAdd = false;
                        }
                        else{
                            mutableData.setValue(avail + 1);
                            didAdd = true;
                        }
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                        // transaction completed
                        if(didAdd) waitForPenpal();
                        else prepChat(usernames);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                print("cancelled");
            }
        });
    }

    void prepChat(Iterable<DataSnapshot> usernames){
        if (!usernames.iterator().hasNext()){
            matchingDB.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    prepChat(dataSnapshot.getChildren());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            return;
        }

        String penpalTemp = usernames.iterator().next().getKey();
        if(penpalTemp.equals("available")) penpalTemp = usernames.iterator().next().getKey();

        final String penpal = penpalTemp;

        final String chatroomID = mDatabase.child("Chatrooms").push().getKey();
        final DatabaseReference chatroomDB = mDatabase.child("Chatrooms").child(chatroomID);
        chatroomDB.child("category").setValue(category);
        listenerReferences.add(mDatabase.child("Database").child(category).child("Size"));
        mDatabase.child("Database").child(category).child("Size").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer size = dataSnapshot.getValue(Integer.class);
                if (size!=null && size>0){
                    ArrayList<Integer> range = new ArrayList();
                    for (int i=0; i<size; i++) range.add(i);
                    Collections.shuffle(range);
                    String rangeString = "";
                    for (int i : range) rangeString += String.valueOf(i) + ",";
                    chatroomDB.child("questionOrder").setValue(rangeString);
                    chatroomDB.child("currentIndex").setValue(0);

                    matchingDB.child("users").child(penpal).setValue(chatroomID);
                    startChat(chatroomID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void waitForPenpal(){
        matchingDB.child("users").child(currentUserId).setValue("available");
        listenerReferences.add(matchingDB.child("users").child(currentUserId));
        matchingDB.child("users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String s = dataSnapshot.getValue(String.class);
                if (s!=null && !s.equals("available")) {
                    matchingDB.child("users").child(currentUserId).removeValue();
                    startChat(s);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void startChat(final String chatroomID){
        //TODO: get the question order and everything offline here first before launching ChatActivity

        if (chatroomID==null) {
            finish();
            return;
        }
        mDatabase.child("Chatrooms").child(chatroomID).child("users").child(currentUserId).setValue("online");
        mDatabase.child("Chatrooms").child(chatroomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue()==null){
                    //TODO: do something. the original host has quit
                }

                Intent intent = new Intent(MatchmakerActivity.this, ChatActivity.class);
                intent.putExtra(INTENT_CHATROOOM_ID, chatroomID);
                intent.putExtra(MainActivity.CATEGORY_NAME, category);
                intent.putExtra(MainActivity.CATEGORY_DETAILS, catDetails);

                ArrayList<Integer> questionOrder = new ArrayList<>();
                for (String c : dataSnapshot.child("questionOrder").getValue(String.class).split(",")){
                    questionOrder.add(Integer.valueOf(c));
                }
                intent.putExtra(INTENT_QUESTION_ORDER, questionOrder);
                startActivityForResult(intent, REQUEST_CHAT);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                print("Error");
            }
        });
    }

    @Override
    public void onBackPressed() {
        matchingDB.child("Available").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull final MutableData mutableData) {

                Integer avail = mutableData.getValue(Integer.class);

                if (avail==null) return Transaction.success(mutableData);
                else mutableData.setValue(avail - 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                // transaction completed
                matchingDB.child("users").child(currentUserId).removeValue();
                actuallyPressBack();
            }
        });
    }

    private void actuallyPressBack(){
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_CHAT){
            finish();
        }
    }

    void print(String m){
        Toast.makeText(MatchmakerActivity.this, m, Toast.LENGTH_SHORT).show();
        adviceTV.setText(adviceTV.getText() + "\n" + m);
    }
}
