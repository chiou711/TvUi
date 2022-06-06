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

package com.test.cw.tvui.details;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.test.cw.tvui.R;
import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;
import com.test.cw.tvui.main.CardPresenter;
import com.test.cw.tvui.main.MainActivity;
import com.test.cw.tvui.main.MainFragment;
import com.test.cw.tvui.main.Movie;
import com.test.cw.tvui.main.MovieList;
import com.test.cw.tvui.util.Util;
import com.test.cw.tvui.util.Utils;

import java.util.List;

import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.DetailsFragment;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.DetailsOverviewRow;
import androidx.leanback.widget.DetailsOverviewRowPresenter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class VideoDetailsFragment extends DetailsFragment {
    private static final String TAG = "VideoDetailsFragment";

    private static final int ACTION_WATCH = 1;
    private static final int ACTION_DELETE = 2;
    private static final int ACTION_BUY = 3;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private static final int NUM_COLS = 10;

    private Movie mSelectedMovie;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;

    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        super.onCreate(savedInstanceState);
        isDelete = false;
        prepareBackgroundManager();

        mSelectedMovie = (Movie) getActivity().getIntent()
                .getSerializableExtra(DetailsActivity.MOVIE);
        if (mSelectedMovie != null) {
            setupAdapter();
            setupDetailsOverviewRow();
            setupDetailsOverviewRowPresenter();
            setupMovieListRow();
            setupMovieListRowPresenter();
            updateBackground(mSelectedMovie.getBackgroundImageUrl());
            setOnItemViewClickedListener(new ItemViewClickedListener());
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    protected void updateBackground(String uri) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .error(mDefaultBackground);

        Glide.with(getActivity())
                .asBitmap()
                .load(uri)
                .centerCrop()
                .apply(options)
                .error(mDefaultBackground)
                .into(new SimpleTarget<Bitmap>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(
                            Bitmap resource,
                            Transition<? super Bitmap> transition) {
                        mBackgroundManager.setBitmap(resource);
                    }
                });
    }

    private void setupAdapter() {
        mPresenterSelector = new ClassPresenterSelector();
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }

    private void setupDetailsOverviewRow() {
        Log.d(TAG, "doInBackground: " + mSelectedMovie.toString());
        final DetailsOverviewRow row = new DetailsOverviewRow(mSelectedMovie);
        row.setImageDrawable(getResources().getDrawable(R.drawable.default_background));
        int width = Utils.convertDpToPixel(getActivity()
                .getApplicationContext(), DETAIL_THUMB_WIDTH);
        int height = Utils.convertDpToPixel(getActivity()
                .getApplicationContext(), DETAIL_THUMB_HEIGHT);

        RequestOptions options = new RequestOptions()
                .error(R.drawable.default_background)
                .dontAnimate();

        Glide.with(getActivity())
                .asBitmap()
                .load(mSelectedMovie.getCardImageUrl())
                .centerCrop()
                .error(R.drawable.default_background)
                .into(new SimpleTarget<Bitmap>( ) {
                    @Override
                    public void onResourceReady(Bitmap resource,
                                                Transition<? super Bitmap> transition
                                                        ) {
                        Log.d(TAG, "details overview card image url ready: " + resource);
                        row.setImageBitmap(getActivity(),resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });

//        row.addAction(new Action(ACTION_WATCH, getResources().getString(
//                R.string.watch_trailer_1), getResources().getString(R.string.watch_trailer_2)));
//        row.addAction(new Action(ACTION_DELETE, getResources().getString(R.string.rent_1),
//                getResources().getString(R.string.rent_2)));
//        row.addAction(new Action(ACTION_BUY, getResources().getString(R.string.buy_1),
//                getResources().getString(R.string.buy_2)));
        row.addAction(new Action(ACTION_WATCH, "Open", "YouTube"));//TODO locale
        row.addAction(new Action(ACTION_DELETE, "Delete", "Current item"));//TODO locale

        mAdapter.add(row);
    }

    RadioGroup radioGroup;
    RadioButton deleteNote;
    RadioButton deletePage;
    public static boolean isDelete;
    private void setupDetailsOverviewRowPresenter() {
        // Set detail background and style.
        DetailsOverviewRowPresenter detailsPresenter =
                new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(getResources().getColor(R.color.bar_color));//R.color.selected_background
        detailsPresenter.setStyleLarge(true);

        // Hook up transition element.
        detailsPresenter.setSharedElementEnterTransition(getActivity(),
                DetailsActivity.SHARED_ELEMENT_NAME);

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_WATCH) {
//                    Intent intent = new Intent(getActivity(), PlaybackOverlayActivity.class);
//                    intent.putExtra(DetailsActivity.MOVIE, mSelectedMovie);
//                    startActivity(intent);

                    //Launch YouTube by item view click
                    Util.openLink_YouTube(getActivity(),mSelectedMovie.getVideoUrl(),0);
                }
                else if (action.getId() == ACTION_DELETE)
                {
                    //Confirmation dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle(R.string.delete_option_title)
                           .setPositiveButton(R.string.btn_OK, listener_OK)
                           .setNegativeButton(R.string.btn_NO, listener_NO);//TODO locale

                    // inflate select style layout
                    LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = mInflater.inflate(R.layout.delete_option_dlg, null);

                    radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
                    deleteNote = (RadioButton) view.findViewById(R.id.deleteNote);
                    deleteNote.setChecked(true);
                    deletePage = (RadioButton) view.findViewById(R.id.deletePage);

                    builder.setView(view);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    // request focus
                    Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positive.setFocusable(true);
                    positive.setFocusableInTouchMode(true);
                    positive.requestFocus();

                }
                else
                {
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    DialogInterface.OnClickListener listener_OK = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            DB_folder db_folder = new DB_folder(getActivity(), DB_folder.getFocusFolder_tableId());
//          if(radioGroup.getCheckedRadioButtonId() == R.id.deleteNote) {
            if(deleteNote.isChecked()) {
                //delete current item in DB
                DB_page db_page = new DB_page(getActivity(), DB_page.getFocusPage_tableId());
                long id = db_page.getNoteId(MainFragment.currLinkId, true);
                db_page.deleteNote(id, true);

                db_page.open();
                int notesCount = db_page.getNotesCount(false);
                db_page.close();

                // delete current page if no any note
                if (notesCount == 0)
                    db_folder.deleteCurrentPage();
            }
            else if(deletePage.isChecked())
                db_folder.deleteCurrentPage();

            isDelete = true;
            getActivity().finish();

            //end
            dialog.dismiss();
        }
    };

    DialogInterface.OnClickListener listener_NO = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    private void setupMovieListRow() {
        String subcategories[] = {getString(R.string.related_movies)};

        List<Movie> list = MovieList.setupMoviesByDB(getActivity(),MainFragment.currPageId);

//        List<Movie> list = MovieList.list;
//        Collections.shuffle(list);
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (int j = 0; j < list.size() ; j++) {
//            listRowAdapter.add(list.get(j % 5));
            listRowAdapter.add(list.get(j));
        }

        HeaderItem header = new HeaderItem(0, subcategories[0]);
        mAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void setupMovieListRowPresenter() {
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Movie) {
//                Movie movie = (Movie) item;
//                Log.d(TAG, "Item: " + item.toString());
//                Intent intent = new Intent(getActivity(), DetailsActivity.class);
//                intent.putExtra(getResources().getString(R.string.movie), movie);
//                intent.putExtra(getResources().getString(R.string.should_start), true);
//                startActivity(intent);

                //Launch YouTube by item view click
                Movie movie = (Movie) item;
                Util.openLink_YouTube(getActivity(),movie.getVideoUrl(),0);

//                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(),
//                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
//                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
//                getActivity().startActivity(intent, bundle);

            }
        }
    }
}
