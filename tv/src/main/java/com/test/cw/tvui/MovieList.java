package com.test.cw.tvui;

import java.util.ArrayList;
import java.util.List;

//TODO
public final class MovieList {
    public static final String MOVIE_CATEGORY[] = {
            "Page 1",
            "Page 2",
    };

    public static List<Movie> list;

    public static List<Movie> setupMovies(int pageNo) {
        list = new ArrayList<Movie>();
        String title[] = {"","","","",""
        };

        String description = "Movie description";

        //TODO
        String videoUrl[]={"","","","",""};
        if(pageNo == 0) {
            videoUrl[0] = "https://youtu.be/LYFJRomR12k";
            videoUrl[1] = "https://youtu.be/yBOQgL731Ac";
            videoUrl[2] = "https://youtu.be/5Go6I2_PpBU";
            videoUrl[3] = "https://youtu.be/87qu-zMQHk4";
            videoUrl[4] = "https://youtu.be/iczdtVWaSHE";
        }
        else if(pageNo == 1) {
            videoUrl[0] = "https://youtu.be/Va0vs1fhhNI";
            videoUrl[1] = "https://youtu.be/QO9A9u4GyGc";
            videoUrl[2] = "https://youtu.be/1oDfWd8-srY";
            videoUrl[3] = "https://youtu.be/xB2cY-8xFdI";
            videoUrl[4] = "https://youtu.be/Q0li2uMNOVs";
        }

        //TODO
        String bgImageUrl[] = {
                "http://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
        };

        //TODO
        String cardImageUrl[]={"","","","",""};
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

        //TODO
        list.add(buildMovieInfo("category", title[0],
            description, "Song 1", videoUrl[0], cardImageUrl[0], bgImageUrl[0]));
        list.add(buildMovieInfo("category", title[1],
            description, "Song 2", videoUrl[1], cardImageUrl[1], bgImageUrl[0]));
        list.add(buildMovieInfo("category", title[2],
            description, "Song 3", videoUrl[2], cardImageUrl[2], bgImageUrl[0]));
        list.add(buildMovieInfo("category", title[3],
                description, "Song 4", videoUrl[3], cardImageUrl[3], bgImageUrl[0]));
        list.add(buildMovieInfo("category", title[4],
            description, "Song 5", videoUrl[4], cardImageUrl[4], bgImageUrl[0]));

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
