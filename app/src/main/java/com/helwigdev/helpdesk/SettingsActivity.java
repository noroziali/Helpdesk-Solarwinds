package com.helwigdev.helpdesk;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by helwi on 10/15/2016.
 */

public class SettingsActivity extends AppCompatActivity {


    private static final String ADS_ORDER_ID = "ad_removal_order_id";
    private static final String ADS_PURCHASE_TOKEN = "ad_removal_purchase_token";
    protected static final String PREF_ADS_REMOVED = "pref_ads_removed";
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public Boolean registerPurchase(String purchaseData, Boolean restore){

        try {
            JSONObject jo = new JSONObject(purchaseData);
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
            String sku = jo.getString("productId");
            String productId = "test";
            if(jo.has("orderId")) {
                productId = jo.getString("orderId");
                prefs.putString(ADS_ORDER_ID, productId);
            } else {
                Log.w(TAG, "No orderID in return object. Was it a test purchase?");
            }
            String devPayload = jo.getString("developerPayload");
            String purchaseToken = jo.getString("purchaseToken");
            if(sku.equals(SettingsFragment.SKU_REMOVE_ADS)){
                if(!Build.SERIAL.equals(devPayload)){
                    Log.e("Billing","Payload and device serial do not match!");
                }
                //log purchase event
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, productId);
                if(restore){
                    FirebaseAnalytics.getInstance(this).logEvent("restore_purchase",bundle);
                } else {
                    FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);
                }
                prefs.putString(ADS_PURCHASE_TOKEN, purchaseToken);
                prefs.putBoolean(PREF_ADS_REMOVED, true);
                prefs.apply();
                Toast.makeText(this, getResources().getString(R.string.thanks_billing), Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("SettingsActivity","Got activity result!");



        Bundle b = data.getExtras();
        if (resultCode != Activity.RESULT_OK) {
            Log.e("SettingsActivity", "Got result code " + resultCode);
            return;
        }
        if (requestCode == SettingsFragment.PURCHASE_ADS_REQUEST_CODE) {
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");

            if(purchaseData == null){
                new AlertDialog.Builder(this)
                        .setTitle("Billing error")
                        .setMessage("Transaction canceled.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // close
                            }
                        })
                        .setIcon(R.drawable.ic_themed_error)
                        .show();
                return;
            }
            Log.i("SettingsActivity data",purchaseData);
            registerPurchase(purchaseData, false);

        }
    }



}
