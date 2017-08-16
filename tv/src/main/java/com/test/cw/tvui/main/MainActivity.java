/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.test.cw.tvui.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.test.cw.tvui.R;
import com.test.cw.tvui.db.DB_drawer;
import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;
import com.test.cw.tvui.folder.Folder;
import com.test.cw.tvui.operation.Import_fileView;
import com.test.cw.tvui.preference.Define;
import com.test.cw.tvui.util.Util;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity {
    public DB_drawer mDb_drawer;
    /**
     * Called when the activity is first created.
     */
    public Activity act;
    Context context;
    final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 98;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        act = this;
        context = getApplicationContext();


        //refer: https://developer.android.com/training/permissions/requesting.html#perm-request
        // runtime permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)//api23
        {
            // check permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                                                  new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                Manifest.permission.READ_EXTERNAL_STORAGE  },
                                                  PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            else
            {
                final boolean ENABLE_DB_CHECK = false;//true;//false
                if(ENABLE_DB_CHECK)
                    Folder.listAllPageTables(act);
            }
        }
        else
            createDB_data();
    }

    // callback of granted permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        System.out.println("grantResults.length =" + grantResults.length);
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    createDB_data();
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }//case
        }//switch
    }

    AlertDialog.Builder builder;
    AlertDialog alertDlg;
    Handler handler;
    int count;
    String countStr;
    String nextLinkTitle;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(Define.AUTO_PLAY_NEXT)
        {
            if (requestCode == MovieList.REQUEST_CONTINUE_PLAY) {
                MainFragment.currLinkId++;
                System.out.println("MainActivity / _onActivityResult / MainFragment.currLinkId = " + MainFragment.currLinkId);

                count = 2; // waiting time to next
                builder = new AlertDialog.Builder(this);

                String link = getYouTubeLink();
                nextLinkTitle =  Util.getYouTubeTitle(link);

                countStr = "If not running, please press No within " + count + " seconds.";
                countStr = countStr.replaceFirst("[0-9]",String.valueOf(count));
                builder.setTitle("Continue running next link?")
                        .setMessage(nextLinkTitle +"\n\n" + countStr)
                        .setPositiveButton("No", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog1, int which1)
                            {
                                alertDlg.dismiss();
                                cancelYouTubeHandler();
                            }
                        });
                alertDlg = builder.create();

                // set listener for selection
                alertDlg.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dlgInterface) {
                        handler = new Handler();
                        handler.postDelayed(runCountDown,1000);
                    }
                });
                alertDlg.show();
            }
        }
    }

    /**
     *  get YouTube link
     */
    String getYouTubeLink()
    {
        // page
        DB_folder db_folder = new DB_folder(this, DB_folder.getFocusFolder_tableId());
        int pageTableId = db_folder.getPageTableId(MainFragment.currPageId, true);

        // link
        DB_page db_page = new DB_page(act, pageTableId);
        db_page.open();
        int linksLen = db_page.getNotesCount(false);
        db_page.close();
        System.out.println("MainActivity / _onActivityResult / linksLen = " + linksLen);

        // meet boundary
        if (MainFragment.currLinkId >= linksLen) {
            MainFragment.currPageId++;
            MainFragment.currLinkId = 0;
        }

        // new page
        db_folder = new DB_folder(this, DB_folder.getFocusFolder_tableId()); //1
        db_folder.open();
        int pagesLen = db_folder.getPagesCount(false);
        db_folder.close();

        if (MainFragment.currPageId >= pagesLen)
            MainFragment.currPageId = 0;

        System.out.println("MainActivity / _onActivityResult / currPageId = " + MainFragment.currPageId);
        System.out.println("MainActivity / _onActivityResult / currLinkId = " + MainFragment.currLinkId);

        // link
        pageTableId = db_folder.getPageTableId(MainFragment.currPageId, true);
        db_page = new DB_page(act, pageTableId);
        String urlStr = db_page.getNoteLinkUri(MainFragment.currLinkId, true);
        return urlStr;
    }

    void cancelYouTubeHandler()
    {
        if(handler != null) {
            handler.removeCallbacks(runCountDown);
            handler = null;
        }
    }

    /**
     * runnable for counting down
     */
    Runnable runCountDown = new Runnable() {
        public void run() {
            // show count down
            TextView messageView = (TextView) alertDlg.findViewById(android.R.id.message);
            count--;
            countStr = "If not running, please press No within " + count + " seconds.";
            countStr = countStr.replaceFirst("[0-9]",String.valueOf(count));
            messageView.setText(nextLinkTitle + "\n\n" +countStr);

            if(count>0)
                handler.postDelayed(runCountDown,1000);
            else
            {
                // launch next intent
                alertDlg.dismiss();
                cancelYouTubeHandler();
                launchNextYouTubeIntent();
            }
        }
    };

    /**
     *  launch next YouTube intent
     */
    void launchNextYouTubeIntent()
    {
        String urlStr = getYouTubeLink();
        // intent
        String id = Util.getYoutubeId(urlStr);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
        intent.putExtra("force_fullscreen", true);
        intent.putExtra("finish_on_ended", true);
        startActivityForResult(intent, MovieList.REQUEST_CONTINUE_PLAY);
    }

    void createDB_data()
    {
        // init DB
        mDb_drawer = new DB_drawer(context);

        if(Define.HAS_PREFERENCE)
        {
            // Check preference and Create default tables
            if( !Util.getPref_has_default_import(act,0) )
            {
                for(int i=1;i<=Define.ORIGIN_FOLDERS_COUNT;i++)
                {
                    MainFragment.isNewDB = true;
                    DB_folder.setFocusFolder_tableId(i);

                    // import default tables
                    Import_fileView.createDefaultTables(act, i);
                }
            }
            else
                MainFragment.isNewDB = false;
        }

    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//
//        if (event.getAction() == KeyEvent.ACTION_DOWN) {
//            System.out.println("Key down, code " + event.getKeyCode());
//
//        } else if (event.getAction() == KeyEvent.ACTION_UP) {
//            System.out.println("Key up, code " + event.getKeyCode());
//        }
//
//        return false;
//    }

}
