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

import java.net.URI;
import java.util.List;
import java.util.Timer;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;
import com.test.cw.tvui.operation.Import_fileListAct;
import com.test.cw.tvui.operation.Import_fileViewAct;

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;
    public static AlertDialog alertDlg;
    public static boolean isNewDB;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        //???  java.lang.NullPointerException:
        // Attempt to invoke virtual method 'void android.support.v17.leanback.app
        // .BackgroundManager$TranslucentLayerDrawable.setWrapperAlpha(int, int)' on a null object reference
//        prepareBackgroundManager();

        setupUIElements();

        // set true at Import runtime
        Import_fileViewAct.isAddingNewFolder = false;

        // dialog for Continue loading
        isNewDB = !Util.getPref_has_default_import(MainActivity.mAct,0);
        if(isNewDB) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Will load data items")
                   .setMessage("It takes a period to load completely.")
                   .setNegativeButton("Wait", listener_wait)
                   .setPositiveButton("No", null);
            alertDlg = builder.create();
            alertDlg.show();

            alertDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    loadItemsByDB(1);
                    setupEventListeners();
                }
            });
        }
        else
        {
            loadItemsByDB(1);
            setupEventListeners();
        }
    }

    DialogInterface.OnClickListener listener_wait = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(getActivity(),"Wait",Toast.LENGTH_LONG).show();//??? not show
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    // load items by DB
    void loadItemsByDB(int folderTableId)
    {
        System.out.println("MainFragment / _loadItemsByDB / folderTableId = " + folderTableId);
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        CardPresenter cardPresenter = new CardPresenter();

        //TODO import DB data
        // prepare items
        int countRows = 0;

        // other
        HeaderItem gridHeader = new HeaderItem(countRows, "Folders");
        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add("1st");
        gridRowAdapter.add("2nd");
        gridRowAdapter.add("3rd");
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        // Only one folder, default folder table id = 1
        DB_folder db_folder = new DB_folder(getActivity(),folderTableId);
        db_folder.open();
        int pagesCount = db_folder.getPagesCount(false);
        System.out.println("MainFragment / _loadItemsByDB / pagesCount = " + pagesCount);
        db_folder.close();

        for(int i = 0; i< pagesCount; i++)
        {
            // Page
            String pageTitle = db_folder.getPageTitle(i,true);

            List<Movie> list = MovieList.setupMoviesByDB(i);
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

            // pages
            if(!Util.isEmptyString(pageTitle)) {
                HeaderItem header = new HeaderItem(i, pageTitle);
                mRowsAdapter.add(new ListRow(header, listRowAdapter));
                System.out.println("MainFragment / _loadItemsByDB / pageTitle = " + pageTitle);
            }

            // links
            int pageTableId = db_folder.getPageTableId(i,true);
            DB_page db_page = new DB_page(getActivity(),pageTableId);
            db_page.open();
            int linkCount = db_page.getNotesCount(false);
            db_page.close();

            for(int j = 0; j<linkCount ; j++)
            {
                String link = db_page.getNoteLinkUri(j,true);
                listRowAdapter.add(list.get(j));
                // verify
                System.out.println("MainFragment / _loadItemsByDB / link = " + link);
            }

            countRows++;
        }

        // set adapter
        setAdapter(mRowsAdapter);
    }

//    private void prepareBackgroundManager() {
//
//        mBackgroundManager = BackgroundManager.getInstance(getActivity());
//        mBackgroundManager.attach(getActivity().getWindow());
//        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
//        mMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
//    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    private void setupEventListeners() {
        System.out.println("_setupEventListeners");

        // Search Click
        setOnSearchClickedListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), Import_fileListAct.class);
                startActivityForResult(intent,MovieList.REQUEST_IMPORT);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }

//    protected void updateBackground(String uri) {
//        int width = mMetrics.widthPixels;
//        int height = mMetrics.heightPixels;
//        Glide.with(getActivity())
//                .load(uri)
//                .centerCrop()
//                .error(mDefaultBackground)
//                .into(new SimpleTarget<GlideDrawable>(width, height) {
//                    @Override
//                    public void onResourceReady(GlideDrawable resource,
//                                                GlideAnimation<? super GlideDrawable>
//                                                        glideAnimation) {
//                        mBackgroundManager.setDrawable(resource);
//                    }
//                });
//        mBackgroundTimer.cancel();
//    }

//    private void startBackgroundTimer() {
//        if (null != mBackgroundTimer) {
//            mBackgroundTimer.cancel();
//        }
//        mBackgroundTimer = new Timer();
//        mBackgroundTimer.schedule(new UpdateBackgroundTask(), BACKGROUND_UPDATE_DELAY);
//    }

    static int currPageId;
    static int currLinkId;
    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
                Movie movie = (Movie) item;
                Log.d(TAG, "Item: " + item.toString());
//                Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                intent.putExtra(DetailsActivity.MOVIE, movie);
//
//                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(),
//                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
//                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
//                getActivity().startActivity(intent, bundle);

                //Launch YouTube by item view click
                currPageId = (int)row.getId();
                currLinkId = (int)movie.getId();
                // get real link Id in row

                for(int i = 0; i< currPageId; i++) {

                    DB_folder db_folder = new DB_folder(MainActivity.mAct,1);
                    int page_table = db_folder.getPageTableId(i,true);

                    DB_page db_page = new DB_page(MainActivity.mAct,page_table);
                    db_page.open();
                    int length = db_page.getNotesCount(false);
                    db_page.close();
                    System.out.println("MainFragment / _onItemClicked / length ("+i+")= "+ length);

                    currLinkId -= length;
                }
                System.out.println("MainFragment / _onItemClicked / currPageId = "+ currPageId);
                System.out.println("MainFragment / _onItemClicked / currLinkId = "+ currLinkId);
                String id = Util.getYoutubeId(movie.getVideoUrl());
                System.out.println("MainFragment / _onItemClicked / YouTube id = "+ id);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
                intent.putExtra("force_fullscreen",true);
                intent.putExtra("finish_on_ended",true);

                // Play once
                getActivity().startActivity(intent);

                // Continue play
//                getActivity().startActivityForResult(intent,MovieList.REQUEST_CONTINUE_PLAY);

            }
            else if (item instanceof String)
            {
//                if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0)
//                if (((String) item).contains(getString(R.string.error_fragment)))
                if (((String) item).contains("1st"))
                {
                    loadItemsByDB(1);
                }
                else if (((String) item).contains("2nd"))
                {
                    loadItemsByDB(2);
                }
                else
                {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
//            if (item instanceof Movie) {
//                mBackgroundURI = ((Movie) item).getBackgroundImageURI();
//                startBackgroundTimer();
//            }

        }
    }

//    private class UpdateBackgroundTask extends TimerTask {
//
//        @Override
//        public void run() {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mBackgroundURI != null) {
//                        updateBackground(mBackgroundURI.toString());
//                    }
//                }
//            });
//
//        }
//    }

    private class GridItemPresenter extends Presenter {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            TextView view = new TextView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(GRID_ITEM_WIDTH, GRID_ITEM_HEIGHT));
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("MainFragment / _onActivityResult / requestCode = " + requestCode);

        if(requestCode == MovieList.REQUEST_IMPORT)
        {
            loadItemsByDB(DB_folder.getFocusFolder_tableId());
        }
    }
}