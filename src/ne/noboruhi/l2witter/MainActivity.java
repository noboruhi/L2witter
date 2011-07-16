package ne.noboruhi.l2witter;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_reauth:
            // 再認証設定
            break;

        default:
            break;
        }
        return true;
    }

    private boolean isBackButtonLongPressed = false;
/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == keyCode && 3 < event.getRepeatCount()) {
            Log.d(Const.LoggerTag, "onKeyDown() : back button is long pressed.");
            isBackButtonLongPressed = true;
            return true;
        } else {
            Log.d(Const.LoggerTag, "nowPless : " + keyCode);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(Const.LoggerTag, "nowPless : " + keyCode);


        return super.onKeyUp(keyCode, event);
    }
*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                    3 < event.getRepeatCount()) {
                        Builder builder = new Builder(this);
                        // TODO:定数化・リソース化
                        builder.setMessage("終了しますか?");
                        builder.setPositiveButton("終了", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(Const.LoggerTag, "applicationFinish() : aplication is finished. All processes killed");
                                System.exit(RESULT_OK);
                            }
                        });
                        builder.setNegativeButton("キャンセル", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.setCancelable(true);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
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