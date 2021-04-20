package cn.ghzn.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.apkfuns.logutils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Delayed;

import cn.ghzn.player.receiver.VarReceiver;
import cn.ghzn.player.sqlite.source.Source;

import static androidx.core.os.HandlerCompat.postDelayed;
import static cn.ghzn.player.ImportActivity.getMap1;
import static cn.ghzn.player.MainActivity.*;
import static cn.ghzn.player.util.InfoUtils.getDeviceId;
import static cn.ghzn.player.util.InfoUtils.getRandomString;
import static cn.ghzn.player.util.ViewImportUtils.getSonImage;
import static java.lang.Thread.sleep;

public class OneSplitViewActivity extends Activity {
    private static final String TAG = "OneSplitViewActivity";
    private BroadcastReceiver mBroadcastReceiver;
    private ArrayList arrayList;
    GestureDetector mGestureDetector;//私有控件
    private Runnable mRunnable;//目前暂时用不到，对于其他分屏的使用，可删或不变该变量


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app.setCurrentActivity(this);
        app.setMediaPlayState(true);
        Log.d(TAG,"this is 跳转成功");

        if (app.isExtraState()) {//外部导入状态为真
            Log.d(TAG,"app.isExtraState()" + app.isExtraState());
            Intent intent = getIntent();
            //以文件的数量获取分屏样式，
            if (app.getFileCounts() == 0 && app.getFilesParent() == null) {
                app.setFileCounts(intent.getIntExtra("splitView",0));
                app.setFilesParent(intent.getStringExtra("filesParent"));
            }
            LogUtils.e(app.getFileCounts());
            LogUtils.e(app.getFilesParent());

            File f = new File(app.getFilesParent());//创建文件对象需判断有无，无则创建
            if (!f.exists()) {
                f.mkdirs();//区分之二：创建多级目录和创建当前目录区别
            }
            File[] files = f.listFiles();

            String[] splits = files[0].getName().split("\\-");//A-B-C；任取一文件夹，仅作为数据库存储信息的参考对象
            Log.d(TAG,"this is files[0].getName()" + files[0].getName());
            app.setSplit_view(splits[0]);
            app.setSplit_mode(splits[1]);
            String split_widget = splits[2];//c
            Log.d(TAG,"this is split_view,split_mode,split_widget" + app.getSplit_view() +"***"+ app.getSplit_mode() + "***" + split_widget);

            initWidget(app.getSplit_mode());
            Log.d(TAG,"this is initWidget(app.getSplit_mode())");

            for(File file : files){//将子类文件夹名与其绝对地址放入map集合中，不用管有多少个文件夹
                getMap1().put(file.getName(), file.getAbsolutePath());//形成键值对，方便取出作为资源导入
            }

            String key = app.getSplit_view() + "-" + app.getSplit_mode();
            arrayList = getSonImage(getMap1().get(key + "-1").toString());//将子文件夹里的地址全部赋给ArrayList的数组里
            app.setSonSource(getMap1().get(key + "-1").toString());//data：所有存储子文件夹的地址
            LogUtils.d(TAG,"getMap1().size()" + getMap1().size(),"Integer.parseInt(app.getSplit_view())" + Integer.parseInt(app.getSplit_view()));

        }else {
            //进行数据库读取
            initWidget(app.getSplit_mode());
            arrayList = getSonImage(app.getSon_source());
        }

