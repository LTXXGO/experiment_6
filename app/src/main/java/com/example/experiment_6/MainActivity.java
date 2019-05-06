package com.example.experiment_6;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends Activity implements View.OnClickListener {
    private EditText editText;
    private Button button;
    private TextView textView;
    private final int SUCCESS = 1;
    private final int FAILURE = 0;
    private final int ERRORCODE = 2;
    protected String searchResult;
    JSONArray results = null;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    //获取信息成功后，对该信息进行JSON解析，得到所需要的信息，然后在textView上展示出来。
                    parseJSON(msg.obj.toString());
                    Toast.makeText(MainActivity.this, "获取数据成功！", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case FAILURE:
                    Toast.makeText(MainActivity.this, "获取数据失败！", Toast.LENGTH_SHORT)
                            .show();
                    break;

                case ERRORCODE:
                    Toast.makeText(MainActivity.this, "状态代码不为200！",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        init();
    }

    private void init() {
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textViewContent);
        button.setOnClickListener(this);
    }

//    //JSON解析方法
//    protected void JSONAnalysis(String string) {
//        JSONObject object = null;
//        try {
//            object = new JSONObject(string);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        //在获取的string这个JSON对象中，提取所需要的信息
//        JSONObject ObjectInfo = object.optJSONObject("results");
//        String trackName = ObjectInfo.optString("trackName");
//        String artistName = ObjectInfo.optString("artistName");
//        String kind = ObjectInfo.optString("kind");
//
//        searchResult = "名称：" + trackName + "\n创作者：" + artistName + "\n类型：";
//        textView.setText(searchResult);
//    }

    private void parseJSON(String jsonData) {
        try {
            JSONObject object = new JSONObject(jsonData);
            results = object.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject jsonObject = results.getJSONObject(i);
                String trackName = jsonObject.getString("trackName");
                String artistName = jsonObject.getString("artistName");
                String kind = jsonObject.getString("kind");

                searchResult = "名称：" + trackName + "\n创作者：" + artistName + "\n类型：";
                textView.setText(searchResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        //点击按钮事件，在主线程中开启一个子线程进行网络请求
        switch (v.getId()) {
            case R.id.button:
                new Thread() {
                    public void run() {
                        int code;
                        try {
                            String item = editText.getText().toString();
                            String path = "https://itunes.apple.com/search?term=" + item;
                            URL url = new URL(path);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");//使用GET方法获取
                            conn.setConnectTimeout(6000);
                            conn.setReadTimeout(6000);
                            code = conn.getResponseCode();
                            if(code == 200) {
                                //status code为200，则数据获取是正确的
                                InputStream in = conn.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                Message message = new Message();
                                message.what = SUCCESS;
                                message.obj = response.toString(); ;
                                handler.sendMessage(message);
                                parseJSON(response.toString());
                            } else {
                                Message message = new Message();
                                message.what = ERRORCODE;
                                handler.sendMessage(message);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //如果获取失败，或出现异常，那么子线程发送失败的消息（FAILURE）到主线程，主线程显示Toast，来告诉使用者，数据获取是失败。
                            Message message = new Message();
                            message.what = FAILURE;
                            handler.sendMessage(message);
                        }
                    };
                }.start();
                break;
            default:
                break;
        }
    }
}
