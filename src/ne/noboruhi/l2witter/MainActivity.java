package ne.noboruhi.l2witter;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private L2witterStreamAdapter listener;
    private String[] trackList = null;


    public String[] getTrackList() {
        return trackList;
    }

    public void setTrackList(String[] trackList) {
        this.trackList = trackList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        startView();
    }

    private boolean isBackButtonLongPressed = false;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == keyCode && 3 < event.getRepeatCount()) {
            Log.d(Const.LoggerTag, "onKeyDown() : back button is long pressed.");
            isBackButtonLongPressed = true;
        } else {
            Log.d(Const.LoggerTag, "nowPless : " + keyCode);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(Const.LoggerTag, "nowPless : " + keyCode);

        if (!isBackButtonLongPressed) {
            isBackButtonLongPressed = false;
        } else {
            applicationFinish();
        }

        return true;
    }

    /**
     * Application is finished.
     * All process will be killed.
     */
    private void applicationFinish() {
        Log.d(Const.LoggerTag, "applicationFinish() : aplication is finished. All processes killed");
        System.exit(RESULT_OK);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        // TODO:リソースを見るようにする
        if (uri != null && uri.toString().startsWith("l2witter://oauth")) {
            String verifier = uri.getQueryParameter("oauth_verifier");
            L2witterApprication l2app = (L2witterApprication)getApplication();
            try {
                AccessToken ta = l2app.getOAuth().getOAuthAccessToken(verifier);
                l2app.setOauthAccessToken(ta.getToken(),ta.getTokenSecret());
                l2app.startStream();
            } catch (Exception e) {
                Log.e(Const.LoggerTag, e.getMessage());
            }
        }
    }



    private void startView() {
        // ここからtwitter周り
        listener = new L2witterStreamAdapter();
        LedView ledView  = (LedView) findViewById(R.id.ledView1);
        ledView.setTextProducer(listener);

        L2witterApprication l2wApp = (L2witterApprication)this.getApplication();
        // TODO:TrackList管理用UI作る
        //trackList = new String[]{"#l2witter"};
        TwitterStream twitterStream = l2wApp.getTwitterStream();
        if (twitterStream != null) {
            // TODO:listener共有して管理する。
            twitterStream.addListener(listener);
            twitterStream.user();
            if (trackList != null) {
                twitterStream.user(trackList);
                twitterStream.filter(new FilterQuery(0, null, trackList));
            }
        }
    }

}