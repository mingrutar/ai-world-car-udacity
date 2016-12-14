package com.coderming.worldcar.ros;

import android.util.Log;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.ros.EnvironmentVariables;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.namespace.NameResolver;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Subscriber;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import runtime_manager.steer_cmd;
import std_msgs.Header;

public class TopicSub extends AbstractNodeMain {
    private static final String LOG_TAG = TopicSub.class.getSimpleName();

    private static final String DEFAULT_MyNAMESPACE = "coderming";
    private static final String DEFAULT_NODENAME = "ai_ros";
    private static final String DEFAULT_MASTER_IP = "192.168.0.196";
    private static final int DEFAULT_MASTER_PORT = 11311;

    private ConnectedNode mNode;
    private NodeMainExecutor mNodeMainExecutor;
    private Subscriber<runtime_manager.steer_cmd> mSteerSub;
    private Subscriber<runtime_manager.accel_cmd> mAccelSub;
    private Subscriber<runtime_manager.brake_cmd> mBrakeSub;
//    private Subscriber<geometry_msgs.TwistStamped> mTwistSub;
//    private Subscriber<nmea_msgs.Sentence> mNmeaSentenceSub;

//    private Log mLog;
    private final Map<GraphName, GraphName> remappings;
    private URI mMasterUri;
    private String mNodeName;
    private String mNameSpace;
    private RosSubCallback mCallback;

