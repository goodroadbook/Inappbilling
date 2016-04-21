package com.njh.inappbilling;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.njh.inappbilling.util.IabBroadcastReceiver;
import com.njh.inappbilling.util.IabHelper;
import com.njh.inappbilling.util.IabResult;
import com.njh.inappbilling.util.Inventory;
import com.njh.inappbilling.util.Purchase;

public class MainActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener, View.OnClickListener {
    private static final String SKU_PREMIUM = "premium";
    private static final int RC_REQUEST = 10001;

    private IabHelper mHelper;
    private IabBroadcastReceiver mBroadcastReceiver;

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 0:
                    showTextView((String)msg.obj);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgUP+C/tFEGtt9xeM1CRcykKkErG9wTS6OqAVjjwBABB6/gh7PT5GxxtZIC3+FKokanUybWknOd4z1sI7yTUVdNPw1zNpvaB0Zt4yHtDiJPTWyxEJgvNqarXSFBGm66+hP6i18Q49RAxlnt/yJ6FdgtQQHx8sE9DO3ffLGq1asdl4WthC4z74v+8oFynaQmx2ZuLTyN9Rkfk3qYXqCbDj6xyxQkGp6IOfiTmcPq2hh22GJtVp2qZvfnhTGsCTQZu3sIpBzJD2ScCl3Ujj4FjEJMd8BP5/XvFhwTdGz/gS0y1LRlDz24x0MJx03x9nnPhNlHUyEG1mRNYTgqPFDRrUfwIDAQAB";

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener()
        {
            public void onIabSetupFinished(IabResult result)
            {
                if (!result.isSuccess())
                {
                    return;
                }

                if (mHelper == null) return;

                mBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });

        Button purchaseBtn = (Button)findViewById(R.id.purchasebtn);
        purchaseBtn.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else
        {
        }
    }

    @Override
    public void receivedBroadcast()
    {
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener()
    {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (mHelper == null) return;

            if (result.isFailure())
            {
                return;
            }

            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            if(null == premiumPurchase)
            {
                return;
            }
            String premiumdata = premiumPurchase.toString();
            Message msg = handler.obtainMessage();
            msg.what = 0;
            msg.obj = (String)premiumdata;
            handler.sendMessage(msg);

        }
    };

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.purchasebtn:
                String payload = "";

                mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                        mPurchaseFinishedListener, payload);
                break;
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener()
    {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (mHelper == null) return;

            if (result.isFailure())
            {
                return;
            }

           if (purchase.getSku().equals(SKU_PREMIUM))
           {
               String premiumdata = purchase.toString();
               if(null == premiumdata)
               {
                   return;
               }

               Message msg = handler.obtainMessage();
               msg.what = 0;
               msg.obj = (String)premiumdata;
               handler.sendMessage(msg);
           }
        }
    };

    private void showTextView(String aText)
    {
        TextView purchasedata = (TextView)findViewById(R.id.purchaseinfotxt);
        purchasedata.setText(aText);
    }
}
