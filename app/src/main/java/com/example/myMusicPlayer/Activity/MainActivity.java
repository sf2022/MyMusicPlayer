package com.example.myMusicPlayer.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myMusicPlayer.Bean.MusicBean;
import com.example.myMusicPlayer.R;
import com.example.myMusicPlayer.Service.FileService;
import com.example.myMusicPlayer.Util.Utility;
import com.example.myMusicPlayer.Gson.MusicId2Url;
import com.example.myMusicPlayer.Gson.MusicName2Id;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText search_et;     //搜索框
    private Button confirm_btn;     //搜索btn
    private Button look_localMusic; //查询本地音乐键
    private ListView webMusic_lv;   //音乐列表
    private ArrayAdapter<String> adapter;
    private List<String> webMusicList = new ArrayList<>();//仅记录歌名
    private List<MusicBean> webMusicBeanList = new ArrayList<>();//记录歌名和id
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        webMusic_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mid=webMusicBeanList.get(position).getmId();
                String songName=webMusicBeanList.get(position).getSongName();
                String songIdUrl="https://cloud-music-api-f494k233x-mgod-monkey.vercel.app/song/url?id="+mid;

                String ss=getFilesDir().getAbsolutePath()+"/"+webMusicBeanList.
                        get(position).getSongName()+".mp3";   //音乐文件的绝对路径
                //存在则播放
                if(hasDownload(ss)){
                    System.out.println(ss);
                    //播放本地音乐
                    Intent intent=new Intent(MainActivity.this, MusicActivity.class);
                    //把被点击的音乐名字
                    intent.putExtra("songName",webMusicBeanList.get(position).getSongName());
                    //int currentMusicPos=getCurrentMusicPos(songName);
                    //intent.putExtra("currentMusicPos",currentMusicPos);
                    //开始跳转
                    startActivity(intent);
                }
                //不存在就下载
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    //设置弹出框标题
                    builder.setTitle("是否下载歌曲？");
                    builder.setItems(new String[]{"是","否"}, new DialogInterface.OnClickListener() {
                        //类型码
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    //查询下载的url
                                    queryUrlWithId(songName,songIdUrl);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
            }
        });
    }

    /**
     * 初始化控件
     */
    private void init(){
        search_et=findViewById(R.id.search_et);
        confirm_btn=findViewById(R.id.confirm_btn);
        look_localMusic=findViewById(R.id.look_localMusic);
        webMusic_lv=findViewById(R.id.webMusic_lv);
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,webMusicList);
        confirm_btn.setOnClickListener(this);
        look_localMusic.setOnClickListener(this);
        webMusic_lv.setAdapter(adapter);

    }

    /**
     * 通过歌名搜索歌曲，获得歌曲id
     * @param songNameUrl
     */
    private void queryIdWithName(String songNameUrl){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(songNameUrl).build();
                    Response response = client.newCall((request)).execute();

                    MusicName2Id mni = Utility.handleName2IdrResponse(response);
                    //搜索到的音乐列表
                    List<MusicBean> webMusicBeanList = new ArrayList<MusicBean>();
                    for(int i=0;i<mni.getResult().getSongs().size();i++){
                        MusicBean mb=new MusicBean();
                        //取歌曲id和歌名
                        mb.setmId(""+mni.getResult().getSongs().get(i).getId());
                        mb.setSongName(mni.getResult().getSongs().get(i).getName());
                        webMusicBeanList.add(mb);
                    }
                    //显示在界面上
                    showWebMusic(webMusicBeanList);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 通过音乐id查询下载的url
     * @param songName
     * @param songIdUrl
     */
    private void queryUrlWithId(String songName, String songIdUrl){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(songIdUrl).build();
                    Response response = client.newCall((request)).execute();

                    MusicId2Url miu = Utility.handleMusicId2UrlResponse(response);
                    String musicUrl=miu.getData().get(0).getUrl();
                    if(!(musicUrl==null))
                        downLoadMusic(songName,musicUrl);
                    else
                        showToast("此歌曲url为空，无法下载");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 启动下载音乐的服务
     * @param songName
     * @param musicUrl
     */
    private void downLoadMusic(String songName,String musicUrl){
        runOnUiThread(()->{
            Intent intent1=new Intent(MainActivity.this,FileService.class);
            intent1.putExtra("musicUrl",musicUrl);
            intent1.putExtra("songName",songName);
            startService(intent1);
        });
    }

    /**
     * 显示查询到的音乐列表
     * @param MusicBeanList
     */
    private void showWebMusic(List<MusicBean> MusicBeanList){
        runOnUiThread(()->{
            webMusicBeanList=MusicBeanList;
            for(int i=0;i<MusicBeanList.size();i++){
                //webMusicList只显示歌名
                webMusicList.add(MusicBeanList.get(i).getSongName());
            }
            adapter.notifyDataSetChanged();
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.confirm_btn://搜索音乐
                webMusicList.clear();
                webMusicBeanList.clear();
                String songName=search_et.getText().toString();
                String songNameUrl = "http://api.we-chat.cn/search?keywords="+songName;
                queryIdWithName(songNameUrl);
                break;
            case R.id.look_localMusic://插叙本地音乐
                Intent intent=new Intent(this, LocalMusicActivity.class);
                startActivity(intent);
            default:
                break;
        }
    }

    /**
     * 判断某音乐是否已经下载到本地
     * @param strFile
     * @return
     */
    public boolean hasDownload(String strFile){
        try{
            File f=new File(strFile);
            if(!f.exists()){
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    /**
     * toast提示信息
     * @param s
     */
    private void showToast(String s){
        runOnUiThread(()->{
            Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
        });
    }
}
