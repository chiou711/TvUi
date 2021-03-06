package com.test.cw.tvui.main;

import android.app.Activity;

import com.test.cw.tvui.db.DB_folder;
import com.test.cw.tvui.db.DB_page;
import com.test.cw.tvui.util.Util;

import java.util.ArrayList;
import java.util.List;

public final class MovieList {
    static int REQUEST_CONTINUE_PLAY = 999;
    static int REQUEST_IMPORT = 998;
    public static int REQUEST_DETAIL = 997;
    public static List<Movie> list;
    public static List<Movie> setupMoviesByDB(Activity act,int pagePosition)
    {
        int pageTableId;
        DB_folder db_folder = new DB_folder(act,DB_folder.getFocusFolder_tableId());
        pageTableId = db_folder.getPageTableId(pagePosition,true);
        list = new ArrayList<>();

        // description
        String description = "Select action: Open or Delete";

        // URL list
        DB_page db_page = new DB_page(act,pageTableId);
        db_page.open();
        int len = db_page.getNotesCount(false);
        db_page.close();

        String[] title = new String[len];
        String[] videoUrl = new String[len];
        String[] cardImageUrl = new String[len];

        for(int i=0;i<len;i++)
        {
            videoUrl[i] = db_page.getNoteLinkUri(i,true);
            // card image
            cardImageUrl[i] = "http://img.youtube.com/vi/"+ Util.getYoutubeId(videoUrl[i])+"/0.jpg";
            if(!Util.isEmptyString(videoUrl[i])) {
//                title[i] = Util.getYouTubeTitle(videoUrl[i]); //will affect dialog for showing loading
                title[i] = db_page.getNoteTitle(i,true);
            }
        }

        // background image
        String bgImageUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
        };

        // add to list
        for(int i=0;i<len;i++)
        {
            list.add(buildMovieInfo("category",
                    title[i],
                    description,
                    "Item "+ pageTableId +"-"+String.valueOf(i+1),
                    videoUrl[i],
                    cardImageUrl[i],
                    bgImageUrl[0])  );
        }
        return list;
    }

    private static Movie buildMovieInfo(String category,
                                        String title,
                                        String description,
                                        String studio,
                                        String videoUrl,
                                        String cardImageUrl,
                                        String bgImageUrl)
    {
        Movie movie = new Movie();
        movie.setId(Movie.getCount());
        Movie.incCount();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setStudio(studio);
        movie.setCategory(category);
        movie.setCardImageUrl(cardImageUrl);
        movie.setBackgroundImageUrl(bgImageUrl);
        movie.setVideoUrl(videoUrl);
        return movie;
    }
}