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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.widget.TextView;

import com.test.cw.tvui.R;
import com.test.cw.tvui.db.DB_drawer;
import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;
import com.test.cw.tvui.folder.Folder;
import com.test.cw.tvui.operation.Import_fileView;
import com.test.cw.tvui.preference.Define;
import com.test.cw.tvui.util.Util;

import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

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

        // enable auto play for older YouTube App
        if(Util.getYouTube_verNumber(this) <= 10311100)
            Define.AUTO_PLAY_NEXT = true;
        else
            Define.AUTO_PLAY_NEXT = false;
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
        System.out.println("MainActivity / _onActivityResult  / requestCode = " + requestCode);
        if(Define.AUTO_PLAY_NEXT)
        {
            if (requestCode == MovieList.REQUEST_CONTINUE_PLAY) {
                MainFragment.currLinkId++;
                System.out.println("MainActivity / _onActivityResult / MainFragment.currLinkId = " + MainFragment.currLinkId);

                count = 3; // waiting time to next
                builder = new AlertDialog.Builder(this);

                String link = getYouTubeLink();
                nextLinkTitle =  Util.getYouTubeTitle(link);

                countStr = "If not running, please press No within " + count + " seconds.";//TODO lcoale
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
            countStr = "If not running, please press No within " + count + " seconds.";//TODO locale
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

    int delay = 10;
    /**
     *  launch next YouTube intent
     */
    void launchNextYouTubeIntent()
    {
        System.out.println("MainActivity / _launchNextYouTubeIntent / MainFragment.currLinkId = " + MainFragment.currLinkId);
        System.out.println("MainActivity / _launchNextYouTubeIntent / MainFragment.getCurrLinksLength() = " + MainFragment.getCurrLinksLength());
//        if(MainFragment.currLinkId >= MainFragment.getCurrLinksLength())
        //refer: https://developer.android.com/reference/android/view/KeyEvent.html#KEYCODE_DPAD_DOWN_RIGHT

        // check if at the end of row
        if(MainFragment.currLinkId == 0)
        {
            // from test result current capability is shift left 15 steps only
            DPadAsyncTask task = new DPadAsyncTask(MainFragment.getCurrLinksLength());
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else
        {
            // delay
            try {
                Thread.sleep(delay*100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BaseInputConnection mInputConnection = new BaseInputConnection(findViewById(R.id.main_browse_fragment), true);
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_DPAD_RIGHT));
            mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_DPAD_RIGHT));

            // delay
            try {
                Thread.sleep(delay * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String urlStr = getYouTubeLink();
            // intent
            String id = Util.getYoutubeId(urlStr);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
            if(Util.getYouTube_verNumber(this) <= 10311100){
                intent.putExtra("force_fullscreen", true);
                intent.putExtra("finish_on_ended", true);
            }
            startActivityForResult(intent, MovieList.REQUEST_CONTINUE_PLAY);
        }
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


    private class DPadAsyncTask extends AsyncTask<Void, Integer, Void> {
        int dPadSteps;
        DPadAsyncTask(int dPadSteps)
        {
            this.dPadSteps = dPadSteps;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            BaseInputConnection mInputConnection = new BaseInputConnection(findViewById(R.id.main_browse_fragment), true);

            // point to first item of current row
            for(int i=0;i<dPadSteps;i++)
            {
                mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_DPAD_LEFT));
                mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_DPAD_LEFT));
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // add delay to make sure key event works
            try {
                Thread.sleep(delay * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // point to first row if meets the end of last row
            if(MainFragment.currPageId == 0)
            {
                for (int i = (MainFragment.getCurrPagesLength()-1); i >= 1; i--) {
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_DPAD_UP));
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_DPAD_UP));
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                // point to next row
                BaseInputConnection connection = new BaseInputConnection(findViewById(R.id.main_browse_fragment), true);
                connection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_DPAD_DOWN));
                connection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KEYCODE_DPAD_DOWN));
            }

            // add delay for viewer
            try {
                Thread.sleep(delay * 100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            String urlStr = getYouTubeLink();
            // intent
            String id = Util.getYoutubeId(urlStr);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
            if(Util.getYouTube_verNumber(MainActivity.this) <= 10311100){
                intent.putExtra("force_fullscreen", true);
                intent.putExtra("finish_on_ended", true);
            }
            startActivityForResult(intent, MovieList.REQUEST_CONTINUE_PLAY);
        }
    }

}
