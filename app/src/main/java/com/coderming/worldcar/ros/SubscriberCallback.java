package com.coderming.worldcar.ros;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by linna on 12/6/2016.
 */
public class SubscriberCallback implements RosSubCallback {
    private LinkedBlockingQueue<ROSMessage> mQueue = new LinkedBlockingQueue<>();

    public void printQueue() {
        while (true) {
            try {
                ROSMessage msg = mQueue.take();
                System.out.println(Thread.currentThread()+msg.toString());
            } catch (InterruptedException iex) {
                System.out.println("printQueue caught InterruptedException" );
            }
        }
    }
    @Override
    public void handleMessage(ROSMessage msg) {
        mQueue.add(msg);
    }
}
