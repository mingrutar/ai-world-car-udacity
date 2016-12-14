package com.coderming.worldcar.model;

import android.os.AsyncTask;
import android.util.Log;

import com.coderming.worldcar.ros.RosSubCallback;
import com.coderming.worldcar.ros.TopicSub;
import com.coderming.worldcar.ros.ROSMessage;
import com.coderming.worldcar.ros.ROSTwistMessage;

import java.io.IOException;

/**
 * Created by linna on 12/5/2016.
 */
public class AutowareTask extends AsyncTask<String, ROSMessage, Void>
    implements RosSubCallback {
    private static final String LOG_TAG = AutowareTask.class.getSimpleName();

    private static final int DEFAULT_SLEEP = 200;

    private TopicSub mMyNode;

    private UIUpdateHandler mUpdateHandler;

    public AutowareTask( UIUpdateHandler updateHandler) {
        mUpdateHandler = updateHandler;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            mMyNode = new TopicSub(this, strings[0]);
            mMyNode.execute();
            while (!isCancelled()) {
                Thread.sleep(DEFAULT_SLEEP);
            }
        } catch ( InterruptedException | RuntimeException | IOException re) {
            Log.e(LOG_TAG, "AutowareTask caught exception "+re.getMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(ROSMessage... values) {
        ROSMessage msg = values[0];
        if (msg.isTypeOf(ROSMessage.Type.accel) ) {
            mUpdateHandler.updateAccel(msg.getValue());
        } else if (msg.isTypeOf(ROSMessage.Type.brake)) {
            mUpdateHandler.updateBrake(msg.getValue());
        } else if (msg.isTypeOf(ROSMessage.Type.steer)) {
            mUpdateHandler.updateSteer(msg.getValue());
        } else if (msg.isTypeOf(ROSMessage.Type.twist)) {
            ROSTwistMessage tmsg = (ROSTwistMessage) msg;
            mUpdateHandler.updateTwist(tmsg.getLinear(), tmsg.getAngular());
        }
        super.onProgressUpdate(values);
    }

    @Override
    public void handleMessage(ROSMessage rosMessage) {
        publishProgress(rosMessage);
    }
}
