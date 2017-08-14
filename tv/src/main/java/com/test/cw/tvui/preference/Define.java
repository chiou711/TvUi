package com.test.cw.tvui.preference;

import android.app.Activity;

/**
 * Created by CW on 2016/6/16.
 *
 * build apk file size:
 * 1) prefer w/ assets files: 15,483 KB
 *
 * 2) default w/ assets files: 15,483 KB
 *
 * 3) default w/o assets files: 1,173 KB
 *
 * 4) release: 706 KB
 */
public class Define {

    public static boolean DEBUG_MODE = false;
    public static boolean RELEASE_MODE = !DEBUG_MODE;

    /**
     * Set release/debug mode
     * - RELEASE_MODE
     * - DEBUG_MODE
     */
    public static boolean CODE_MODE = DEBUG_MODE; //DEBUG_MODE; //RELEASE_MODE;

    /**
     * Has preferred table
     * - true: need to add preferred/assets/ in build.gradle
     * - false: need to remove preferred/assets/ in build.gradle
     */
    public static boolean HAS_PREFERENCE = true;

    // Apply system default for picture path
    public static boolean PICTURE_PATH_BY_SYSTEM_DEFAULT = true;

    // default table count
    public static int ORIGIN_PAGES_COUNT = 1;//5; // Page1_1, Page1_2, Page1_3, Page1_4, Page1_5
    public static int ORIGIN_FOLDERS_COUNT =15;//2;  // Folder1, Folder2, Folder3

    // default style
    public static int STYLE_DEFAULT = 1;
    public static int STYLE_PREFER = 2;

    public static String getFolderTitle(Activity act, Integer i)
    {
        String title = null;
        if(Define.HAS_PREFERENCE) {
            if (i == 0)
//                title = act.getResources().getString(R.string.prefer_folder_name_local);
                title = "local";
            else if (i == 1)
//                title = act.getResources().getString(R.string.prefer_folder_name_web);
                title = "web";
        }
        else {
//            title = act.getResources().getString(R.string.default_folder_name).concat(String.valueOf(i+1));
            title = "com/test/cw/tvui/folder";
        }
        return title;
    }

    public static String getTabTitle(Activity act,Integer Id)
    {
        String title;

        if(Define.HAS_PREFERENCE) {
//            title = act.getResources().getString(R.string.prefer_page_name).concat(String.valueOf(Id));
            title = "New page";
        }
        else {
//            title = act.getResources().getString(R.string.default_page_name).concat(String.valueOf(Id));
            title = "Note page";
        }
        return title;
    }
}
