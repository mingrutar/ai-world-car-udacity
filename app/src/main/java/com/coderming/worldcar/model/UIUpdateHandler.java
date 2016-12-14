package com.coderming.worldcar.model;

/**
 * Created by linna on 12/5/2016.
 */
public interface UIUpdateHandler {
    void updateAccel(int val);
    void updateBrake(int val);
    void updateSteer(int val);
    void updateTwist(double[] linear, double[] angular);
}
