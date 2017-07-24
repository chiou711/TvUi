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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Timer;

import android.app.Activity;
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

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 200;
    private static final int GRID_ITEM_HEIGHT = 200;
    static final int NUM_ROWS = 5; //TODO rows
    static final int NUM_COLS = 150;//TODO columns

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);

        //???  java.lang.NullPointerException:
        // Attempt to invoke virtual method 'void android.support.v17.leanback.app
        // .BackgroundManager$TranslucentLayerDrawable.setWrapperAlpha(int, int)' on a null object reference
//        prepareBackgroundManager();

        setupUIElements();

        loadRows();

        setupEventListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    private void loadRows() {
//        List<Movie> list = MovieList.setupMovies();

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        CardPresenter cardPresenter = new CardPresenter();

        //TODO import asset file
        // get file name
        int position  = 0;
        String fileName = "default"+ (position+1) + ".xml";

        // extract
        createDefaultRows(getActivity(), fileName);


        // verify
        int countColumns = 0;
        int countRows = 0;
        for(int i=0;i<Import_handleXmlFile.pageArr.length;i++)
        {
            String pageTitle = Import_handleXmlFile.pageArr[i];
            if(!Util.isEmptyString(pageTitle)) {
                countRows++;
                System.out.println("Import_handleXmlFile.pageArr[" + i + "]=" + pageTitle);
            }
            for(int j=0;j<Import_handleXmlFile.linkArr[i].length;j++)
            {
                String link = Import_handleXmlFile.linkArr[i][j];
                if(!Util.isEmptyString(link)) {
                    countColumns++;
                    System.out.println("Import_handleXmlFile.linkArr[" + i + "][" + j + "]=" + link);
                }
            }
        }

        // prepare for setup movies
        int i;
        for (i = 0; i < countRows; i++)
        {
            List<Movie> list = MovieList.setupMovies(i);
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

            for (int j = 0; j < countColumns; j++)
            {
                listRowAdapter.add(list.get(j));
            }

//            HeaderItem header = new HeaderItem(i, MovieList.MOVIE_CATEGORY[i]);
            HeaderItem header = new HeaderItem(i, Import_handleXmlFile.pageArr[i]);
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        HeaderItem gridHeader = new HeaderItem(i, "PREFERENCES");

        GridItemPresenter mGridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(mGridPresenter);
        gridRowAdapter.add(getResources().getString(R.string.grid_view));
        gridRowAdapter.add(getString(R.string.error_fragment));
        gridRowAdapter.add(getResources().getString(R.string.personal_settings));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);

    }

    Import_handleXmlFile importObject;
    //TODO create default rows
    public void createDefaultRows(Activity act, String fileName)
    {
        System.out.println("Import_selectedFileAct / _createDefaultRows / fileName = " + fileName);

        FileInputStream fileInputStream = null;
        File assetsFile = Util.createAssetsFile(act,fileName);
        try
        {
            fileInputStream = new FileInputStream(assetsFile);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        // import data by HandleXmlByFile class
        importObject = new Import_handleXmlFile(fileInputStream,act);
        importObject.handleXML();
        while(importObject.parsingComplete){
        }

    }


    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

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
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Implement your own in-app search", Toast.LENGTH_LONG)
                        .show();
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

                //TODO: launch YouTube by item view click
                String id = Util.getYoutubeId(movie.getVideoUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + id));
                intent.putExtra("force_fullscreen",true);
                intent.putExtra("finish_on_ended",true);
                getActivity().startActivity(intent);

            } else if (item instanceof String) {
                if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0) {
                    Intent intent = new Intent(getActivity(), BrowseErrorActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), ((String) item), Toast.LENGTH_SHORT)
                            .show();
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

}
