package com.test.cw.tvui;

import java.util.ArrayList;
import java.util.List;

//TODO
public final class MovieList {
    public static final String MOVIE_CATEGORY[] = {
            "Page 1",
            "Page 2",
            "Page 3",
            "Page 4",
            "Page 5",
    };

    public static List<Movie> list;

    public static List<Movie> setupMovies(int pageNo) {
        list = new ArrayList<Movie>();

        // title
        String[] title = new String[MainFragment.NUM_COLS]; //

        //
        String description = "Movie description: oxoxoxox....";

        //TODO: URL list
        String[] videoUrl = new String[MainFragment.NUM_COLS]; //
        if(pageNo == 0) {
            videoUrl[0] = "https://youtu.be/LYFJRomR12k";
            videoUrl[1] = "https://youtu.be/yBOQgL731Ac";
            videoUrl[2] = "https://youtu.be/5Go6I2_PpBU";
            videoUrl[3] = "https://youtu.be/87qu-zMQHk4";
            videoUrl[4] = "https://youtu.be/iczdtVWaSHE";
            videoUrl[5] = "https://youtu.be/Va0vs1fhhNI";
            videoUrl[6] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[7] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[8] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[9] = "https://youtu.be/Q0li2uMNOVs";
        }
        else if(pageNo == 1) {
            videoUrl[0] = "https://youtu.be/Va0vs1fhhNI";
            videoUrl[1] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[2] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[3] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[4] = "https://youtu.be/Q0li2uMNOVs";
            videoUrl[5] = "https://youtu.be/Va0vs1fhhNI";
            videoUrl[6] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[7] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[8] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[9] = "https://youtu.be/Q0li2uMNOVs";
        }
        else if(pageNo == 2) {
            videoUrl[0] = "https://youtu.be/0KJ60uJZ3-Q";
            videoUrl[1] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[2] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[3] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[4] = "https://youtu.be/Q0li2uMNOVs";
            videoUrl[5] = "https://youtu.be/Va0vs1fhhNI";
            videoUrl[6] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[7] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[8] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[9] = "https://youtu.be/Q0li2uMNOVs";
        }
        else if(pageNo == 3) {
            videoUrl[0] = "https://youtu.be/8rT46hVQHIA";
            videoUrl[1] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[2] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[3] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[4] = "https://youtu.be/Q0li2uMNOVs";
            videoUrl[5] = "https://youtu.be/Va0vs1fhhNI";
            videoUrl[6] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[7] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[8] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[9] = "https://youtu.be/Q0li2uMNOVs";
        }
        else if(pageNo == 4) {
            videoUrl[0] = "https://youtu.be/8tbP3f3i03E";
            videoUrl[1] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[2] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[3] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[4] = "https://youtu.be/Q0li2uMNOVs";
            videoUrl[5] = "https://youtu.be/Va0vs1fhhNI";
            videoUrl[6] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[7] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[8] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[9] = "https://youtu.be/Q0li2uMNOVs";
        }

        //TODO: background
        String bgImageUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
        };

        //TODO: card image
        String[] cardImageUrl = new String[MainFragment.NUM_COLS];
        for(int i=0; i<videoUrl.length; i++)
        {
            String url = videoUrl[i];
            cardImageUrl[i] = "http://img.youtube.com/vi/"+ Util.getYoutubeId(url)+"/0.jpg";
        }
        for(int i=0; i<title.length; i++)
        {
            String url = videoUrl[i];
            title[i] = Util.getYouTubeTitle(url);
        }

        //TODO: add to list
        for(int i=0;i<MainFragment.NUM_COLS;i++)
        {
            list.add(buildMovieInfo("category", title[i],
                    description, "Song"+i+1, videoUrl[i], cardImageUrl[i], bgImageUrl[0]));
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
