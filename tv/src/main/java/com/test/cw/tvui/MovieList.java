package com.test.cw.tvui;

import java.util.ArrayList;
import java.util.List;

//TODO
public final class MovieList {
    static int REQUEST_CONTINUE_PLAY = 999;
    static List<Movie> list;

    static List<Movie> setupMovies(int pageNo)
    {
        list = new ArrayList<>();

        // description
        String description = "Movie description: oxoxoxox....";

        //TODO: URL list
        int len = MainFragment.linksArr[pageNo].length;
        String[] title = new String[len];
        String[] videoUrl = new String[len];
        for(int i=0;i<len;i++)
        {
            videoUrl[i] = MainFragment.linksArr[pageNo][i];
        }


        //TODO: background image
        String bgImageUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
        };

        //TODO: card image
        String[] cardImageUrl = new String[len];
        for(int i=0; i<len; i++)
        {
            String url = videoUrl[i];
            cardImageUrl[i] = "http://img.youtube.com/vi/"+ Util.getYoutubeId(url)+"/0.jpg";
        }
        for(int i=0; i<len; i++)
        {
            String url = videoUrl[i];
            if(!Util.isEmptyString(url)) {
                title[i] = Util.getYouTubeTitle(url); //??? will affect dialog for showing loading
            }
        }

        //TODO: add to list
        for(int i=0;i<len;i++)
        {
            list.add(buildMovieInfo("category",
                                    title[i],
                                    description,
                                    "Item "+ pageNo +"-"+String.valueOf(i+1),
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
