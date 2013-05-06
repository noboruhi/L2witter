package net.noboruhi.l2witter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.Authorization;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

/**
 * アイコンをダウンロードするタスク
 * @author noboruhi
 *
 */
public class IconDownloadTask extends AsyncTask<Void, Void, Void> {
    private Authorization auth = null;
    private ConcurrentMap<String, Bitmap> cache = null;
    private ArrayList<String> userNameList = null;
    private static final TwitterFactory twitterFactory = new TwitterFactory();

    /**
     * コンストラクタ
     * @param auth
     * @param cache
     * @param userNameList
     */
    public IconDownloadTask(Authorization auth,ConcurrentMap<String, Bitmap> cache,
            ArrayList<String> userNameList) {
        this.auth = auth;
        this.cache = cache;
        this.userNameList = userNameList;
    }
    
    /**
     * ユーザアイコンを取得するバックグラウンド処理
     */
    @Override
    protected Void doInBackground(Void... params) {
        Twitter twitter = twitterFactory.getInstance(auth);
        // ユーザ情報をまとめて取得
        ResponseList<User> responseList = null;
        try {
            responseList = twitter.lookupUsers(userNameList.toArray(new String[0]));
        } catch (TwitterException e) {
            Log.e(Const.LOGGER_TAG,  e.getMessage());
        }
        // まとめて取得したユーザ名に対してアイコンを得る
        try {
            for (User user : responseList) {
                URL url =  new URL(user.getProfileImageURL());
                Log.d(Const.LOGGER_TAG, "Start Get icon url: "+url);
                URLConnection connection = url.openConnection();
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap != null) {
                    cache.put(user.getScreenName(),bitmap );
                    Log.d(Const.LOGGER_TAG, "Chached icon :@"+user.getScreenName());
                }
            }
        } catch (IOException e) {
            Log.e(Const.LOGGER_TAG,  e.getMessage());
        } finally {
            // 取得完了したら名前リストをクリアする
            userNameList.clear();
        }
        return null;
    }

}
