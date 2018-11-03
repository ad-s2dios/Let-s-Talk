package com.ads2dios.adrieladtan.letstalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.ads.MobileAds;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class SplashActivity extends AppCompatActivity {

    DatabaseReference mDatabase;
    SharedPreferences detailsSP;
    SharedPreferences.Editor detailsEditor;

    public static final String DETAILS_SP = "TALK_DETAILS_SHARED_PREF";
    public static final String DEVICE_VERSION = "details_DEVICE_DATABASE_VERSION";
    public static final String CAT_NAMES = "details_CAT_NAMES";

    //Category databases' shared preferences are called thus:
    //TALK_<cat name>_SP
    public static final String CAT_DETAILS = "cat_DETAILS";
    public static final String CAT_SIZE = "cat_SIZE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        MobileAds.initialize(this, getString(R.string.app_admob_id));
    }

    @Override
    protected void onResume() {
        super.onResume();
        detailsSP = getSharedPreferences(DETAILS_SP, Context.MODE_PRIVATE);

        //Check if device is connected to internet. If not, just start app
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected) startApp();

        //if device is connected to internet, check if the phone database version is up to date with the online one
        final String deviceVersion = detailsSP.getString(DEVICE_VERSION, "dope");

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("DatabaseDetails").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("versionCode").getValue(String.class).equals(deviceVersion)){
                    //device version matches the online one
                    startApp();
                }
                else{
                    String catNamesString = dataSnapshot.child("catNames").getValue(String.class);
                    detailsEditor = detailsSP.edit();
                    detailsEditor.putString(CAT_NAMES, catNamesString);
                    detailsEditor.apply();
                    final ArrayList<String> newCats = new ArrayList<>();
                    newCats.addAll(Arrays.asList(catNamesString.split(",")));
                    final ArrayList<String> oldCats = new ArrayList<>();
                    oldCats.addAll(Arrays.asList(detailsSP.getString(CAT_NAMES, "").split(",")));

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            SharedPreferences.Editor editor;
                            for(String cat:oldCats){
                                String SP_string = "TALK_" + cat + "_SP";
                                //see note abv
                                editor = getSharedPreferences(SP_string, Context.MODE_PRIVATE).edit();
                                editor.clear();
                                editor.commit();
                                //delete all old category details
                            }

                            for(String cat:newCats){
                                String SP_string = "TALK_" + cat + "_SP";
                                //see note abv
                                editor = getSharedPreferences(SP_string, Context.MODE_PRIVATE).edit();
                                editor.putString(CAT_DETAILS, dataSnapshot.child(cat).child("Details").getValue(String.class));
                                int size = dataSnapshot.child(cat).child("Size").getValue(int.class);
                                editor.putInt(CAT_SIZE, size);
                                for(int i=0; i<size; i++){
                                    String iStr = String.valueOf(i);
                                    if(iStr.length()<2) iStr = "0" + iStr;
                                    String reference = cat + iStr;
                                    String question = dataSnapshot.child("allQns").child(reference).getValue(String.class);
                                    editor.putString(reference, question);
                                }
                                editor.commit();
                                //delete all old category details
                            }
                            startApp();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startApp(){
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
