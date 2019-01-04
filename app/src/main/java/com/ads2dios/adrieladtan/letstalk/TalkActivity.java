package com.ads2dios.adrieladtan.letstalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TalkActivity extends AppCompatActivity {

    String category;
    int currentQuestion;
    ArrayList<String> questions;

    TextView catNameTV;
    TextView questionTV;
    TextView catDetailsTV;
    ImageButton prevButt;
    //ImageButton favButt;
    ImageButton nextButt;
    ImageButton shareButt;

    AdView mAdView;
    private InterstitialAd mInterstitialAd;
    int adCount;
    long adTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        /*mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.device_id))
                .build();
        mAdView.loadAd(adRequest);*/

        //prepare full screen ad
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

        Intent intent = getIntent();

        //register UI elements
        catNameTV = findViewById(R.id.catNameTV);
        category = intent.getStringExtra(MainActivity.CATEGORY_NAME);
        catNameTV.setText(category);
        catDetailsTV = findViewById(R.id.catDetailsTV);
        catDetailsTV.setText(intent.getStringExtra(MainActivity.CATEGORY_DETAILS));
        questionTV = findViewById(R.id.questionTV);

        //instantiate variables
        currentQuestion = 0;
        adCount = 0;
        adTimer = System.currentTimeMillis();
        questions = new ArrayList<>();
        if(category.equals("Anything")){
            //load every darned question
            for (String cat : getSharedPreferences(SplashActivity.DETAILS_SP, Context.MODE_PRIVATE).getString(SplashActivity.CAT_NAMES, "").split(",")){
                loadQuestions(cat, true);
            }
        }
        else {
            loadQuestions(category, false);
        }
        Collections.shuffle(questions);
        updateQuestion();

        prevButt = findViewById(R.id.prevButt);
        prevButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuestion -= 1;
                if(currentQuestion<0)currentQuestion = questions.size() - 1;
                updateQuestion();
            }
        });
        //favButt = findViewById(R.id.favButt);
        nextButt = findViewById(R.id.nextButt);
        nextButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuestion += 1;
                if(currentQuestion>=questions.size()) currentQuestion = 0;
                updateQuestion();
            }
        });
        shareButt = findViewById(R.id.shareButt);
        shareButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/html");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, category);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Let's Talk.\n" + questions.get(currentQuestion) + "\n\n<a href=\"http://tinyurl.com/getletstalk\"Download</a> and get talking!");
                startActivity(Intent.createChooser(sharingIntent, "Share Prompt"));
            }
        });
    }

    void updateQuestion(){
        adCount += 1;
        questionTV.setText(questions.get(currentQuestion));
        if (mInterstitialAd.isLoaded() && (adCount>=10 || (adCount>=5 && (System.currentTimeMillis() - adTimer)>60000))){
            //every 5th count, if ad is loaded and 1 min (60s * 1000) has elapsed, ad shows. Or every 10 if u spam.
            mInterstitialAd.show();
            adTimer = System.currentTimeMillis();
            adCount = 0;
        }
    }

    void loadQuestions(String cat, boolean isMultiload){
        if(cat.equals("The Little Red Dot") && isMultiload){
            //Don't add the little red dot to "anything"
            return;
        }
        SharedPreferences sp = getSharedPreferences("TALK_" + cat + "_SP", Context.MODE_PRIVATE);

        for(int i=0; i<sp.getInt(SplashActivity.CAT_SIZE, 1); i++){
            String iStr = String.valueOf(i);
            if(iStr.length()<2) iStr = "0" + iStr;
            String reference = cat + iStr;
            //default text is 'error' in case it doesnt looad properly
            questions.add(sp.getString(reference, "Error. Go back home and reload."));
        }
    }
}
