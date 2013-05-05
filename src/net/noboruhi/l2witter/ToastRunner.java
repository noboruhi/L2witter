package net.noboruhi.l2witter;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import twitter4j.auth.Authorization;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ToastRunner implements Runnable {
    private ArrayList<TimeLineListItem> statusList = new ArrayList<TimeLineListItem>();
    private Context context = null;
    private Authorization auth = null;
    private ConcurrentMap<String, Bitmap> cache = null;
    private ArrayList<String> userNameList = null;

    public ToastRunner(ArrayList<TimeLineListItem> statusList,Context context,
            Authorization auth,ConcurrentMap<String, Bitmap> cache,
            ArrayList<String> userNameList) {
        this.statusList   = statusList;
        this.context      = context;
        this.auth         = auth;
        this.cache        = cache;
        this.userNameList = userNameList;
    }
    
    @Override
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

        IconDownloadTask downloadTask = new IconDownloadTask(auth,cache,userNameList);
        downloadTask.execute((Void[])null);
    }

}
