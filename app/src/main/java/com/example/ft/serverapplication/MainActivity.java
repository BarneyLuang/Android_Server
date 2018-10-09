package com.example.ft.serverapplication;

import android.Manifest;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.utils.PermissionHelper;
import com.example.utils.PermissionInterface;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PermissionInterface {
    private AsyncHttpServer server = new AsyncHttpServer();
    private AsyncServer mAsyncServer = new AsyncServer();
    private TextView tv_main ;
    static  final String TAG ="Async";

    private PermissionHelper mPermissionHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();

        tv_main = findViewById(R.id.tv_main);
        tv_main.setText(getHostIP());


        server.get("/", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                try {
                    Log.i(TAG, "onRequest:get ");
                    response.send(getIndexContent());
                }catch (IOException e){
                    e.printStackTrace();
                    response.code(500).end();
                }
            }
        });

        server.get("/files", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                JSONArray array = new JSONArray();
                File dir = new File("/storage/sdcard0/DCIM/Camera");
                File[] fileNames =  dir.listFiles();//
                Log.i(TAG, "Async onRequest: "+dir.toString());
                Log.i(TAG, " dir file count "+dir.listFiles().length); //
                if(fileNames!=null)
                {
                    for (File fileName :fileNames){
//                        File file = new File(dir, fileName);
                        File file = fileName ;
                        if (file.exists()&& file.isFile()&& file.getName().endsWith(".png")){   //
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("name",fileName);
                                jsonObject.put("paht",file.getAbsolutePath());
                                array.put(jsonObject);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    response.send(array.toString());
                }
                else {
                    response.send("没有文件");
                    Log.i("Async", "Async onRequest: 没有文件");
                }

            }
        });

        server.get("/file", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                JSONArray array = new JSONArray();
                File dir = new File("/storage/sdcard0/Pictures/Screenshots/Screenshot_20180920-142548.png");
                String fileNames = dir.toString();
                if(fileNames!=null)
                {
                    {
                        File file = dir;
                        if (file.exists()&& file.isFile()&& file.getName().endsWith(".png")){   //
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("name",fileNames);
                                jsonObject.put("paht",file.getAbsolutePath());
                                array.put(jsonObject);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                    response.send(array.toString());
                }
                else {
                    response.send("没有文件");
                }

            }
        });



        server.get("/file/*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {

                String path = request.getPath().replace("/file/", "");
                try {
                    path = URLDecoder.decode(path, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        response.sendStream(fis, fis.available());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                Log.i("Async", "Not  found path :"+path);
                response.code(404).send("Not found!");
            }
        });




//        server.get("/jquery-1.7.2.min.js", new HttpServerRequestCallback() {
//            @Override
//            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
//                try {
//                    String fullPath = request.getPath();
//                    fullPath = fullPath.replace("%20", " ");
//                    String resourceName = fullPath;
//                    if (resourceName.startsWith("/")) {
//                        resourceName = resourceName.substring(1);
//                    }
//                    if (resourceName.indexOf("?") > 0) {
//                        resourceName = resourceName.substring(0, resourceName.indexOf("?"));
//                    }
//                    response.setContentType("application/javascript");
//                    BufferedInputStream bInputStream = new BufferedInputStream(getAssets().open(resourceName));
//                    response.sendStream(bInputStream, bInputStream.available());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    response.code(404).end();
//                    return;
//                }
//            }
//        });

        server.listen(mAsyncServer,54321);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (server!= null){
            server.stop();
        }
        if (mAsyncServer !=null){
            mAsyncServer.stop();
        }
    }

    private static List<String> getFileNames(String path )
    {
        File file  = new  File(path);
        File[] files = file.listFiles();
        if (files== null) {
            Log.e("Async", "getFileNames: 空目录" );
            return  null;
        }
        List<String> s = new ArrayList<>();
        for (int i=0;i<file.length();i++){
            s.add(files[i].getName());
        }
        return  s;
    }

    private String getIndexContent() throws IOException{
        BufferedInputStream bInputStream = null;
        try{
            bInputStream = new BufferedInputStream(getAssets().open("connect.html"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = 0;
            byte[] tmp = new byte[10240];
            while ((len = bInputStream.read(tmp))>0){
                baos.write(tmp,0,len );
            }
            return new String(baos.toByteArray(),"utf-8");
        }catch (IOException e){
            e.printStackTrace();
            throw  e ;
        }finally {
            if (bInputStream!= null){
                try {
                    bInputStream.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }
    @Override    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults))
        {
            //权限请求结果，并已经处理了该回调
             return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public int getPermissionsRequestCode() {
        //设置权限请求requestCode ,只有不限onRequstPermissionsResult法中的其他请求码冲突即可
        return 10000;
    }

    @Override
    public String[] getPermissions() {
        //设置该界面所需的全部权限
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
    }

    @Override
    public void requestPermissionsSuccess() {
        //权限请求用户已经全部允许
        initView();
    }

    private void initView() {
        //已经拥有所需的权限，可以放心操作任何东西了
    }

    @Override
    public void requestPermissionsFail() {
        //权限请求不被用户允许。可以提示并退出或者提示权限的用途并重新发起权限申请

        finish();
    }


}
