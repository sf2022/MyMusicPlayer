package com.example.myMusicPlayer.Activity;

import static com.example.myMusicPlayer.Util.MP3Util.getAllFiles;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myMusicPlayer.R;
import com.example.myMusicPlayer.Service.MusicPlayerService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView musicName_tv;             //音乐的图片
    private static SeekBar time_skb;        //进度条
    private static TextView prgTime_tv;    //已经播放的时间
    private static TextView totalTime_tv;   //总时间
    private Button pauseMusic_btn;          //暂停音乐
    private Button continueMusic_btn;       //继续播放
    private Button exit_btn;                //退出播放
    private Button lastMusic_btn;           //上一首歌
    private Button nextMusic_btn;           //下一首歌
    private MusicPlayerService.MusicControl mCtl;
    private String songName;                    //歌名
    private List<String> musicList = new ArrayList<>();//音乐列表
    private int currentMusicPos=-1;            //当前音乐在列表中的位置
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCtl=(MusicPlayerService.MusicControl)service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_view);
        initView();
    }

    /**
     * 初始化控件
     * 获取音乐播放列表
     * 启动，绑定服务
     */
    private void initView() {
        musicName_tv=findViewById(R.id.musicName_tv);
        time_skb=findViewById(R.id.time_skb);
        prgTime_tv=findViewById(R.id.prgTime_tv);
        totalTime_tv=findViewById(R.id.totalTime_tv);
        pauseMusic_btn=findViewById(R.id.pauseMusic_btn);
        continueMusic_btn=findViewById(R.id.continueMusic_btn);
        exit_btn=findViewById(R.id.exit_btn);
        lastMusic_btn=findViewById(R.id.lastMusic_btn);
        nextMusic_btn=findViewById(R.id.nextMusic_btn);

        pauseMusic_btn.setOnClickListener(this);
        continueMusic_btn.setOnClickListener(this);
        exit_btn.setOnClickListener(this);
        lastMusic_btn.setOnClickListener(this);
        nextMusic_btn.setOnClickListener(this);
        time_skb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    mCtl.SeekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mCtl.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCtl.resume();
            }
        });
        Vector<File> mp3 = getAllFiles(getFilesDir().getAbsolutePath(), "mp3");
        //本地音乐列表——音乐名
        for(int i=0;i<mp3.size();i++) {
            String song = mp3.get(i).getName();
            musicList.add(song);
        }

        //播放指定的音乐
        Intent intent0=getIntent();
        songName=intent0.getStringExtra("songName");
        //currentMusicPos= Integer.parseInt(intent0.getStringExtra("currentMusicPos"));
        //获得指定的音乐在本地音乐列表中的位置
        currentMusicPos=getCurrentMusicPos(songName);
        //创建时自动绑定服务
        Intent intent=new Intent(getApplicationContext(), MusicPlayerService.class);
        intent.putExtra("songName",songName);
        musicName_tv.setText(songName);
        // 启动服务，保证MusicPlayerService一直在后台运行
        startService(intent);
        // 绑定服务，让MusicActivity与MusicPlayerService通信
        bindService(intent,conn,BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pauseMusic_btn://暂停
                mCtl.pause();
                break;
            case R.id.continueMusic_btn://继续
                mCtl.resume();
                break;
            case R.id.exit_btn://退出
                finish();
                break;
            case R.id.lastMusic_btn://上一首
                if(currentMusicPos>0){
                    currentMusicPos-=1;
                    String songName=musicList.get(currentMusicPos);
                    String[] strArray = songName.split(".mp3");
                    songName=strArray[0].trim();
                    musicName_tv.setText(songName);
                    Intent intent=new Intent(this, MusicPlayerService.class);
                    intent.putExtra("songName",songName);
                    startService(intent);
                }
                //如果此时播放的是第一首歌曲
                else
                    Toast.makeText(this,"第一首歌曲！",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nextMusic_btn:
                if(currentMusicPos<musicList.size()-1){
                    currentMusicPos+=1;
                    String songName=musicList.get(currentMusicPos);
                    String[] strArray = songName.split(".mp3");
                    songName=strArray[0].trim();
                    musicName_tv.setText(songName);
                    Intent intent=new Intent(this, MusicPlayerService.class);
                    intent.putExtra("songName",songName);
                    startService(intent);
                }
                //如果此时播放的是最后一首歌曲
                else
                    Toast.makeText(this,"最后一首歌曲！",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent=new Intent(this, MusicPlayerService.class);
        stopService(intent);
        mCtl.stop();
        unbindService(conn);
    }
    /**
     * 通过音乐名获得音乐在本地列表中的位置
     */
    private int getCurrentMusicPos(String songName){
        for(int i=0;i<musicList.size();i++) {
            String s=songName+".mp3";
             if (musicList.get(i).equals(s)){
                 return i;
             }
        }
        return -1;
    }
    /**
     * 获取MusicPlayerService传递过来的消息
     */
    public static Handler handler=new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg){
            super.handleMessage(msg);
            Bundle bundle=msg.getData();//获取从子线程发送过来的播放进度
            int duration=bundle.getInt("duration");
            int currentPos=bundle.getInt("currentPos");
            time_skb.setMax(duration);
            time_skb.setProgress(currentPos);
            SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
            String totalTime=sdf.format(new Date(duration));
            String currentTime=sdf.format(new Date(currentPos));
            totalTime_tv.setText(totalTime);
            prgTime_tv.setText(currentTime);
        }
    };
}