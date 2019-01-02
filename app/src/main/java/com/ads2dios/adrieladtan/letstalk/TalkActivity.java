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

    TextView aboutTV;
    TextView catNameTV;
    TextView questionTV;
    TextView catDetailsTV;
    ImageButton prevButt;
    //ImageButton favButt;
    ImageButton nextButt;
    ImageButton shareButt;

    Typeface appFont;
    Typeface boldFont;
    Typeface lightFont;

    AdView mAdView;
    private InterstitialAd mInterstitialAd;
    int adCount;
    long adTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        appFont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Regular.ttf");
        boldFont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Bold.ttf");
        lightFont = Typeface.createFromAsset(getAssets(),"fonts/Lato-Light.ttf");

        /*mAdView = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.device_id))
                .build();
        mAdView.loadAd(adRequest);*/

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
        category = intent.getStringExtra(MainActivity.CATEGORY_NAME);
        currentQuestion = 0;
        adCount = 0;
        adTimer = System.currentTimeMillis();

        aboutTV = (TextView)findViewById(R.id.aboutTV);
        aboutTV.setTypeface(appFont);
        catNameTV = (TextView)findViewById(R.id.catNameTV);
        catNameTV.setTypeface(appFont);
        catNameTV.setText(category);
        catDetailsTV = (TextView)findViewById(R.id.catDetailsTV);
        catDetailsTV.setTypeface(lightFont);
        catDetailsTV.setText(intent.getStringExtra(MainActivity.CATEGORY_DETAILS));
        questionTV = (TextView)findViewById(R.id.questionTV);
        questionTV.setTypeface(appFont);

        questions = new ArrayList<>();
        if(category.equals("Anything")){
            for (String cat : getSharedPreferences(SplashActivity.DETAILS_SP, Context.MODE_PRIVATE).getString(SplashActivity.CAT_NAMES, "").split(",")){
                loadQuestions(cat, true);
            }
        }
        else {
            loadQuestions(category, false);
        }
        Collections.shuffle(questions);
        updateQuestion();

        prevButt = (ImageButton)findViewById(R.id.prevButt);
        prevButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentQuestion -= 1;
                if(currentQuestion<0)currentQuestion = questions.size() - 1;
                updateQuestion();
            }
        });
        //favButt = (ImageButton)findViewById(R.id.favButt);
        nextButt = (ImageButton)findViewById(R.id.nextButt);
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
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, category);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, questions.get(currentQuestion));
                startActivity(Intent.createChooser(sharingIntent, "Share Prompt"));
            }
        });
    }

    void updateQuestion(){
        adCount += 1;
        questionTV.setText(questions.get(currentQuestion));
        if(adCount%15 == 0 || (adCount%5 == 0 && mInterstitialAd.isLoaded() && (System.currentTimeMillis() - adTimer) > 60000)){
            //every 5th count, if ad is loaded and 1 min (60s * 1000) has elapsed, ad shows. Or every 15 if u spam.
            mInterstitialAd.show();
            adTimer = System.currentTimeMillis();
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
            questions.add(sp.getString(reference, "Error. Go back home and reload."));
        }
    }
}
