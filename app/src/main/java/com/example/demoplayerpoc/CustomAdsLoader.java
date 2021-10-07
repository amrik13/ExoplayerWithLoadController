package com.example.demoplayerpoc;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.ads.AdsLoader;

public class CustomAdsLoader {

    private static final String TAG = CustomAdsLoader.class.getSimpleName();
    private AdsLoader adsLoader;
    private Context mContext;


    public CustomAdsLoader(Context mContext){
        this.mContext = mContext;
        adsLoader = new ImaAdsLoader.Builder(mContext)
                .setAdEventListener(new AdsEventListeners())
                .setAdErrorListener(new AdsErrorListeners())
                .build();
    }

    public AdsLoader getAdsLoader() {
        return adsLoader;
    }

    class AdsEventListeners implements AdEvent.AdEventListener{

        @Override
        public void onAdEvent(AdEvent adEvent) {
            Log.i(TAG, "onAdEvent: "+adEvent.getAdData());
            switch (adEvent.getType()){
                case LOADED:
                    Toast.makeText(mContext, "Ads Loaded", Toast.LENGTH_SHORT).show();
                    break;
                case RESUMED:
                    Toast.makeText(mContext, "Ads Resumed", Toast.LENGTH_SHORT).show();
                    break;
                case PAUSED:
                    Toast.makeText(mContext, "Ads Paused", Toast.LENGTH_SHORT).show();
                    break;
                case STARTED:
                    Toast.makeText(mContext, "Ads Started", Toast.LENGTH_SHORT).show();
                    break;
                case COMPLETED:
                    Toast.makeText(mContext, "Ads Completed", Toast.LENGTH_SHORT).show();
                    break;
                case AD_BUFFERING:
                    Toast.makeText(mContext, "Ads Buffering", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    class AdsErrorListeners implements AdErrorEvent.AdErrorListener{

        @Override
        public void onAdError(AdErrorEvent adErrorEvent) {
            Log.i(TAG, "onAdEvent: "+adErrorEvent.toString());
            Toast.makeText(mContext, adErrorEvent.getError().getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


}
