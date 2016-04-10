package com.github.lzyzsd.memorybugs;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

//    private static TextView sTextView;//Leak1 使用静态变量，则在start 新的activity的时候，sTextView仍然指向有效，而这个最终导致了整个mainactivity的instance都不能释放
    private TextView sTextView;
    private Button mStartBButton;
    private Button mStartAllocationButton;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sTextView = (TextView) findViewById(R.id.tv_text);
        sTextView.setText("Hello World!");

        mStartBButton = (Button) findViewById(R.id.btn_start_b);
        mStartBButton.setOnClickListener(this);

        mStartAllocationButton = (Button) findViewById(R.id.btn_allocation);
        mStartAllocationButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_b:
                startB();
                break;
            case R.id.btn_allocation:
                startAllocationLargeNumbersOfObjects();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);//remove all handler and messages.
    }

    private void startB() {

//        sTextView = null;//leak1 另一种改法
        finish();
        startActivity(new Intent(this, ActivityB.class));
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("post delayed may leak");//没有观察到leak发生
                //Android Lint也没有提示警告
            }
        }, 5000);
        Toast.makeText(this, "请注意查看通知栏LeakMemory", Toast.LENGTH_SHORT).show();
    }

    private void startAllocationLargeNumbersOfObjects() {
        Toast.makeText(this, "请注意查看MemoryMonitor 以及AllocationTracker", Toast.LENGTH_SHORT).show();
//        for (int i = 0; i < 10000; i++) {
            Rect rect = new Rect(0, 0, 100, 100);//这段代码创建了多个Rect对象，会造成内存上升，但并不会发生内存泄漏，通过GC可以释放。
            //正常情况下这么写代码很少见。一个常见的错误例子是在listview 的getView里面每次都申请一个view而不是复用。
            System.out.println("-------: " + rect.width());
//        }
    }
}
/*
* Reference
* http://www.bkjia.com/Androidjc/904357.html
* http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2014/1123/2047.html
* http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0511/2861.html
* */