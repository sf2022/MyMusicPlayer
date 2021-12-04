package com.example.myMusicPlayer.Service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.Nullable;

import com.example.myMusicPlayer.Activity.MusicActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerService extends Service {
    private MediaPlayer mediaplayer;
    private Timer timer;
    private Uri uri;
    private String uriString;
    private MusicControl mCtl=new MusicControl();
    public MusicPlayerService(){}
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //uriString="/data/user/0/com.example.myMusic/files/"+intent.getStringExtra("songName");
        uriString=getFilesDir().getAbsolutePath()+"/"+intent.getStringExtra("songName")+".mp3";
        System.out.println("------------------------------onBind");
        mCtl.play();
        return mCtl;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaplayer=new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //uriString="/data/user/0/com.example.myMusic/files/"+intent.getStringExtra("songName");
        uriString=getFilesDir().getAbsolutePath()+"/"+intent.getStringExtra("songName")+".mp3";
        System.out.println("------------------------------onStartCommand"+uriString);
        mCtl.play();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 计时器
     */
    public void addTimer(){
        if(timer==null){
            timer=new Timer();
            TimerTask task=new TimerTask() {
                @Override
                public void run() {
                    try{
                        int duration=mediaplayer.getDuration();//总时长
                        int currentPos=mediaplayer.getCurrentPosition();//当前进度
                        Message msg= MusicActivity.handler.obtainMessage();
                        Bundle bundle=new Bundle();
                        bundle.putInt("duration",duration);
                        bundle.putInt("currentPos",currentPos);
                        msg.setData(bundle);
                        MusicActivity.handler.sendMessage(msg);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            //开始计时任务后的5ms,第一次执行task任务，以后每500ms执行一次
            timer.schedule(task,5,500);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public class MusicControl extends Binder{
        //播放音乐
        public void play(){
            mediaplayer.reset();
            uri = Uri.parse(uriString);
            //System.out.println("----------------------"+uri);
            mediaplayer=MediaPlayer.create(getApplicationContext(),uri);
            mediaplayer.start();
            addTimer();
        }
        //停止
        public void stop(){
            mediaplayer.stop();
            mediaplayer.release();
            try{
                timer.cancel();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //暂停
        public void pause(){
            mediaplayer.pause();
        }
        //继续播放
        public void resume(){
            mediaplayer.start();
        }
        //打带
        public void SeekTo(int ms){
            mediaplayer.seekTo(ms);
        }
    }
}
