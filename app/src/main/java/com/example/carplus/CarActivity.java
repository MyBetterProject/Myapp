package com.example.carplus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.widget.TextView;

import com.example.carplus.databinding.BluetoothActivityBinding;


public class CarActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button startButton;
    private Button lightBUtton;
    private Button BreakButton;
    private Button FollowBUtton;

    private boolean isRunning = false; // 表示摄像头是否正在运行
    private boolean Cameraicon = false; //摄像头图标状态
    private boolean Lighticon = false; //闪光灯图标状态
    private boolean Breakicon = false;//停止图标状态
    private boolean Followicon = false;//循迹图标状态


    private TextView mTextView;


    // 创建 OkHttpClient 对象
    private OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS) // 设置连接超时时间为 5 秒
            .readTimeout(5, TimeUnit.SECONDS) // 设置读取超时时间为 5 秒
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.video);
        startButton = findViewById(R.id.btn_start);
        lightBUtton = findViewById(R.id.btn_light);
        BreakButton = findViewById(R.id.btn_break);
        FollowBUtton = findViewById(R.id.btn_follow);


        FollowBUtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFollow(v);
            }
        });

        BreakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBreak(v);
            }
        });

        lightBUtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLight(v);
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCamera(v);
                if (!isRunning) {
                    isRunning = true;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (isRunning) {
                                try {
                                    String url = "http://192.168.4.1/capture"; // 发送请求的URL
                                    Request request = new Request.Builder()
                                            .url(url)
                                            .build();

                                    // 异步发送请求并获取响应
                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response) throws IOException {
                                            InputStream inputStream = response.body().byteStream();
                                            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                                            // 在主线程更新UI
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    imageView.setImageBitmap(bitmap);
                                                }
                                            });

                                            inputStream.close(); // 关闭输入流
                                        }
                                    });

                                    Thread.sleep(200); // 延迟 0.2 秒，避免过于频繁的刷新
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                } else {
                    isRunning = false;
                    // 清空 ImageView 显示的图像
                    imageView.setImageBitmap(null);

                    // 向esp32-cam发送停止摄像头的指令
                    try {
                        String url = "http://192.168.4.1/stop"; // 发送请求的URL
                        Request request = new Request.Builder()
                                .url(url)
                                .build();

                        // 异步发送请求并获取响应
                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                // TODO: 处理响应结果
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Cameraicon = false;

    }

    public void toggleCamera(View view) {
        Button btnStart = (Button) findViewById(R.id.btn_start);

        if (!Cameraicon) {
            // 当前相机未开启，开启相机
            Cameraicon = true;
            btnStart.setBackgroundResource(R.drawable.cameraopen); // 将按钮背景设置为已开启
        } else {
            // 当前相机已开启，关闭相机
            Cameraicon = false;
            btnStart.setBackgroundResource(R.drawable.cameraclose); // 将按钮背景设置为已关闭
        }


    }

    public void toggleLight(View view) {
        if (!Lighticon) {
            // 向esp32-cam发送开启闪光灯的指令
            try {
                String url = "http://192.168.4.1/control?var=flash&val=1"; // 发送请求的URL
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                // 异步发送请求并获取响应
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // TODO: 处理响应结果
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            Lighticon = true;
            lightBUtton.setBackgroundResource(R.drawable.lightopen);
        } else {
            // 向esp32-cam发送关闭闪光灯的指令
            try {
                String url = "http://192.168.4.1/control?var=flash&val=0"; // 发送请求的URL
                Request request = new Request.Builder()
                        .url(url)
                        .build();

                // 异步发送请求并获取响应
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        // TODO: 处理响应结果
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            Lighticon = false;
            lightBUtton.setBackgroundResource(R.drawable.lightclose);
        }
    }

    public void toggleBreak(View view) {
        if (!Breakicon) {
            // 如果当前状态是false，就把变量的值设为true，并且把寻迹按钮的背景设置成新的图标
            Breakicon = true;
            BreakButton.setBackgroundResource(R.drawable.break0);
        } else {
            // 否则，把变量的值设为false，并且把寻迹按钮的背景设置成原来的图标
            Breakicon = false;
            BreakButton.setBackgroundResource(R.drawable.break1);
        }
    }

    public void toggleFollow(View view) {
        if (!Followicon) {
            Followicon = true;
            FollowBUtton.setBackgroundResource(R.drawable.follow0);
        } else {
            Followicon = false;
            FollowBUtton.setBackgroundResource(R.drawable.follow1);
        }
    }

    //该方法用于创建显示Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //在选项菜单打开以后会调用这个方法，设置menu图标显示（icon）
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    //该方法对菜单的item进行监听
    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                if (!CarActivity.class.isAssignableFrom(this.getClass())) {
                    Intent intent1 = new Intent(CarActivity.this, CarActivity.class);
                    startActivity(intent1);
                }
                return true;

            case R.id.menu2:
                if (!MainActivity.class.isAssignableFrom(this.getClass())) {
                    Intent intent2 = new Intent(CarActivity.this, MainActivity.class);
                    startActivity(intent2);
                }
                return true;

            // 添加其他菜单项的处理逻辑


            default:
                return super.onOptionsItemSelected(item);
        }
    }
}














