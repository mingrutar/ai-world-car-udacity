package com.coderming.worldcar.ros;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linna on 12/6/2016.
 */
public class ROSMessage {
    public enum Type { accel, brake, steer, twist };
    Type mMessageType;
    int value;

    public ROSMessage(Type type, int val) {
        mMessageType = type;
        value = val;
    }
    public Map<String, String> getNameValues() {
        Map<String, String> ret = new HashMap<>();
        ret.put(mMessageType.name(), Integer.toString(value));
        return ret;
    }
    @Override
    public String toString() {
        return "message "+mMessageType+": value="+value;
    }
    public boolean isTypeOf(Type isType) {
        return mMessageType.equals(isType);
    }
    public int getValue() {
        return value;
    }
}
