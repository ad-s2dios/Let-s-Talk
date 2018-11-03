package com.ads2dios.adrieladtan.letstalk;

/**
 * Created by adrieladtan on 21/9/17.


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.ads2dios.adrieladtan.resistance.billing.utils.IabHelper;
import com.ads2dios.adrieladtan.resistance.billing.utils.IabResult;
import com.ads2dios.adrieladtan.resistance.billing.utils.Inventory;
import com.ads2dios.adrieladtan.resistance.billing.utils.Purchase;

public class MyBilling {
    // Debug tag, for logging
    static final String TAG = "Resistance";

    static final String SKU_REMOVE_ADS = "remove_ads_1.0";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10111;

    Activity activity;
    boolean toPurchase;
    String payload;

    // The helper object
    IabHelper mHelper;

    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApry2T2FzQrFwejy4GuAZgNuRRTjrdqt5A+q7+8gtQ6qeIEVyCjiAVdWWIv/uGSouwe/Phit6NdZax92K8/qYn8SJh5i/a/ECN+NQ9Vl5+hI0e8YBTO8Um4Ml7xXt8q8XuauYEPbyE6hy4osZk1qssf3f1CnZG5HfB09UDIw98b6XKBdkkjGrPM+4EOt8n/3B/YK9UzPqVXqYF53RWap5yqAEhCZ1WanLZUTypCN/o1S14Eb/I7MH4qBGQG/HbBdeMhE6+DjUAs5VFgRf4ygTCLeV46ZS+UyISDs8skXDqZ1tlqywk6Exx5bJbLEx9WmS60Xbmc6eFBMcz40v75TaPQIDAQAB";

    public MyBilling(Activity launcher, boolean toPurchaseBool, String payloadStr) {
        this.activity = launcher;
        this.toPurchase = toPurchaseBool;
        this.payload = payloadStr;
    }

    public void onCreate() {
        //TODO: start
        //SETUP -> query inventory -> gotInventoryListener -> implementRemoveAds

        // Create the helper, passing it our context and the public key to
        // verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(activity, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set
        // this to false).
        mHelper.enableDebugLogging(false);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed off in the meantime? If so, quit.
                if (mHelper == null)
                    return;

                // IAB is fully set up. Now, let's get an inventory of stuff we
                // own.
                Log.d(TAG, "Setup successful. Querying inventory.");

                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error launching inventory query. Another async operation in progress.");
                }

            }
        });
    }

    // Listener that's called when we finish querying the items and
    // subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null)
                return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                /*if (inventory.hasPurchase(SKU_REMOVE_ADS)) {
                    try {
                        mHelper.consumeAsync(inventory.getPurchase(SKU_REMOVE_ADS), null);
                    }
                    catch (IabHelper.IabAsyncInProgressException e){
                        complain("nuuuu");
                    }
                }
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().


            // Do we have the premium upgrade?
            //If yes, and it is verified, remove ads
            Purchase removeAdsPurchase = inventory.getPurchase(SKU_REMOVE_ADS);
            if(removeAdsPurchase != null && verifyDeveloperPayload(removeAdsPurchase)) {
                SettingsActivity.removeAds(toPurchase, activity);
                onDestroy();
                //TODO: end
            }
            else if (toPurchase) purchaseRemoveAds();
            else complain("No purchase detected on this account.");

            /*Log.d(TAG, "User has "
                    + (Constants.isAdsDisabled ? "REMOVED ADS"
                    : "NOT REMOVED ADS"));

            // setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    //TODO: actually purchase da goods
    public void purchaseRemoveAds() {

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    mHelper.launchPurchaseFlow(activity, SKU_REMOVE_ADS, RC_REQUEST, mPurchaseFinishedListener, payload);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    complain("Error launching purchase flow. Another async operation in progress.");
                }
            }
        });
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + ","
                + data);
        if (mHelper == null)
            return true;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            return false;
        } else {

            Log.d(TAG, "onActivityResult handled by IABUtil.");

            return true;
        }

    }

    /** Verifies the developer payload of a purchase.
    boolean verifyDeveloperPayload(Purchase p) {
        String payloadStr = p.getDeveloperPayload();

        return payloadStr.equals(payload);
        /*
         * TODO: verify that the developer payload of the purchase is correct.
         * It will be the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase
         * and verifying it here might seem like a good approach, but this will
         * fail in the case where the user purchases an item on one device and
         * then uses your app on a different device, because on the other device
         * you will not have access to the random string you originally
         * generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different
         * between them, so that one user's purchase can't be replayed to
         * another user.
         *
         * 2. The payload must be such that you can verify it even when the app
         * wasn't the one who initiated the purchase flow (so that items
         * purchased by the user on one device work on other devices owned by
         * the user).
         *
         * Using your own server to store and verify developer payloads across
         * app installations is recommended.

    }

    // Callback for when a purchase is finished
    //TODO: purchase goods continued
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: "
                    + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null)
                return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_REMOVE_ADS)) {
                // bought the premium upgrade!
                SettingsActivity.removeAds(true, activity);
                onDestroy();
                //TODO: END

            }
        }
    };

    // We're being destroyed. It's important to dispose of the helper here!
    public void onDestroy() {
        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    void complain(final String message) {
        Log.e(TAG, "**** Resistance Error: " + message);
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                AlertDialog.Builder bld = new AlertDialog.Builder(activity);
                bld.setTitle("Oops");
                bld.setMessage(message);
                bld.setPositiveButton("OK", null);
                bld.setNegativeButton("Email AD s2dios", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ad.s2dios@gmail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Resistance: ");
                        String os = System.getProperty("os.version"); // OS version
                        intent.putExtra(Intent.EXTRA_TEXT, "Hey AD s2dios,\n\n...\n\n\n" + os + "\n" + Build.VERSION.SDK + "\n" + Build.DEVICE + "\n" + Build.MODEL + "\n" + Build.MANUFACTURER + "\n" + "In-app Purchase" + "\n" + message);
                        if (intent.resolveActivity(activity.getPackageManager()) != null) {
                            activity.startActivity(intent);
                        }
                    }
                });
                Log.d(TAG, "Showing alert dialog: " + message);
                bld.create().show();
                onDestroy();
            }
        });
    }

}

*/