package com.example.android.common.logger;

import android.app.Activity;
import android.content.Context;
import android.util.*;
import android.widget.TextView;

/** Simple TextView which is used to output log data.
 */
public class LogView extends TextView {

    public LogView(Context context) {
        super(context);
    }

    public LogView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LogView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Formats the log data and prints it out to the LogView.
     * @param msg The actual message to be logged. The actual message to be logged.
     */
    public void println(final String msg) {

        // In case this was originally called from an AsyncTask or some other off-UI thread,
        // make sure the update occurs within the UI thread.
        ((Activity) getContext()).runOnUiThread( (new Thread(new Runnable() {
            @Override
            public void run() {
                // Display the text we just generated within the LogView.
                appendToLog(msg);
            }
        })));
    }

    /** Outputs the string as a new line of log data in the LogView. */
    public void appendToLog(String s) {
        append("\n" + s);
    }
}