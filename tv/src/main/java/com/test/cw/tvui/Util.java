package com.test.cw.tvui;

import android.os.AsyncTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cw on 2017/7/23.
 */

public class Util {

    // Get YouTube Id
    public static String getYoutubeId(String url) {

        String videoId = "";

        if (url != null && url.trim().length() > 0 && url.startsWith("http")) {
            String expression = "^.*((youtu.be\\/)|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|(watch\\?))\\??(v=)?([^#\\&\\?]*).*";
            CharSequence input = url;
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);//??? some Urls are NG
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                String groupIndex1 = matcher.group(8);
                if (groupIndex1 != null && groupIndex1.length() == 11)
                    videoId = groupIndex1;
            }
        }
        return videoId;
    }

    public static boolean isTimeUp;
    public static Timer longTimer;
    static JsonAsync jsonAsyncTask;

    // Get YouTube title
    public static String getYouTubeTitle(String youtubeUrl)
    {
        URL embeddedURL = null;
        if (youtubeUrl != null)
        {
            try {
                embeddedURL = new URL("http://www.youtube.com/oembed?url=" +
                        youtubeUrl + "&format=json");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        jsonAsyncTask = new JsonAsync();
        jsonAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,embeddedURL);
        isTimeUp = false;
        setupLongTimeout(1000);

        while(isEmptyString(jsonAsyncTask.title) && !isTimeUp)
        {
            //??? add time out?
//    			System.out.print("?");
        }
        isTimeUp = true;
        return jsonAsyncTask.title;
    }

    public synchronized static void setupLongTimeout(long timeout)
    {
        if(longTimer != null)
        {
            longTimer.cancel();
            longTimer = null;
        }

        if(longTimer == null)
        {
            longTimer = new Timer();
            longTimer.schedule(new TimerTask()
            {
                public void run()
                {
                    longTimer.cancel();
                    longTimer = null;
                    //do your stuff, i.e. finishing activity etc.
                    isTimeUp = true;
                }
            }, timeout /*in milliseconds*/);
        }
    }

    public static boolean isEmptyString(String str)
    {
        boolean empty = true;
        if( str != null )
        {
            if(str.length() > 0 )
                empty = false;
        }
        return empty;
    }

}
