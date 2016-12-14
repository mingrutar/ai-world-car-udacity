package com.coderming.worldcar.ros;

/**
 * Created by linna on 12/6/2016.
 */
public interface RosSubCallback {
    void handleMessage(ROSMessage msg);
}