        if (app.getSplit_view() != null) {//该判断可有可无，命名规则跳转前已检查
            mBroadcastReceiver = VarReceiver.getInstance().setBroadListener(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG,"this is varReceiver");
                    Log.d(TAG,"why is mBroadcastReceiver:app.getListNum1()广播：" + app.getListNum1());
                    playSonImage();
//                    unregisterReceiver(mBroadcastReceiver);
                }
            });
            IntentFilter filter = new IntentFilter();
            filter.addAction("0");
            registerReceiver(mBroadcastReceiver,filter);//注册广播

            playSonImage();//参数arrayList是默认的，可含或不含参，结果都一样
            //只需要存储有效数据
            if (app.isExtraState()) {
//                app.setCreate_time(new Date());//new Date()出来的时间是本地时间
//            Source source = DaoManager.getInstance().getSession().getSourceDao().queryBuilder().unique();//一查新建的Device表
                if(app.getSource() == null){//这一步多余
                    app.setSource(new Source());//表不存在则新建赋值
                    daoManager.getSession().getSourceDao().insert(getSource(app.getSource()));//单例(操作库对象)-操作表对象-操作表实例.进行操作；
                }else{//存在则直接修改
                    daoManager.getSession().getSourceDao().update(getSource(app.getSource()));
                }
                app.getDevice().setAuthority_state(app.isAuthority_state());//device表在main中一定创建，故不为null
                app.getDevice().setAuthority_time(app.getAuthority_time());
                app.getDevice().setAuthority_expired(app.getAuthority_expired());
                daoManager.getSession().getDeviceDao().update(app.getDevice());//更新表
                Log.d(TAG,"this is done 数据存储");
//                MainActivity main = new MainActivity();
//                main.initAuthorXml();

            }
        } else {
            Log.d(TAG,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作");
            Toast.makeText(this,"ghznPlayer文件夹内文件数量与分屏要求的文件数不同，请按照使用手册进行操作",Toast.LENGTH_LONG).show();
        }
    }

    public OneSplitViewActivity() {
        super();
    }

    private Source getSource(Source source) {//对数据库进行覆写
        source.setProgram_id(getRandomString(5));
        source.setSplit_view(app.getSplit_view());
        source.setSplit_mode(app.getSplit_mode());
        source.setSon_source(app.getSonSource());//存储的是子资源，但取出来用时需用来获取对象。
        source.setStart_time(app.getStart_time());
        source.setEnd_time(app.getEnd_time());
        source.setCreate_time(app.getCreate_time());
        source.setFirst_time(app.getFirst_time());
        source.setTime_difference(app.getTime_difference());
        source.setRelative_time(app.getRelative_time());

        return source;
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initWidget(String split_mode) {

        switch (split_mode){//该步骤可以简写，这样写仅为了和其他activity代码相似
            case "1":
//                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
//                View view=inflater.inflate(R.layout.activity_splitview_one1, null);//setContent()的实际底层代码逻辑相关
                setContentView(R.layout.activity_splitview_one1);
                app.setImageView_1((ImageView)this.findViewById(R.id.imageView_one1_1));//this指当前当前layout里找控件，若是去其他layout找，需要用到layout过滤器如上
                app.setVideoView_1((CustomVideoView)this.findViewById(R.id.videoView_one1_1));

                setDialog(this);
                app.getImageView_1().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                app.getVideoView_1().setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);
                        return true;
                    }
                });
                Log.d(TAG,"this is case \"1\":");
                break;
            default:
                Log.d(TAG,"this is switch(split_mode)_default");
                break;
        }
    }
    public void playSonImage(){

        if (app.getListNum1() >= arrayList.size()) {
            app.setListNum1(0);//当文件1使用此方法用完后，由于是全局变量，找完文件夹1资源后，需置0再拿给文件夹2使用
//            if (app.isFinishFlag()) {
//                finish();
//            }
//            playSonImage();1111
//        } else {1111
        }
            Log.d(TAG,"开始执行执行播放程序");

            app.setFile(new File(arrayList.get(app.getListNum1()).toString()));
            if ((app.getFile().getName().endsWith("jpg") || app.getFile().getName().endsWith("jpeg")||app.getFile().getName().endsWith("png"))) {
                Log.d(TAG,"执行图片播放，添加了图片：》》》》》" + app.getFile().getAbsolutePath());
                app.setForMat1(1);//记录此时控件播放为图片
                app.getImageView_1().setImageURI(Uri.fromFile(app.getFile()));
                app.getImageView_1().setVisibility(View.VISIBLE);
                app.getVideoView_1().setVisibility(View.GONE);
                app.setListNum1(app.getListNum1() + 1);

                app.setStartTime(System.currentTimeMillis());
                app.getHandler().postDelayed(mRunnable = new Runnable(){
                    @Override
                    public void run() {
                        Log.d(TAG,"执行延迟播放图片3秒，图片位于：" + app.getFile().getAbsolutePath());
                        if (app.getPlayFlag() == 0) {//非播放状态下，使其递归方法失效，但listNum递增1.当播放按下时，新建剩余延迟，再进行递增
                            playSonImage();
                        }
                    }
                },app.getDelayMillis());//5秒后结束当前图片
                app.setRunnable1(mRunnable);//无法set线程，只好绑定mRunnable到全局Runnable
            }
            else if (app.getFile().getName().endsWith("mp4") || app.getFile().getName().endsWith("avi") || app.getFile().getName().endsWith("3gp")) {
                Log.d(TAG,"执行视频播放，添加了视频：《《《《《" + app.getFile().getAbsolutePath());
                app.setForMat1(2);//记录此时控件播放为视频
                if (app.isMediaPlayState()) {
                    app.getVideoView_1().setVideoURI(Uri.fromFile(app.getFile()));
                    app.getVideoView_1().setVisibility(View.VISIBLE);
                    app.getImageView_1().setVisibility(View.GONE);
                    app.getVideoView_1().start();

                    app.getVideoView_1().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {//图片处run()是交集，而视频处监听重写方法不是完全交集；
                            Log.d(TAG,"执行播放完视频，视频位于：" + app.getFile().getAbsolutePath());
                            app.setListNum1(app.getListNum1() + 1);
                            playSonImage();
                        }
                    });
                    app.getVideoView_1().setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mp, int what, int extra) {
                            app.getVideoView_1().stopPlayback();
                            return true;
                        }
                    });
                }
            }
//        }1111
    }

    public void setDialog(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
//        View View = this.getLayoutInflater().inflate(R.layout.activity_dialog, null);
        alertDialog.setView(app.getView());
        final AlertDialog AlertDialogs = alertDialog.create();//如上是我自己找到新建的弹窗，下面是把新建的弹窗赋给新建的手势命令中的长按。
        mGestureDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.d(TAG, "OnLongPressTap");
                AlertDialogs.show();
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.setPlayFlag(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.setMediaPlayState(false);
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        if (app.getRunnable1() != null) {
            app.getHandler().removeCallbacks(app.getRunnable1());
        }
    }

}
