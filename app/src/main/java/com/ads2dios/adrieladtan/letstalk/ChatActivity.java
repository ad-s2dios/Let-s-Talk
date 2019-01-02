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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        catNameTV = (TextView)findViewById(R.id.catNameTV);
        //catNameTV.setText(category);
        catDetailsTV = (TextView)findViewById(R.id.catDetailsTV);
        //catDetailsTV.setText(intent.getStringExtra(CATEGORY_DETAILS));
        questionTV = findViewById(R.id.questionTV);

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
