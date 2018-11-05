package com.ads2dios.adrieladtan.letstalk;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {

    TextView titleTV;
    TextView aboutTV;
    TextView catNameTV;
    TextView questionTV;
    TextView catDetailsTV;
    //ImageButton prevFab;
    ImageButton nextFab;

    Typeface appFont;
    Typeface boldFont;
    Typeface lightFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        appFont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Regular.ttf");
        boldFont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Bold.ttf");
        lightFont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Light.ttf");

        titleTV = (TextView)findViewById(R.id.titleTV);
        titleTV.setTypeface(boldFont);
        aboutTV = (TextView)findViewById(R.id.aboutTV);
        aboutTV.setTypeface(appFont);
        catNameTV = (TextView)findViewById(R.id.catNameTV);
        catNameTV.setTypeface(appFont);
        //catNameTV.setText(category);
        catDetailsTV = (TextView)findViewById(R.id.catDetailsTV);
        catDetailsTV.setTypeface(lightFont);
        //catDetailsTV.setText(intent.getStringExtra(CATEGORY_DETAILS));
        questionTV = (TextView)findViewById(R.id.questionTV);
        questionTV.setTypeface(appFont);

        nextFab = (FloatingActionButton) findViewById(R.id.nextFab);
        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /*prevFab = (FloatingActionButton) findViewById(R.id.prevFab);
        prevFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

}
