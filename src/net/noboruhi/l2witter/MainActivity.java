package net.noboruhi.l2witter;

import net.noboruhi.l2witter.R;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private L2witterStreamAdapter listener;
    private static final int PREF_ID = 0;
    private boolean litened = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);

        listener = new L2witterStreamAdapter(this);
        LedView ledView  = (LedView) findViewById(R.id.ledView1);
        ledView.setTextProducer(listener);

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
        /*
        case R.id.menu_reauth:
            L2witterApprication l2wApprication = (L2witterApprication)getApplication();
            l2wApprication.auth();
            break;
            */
        case R.id.menu_stream_config:
            Intent intent = new Intent(this,L2witterPreferenceActivity.class);
            startActivityForResult(intent, PREF_ID);
//            Log.d(Const.LoggerTag, "process returned.");
            break;
        default:
            break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PREF_ID && resultCode == RESULT_FIRST_USER + 1) {
            startView();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d(Const.LoggerTag, "pushed:"+event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
                Builder builder = new Builder(this);
                // TODO:定数化・リソース化
                builder.setMessage("終了しますか?");
                builder.setPositiveButton("終了", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(Const.LoggerTag, "aplication is finished. All processes killed");
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
                return true;
            }
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
            L2witterApprication l2wapp = (L2witterApprication)getApplication();
            try {
                AccessToken ta = l2wapp.getOAuth().getOAuthAccessToken(verifier);
                l2wapp.setOauthAccessToken(ta.getToken(),ta.getTokenSecret());
                l2wapp.startStream();
                startView();
            } catch (Exception e) {
                Log.e(Const.LoggerTag, e.getMessage());
            }
        }
    }

    private void startView() {
        L2witterApprication l2wApp = (L2witterApprication)this.getApplication();

        TwitterStream twitterStream = l2wApp.getTwitterStream();
        if (twitterStream != null) {
            if (!litened) {
                listener.setAuth(twitterStream.getAuthorization());
                twitterStream.addListener(listener);
                litened = true;
            }
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            String stream  = sp.getString(getString(R.string.pref_stream_config_key), "1");
            String hashtag = sp.getString(getString(R.string.pref_hashtag_config_key), Const.DefaultHashTag);

            if ("1".equals(stream)) {
                twitterStream.user();
            } else {
                String[] trackList = new String[]{hashtag};
                twitterStream.user(trackList);
                twitterStream.filter(new FilterQuery(0, null, trackList));
            }
            // TODO:LED View止める
            // TODO:ダイアログとLED Viewどっちがいいかな？
            ProgressDialog waitDialog  = new ProgressDialog(this);
            // TODO:定数化・リソース化
            waitDialog.setTitle("ツイート待機中");
            waitDialog.setMessage("ツイートが流れるまでお待ちください...");
            waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitDialog.show();
            listener.isFirst();
            listener.setWaitDialog(waitDialog);

        }
    }

}