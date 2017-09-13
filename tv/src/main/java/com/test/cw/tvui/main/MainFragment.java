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

import java.net.URI;
import java.util.List;
import java.util.Timer;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.BaseInputConnection;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.test.cw.tvui.R;
import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;
import com.test.cw.tvui.operation.Import_fileListAct;
import com.test.cw.tvui.operation.Import_fileView;
import com.test.cw.tvui.preference.Define;
import com.test.cw.tvui.util.Util;
import com.test.cw.tvui.details.DetailsActivity;
import com.test.cw.tvui.details.VideoDetailsFragment;

public class MainFragment extends BrowseFragment {
    private static final String TAG = "MainFragment";

    private static final int BACKGROUND_UPDATE_DELAY = 300;
    private static final int GRID_ITEM_WIDTH = 100;//200;
    private static final int GRID_ITEM_HEIGHT = 100;//200;

    private final Handler mHandler = new Handler();
    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private URI mBackgroundURI;
    private BackgroundManager mBackgroundManager;
    public static AlertDialog alertDlg;
    public static boolean isNewDB;
    View rootView;
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
        Import_fileView.isAddingNewFolder = false;

        // dialog for Continue loading
        isNewDB = !Util.getPref_has_default_import(getActivity(),0);
        if(isNewDB) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Will load data items")//TODO locale
                   .setMessage("It takes a period to load completely.")
                   .setNegativeButton("Wait", listener_wait)
                   .setPositiveButton("No", null);
            alertDlg = builder.create();
            alertDlg.show();

            alertDlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    loadItemsAsync(1);
                }
            });
        }
        else
        {
            int lastTimeView_folderTableId = Util.getPref_lastTimeView_folder_tableId(getActivity());
            isKeyEventConsumed = true;
            currFolderNum = lastTimeView_folderTableId;
            loadItemsAsync(currFolderNum);//TODO add last time viewed
        }
    }

    DialogInterface.OnClickListener listener_wait = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(getActivity(),"Wait",Toast.LENGTH_LONG).show();//??? not show //TODO local
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        System.out.println("MainFragment / _onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
    }

    // load items by DB
    public void loadItemsByDB(int folderTableId)
    {
        System.out.println("MainFragment / _loadItemsByDB / folderTableId = " + folderTableId);
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());

        CardPresenter cardPresenter = new CardPresenter();

        //import DB data
        // prepare items
        int countRows = 0;

        // header
        HeaderItem gridHeader = new HeaderItem(countRows, "Folders"); //TODO locale

        // folders row
        GridItemPresenter gridPresenter = new GridItemPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(gridPresenter);

        for(int i=1;i<= Define.ORIGIN_FOLDERS_COUNT;i++) {
            gridRowAdapter.add(String.valueOf(i));
        }

        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        // Only one folder, default folder table id = 1
        DB_folder db_folder = new DB_folder(getActivity(),folderTableId);
        db_folder.open();
        int pagesCount = db_folder.getPagesCount(false);
        System.out.println("MainFragment / _loadItemsByDB / pagesCount = " + pagesCount);
        db_folder.close();

        setCurrPagesLength(pagesCount);

        for(int i = 0; i< pagesCount; i++)
        {
            // Page
            String pageTitle = db_folder.getPageTitle(i,true);

            List<Movie> list = MovieList.setupMoviesByDB(getActivity(),i);
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
//                System.out.println("MainFragment / _loadItemsByDB / link = " + link);
            }
            countRows++;
        }
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
//        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));
    }

    public void setupEventListeners() {
        System.out.println("MainFragment / _setupEventListeners");
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

    static int currFolderNum = 1;
    public static int currPageId;
    public static int currLinkId;
    boolean isKeyEventConsumed;
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

                currPageId = (int)row.getId();

                DB_folder db_folder = new DB_folder(getActivity(),DB_folder.getFocusFolder_tableId());
                int pageTableId = db_folder.getPageTableId(currPageId,true);

                DB_page db_page = new DB_page(getActivity(),pageTableId);
                db_page.open();
                int linksCount = db_page.getNotesCount(false);
                setCurrLinksLength(linksCount);
                db_page.close();

                // get real link Id in row
                for(int i=0;i<linksCount;i++)
                {
                    if(movie.getVideoUrl().equalsIgnoreCase(db_page.getNoteLinkUri(i,true)))
                        currLinkId = i;
                }
                System.out.println("MainFragment / _onItemClicked / currPageId = "+ currPageId);
                System.out.println("MainFragment / _onItemClicked / currLinkId in row = "+ currLinkId);

                if(Define.AUTO_PLAY_NEXT)
                {
                    //Launch YouTube by item view click
                    Util.openLink_YouTube(getActivity(),db_page.getNoteLinkUri(currLinkId,true),MovieList.REQUEST_CONTINUE_PLAY);
                    isKeyEventConsumed = false;
                }
                else
                {
                    // Option: Launch YouTube in DetailsActivity
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra(DetailsActivity.MOVIE, movie);

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                                                getActivity(),
                                                                ((ImageCardView) itemViewHolder.view).getMainImageView(),
                                                                DetailsActivity.SHARED_ELEMENT_NAME )
                                                         .toBundle();
                    startActivityForResult(intent, MovieList.REQUEST_DETAIL,bundle);

                    // Option: Launch YouTube by item view click
//                    Util.openLink_YouTube(getActivity(),movie.getVideoUrl(),0);
                }
            }
            else if (item instanceof String)
            {
//                if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0)
//                if (((String) item).contains(getString(R.string.error_fragment)))

                //bug: BrowseFragment onItemClicked callbacks broken in 25.3.0
                //cf https://stackoverflow.com/questions/44049813/android-tv-rowsfragment-item-click-not-working-in-few-cases
                currFolderNum = Util.getNumberInString(((String) item));
                setupUIElements();
                loadItemsAsync(currFolderNum);

                // set last time viewed folder position
                Util.setPref_lastTimeView_folder_tableId(getActivity(),currFolderNum);

                isKeyEventConsumed = false;
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row)
        {
            if (item instanceof Movie)
            {
//                mBackgroundURI = ((Movie) item).getBackgroundImageURI();
//                startBackgroundTimer();
                // workaround: since no way to set focus for selected item yet
            }

            if (item instanceof String)
            {
//                if (((String) item).indexOf(getString(R.string.error_fragment)) >= 0)
//                if (((String) item).contains(getString(R.string.error_fragment)))

                if(currFolderNum == Integer.valueOf((String) item))
                    setTitle("* " + item + " *");
                else
                    setTitle((String)item);

                // workaround: no way to synchronize focus position with clicked item yet
                if(!isKeyEventConsumed) //??? can not work after App open
                {
                    BaseInputConnection  mInputConnection = new BaseInputConnection(itemViewHolder.view.getRootView(), true);
                    for(int i=1;i<currFolderNum;i++) {
                        mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
                        mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                    }
                    isKeyEventConsumed = true;
                }
            }
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
//            view.setBackgroundColor(getResources().getColor(R.color.default_background));
            view.setBackgroundColor(getResources().getColor(R.color.bar_color));
            view.setTextColor(Color.WHITE);
            view.setGravity(Gravity.CENTER);
            view.setTextSize(25);
            view.setTypeface(Typeface.DEFAULT_BOLD);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            ((TextView) viewHolder.view).setText((String) item);
            ((TextView) viewHolder.view).setTextColor(getResources().getColor(R.color.white));

            int folderNum = Util.getNumberInString(((String) item));
            if(folderNum == currFolderNum)//show focus
            {
                viewHolder.view.setBackgroundColor(getResources().getColor(R.color.search_opaque));

                // run scale animation and make focus item bigger
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_to_bigger);
                viewHolder.view.getRootView().startAnimation(anim);
                anim.setFillAfter(true);
            }
            else
            {
                viewHolder.view.setBackgroundColor(getResources().getColor(R.color.bar_color));

                // run scale animation and make focus item normal
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_to_normal);
                viewHolder.view.getRootView().startAnimation(anim);
                anim.setFillAfter(true);
            }
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
            setupUIElements();
            loadItemsAsync(DB_folder.getFocusFolder_tableId());
        }
        else if((requestCode == MovieList.REQUEST_DETAIL) && VideoDetailsFragment.isDelete)
        {
            // add for fixing exception
            DB_folder db_folder = new DB_folder(getActivity(),DB_folder.getFocusFolder_tableId());
            db_folder.open();
            int pageCount = db_folder.getPagesCount(false);
            db_folder.close();
            if(pageCount == 0)
                setSelectedPosition(0);
            else
                setSelectedPosition(1);

            setupUIElements();
            loadItemsAsync(DB_folder.getFocusFolder_tableId());
            VideoDetailsFragment.isDelete = false;
        }
    }

    private class LoadItemAsyncTask extends AsyncTask<Void, Integer, Void>
    {
        int folderNum;
        LoadItemAsyncTask(int folderNum)
        {
            this.folderNum = folderNum;
        }

        ProgressBar bar;
        public void setProgressBar(ProgressBar bar)
        {
            this.bar = bar;
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (this.bar != null) {
                bar.setProgress(values[0]);
            }
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            loadItemsByDB(folderNum);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            bar.setVisibility(View.GONE);
            System.out.println("MainFragment / _LoadItemAsyncTask / _onPostExecute / mRowsAdapter.size() = " +mRowsAdapter.size());
            // set adapter
            setAdapter(mRowsAdapter);
            setupEventListeners();
            rootView.setVisibility(View.VISIBLE);
        }
    }


    void loadItemsAsync(int folderNum)
    {
        ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.load_progress);
        progressBar.setVisibility(View.VISIBLE);
        rootView = getActivity().findViewById(R.id.main_browse_fragment);
        rootView.setVisibility(View.GONE);

        LoadItemAsyncTask loadItemTask = new LoadItemAsyncTask(folderNum);
        loadItemTask.setProgressBar(progressBar);
        loadItemTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    // setter for current links length
    static int currentLinksLength;
    void setCurrLinksLength(int len)
    {
        this.currentLinksLength = len;
    }

    // getter for current links length
    static int getCurrLinksLength()
    {
        return currentLinksLength;
    }

    // setter for current pages length
    static int currentPagesLength;
    void setCurrPagesLength(int len)
    {
        this.currentPagesLength = len;
    }

    // getter for current pages length
    static int getCurrPagesLength()
    {
        return currentPagesLength;
    }
}