package ne.noboruhi.l2witter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.Authorization;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class L2witterStreamAdapter extends UserStreamAdapter implements TextProducer {
    private String nextText = "";
    private ArrayList<TimeLineListItem> statusList = new ArrayList<TimeLineListItem>();
    private static final ConcurrentMap<String, Bitmap> cache = new ConcurrentHashMap<String, Bitmap>();
    private static final ArrayList<String> userNameList = new ArrayList<String>();
    private static final TwitterFactory twitterFactory = new TwitterFactory();
    private Authorization auth;

    public void setAuth(Authorization auth) {
        this.auth = auth;
    }

    // 文字列フィルタ。URLの除去なんかに使う。
    private String[] textFilters = {
            // URL
            "https?://[\\w.-/]*"
            // hash tag
            ,"#\\w+"
            // ust
            ,"\\(  live at \\)"
            };

    public String[] getTextFilters() {
        return textFilters;
    }

    public void setTextFilters(String[] textFilters) {
        this.textFilters = textFilters;
    }

    private Context context = null;

    public L2witterStreamAdapter(Context context) {
        this.context = context;
    }

    public ArrayList<TimeLineListItem> getStatusList() {
        return statusList;
    }

    public void onStatus(Status status) {
        nextText = status.getText();
        Log.d(Const.LoggerTag,nextText);

        TimeLineListItem item = new TimeLineListItem(status);
        statusList.add(item);
        // 最大サイズを超えたら末尾を消す。
        if (Const.StatusListSizeMax < statusList.size()) {
            statusList.remove(0);
        }
        // 画像取得の準備
        String userName = status.getUser().getScreenName();
        if (! cache.containsKey(userName)) {
            userNameList.add(userName);
            Log.d(Const.LoggerTag,"add cacheList:@" + userName);
        }
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        Log.d(Const.LoggerTag,"Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        Log.d(Const.LoggerTag,"Got track limitation notice:" + numberOfLimitedStatuses);
    }

    public void onScrubGeo(long userId, long upToStatusId) {
        Log.d(Const.LoggerTag,"Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
    }

    public void onException(Exception ex) {
        ex.printStackTrace();
    }

    private static Handler handler = new Handler();
    public String popString() {
        if ( statusList.size() < 1) return "";

        handler.post(new Runnable() {
            public void run() {
                String screenName = statusList.get(statusList.size() - 1).screenName;
                LayoutInflater inflater = LayoutInflater.from(context);
                View view = inflater.inflate(R.layout.custom_toast,null);
                TextView textView = (TextView)view.findViewById(R.id.textview);
                ImageView iv = (ImageView)view.findViewById(R.id.imageview);
                //Toast toast = Toast.makeText(context,screenName,Toast.LENGTH_LONG);
                Toast toast = new Toast(context);
                textView.setText("@" + screenName);
                if (cache.containsKey(screenName)) {
                    iv.setImageBitmap(cache.get(screenName));
                    Log.d(Const.LoggerTag, "Set icon :@"+screenName);
                }
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(view);
                toast.show();

                AsyncTask<Void, Void, Void> downloadTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        // TwitterのAPIでまとめてURLを求める。
                        Twitter twitter = twitterFactory.getInstance(auth);
                        ResponseList<User> responseList;
                        try {
                            responseList = twitter.lookupUsers(userNameList.toArray(new String[0]));
                        //
                        for (User user : responseList) {
                            URL url =  user.getProfileImageURL();
                            Log.d(Const.LoggerTag, "Start Get icon url: "+url);
                            URLConnection connection = url.openConnection();
                            InputStream inputStream = connection.getInputStream();
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (bitmap != null) {
                              cache.put(user.getScreenName(),bitmap );
                              Log.d(Const.LoggerTag, "Chached icon :@"+user.getScreenName());
                            }
                        }
                        } catch (TwitterException e) {
                            Log.e(Const.LoggerTag,  e.getMessage());
                        } catch (IOException e) {
                            Log.e(Const.LoggerTag,  e.getMessage());
                        } finally {
                            userNameList.clear();
                        }
                        return null;
                    }

                };
                downloadTask.execute((Void[])null);
            }
        });
        // 文字列フィルタ TODO:フィルタの設定化
        for (String textfilter : textFilters) {
            Pattern p = Pattern.compile(textfilter);
            Matcher m = p.matcher(nextText);
            nextText = m.replaceAll("");
        }
        return nextText;
    }
}
