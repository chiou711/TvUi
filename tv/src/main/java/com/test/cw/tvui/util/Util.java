package com.test.cw.tvui.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Environment;

import com.test.cw.tvui.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cw on 2017/7/23.
 */

public class Util {

    private static Context mContext;
    private Activity mAct;

    public Util(){}

    public Util(Activity activity) {
        mContext = activity;
        mAct = activity;
    }

    public static String NEW_LINE = "\r" + System.getProperty("line.separator");

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

    // Get YouTube list Id
    public static String getYoutubeListId(String url) {

        String videoId = "";

        if (url != null && url.trim().length() > 0 && url.startsWith("http")) {
            String expression = "^.*((youtu.be/)|(v/)|(/u/w/)|(embed/)|(watch\\?))\\??v?=?([^#&?]*).*list?=?([^#&?]*).*";
            CharSequence input = url;
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);//??? some Urls are NG
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                String groupIndex1 = matcher.group(8);
                if (groupIndex1 != null )
                    videoId = groupIndex1;
            }
        }

        System.out.println("Util / _getYoutubeListId / list_id = " + videoId);
        return videoId;
    }

    // Get YouTube playlist Id
    public static String getYoutubePlaylistId(String url) {

        String videoId = "";

        if (url != null && url.trim().length() > 0 && url.startsWith("http")) {
            String expression = "^.*((youtu.be/)|(v/)|(/u/w/)|(embed/)|(playlist\\?))\\??v?=?([^#&?]*).*list?=?([^#&?]*).*";
            CharSequence input = url;
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);//??? some Urls are NG
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                String groupIndex1 = matcher.group(8);
                if (groupIndex1 != null )
                    videoId = groupIndex1;
            }
        }
        System.out.println("Util / _getYoutubePlaylistId / playlist_id = " + videoId);
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

    // Create assets file
    public static File createAssetsFile(Activity act, String fileName)
    {
        File file = null;
        AssetManager am = act.getAssets();
        InputStream inputStream = null;
        try {
            inputStream = am.open(fileName);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // main directory
        String dirString = Environment.getExternalStorageDirectory().toString() +
                "/" + Util.getStorageDirName(act);

        System.out.println("Util / _createAssetsFile / dirString = " + dirString);
        File dir = new File(dirString);
        if(!dir.isDirectory())
            dir.mkdir();

        String filePath = dirString + "/" + fileName;
        System.out.println("Util / _createAssetsFile / filePath = " + filePath);

        if((inputStream != null)) {
            try {
                file = new File(filePath);
                OutputStream outputStream = new FileOutputStream(file);
                byte buffer[] = new byte[1024];
                int length = 0;

                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                //Logging exception
                System.out.println("Util / _createAssetsFile / inputStream = null" + filePath);
            }
        }
        return file;
    }

    // get App default storage directory name
    static public String getStorageDirName(Context context)
    {
//		return context.getResources().getString(R.string.app_name);

        Resources currentResources = context.getResources();
        Configuration conf = new Configuration(currentResources.getConfiguration());
        conf.locale = Locale.ENGLISH; // apply English to avoid reading directory error
        Resources newResources = new Resources(context.getAssets(),
                currentResources.getDisplayMetrics(),
                conf);
        String appName = newResources.getString(R.string.app_name);

        // restore locale
        new Resources(context.getAssets(),
                currentResources.getDisplayMetrics(),
                currentResources.getConfiguration());

		System.out.println("Util / _getStorageDirName / appName = " + appName);
        return appName;
    }

    static int getContentArrayLength(String[] arr)
    {
        int len = 0;
        for(int i=0; i<arr.length; i++)
        {
            if(!Util.isEmptyString(arr[i]))
                len++;
        }
        return len;
    }

    // set has default import
    public static void setPref_has_default_import(Activity act, boolean has,int position )
    {
        SharedPreferences pref = act.getSharedPreferences("last_time_view", 0);
        String keyName = "KEY_HAS_DEFAULT_IMPORT"+position;
        pref.edit().putBoolean(keyName, has).apply();
    }

    // get has default import
    public static boolean getPref_has_default_import(Context context,int position)
    {
        SharedPreferences pref = context.getSharedPreferences("last_time_view", 0);
        String keyName = "KEY_HAS_DEFAULT_IMPORT"+position;
        return pref.getBoolean(keyName, false);
    }
}
