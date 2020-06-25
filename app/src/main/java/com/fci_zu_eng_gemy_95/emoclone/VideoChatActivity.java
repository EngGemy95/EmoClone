package com.fci_zu_eng_gemy_95.emoclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.fci_zu_eng_gemy_95.emoclone.databinding.ActivityVideoChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity
        implements Session.SessionListener, PublisherKit.PublisherListener {

    ActivityVideoChatBinding binding;
    private static String API_Key = "46810654";
    private static String SESSION_ID = "1_MX40NjgxMDY1NH5-MTU5MzAzMTQwMDQyM35MUERVOENBVGRqcGk1VkpzWjhwRURuU0R-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjgxMDY1NCZzaWc9ZWVmODlmMGQyMzMyYTNjZDhiMGRhOWExODJmNjMzY2ZjZjgxNmMwMzpzZXNzaW9uX2lkPTFfTVg0ME5qZ3hNRFkxTkg1LU1UVTVNekF6TVRRd01EUXlNMzVNVUVSVk9FTkJWR1JxY0drMVZrcHpXamh3UlVSdVUwUi1mZyZjcmVhdGVfdGltZT0xNTkzMDMxNDg0Jm5vbmNlPTAuMzI2MjAyNjExMDc4MDY4MjUmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5NTYyMzQ4NCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    private DatabaseReference userRef;
    private String userID = "";
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_chat);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        binding.closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("Ringing")) {
                            userRef.child(userID).child("Ringing").removeValue();
                            if (mPublisher !=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber !=null){
                                mSubscriber.destroy();
                            }
                            Intent intent = new Intent(VideoChatActivity.this, RegisterActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        if (dataSnapshot.child(userID).hasChild("Calling")) {
                            userRef.child(userID).child("Calling").removeValue();
                            if (mPublisher !=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber !=null){
                                mSubscriber.destroy();
                            }
                            Intent intent = new Intent(VideoChatActivity.this, RegisterActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (mPublisher !=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber !=null){
                                mSubscriber.destroy();
                            }
                            Intent intent = new Intent(VideoChatActivity.this, RegisterActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission() {
        String[] perms = {
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 1- initialize and connect to the session
            mSession = new Session.Builder(this, API_Key, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        } else {
            EasyPermissions.requestPermissions(this,
                    "Hi this app needs Mic and Camera , please allow .",
                    RC_VIDEO_APP_PERM,
                    perms);

        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //2- publishing a stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG,"Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);
        binding.publihserContainer.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");
    }

    //3- subscribing to the streams
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");
        if (mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            binding.subscriberContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");
        if (mSubscriber !=null){
            mSubscriber = null;
            binding.subscriberContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}