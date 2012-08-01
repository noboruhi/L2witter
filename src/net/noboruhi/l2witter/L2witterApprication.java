package net.noboruhi.l2witter;

import net.noboruhi.l2witter.R;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;

public class L2witterApprication extends Application {
    private static L2witterApprication sInstance;
    private static TwitterStream twitterStream;
    private boolean isAuthed = false;
    private OAuthAuthorization oAuth;
    private String oauthAccessToken;
    private String oauthAccessTokenSecret;
    private ConfigurationBuilder cbuilder = new ConfigurationBuilder();
    private static TwitterStreamFactory streamFactory = new TwitterStreamFactory();


    public static L2witterApprication getInstance() {
      return sInstance;
    }

    public TwitterStream getTwitterStream(){
        return twitterStream;
    }

    @Override
    public void onCreate() {
      super.onCreate();
      sInstance = this;
      sInstance.initializeInstance();
    }

    public boolean getIsAuthed() {
        return isAuthed;
    }

    public OAuthAuthorization getOAuth() {
        return oAuth;
    }

    public String getOauthAccessToken() {
        return oauthAccessToken;
    }

    public String getOauthAccessTokenSecret() {
        return oauthAccessTokenSecret;
    }

    public void setOauthAccessToken(String oauthAccessToken,String oauthAccessTokenSecret) {
        this.oauthAccessToken = oauthAccessToken;
        this.oauthAccessTokenSecret = oauthAccessTokenSecret;
        SharedPreferences oaPref = getSharedPreferences("oauth", MODE_PRIVATE);
        Editor editor = oaPref.edit();
        editor.putString("oauthAccessToken", oauthAccessToken);
        editor.putString("oauthAccessTokenSecret", oauthAccessTokenSecret);
        editor.commit();
        cbuilder = new ConfigurationBuilder();
        cbuilder.setDebugEnabled(true)
            .setOAuthConsumerKey(Const.OAuthConsumerKey)
            .setOAuthConsumerSecret(Const.OAuthConsumerSecret);

    }

    protected void initializeInstance() {
        // 認証周りの処理
        // TODO:定数化
        SharedPreferences oaPref = getSharedPreferences("oauth", MODE_PRIVATE);

        oauthAccessToken        = oaPref.getString("oauthAccessToken","");
        oauthAccessTokenSecret  = oaPref.getString("oauthAccessTokenSecret","");

        cbuilder.setDebugEnabled(true)
            .setOAuthConsumerKey(Const.OAuthConsumerKey)
            .setOAuthConsumerSecret(Const.OAuthConsumerSecret);

        if ("".equals(oauthAccessToken) || "".equals(oauthAccessTokenSecret) ) {
            auth();
        } else {
            startStream();
        }

    }

    private void auth() {
        Configuration conf = cbuilder.build();
        oAuth = new OAuthAuthorization(conf);
        oAuth.setOAuthAccessToken(null);
        String authUrl = null;
        try {
            // TODO:定数化
            authUrl = oAuth.getOAuthRequestToken("l2witter://oauth").getAuthorizationURL();
        } catch (Exception e) {
            Log.e(Const.LoggerTag, e.getMessage());
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void startStream() {
        cbuilder.setOAuthAccessToken(oauthAccessToken)
            .setOAuthAccessTokenSecret(oauthAccessTokenSecret);
        Log.d(Const.LoggerTag, "oauthAccessToken:"+ oauthAccessToken);
        Log.d(Const.LoggerTag, "oauthAccessTokenSecret:" + oauthAccessTokenSecret);
        oAuth = new OAuthAuthorization(cbuilder.build());
        twitterStream = streamFactory.getInstance(oAuth);
    }
}
