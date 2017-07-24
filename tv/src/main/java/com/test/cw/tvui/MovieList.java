package com.test.cw.tvui;

import java.util.ArrayList;
import java.util.List;

//TODO
public final class MovieList {
//    public static final String MOVIE_CATEGORY[] = {
//            "Page 1",
//            "Page 2",
//            "Page 3",
//            "Page 4",
//            "Page 5",
//    };

    public static List<Movie> list;

    public static List<Movie> setupMovies(int pageNo) {
        list = new ArrayList<Movie>();

        // title
//        String[] title = new String[MainFragment.NUM_COLS]; //

        // description
        String description = "Movie description: oxoxoxox....";

        //TODO: URL list
//        String[] videoUrl = new String[MainFragment.NUM_COLS]; //
//        for(int p=0; p<MainFragment.NUM_ROWS; p++)
//        {
//            for (int i = 0; i < MainFragment.NUM_COLS; i++) {
//                videoUrl[i] = Import_handleXmlFile.linkArr[pageNo][i];
//            }
            int countLink = 0;
            while(!Util.isEmptyString(Import_handleXmlFile.linkArr[pageNo][countLink]))
            {
                countLink++;
            }
//        }
        int columns = Import_handleXmlFile.linkArr[pageNo].length;
        String[] title = new String[columns]; //

        String[] videoUrl = new String[columns]; //
        for(int i=0;i<columns;i++)
        {
            videoUrl[i] = Import_handleXmlFile.linkArr[pageNo][i];
        }


        //TODO: background image
        String bgImageUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
        };

        //TODO: card image
//        String[] cardImageUrl = new String[MainFragment.NUM_COLS];
        String[] cardImageUrl = new String[columns];
//        for(int i=0; i<videoUrl.length; i++)
        for(int i=0; i<columns; i++)
        {
            String url = videoUrl[i];
            cardImageUrl[i] = "http://img.youtube.com/vi/"+ Util.getYoutubeId(url)+"/0.jpg";
        }
//        for(int i=0; i<title.length; i++)
        for(int i=0; i<columns; i++)
        {
            String url = videoUrl[i];
            if(!Util.isEmptyString(url))
                title[i] = Util.getYouTubeTitle(url);//??? exception
//            title[i] = "test";
        }

        //TODO: add to list
//        for(int i=0;i<MainFragment.NUM_COLS;i++)
        for(int i=0;i<columns;i++)
        {
            list.add(buildMovieInfo("category", title[i],
                    description, "Item "+String.valueOf(pageNo*10+i+1), videoUrl[i], cardImageUrl[i], bgImageUrl[0]));
        }
        return list;
    }

    private static Movie buildMovieInfo(String category, String title,
                                        String description, String studio, String videoUrl, String cardImageUrl,
                                        String bgImageUrl) {
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
