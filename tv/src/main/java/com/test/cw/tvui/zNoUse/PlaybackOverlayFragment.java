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

package com.test.cw.tvui.zNoUse;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.test.cw.tvui.R;
import com.test.cw.tvui.details.DetailsActivity;
import com.test.cw.tvui.main.CardPresenter;
import com.test.cw.tvui.main.Movie;
import com.test.cw.tvui.main.MovieList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.leanback.app.VideoSupportFragment;
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.ControlButtonPresenterSelector;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.PlaybackControlsRowPresenter;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

/*
 * Class for video playback with media control
 */
public class PlaybackOverlayFragment extends VideoSupportFragment {
    private static final String TAG = "PlaybackControlsFragmnt";

    private static final boolean SHOW_DETAIL = true;
    private static final boolean HIDE_MORE_ACTIONS = false;
    private static final int PRIMARY_CONTROLS = 5;
    private static final boolean SHOW_IMAGE = PRIMARY_CONTROLS <= 5;
    private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;
    private static final int CARD_WIDTH = 200;
    private static final int CARD_HEIGHT = 240;
    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int UPDATE_PERIOD = 16;
    private static final int SIMULATED_BUFFERED_TIME = 10000;

    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    private PlaybackControlsRow.PlayPauseAction mPlayPauseAction;
    private PlaybackControlsRow.RepeatAction mRepeatAction;
    private PlaybackControlsRow.ThumbsUpAction mThumbsUpAction;
    private PlaybackControlsRow.ThumbsDownAction mThumbsDownAction;
    private PlaybackControlsRow.ShuffleAction mShuffleAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow mPlaybackControlsRow;
    private ArrayList<Movie> mItems = new ArrayList<Movie>();
    private int mCurrentItem;
    private Handler mHandler;
    private Runnable mRunnable;
    private Movie mSelectedMovie;

    private OnPlayPauseClickedListener mCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mItems = new ArrayList<Movie>();
        mSelectedMovie = (Movie) getActivity()
                .getIntent().getSerializableExtra(DetailsActivity.MOVIE);

        List<Movie> movies = MovieList.list;

        for (int j = 0; j < movies.size(); j++) {
            mItems.add(movies.get(j));
            if (mSelectedMovie.getTitle().contentEquals(movies.get(j).getTitle())) {
                mCurrentItem = j;
            }
        }

        mHandler = new Handler();

        setBackgroundType(BACKGROUND_TYPE);
        setFadingEnabled(false);

