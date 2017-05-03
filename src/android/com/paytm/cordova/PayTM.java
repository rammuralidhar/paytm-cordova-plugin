package com.paytm.cordova;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.paytm.pgsdk.*;

public class PayTM extends CordovaPlugin {

    private PaytmPGService paytm_service;

    private String PAYTM_GENERATE_URL;
    private String PAYTM_VALIDATE_URL;

    private String PAYTM_MERCHANT_ID;
    private String PAYTM_INDUSTRY_TYPE_ID;
    private String PAYTM_WEBSITE;

    protected void pluginInitialize() {
        int appResId = cordova.getActivity().getResources().getIdentifier("paytm_gen_url", "string", cordova.getActivity().getPackageName());
        PAYTM_GENERATE_URL = cordova.getActivity().getString(appResId);
        appResId = cordova.getActivity().getResources().getIdentifier("paytm_chk_url", "string", cordova.getActivity().getPackageName());
        PAYTM_VALIDATE_URL = cordova.getActivity().getString(appResId);
        appResId = cordova.getActivity().getResources().getIdentifier("paytm_merchant_id", "string", cordova.getActivity().getPackageName());
        PAYTM_MERCHANT_ID = cordova.getActivity().getString(appResId);
        appResId = cordova.getActivity().getResources().getIdentifier("paytm_industry_type_id", "string", cordova.getActivity().getPackageName());
        PAYTM_INDUSTRY_TYPE_ID = cordova.getActivity().getString(appResId);
        appResId = cordova.getActivity().getResources().getIdentifier("paytm_website", "string", cordova.getActivity().getPackageName());
        PAYTM_WEBSITE = cordova.getActivity().getString(appResId);
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {
        if (action.equals("startPayment")) {
            //orderid, cust_id, email, phone, txn_amt
            startPayment(args.getString(0), args.getString(1), args.getString(2), args.getString(3), args.getString(4), callbackContext);
            return true;
        }
        return false;
    }

    private void startPayment(final String order_id,
                              final String cust_id,
                              final String email,
                              final String phone,
                              final String txn_amt,
                              final CallbackContext callbackContext){

        paytm_service = PaytmPGService.getProductionService();
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("REQUEST_TYPE", "DEFAULT");
        paramMap.put("ORDER_ID", order_id);
        paramMap.put("MID", PAYTM_MERCHANT_ID);
        paramMap.put("CUST_ID", cust_id);
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("INDUSTRY_TYPE_ID", PAYTM_INDUSTRY_TYPE_ID);
        paramMap.put("WEBSITE", PAYTM_WEBSITE);
        paramMap.put("TXN_AMOUNT", txn_amt);
        paramMap.put("EMAIL", email);
        paramMap.put("MOBILE_NO", phone);
        paramMap.put("THEME", "merchant");

        PaytmOrder order = new PaytmOrder(paramMap);
        PaytmMerchant merchant = new PaytmMerchant(this.PAYTM_GENERATE_URL, this.PAYTM_VALIDATE_URL);

        this.paytm_service.initialize(order, merchant, null);
        this.paytm_service.startPaymentTransaction(cordova.getActivity(), false, false, new PaytmPaymentTransactionCallback()
        {

            @Override
            public void onTransactionSuccess(Bundle inResponse) {
                Log.i("Error", "onTransactionSuccess :" + inResponse);
                callbackContext.sucess(inResponses);
            }

            @Override
            public void onTransactionFailure(String inErrorMessage,Bundle inResponse)
            {
                Log.i("Error","onTransactionFailure :"+inErrorMessage);
                JSONObject error = new JSONObject();
                error.put("errormsg", inErrorMessage);
                error.put("responsemsg", inResponse);
                callbackContext.error(error);
            }


            @Override
            public void clientAuthenticationFailed(String inErrorMessage)
            {
                Log.i("Error","clientAuthenticationFailed :"+inErrorMessage);
                JSONObject error = new JSONObject();
                error.put("errormsg", inErrorMessage);
                callbackContext.error(error);
            }


            @Override
            public void networkNotAvailable() {
                Log.i("Error","networkNotAvailable");
                JSONObject error = new JSONObject();
                error.put("errormsg", "networkNotAvailable");
                callbackContext.error(error);
            }

            @Override
            public void onErrorLoadingWebPage(int arg0, String arg1, String arg2) {
                Log.i("Error","onErrorLoadingWebPage arg0  :"+arg0);
                Log.i("Error","onErrorLoadingWebPage arg1  :"+arg1);
                Log.i("Error","onErrorLoadingWebPage arg2  :"+arg2);
                JSONObject error = new JSONObject();
                error.put("errormsg1", arg0);
                error.put("errormsg2", arg1);
                error.put("errormsg3", arg2);
                callbackContext.error(error);
            }

            @Override
            public void someUIErrorOccurred(String arg0) {
                Log.i("Error","someUIErrorOccurred :"+arg0);
                JSONObject error = new JSONObject();
                error.put("errormsg", arg0);
                callbackContext.error(error);
            }
        });
    }
}