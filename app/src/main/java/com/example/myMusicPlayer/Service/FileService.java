package com.example.myMusicPlayer.Service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class FileService extends IntentService {
    private final String TAG="LOGCAT";
    private int fileLength, downloadLength;//文件大小
    private Handler handler = new Handler();
    public FileService() {
        super("DownLoadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    protected void onHandleIntent(Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            String downloadUrl = bundle.getString("musicUrl");
            //文件保存目录
            File dirs = new File(getFilesDir().getAbsolutePath(),"");
            // 检查文件夹是否存在，不存在则创建
            if (!dirs.exists()) {
                dirs.mkdir();
            }
            //文件的绝对路径
            File file = new File(dirs, bundle.getString("songName")+".mp3");
            Log.d(TAG,"下载启动："+downloadUrl+" --to-- "+ file.getPath());
            // 开始下载文件
            downloadFile(downloadUrl, file);
            // 下载结束
            Log.d(TAG,"下载结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件下载
     * @param downloadUrl
     * @param file
     */
    public void downloadFile(String downloadUrl, File file){
        FileOutputStream _outputStream;//文件输出流
        try {
            _outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "找不到目录！");
            e.printStackTrace();
            return;
        }
        InputStream _inputStream = null;//文件输入流
        try {
            //将String类型的url转变为URL类对象
            URL url = new URL(downloadUrl);
            //打开URL连接，获取URLConnection对象
            HttpURLConnection _downLoadCon = (HttpURLConnection) url.openConnection();
            //设定请求方法为GET
            _downLoadCon.setRequestMethod("GET");
            //得到文件总大小
            fileLength = _downLoadCon.getContentLength();
            //如果文件过小，说明文件异常，不应下载
            if(fileLength<=1024){
                return;
            }
            //读取网页内容（字符串流）
            _inputStream = _downLoadCon.getInputStream();
            //服务器返回的响应码
            int respondCode = _downLoadCon.getResponseCode();
            //返回200时，说明成功
            if (respondCode == 200) {
                // 数据块，等下把读取到的数据储存在这个数组，数组的大小视情况而定
                byte[] buffer = new byte[1024*8];
                int len;
                //循环读入
                while ((len = _inputStream.read(buffer)) != -1) {
                    _outputStream.write(buffer, 0, len);
                    downloadLength = downloadLength + len;
                    //Log.d(TAG, downloadLength + "/" + fileLength );
                }

            } else {
                Log.d(TAG, "respondCode:" + respondCode);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭流
                if (_outputStream != null) {
                    _outputStream.close();
                }
                if (_inputStream != null) {
                    _inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
