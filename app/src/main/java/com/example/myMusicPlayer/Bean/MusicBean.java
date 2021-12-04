package com.example.myMusicPlayer.Bean;

public class MusicBean {
    private String mId;         //id
    private String songName;        //歌名
    private String singer;      //歌手
    private String album;       //专辑
    private String totalTime;   //总时长
    private String path;        //路径

    public MusicBean() {
    }

    public MusicBean(String mId, String songName, String singer, String album, String totalTime, String path) {
        this.mId = mId;
        this.songName = songName;
        this.singer = singer;
        this.album = album;
        this.totalTime = totalTime;
        this.path = path;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
