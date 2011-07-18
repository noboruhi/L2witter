package ne.noboruhi.l2witter;

import java.net.URL;
import java.util.Date;

import twitter4j.Status;

import android.graphics.Bitmap;

public class TimeLineListItem {
    public long statusId;
    public Bitmap profileImage;
    public URL profileImageSrc;
    public String screenName;
    public String statusText;
    public Date statusCreatedAt;

    public TimeLineListItem(Status status) {
        statusId        = status.getId();
        profileImageSrc = status.getUser().getProfileImageURL();
        screenName      = status.getUser().getScreenName();
        statusText      = status.getText();
        statusCreatedAt = status.getCreatedAt();
    }
}
