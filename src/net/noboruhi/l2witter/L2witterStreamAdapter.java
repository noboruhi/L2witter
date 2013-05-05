package net.noboruhi.l2witter;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.Authorization;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

public class L2witterStreamAdapter extends UserStreamAdapter implements TextProducer {
    private String nextText = "";
    private ArrayList<TimeLineListItem> statusList = new ArrayList<TimeLineListItem>();
    private static final ConcurrentMap<String, Bitmap> cache = new ConcurrentHashMap<String, Bitmap>();
    private static final ArrayList<String> userNameList = new ArrayList<String>();
    private Authorization auth;
    private boolean isFirst = true;
    private ProgressDialog waitDialog = null;

    public void setAuth(Authorization auth) {
        this.auth = auth;
    }

    public void isFirst () {
        this.isFirst = true;
    }

    public void setWaitDialog(ProgressDialog waitDialog) {
        this.waitDialog = waitDialog;
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

    @Override
    public void onStatus(Status status) {
        if (isFirst) {
            if (waitDialog != null) {
                waitDialog.dismiss();
            }
            this.isFirst = false;
        }

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

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        Log.d(Const.LoggerTag,"Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }

    @Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        Log.d(Const.LoggerTag,"Got track limitation notice:" + numberOfLimitedStatuses);
    }

    @Override
    public void onScrubGeo(long userId, long upToStatusId) {
        Log.d(Const.LoggerTag,"Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
    }

    @Override
    public void onException(Exception ex) {
        ex.printStackTrace();
    }

    private static Handler handler = new Handler();
    public String popString() {
        if ( statusList.size() < 1) return "";

        handler.post(new ToastRunner(statusList,context,auth,cache,userNameList));
        // 文字列フィルタ TODO:フィルタの設定化
        for (String textfilter : textFilters) {
            Pattern p = Pattern.compile(textfilter);
            Matcher m = p.matcher(nextText);
            nextText = m.replaceAll("");
        }
        return nextText;
    }
}
