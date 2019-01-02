package com.ads2dios.adrieladtan.letstalk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
        adviceTV.setText("");

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
        print("preppy");
        String penpalTemp = usernames.iterator().next().getKey();
        if(penpalTemp.equals("available")) penpalTemp = usernames.iterator().next().getKey();

        final String penpal = penpalTemp;

        final DatabaseReference chatroomDB = mDatabase.child("Chatrooms").child(penpal);
        chatroomDB.child("users").child(currentUserId).setValue(true);
        chatroomDB.child("category").setValue(category);
        if (category.equals("Anything")){

        }
        else{
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
                        chatroomDB.child("currentQuestion").setValue(range.get(0));

                        matchingDB.child("users").child(penpal).setValue("ready");
                        startChat(penpal);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    void waitForPenpal(){
        print("waiting");
        matchingDB.child("users").child(currentUserId).setValue("available");
        listenerReferences.add(matchingDB.child("users").child(currentUserId));
        matchingDB.child("users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String s = dataSnapshot.getValue(String.class);
                if (s!=null && s.equals("ready")) {
                    matchingDB.child("users").child(currentUserId).removeValue();
                    startChat(currentUserId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void startChat(String chatroomID){
        //TODO: get the question order and everything offline here first before launching ChatActivity

        print("YEY " + chatroomID);
        Intent intent = new Intent(MatchmakerActivity.this, ChatActivity.class);
        intent.putExtra(INTENT_CHATROOOM_ID, chatroomID);
        intent.putExtra(MainActivity.CATEGORY_NAME, category);
        intent.putExtra(MainActivity.CATEGORY_DETAILS, catDetails);
        startActivity(intent);
    }

    void print(String m){
        Toast.makeText(MatchmakerActivity.this, m, Toast.LENGTH_SHORT).show();
        adviceTV.setText(adviceTV.getText() + "\n" + m);
    }
}
