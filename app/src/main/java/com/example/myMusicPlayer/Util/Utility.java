package com.example.myMusicPlayer.Util;


import com.example.myMusicPlayer.Gson.MusicId2Url;
import com.example.myMusicPlayer.Gson.MusicName2Id;
import com.google.gson.Gson;

import okhttp3.Response;

public class Utility {

    /**
     * 解析出歌曲的id
     */
    public static MusicName2Id handleName2IdrResponse(Response response) {
        try {
            return new Gson().fromJson(response.body().string(), MusicName2Id.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 解析出下载歌曲的url
     */
    public static MusicId2Url handleMusicId2UrlResponse(Response response) {
        try {
            return new Gson().fromJson(response.body().string(), MusicId2Url.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