        setupRows();

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                       RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemSelected: " + item + " row " + row);
            }
        });
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemClicked: " + item + " row " + row);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayPauseClickedListener) {
            mCallback = (OnPlayPauseClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlayPauseClickedListener");
        }
    }

    private void setupRows() {

        ClassPresenterSelector ps = new ClassPresenterSelector();

        PlaybackControlsRowPresenter playbackControlsRowPresenter;
        if (SHOW_DETAIL) {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter(
                    new DescriptionPresenter());
        } else {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter();
        }
        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            public void onActionClicked(Action action) {
                if (action.getId() == mPlayPauseAction.getId()) {
                    togglePlayback(mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY);
                } else if (action.getId() == mSkipNextAction.getId()) {
                    next();
                } else if (action.getId() == mSkipPreviousAction.getId()) {
                    prev();
                } else if (action.getId() == mFastForwardAction.getId()) {
                    Toast.makeText(getActivity(), "TODO: Fast Forward", Toast.LENGTH_SHORT).show();
                } else if (action.getId() == mRewindAction.getId()) {
                    Toast.makeText(getActivity(), "TODO: Rewind", Toast.LENGTH_SHORT).show();
                }
                if (action instanceof PlaybackControlsRow.MultiAction) {
                    ((PlaybackControlsRow.MultiAction) action).nextIndex();
                    notifyChanged(action);
                }
            }
        });
        playbackControlsRowPresenter.setSecondaryActionsHidden(HIDE_MORE_ACTIONS);

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());
        mRowsAdapter = new ArrayObjectAdapter(ps);

        addPlaybackControlsRow();
        addOtherRows();

        setAdapter(mRowsAdapter);
    }

    public void togglePlayback(boolean playPause) {
        if (playPause) {
            startProgressAutomation();
            setFadingEnabled(true);
            mCallback.onFragmentPlayPause(mItems.get(mCurrentItem),
                    mPlaybackControlsRow.getCurrentTime(), true);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));
        } else {
            stopProgressAutomation();
            setFadingEnabled(false);
            mCallback.onFragmentPlayPause(mItems.get(mCurrentItem),
                    mPlaybackControlsRow.getCurrentTime(), false);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PLAY));
        }
        notifyChanged(mPlayPauseAction);
    }

    private int getDuration() {
        Movie movie = mItems.get(mCurrentItem);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mmr.setDataSource(movie.getVideoUrl(), new HashMap<String, String>());
        } else {
            mmr.setDataSource(movie.getVideoUrl());
        }
        String time = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(time);
        return (int) duration;
    }

    private void addPlaybackControlsRow() {
        if (SHOW_DETAIL) {
            mPlaybackControlsRow = new PlaybackControlsRow(mSelectedMovie);
        } else {
            mPlaybackControlsRow = new PlaybackControlsRow();
        }
        mRowsAdapter.add(mPlaybackControlsRow);

        updatePlaybackRow(mCurrentItem);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

        mPlayPauseAction = new PlaybackControlsRow.PlayPauseAction(getActivity());
        mRepeatAction = new PlaybackControlsRow.RepeatAction(getActivity());
        mThumbsUpAction = new PlaybackControlsRow.ThumbsUpAction(getActivity());
        mThumbsDownAction = new PlaybackControlsRow.ThumbsDownAction(getActivity());
        mShuffleAction = new PlaybackControlsRow.ShuffleAction(getActivity());
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(getActivity());
        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(getActivity());
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(getActivity());
        mRewindAction = new PlaybackControlsRow.RewindAction(getActivity());

        if (PRIMARY_CONTROLS > 5) {
            mPrimaryActionsAdapter.add(mThumbsUpAction);
        } else {
            mSecondaryActionsAdapter.add(mThumbsUpAction);
        }
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter.add(new PlaybackControlsRow.RewindAction(getActivity()));
        }
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter.add(new PlaybackControlsRow.FastForwardAction(getActivity()));
        }
        mPrimaryActionsAdapter.add(mSkipNextAction);

        mSecondaryActionsAdapter.add(mRepeatAction);
        mSecondaryActionsAdapter.add(mShuffleAction);
        if (PRIMARY_CONTROLS > 5) {
            mPrimaryActionsAdapter.add(mThumbsDownAction);
        } else {
            mSecondaryActionsAdapter.add(mThumbsDownAction);
        }
        mSecondaryActionsAdapter.add(new PlaybackControlsRow.HighQualityAction(getActivity()));
        mSecondaryActionsAdapter.add(new PlaybackControlsRow.ClosedCaptioningAction(getActivity()));
    }

    private void notifyChanged(Action action) {
        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
        adapter = mSecondaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
    }

    private void updatePlaybackRow(int index) {
        if (mPlaybackControlsRow.getItem() != null) {
            Movie item = (Movie) mPlaybackControlsRow.getItem();
            item.setTitle(mItems.get(mCurrentItem).getTitle());
            item.setStudio(mItems.get(mCurrentItem).getStudio());
        }
        if (SHOW_IMAGE) {
            updateVideoImage(mItems.get(mCurrentItem).getCardImageURI().toString());
        }
        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
        mPlaybackControlsRow.setTotalTime(getDuration());
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
    }

    private void addOtherRows() {
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (Movie movie : mItems) {
            listRowAdapter.add(movie);
        }
        HeaderItem header = new HeaderItem(0, getString(R.string.related_movies));
        mRowsAdapter.add(new ListRow(header, listRowAdapter));

    }

    private int getUpdatePeriod() {
        if (getView() == null || mPlaybackControlsRow.getTotalTime() <= 0) {
            return DEFAULT_UPDATE_PERIOD;
        }
        return Math.max(UPDATE_PERIOD, mPlaybackControlsRow.getTotalTime() / getView().getWidth());
    }

    private void startProgressAutomation() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                int updatePeriod = getUpdatePeriod();
                int currentTime = mPlaybackControlsRow.getCurrentTime() + updatePeriod;
                int totalTime = mPlaybackControlsRow.getTotalTime();
                mPlaybackControlsRow.setCurrentTime(currentTime);
                mPlaybackControlsRow.setBufferedProgress(currentTime + SIMULATED_BUFFERED_TIME);

                if (totalTime > 0 && totalTime <= currentTime) {
                    next();
                }
                mHandler.postDelayed(this, updatePeriod);
            }
        };
        mHandler.postDelayed(mRunnable, getUpdatePeriod());
    }

    private void next() {
        if (++mCurrentItem >= mItems.size()) {
            mCurrentItem = 0;
        }

        if (mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY) {
            mCallback.onFragmentPlayPause(mItems.get(mCurrentItem), 0, false);
        } else {
            mCallback.onFragmentPlayPause(mItems.get(mCurrentItem), 0, true);
        }
        updatePlaybackRow(mCurrentItem);
    }

    private void prev() {
        if (--mCurrentItem < 0) {
            mCurrentItem = mItems.size() - 1;
        }
        if (mPlayPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PLAY) {
            mCallback.onFragmentPlayPause(mItems.get(mCurrentItem), 0, false);
        } else {
            mCallback.onFragmentPlayPause(mItems.get(mCurrentItem), 0, true);
        }
        updatePlaybackRow(mCurrentItem);
    }

    private void stopProgressAutomation() {
        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    @Override
    public void onStop() {
        stopProgressAutomation();
        super.onStop();
    }

    protected void updateVideoImage(String uri) {
        Glide.with(getActivity())
                .asBitmap()
                .load(uri)
                .centerCrop()
                .into(new SimpleTarget<Bitmap>(CARD_WIDTH, CARD_HEIGHT) {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        mPlaybackControlsRow.setImageBitmap(getActivity(),resource);
                        mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
                    }
                });
    }

    // Container Activity must implement this interface
    public interface OnPlayPauseClickedListener {
        void onFragmentPlayPause(Movie movie, int position, Boolean playPause);
    }

    static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {
        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {
            viewHolder.getTitle().setText(((Movie) item).getTitle());
            viewHolder.getSubtitle().setText(((Movie) item).getStudio());
        }
    }
}
