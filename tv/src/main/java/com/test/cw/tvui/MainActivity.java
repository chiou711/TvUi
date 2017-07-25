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

package com.test.cw.tvui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MovieList.REQUEST_CONTINUE_PLAY)
        {
            MainFragment.currLinkId++;

            int linksLen = Util.getContentArrayLength(MainFragment.linksArr[MainFragment.currPageId]);
            System.out.println("MainActivity / _onActivityResult / linksLen = " + linksLen);
            // meet boundary
            if(MainFragment.currLinkId >= linksLen)
            {
                MainFragment.currPageId++;
                MainFragment.currLinkId =0;
            }

            int pagesLen = Util.getContentArrayLength(MainFragment.pagesArr);
            if(MainFragment.currPageId >= pagesLen)
                MainFragment.currPageId = 0;

            System.out.println("MainActivity / _onActivityResult / currPageId = " + MainFragment.currPageId);
            System.out.println("MainActivity / _onActivityResult / currLinkId = " + MainFragment.currLinkId);

            String urlStr = MainFragment.linksArr[MainFragment.currPageId][MainFragment.currLinkId];
            String id = Util.getYoutubeId(urlStr);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
            intent.putExtra("force_fullscreen",true);
            intent.putExtra("finish_on_ended",true);
            startActivityForResult(intent,999);
        }
    }
}
