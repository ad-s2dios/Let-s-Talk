package com.ads2dios.adrieladtan.letstalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    Button localButt;
    Button onlineButt;
    Button helpButt;

    boolean helpMode = false;
    LinearLayout menuLL;
    Button feedbackButt;

    ListView helpLV;
    HelpAdapter helpAdapter;
    ArrayList<String> talkTo;
    ArrayList<String> talkToDetails;

    LinearLayout userLL;
    ImageView profile;
    TextView usernameTV;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        localButt = findViewById(R.id.localButt);
        onlineButt = findViewById(R.id.onlineButt);
        helpButt = findViewById(R.id.helpButt);
        menuLL = findViewById(R.id.menuLL);
        feedbackButt = findViewById(R.id.feedbackButt);
        helpLV = findViewById(R.id.helpLV);
        userLL = findViewById(R.id.userLL);
        profile = findViewById(R.id.profile);
        usernameTV = findViewById(R.id.usernameTV);

        localButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            }
        });

        onlineButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: does stuff
            }
        });

        helpButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchMode();
            }
        });
        feedbackButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ad.s2dios@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Let's Talk: ");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        talkTo = new ArrayList<>();
        talkTo.add("to Anyone");
        talkTo.add("with Friends");
        talkTo.add("to Yourself");
        talkToDetails = new ArrayList<>();
        talkToDetails.add("Choose a category to get awesome conversation prompts! Start any conversation in real life by asking the question given.");
        talkToDetails.add("Sit in a circle and take turns to answer a prompt. Or answer a different prompt each.");
        talkToDetails.add("Create a vlog of yourself answering prompts!");

        userLL.setClickable(true);
        userLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(!isConnected) onlineButt.setEnabled(false);
        else onlineButt.setEnabled(true);

        helpAdapter = new HelpAdapter(getApplicationContext(), R.layout.about_lv_item, talkTo, talkToDetails);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser!=null){
            // someone is loged in
            userLL.setBackgroundColor(getResources().getColor(R.color.online));
            usernameTV.setText(currentUser.getDisplayName());
            profile.setVisibility(View.VISIBLE);
            profile.setImageURI(currentUser.getPhotoUrl());
        }
        else{
            userLL.setBackgroundColor(getResources().getColor(R.color.grey));
            usernameTV.setText("Click to log in");
            profile.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed(){
        if(helpMode) switchMode();
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            MenuActivity.super.onBackPressed();
                        }
                    }).create().show();
        }
    }

    private void switchMode(){
        //if in help mode back should switch back
        helpMode = !helpMode;
        if(helpMode){
            feedbackButt.setVisibility(View.VISIBLE);
            helpLV.setVisibility(View.VISIBLE);
            helpLV.setAdapter(helpAdapter);
            menuLL.setVisibility(View.GONE);
        }
        else{
            feedbackButt.setVisibility(View.GONE);
            helpLV.setVisibility(View.GONE);
            menuLL.setVisibility(View.VISIBLE);
        }
    }

    public class HelpAdapter extends ArrayAdapter<String> {
        private int mResource;
        private ArrayList<String> mCats;
        private ArrayList<String> mCatDetails;

        public HelpAdapter(Context context, int resource, ArrayList<String> cats, ArrayList<String> catDetails) {
            super(context, resource, cats);
            this.mResource = resource;
            this.mCats = cats;
            this.mCatDetails = catDetails;
        }

        @Override
        public View getView(final int position, View row, ViewGroup parent) {
            if (row == null) {
                row = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
            }

            final String cat = mCats.get(position);
            final String catDetails = mCatDetails.get(position);

            TextView aboutTV = row.findViewById(R.id.aboutTV);
            TextView catNameTV = row.findViewById(R.id.catNameTV);
            TextView catDetailsTV = row.findViewById(R.id.catDetailsTV);

            aboutTV.setText("Talk");
            catNameTV.setText(cat);
            catDetailsTV.setText(catDetails);

            row.setClickable(false);

            return row;
        }
    }
}
