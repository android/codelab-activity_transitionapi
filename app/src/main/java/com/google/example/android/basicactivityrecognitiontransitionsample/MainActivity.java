package com.google.example.android.basicactivityrecognitiontransitionsample;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.common.logger.LogFragment;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Sample application which requests Activity recognition transitions.
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    // The intent action which will be fired when transitions are triggered.
    private final String TRANSITIONS_RECEIVER_ACTION =
            BuildConfig.APPLICATION_ID + "TRANSITIONS_RECEIVER_ACTION";
    private PendingIntent mPendingIntent;
    private TransitionsReceiver mTransitionsReceiver;
    private LogFragment mLogFragment;

    private static String toActivityString(int activity) {
        switch (activity) {
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "UNKNOWN";
        }
    }

    private static String toTransitionType(int transitionType) {
        switch (transitionType) {
            case ActivityTransition.ACTIVITY_TRANSITION_ENTER:
                return "ENTER";
            case ActivityTransition.ACTIVITY_TRANSITION_EXIT:
                return "EXIT";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        mTransitionsReceiver = new TransitionsReceiver();
        registerReceiver(mTransitionsReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION));

        mLogFragment = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.log_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupActivityTransitions();
    }

    @Override
    protected void onPause() {
        // Unregister the transitions:
        ActivityRecognition.getClient(this).removeActivityTransitionUpdates(mPendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Transitions successfully unregistered.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Transitions could not be unregistered: " + e);
                    }
                });

        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mTransitionsReceiver != null) {
            unregisterReceiver(mTransitionsReceiver);
            mTransitionsReceiver = null;
        }
        super.onStop();
    }

    /**
     * Sets up {@link ActivityTransitionRequest}'s for the sample app, and registers callbacks for them
     * with a custom {@link BroadcastReceiver}
     */
    private void setupActivityTransitions() {
        List<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        // Register for Transitions Updates.
        Task<Void> task =
                ActivityRecognition.getClient(this)
                        .requestActivityTransitionUpdates(request, mPendingIntent);
        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.i(TAG, "Transitions Api was successfully registered.");
                    }
                });
        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Transitions Api could not be registered: " + e);
                    }
                });
    }

    /**
     * A basic BroadcastReceiver to handle intents from from the Transitions API.
     */
    public class TransitionsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.equals(TRANSITIONS_RECEIVER_ACTION, intent.getAction())) {
                mLogFragment.getLogView()
                        .println("Received an unsupported action in TransitionsReceiver: action="
                                + intent.getAction());
                return;
            }
            if (ActivityTransitionResult.hasResult(intent)) {
                ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    String activity = toActivityString(event.getActivityType());
                    String transitionType = toTransitionType(event.getTransitionType());
                    mLogFragment.getLogView()
                            .println("Transition: "
                                    + activity + " (" + transitionType + ")" + "   "
                                    + new SimpleDateFormat("HH:mm:ss", Locale.US)
                                    .format(new Date()));
                }
            }
        }
    }
}
