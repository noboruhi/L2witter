package ne.noboruhi.l2witter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.UserStreamAdapter;

public class L2witterStreamAdapter extends UserStreamAdapter implements TextProducer {
    private String nextText = "";
    private ArrayList<TimeLineListItem> statusList = new ArrayList<TimeLineListItem>();
    // 文字列フィルタ。URLの除去なんかに使う。
    private String[] textFilters = {
            // URL
            "https?://[\\w.-/]*"
            // hash tag
            ,"#\\w+"
            // ust
            //,"\\(  live at \\)"
            };

    public ArrayList<TimeLineListItem> getStatusList() {
        return statusList;
    }

    public void onStatus(Status status) {
        nextText = status.getUser().getScreenName() + " - " + status.getText();
        Log.d(Const.LoggerTag,nextText);

        TimeLineListItem item = new TimeLineListItem(status);
        statusList.add(item);
        // 最大サイズを超えたら末尾を消す。
        if (Const.StatusListSizeMax < statusList.size()) {
            statusList.remove(0);
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

    public String popString() {
        if ( statusList.size() < 1) return "";
        TimeLineListItem item = statusList.get(statusList.size() - 1);
        String text = "@" + item.screenName + " - " + item.statusText;

        // 文字列フィルタ TODO:フィルタの設定化
        for (String textfilter : textFilters) {
            Pattern p = Pattern.compile(textfilter);
            Matcher m = p.matcher(nextText);
            nextText = m.replaceAll("");
        }
        return text;
    }
}
