package com.example.myMusicPlayer.Activity;

import static com.example.myMusicPlayer.Util.MP3Util.getAllFiles;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.myMusicPlayer.Bean.MusicBean;
import com.example.myMusicPlayer.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class LocalMusicActivity extends AppCompatActivity implements View.OnClickListener{
    private ListView music_lv;
    private Button back_btn;
    private ArrayAdapter<String> adapter;
    private List<String> musicList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_music);

        music_lv=findViewById(R.id.music_lv);
        back_btn=findViewById(R.id.back_btn);
        initDataMusic();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,musicList);
        music_lv.setAdapter(adapter);
        back_btn.setOnClickListener(this);
        //loadLocalMusic();
    }

    /**
     * 初始化本地音乐列表和设置listview点击事件
     */
    private void initDataMusic() {
        //查询指定目录下的MP3文件
        Vector<File> mp3 = getAllFiles(getFilesDir().getAbsolutePath(), "mp3");
        for(int i=0;i<mp3.size();i++){
            String song = mp3.get(i).getName();
            musicList.add(song);
        }
        music_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = musicList.get(position);
                Intent intent = new Intent(LocalMusicActivity.this, MusicActivity.class);
                String[] strArray = songName.split(".mp3");
                songName=strArray[0].trim();
                intent.putExtra("songName",songName);
                //intent.putExtra("currentMusicPos",position+"");
                startActivity(intent);
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_btn://返回
                finish();
                break;
            default:
                break;
        }
    }

    private void loadLocalMusic(){
        //加载本地存储的音乐mp3文件到集合当中
        //1。获取ContentResolver对象
        ContentResolver resolver=getContentResolver();
        //2.获取本地音乐存储的路径
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //3.开始查询地址
        Cursor cursor=resolver.query(uri,null,null,null,null);
        //4.遍历Cursor
        int id=0;
        String s=MediaStore.Audio.Media.TITLE;
        while (cursor.moveToNext()){
            @SuppressLint("Range") String song=cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            @SuppressLint("Range") String singer=cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            @SuppressLint("Range") String album=cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid=String.valueOf(id);
            @SuppressLint("Range") String path=cursor.getString(
                    cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            @SuppressLint("Range") long duration=cursor.getLong(
                    cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
            String totalTime=sdf.format(new Date(duration));
            MusicBean mb=new MusicBean(sid,song,singer,album,totalTime,path);
            //!!!
            musicList.add(song);
        }
        //适配器更新
        adapter.notifyDataSetChanged();
    }
}