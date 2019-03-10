package com.interswitchng.interswitchpossdkdemo;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public class Keyboard implements View.OnClickListener {
    
    interface KeyBoardListener {
        void onTextChange(String text);
        void onSubmit(String text);
    }

    private Context mContext;
    private String result = "";
    private KeyBoardListener mCallback;
    
    
    public Keyboard(Activity activity, KeyBoardListener callback) {
        setupButtons(activity, callback);
    }


    private void setupButtons(Activity activity, KeyBoardListener callback) {
        // add buttons
        activity.findViewById(R.id.one).setOnClickListener(this);
        activity.findViewById(R.id.two).setOnClickListener(this);
        activity.findViewById(R.id.three).setOnClickListener(this);
        activity.findViewById(R.id.four).setOnClickListener(this);
        activity.findViewById(R.id.five).setOnClickListener(this);
        activity.findViewById(R.id.six).setOnClickListener(this);
        activity.findViewById(R.id.seven).setOnClickListener(this);
        activity.findViewById(R.id.eight).setOnClickListener(this);
        activity.findViewById(R.id.nine).setOnClickListener(this);
        activity.findViewById(R.id.oneZero).setOnClickListener(this);
        activity.findViewById(R.id.twoZeros).setOnClickListener(this);
        activity.findViewById(R.id.threeZeros).setOnClickListener(this);
        activity.findViewById(R.id.delete).setOnClickListener(this);
        activity.findViewById(R.id.done).setOnClickListener(this);
        
        // set callback
        mCallback = callback;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.one : {
                result += "1";
                break;
            }
            case R.id.two : {
                result += "2";
                break;
            }
            case R.id.three : {
                result += "3";
                break;
            }
            case R.id.four : {
                result += "4";
                break;
            }
            case R.id.five : {
                result += "5";
                break;
            }
            case R.id.six: {
                result += "6";
                break;
            }
            case R.id.seven: {
                result += "7";
                break;
            }
            case R.id.eight: {
                result += "8";
                break;
            }
            case R.id.nine: {
                result += "9";
                break;
            }
            case R.id.oneZero: {
                result += "0";
                break;
            }
            case R.id.twoZeros: {
                result += "00";
                break;
            }
            case R.id.threeZeros: {
                result += "000";
                break;
            }
            case R.id.delete: {
                if (result.length() > 0) {
                    result = result.substring(0, result.length() - 1);
                }
                break;
            }
            case R.id.done: {
                if (mCallback != null) mCallback.onSubmit(result);
            }
            default: {
                return;
            }
        }
        
        
        if (mCallback != null) mCallback.onTextChange(result);
    }

    public void setText(String text) {
        result = text;
    }
}