    public TopicSub(RosSubCallback cb, Log lod) {
        this(DEFAULT_MyNAMESPACE, DEFAULT_NODENAME, DEFAULT_MASTER_IP, DEFAULT_MASTER_PORT);
        mCallback = cb;
    }
    public TopicSub(RosSubCallback cb, String masterIp) {
        this(DEFAULT_MyNAMESPACE, DEFAULT_NODENAME, masterIp, DEFAULT_MASTER_PORT);
        mCallback = cb;
    }
    private TopicSub(String ns, String nodeName, String masterIp, int masterPort ) {
        remappings = new HashMap<>();
        mNameSpace = ns;
        mNodeName = nodeName;
        String uriText = String.format("http://%s:%d", masterIp, masterPort);
        try {
            Log.v(LOG_TAG, "++++TopicSub masterIp="+masterIp+",masterPort="+masterPort);
            mMasterUri = new URI(uriText);	// https://docs.oracle.com/javase/tutorial/networking/urls/urlInfo.html
            System.out.println("TopicSub masteruri="+mMasterUri.toString()+",host="+mMasterUri.getHost()+",port="+mMasterUri.getPort());
        } catch (URISyntaxException e) {
            System.out.println("TopicSub caught exception:" + e.getMessage());
        }
    }
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of(mNodeName);
    }
    @Override
    public void onStart(ConnectedNode connectedNode) {
        Log.v(LOG_TAG, "++++TopicSub onStart() called") ;
        if (connectedNode != null) {
            mNode = connectedNode;
//            mLog = connectedNode.getLog();
            initSubscribers(connectedNode);
        } else {
            Log.e(LOG_TAG, "++++TopicSub onStart() cannot continue") ;
        }
    }
    @Override
    public void onShutdown(Node node) {
        Log.v(LOG_TAG, "++++TopicSub onShutdown() called") ;
        unSubscriber();
        mNode.shutdown();
        mNode = null;
        super.onShutdown(node);
    }

    private void unSubscriber() {
        if (mSteerSub != null) {
            mSteerSub.shutdown();
            mAccelSub.shutdown();
            mBrakeSub.shutdown();
//            mTwistSub.shutdown();
//            mNmeaSentenceSub.shutdown();
        }
    }
    private void initSubscribers(ConnectedNode node) {
        mSteerSub = node.newSubscriber("/steer_cmd", "runtime_manager/steer_cmd");
        mSteerSub.addMessageListener( new MessageListener<steer_cmd>() {
            @Override
            public void onNewMessage(runtime_manager.steer_cmd msg) {
                Header header = msg.getHeader();
                String hdr=String.format("Header:seq=%d, frameId=%s",header.getSeq(), header.getFrameId());
                Log.i(LOG_TAG, "++++steer: tid="+Thread.currentThread().getId()+", hdr="+hdr);
                mCallback.handleMessage(new ROSMessage(ROSMessage.Type.steer, msg.getSteer()));
            }
        });

        mAccelSub = node.newSubscriber("/accel_cmd", "runtime_manager/accel_cmd");
        mAccelSub.addMessageListener( new MessageListener<runtime_manager.accel_cmd>() {
            @Override
            public void onNewMessage(runtime_manager.accel_cmd msg) {
                Header header = msg.getHeader();
                String hdr=String.format("Header:seq=%d, frameId=%s",header.getSeq(), header.getFrameId());
                Log.i(LOG_TAG, "++++accel: tid="+Thread.currentThread().getId()+", hdr="+hdr);
                mCallback.handleMessage(new ROSMessage(ROSMessage.Type.accel, msg.getAccel()));
            }
        });

        mBrakeSub = node.newSubscriber("/brake_cmd", runtime_manager.brake_cmd._TYPE);
        mBrakeSub.addMessageListener( new MessageListener<runtime_manager.brake_cmd>() {
            @Override
            public void onNewMessage(runtime_manager.brake_cmd msg) {
                Header header = msg.getHeader();
                String hdr=String.format("Header:seq=%d, frameId=%s",header.getSeq(), header.getFrameId());
                Log.i(LOG_TAG, "++++brake: tid="+Thread.currentThread().getId()+", hdr="+hdr);
                mCallback.handleMessage(new ROSMessage(ROSMessage.Type.brake, msg.getBrake()));
            }
        });

//        mTwistSub = node.newSubscriber("/twist_cmd", geometry_msgs.TwistStamped._TYPE);
//        mTwistSub.addMessageListener( new MessageListener<geometry_msgs.TwistStamped>() {
//            @Override
//            public void onNewMessage(geometry_msgs.TwistStamped msg) {
//                Header header = msg.getHeader();
//                String hdr=String.format("Header:seq=%d, frameId=%s",header.getSeq(), header.getFrameId());
//                Log.i(LOG_TAG, "++++twist: tid="+Thread.currentThread().getId()+", hdr="+hdr);
//                geometry_msgs.Twist aTwist = msg.getTwist();
//                ROSMessage rosmsg = new ROSTwistMessage(aTwist.getLinear(), aTwist.getAngular());
//                mCallback.handleMessage(rosmsg);
//            }
//        });
        // TODO: did not receive any message
//        mNmeaSentenceSub = node.newSubscriber("/nmea_sentence", nmea_msgs.Sentence._TYPE);
//        mNmeaSentenceSub.addMessageListener( new MessageListener<nmea_msgs.Sentence>() {
//            @Override
//            public void onNewMessage(nmea_msgs.Sentence sentence) {
//                Header header = sentence.getHeader();
//                String hdr=String.format("Header:seq=%d, frameId=%s",header.getSeq(), header.getFrameId());
//                mLog.info("nmea_sentence: tid="+Thread.currentThread().getId()+", hdr="+hdr);
//                //		    mCallback.handleSentence(sentence.getSentence());
//            }
//        });
    }
    private String getLocalIP() throws IOException {
        Socket socket = new Socket(mMasterUri.getHost(), mMasterUri.getPort());
        String local_address = socket.getLocalAddress().getHostAddress();
        socket.close();
        return local_address;
    }
    private NodeConfiguration intiConfig() throws IOException {
        String local_address = getLocalIP();
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(local_address);
        nodeConfiguration.setParentResolver(buildParentResolver());
        nodeConfiguration.setRosRoot(null);				// no EnvironmentVariables.ROS_ROOT
        nodeConfiguration.setRosPackagePath(getRosPackagePath());		// no EnvironmentVariables.ROS_PACKAGE_PATH
        nodeConfiguration.setMasterUri(mMasterUri);
        nodeConfiguration.setNodeName(mNodeName);
        return nodeConfiguration;
    }
    private NameResolver buildParentResolver() {
        GraphName namespace = GraphName.root();
        namespace = GraphName.of(mNameSpace).toGlobal();
        return new NameResolver(namespace, remappings);
    }
    private List<File> getRosPackagePath() {
        List<File> paths = Lists.newArrayList();
        String rosPackagePath = EnvironmentVariables.ROS_PACKAGE_PATH;
        if (rosPackagePath != null) {
            for (String path : rosPackagePath.split(File.pathSeparator)) {
                paths.add(new File(path));
            }
        }
        return paths;
    }
    public void execute() throws IOException {
        try {
            Log.v(LOG_TAG, "++++execute called") ;
            Preconditions.checkState(true);
            NodeConfiguration nodeConfig = intiConfig();
            mNodeMainExecutor = DefaultNodeMainExecutor.newDefault();
            mNodeMainExecutor.execute(this, nodeConfig);
        } finally {
            unSubscriber();
            if (mNodeMainExecutor != null) {
                mNodeMainExecutor.shutdown();
            }
        }
    }
    public static void main(String[] args) {
        TopicSub myNode = null;
        try {
            SubscriberCallback cb = new SubscriberCallback();
            myNode = new TopicSub(cb, TopicSub.DEFAULT_MASTER_IP);
            myNode.execute();
            cb.printQueue();
//			TopicSub.execute(TopicSub.DEFAULT_MASTER_IP);
        } catch (Exception re) {
            System.out.println("Main exit due to exception: " + re.getMessage());
            re.printStackTrace();
        }
    }
}
