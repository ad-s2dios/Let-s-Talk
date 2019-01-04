package com.ads2dios.adrieladtan.letstalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public static final String CATEGORY_NAME = "category name";
    public static final String CATEGORY_DETAILS = "category details";

    public static final int REQUEST_MATCHMAKER = 2;

    AboutAdapter mAdapter;
    ListView aboutLV;
    ArrayList<String> cats;
    ArrayList<String> catDetails;

    boolean isLocal;
    boolean isConnected;
    ConnectivityManager cm;
    TextView onlineTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        isLocal = intent.getBooleanExtra(MenuActivity.INTENT_IS_LOCAL,true);
        onlineTV = findViewById(R.id.onlineTV);
        if (isLocal) onlineTV.setVisibility(View.GONE);
        else {
            cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            onlineTV.setVisibility(View.VISIBLE);
            setColour();
            onlineTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setColour();
                }
            });
        }

        //extracting saved category information
        SharedPreferences detailsSP = getSharedPreferences(SplashActivity.DETAILS_SP, Context.MODE_PRIVATE);
        cats = new ArrayList<>();
        cats.addAll(Arrays.asList(detailsSP.getString(SplashActivity.CAT_NAMES, "").split(",")));
        catDetails = new ArrayList<>();
        SharedPreferences sp;
        for(String cat:cats){
            String SP_string = "TALK_" + cat + "_SP";
            sp = getSharedPreferences(SP_string, Context.MODE_PRIVATE);
            catDetails.add(sp.getString(SplashActivity.CAT_DETAILS, null));
        }

        if (isLocal) {
            //add the 'anything' category for local (future implementation online)
            cats.add(0, "Anything");
            catDetails.add(0, "conversations about any topic under the sun");
        }
        mAdapter = new AboutAdapter(MainActivity.this, R.layout.about_lv_item, cats, catDetails);
        aboutLV = findViewById(R.id.aboutLV);
        aboutLV.setAdapter(mAdapter);
    }

    private void setColour(){
        //check connectivity and update banner
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected){
            onlineTV.setBackgroundColor(getResources().getColor(R.color.online));
            onlineTV.setText("Online");
        }
        else{
            onlineTV.setBackgroundColor(getResources().getColor(R.color.grey));
            onlineTV.setText("Offline");
        }
    }

    public class AboutAdapter extends ArrayAdapter<String> {
        private int mResource;
        private ArrayList<String> mCats;
        private ArrayList<String> mCatDetails;

        public AboutAdapter(Context context, int resource,ArrayList<String> cats, ArrayList<String> catDetails){
            super(context, resource, cats);
            this.mResource = resource;
            this.mCats = cats;
            this.mCatDetails = catDetails;
        }
        @Override
        public View getView(final int position, View row, ViewGroup parent){
            if (row==null){
                row = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
            }

            final String cat = mCats.get(position);
            //category
            final String catDetails = mCatDetails.get(position);

            TextView catNameTV = row.findViewById(R.id.catNameTV);
            TextView catDetailsTV = row.findViewById(R.id.catDetailsTV);
            catNameTV.setText(cat);
            catDetailsTV.setText(catDetails);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if it's an online session, double check connectivity
                    if (!isLocal) setColour();

                    if (isLocal || !isConnected) {
                        Intent talkIntent = new Intent(MainActivity.this, TalkActivity.class);
                        talkIntent.putExtra(CATEGORY_NAME, cat);
                        talkIntent.putExtra(CATEGORY_DETAILS, catDetails);
                        startActivity(talkIntent);
                    }
                    else{
                        Intent matchmakerIntent = new Intent(MainActivity.this, MatchmakerActivity.class);
                        matchmakerIntent.putExtra(CATEGORY_NAME, cat);
                        matchmakerIntent.putExtra(CATEGORY_DETAILS, catDetails);
                        startActivityForResult(matchmakerIntent, REQUEST_MATCHMAKER);
                    }
                }
            });

            return row;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_MATCHMAKER){
            Toast.makeText(MainActivity.this, "Chat ended", Toast.LENGTH_SHORT).show();
        }
    }

    /*private void billButtClicked (final boolean purchasedNotRestored){
        //TODO: check this is removed (yup)
        //SettingsActivity.removeAds(purchasedNotRestored, SettingsActivity.this);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final EditText nameBlank = new EditText(MainActivity.this);
        builder.setTitle("Remove Ads");
        if(purchasedNotRestored)  builder.setMessage("Are you sure you want to remove ads?\n\nPlease enter your email for verification.");
        else  builder.setMessage("We'll help check if you've removed ads on this account.\n\nPlease enter your email for verification.");
        builder.setView(nameBlank);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
                Pattern pattern = Pattern.compile(regex);
                String email = nameBlank.getText().toString();
                Matcher matcher = pattern.matcher(email);
                if (matcher.matches()) {
                    MyBilling bill = new MyBilling(SettingsActivity.this, purchasedNotRestored, email);
                    bill.onCreate();
                }
                else{
                    Toast.makeText(SettingsActivity.this, "Invalid email!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }

    public static void removeAds(boolean purchasedNotRestored, Context corn){
        SHOULD_REMOVE_ADS = true;
        SharedPreferences sharedPref = corn.getSharedPreferences(SETTINGS_SHARED_PREFS_STRING,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SHOULD_REMOVE_ADS_STRING, true);
        editor.apply();

        AlertDialog.Builder builder = new AlertDialog.Builder(corn);
        builder.setTitle("Remove Ads");
        if(purchasedNotRestored) builder.setMessage("Ads successfully removed! Thank you for your support :D");
        else builder.setMessage("Your purchase has been restored. Ads successfully removed! Thank you for your support :D");
        builder.setPositiveButton("Hurray!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static void enableAdsAgain(Context corn){
        SHOULD_REMOVE_ADS = false;
        SharedPreferences sharedPref = corn.getSharedPreferences(SETTINGS_SHARED_PREFS_STRING,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(SHOULD_REMOVE_ADS_STRING, false);
        editor.apply();
    }*/
}
