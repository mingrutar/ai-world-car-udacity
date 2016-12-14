package com.coderming.worldcar.ros;

import java.util.HashMap;
import java.util.Map;

import geometry_msgs.Vector3;

/**
 * Created by linna on 12/6/2016.
 */
public class ROSTwistMessage extends ROSMessage {
    double[] mLinear;
    double[] mAngular;

    public ROSTwistMessage(Vector3 linear, Vector3 angular) {
        super(Type.twist, Integer.MAX_VALUE);
        mLinear = new double[] {linear.getX(), linear.getY(), linear.getZ()};
        mAngular = new double[] {angular.getX(), angular.getY(), angular.getZ()};
    }
    static public String vector3String(double[] vals) {
        return "x="+vals[0]+",y="+vals[1]+",z="+vals[2];
    }
    private String makeLabel(String str) {
        return mMessageType.name()+"."+str;
    }
    public double[] getAngular() {
        return mAngular;
    }
    public double[] getLinear() {
        return mLinear;
    }
    @Override
    public Map<String, String> getNameValues() {
        Map<String, String> ret = new HashMap<>();
        ret.put(makeLabel("Linear"), vector3String(mLinear));
        ret.put(makeLabel("Angular"), vector3String(mAngular));
        return ret;
    }
    @Override
    public String toString() {
        return "message "+mMessageType+": linear="+vector3String(mLinear)+" and angular="+vector3String(mAngular);
    }
}
