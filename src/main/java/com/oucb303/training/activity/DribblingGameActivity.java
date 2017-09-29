package com.oucb303.training.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.oucb303.training.R;
import com.oucb303.training.adpter.DGroupListViewAdapter;
import com.oucb303.training.adpter.DribblingGameAdapter;
import com.oucb303.training.device.Device;
import com.oucb303.training.device.Order;
import com.oucb303.training.listener.AddOrSubBtnClickListener;
import com.oucb303.training.listener.CheckBoxClickListener;
import com.oucb303.training.listener.MySeekBarListener;
import com.oucb303.training.listener.SpinnerItemSelectedListener;
import com.oucb303.training.model.CheckBox;
import com.oucb303.training.model.TimeInfo;
import com.oucb303.training.threads.ReceiveThread;
import com.oucb303.training.threads.Timer;
import com.oucb303.training.utils.DataAnalyzeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 运球比赛
 * Created by HP on 2016/12/5.
 */
public class DribblingGameActivity extends AppCompatActivity {


    @Bind(R.id.bt_distance_cancel)
    ImageView btDistanceCancel;
    @Bind(R.id.layout_cancel)
    LinearLayout layoutCancel;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_training_time)
    TextView tvTrainingTime;
    @Bind(R.id.img_training_time_sub)
    ImageView imgTrainingTimeSub;
    @Bind(R.id.bar_training_time)
    SeekBar barTrainingTime;
    @Bind(R.id.img_training_time_add)
    ImageView imgTrainingTimeAdd;
    @Bind(R.id.training_subgroups)
    Spinner trainingSubgroups;
    @Bind(R.id.lv_group)
    ListView lvGroup;
    @Bind(R.id.img_action_mode_light)
    ImageView imgActionModeLight;
    @Bind(R.id.img_action_mode_touch)
    ImageView imgActionModeTouch;
    @Bind(R.id.img_action_mode_together)
    ImageView imgActionModeTogether;
    @Bind(R.id.img_light_mode_beside)
    ImageView imgLightModeBeside;
    @Bind(R.id.img_light_mode_center)
    ImageView imgLightModeCenter;
    @Bind(R.id.img_light_mode_allr)
    ImageView imgLightModeAllr;
    @Bind(R.id.cb_voice)
    android.widget.CheckBox cbVoice;
    @Bind(R.id.cb_end_voice)
    android.widget.CheckBox cbEndVoice;
    @Bind(R.id.ll_params)
    LinearLayout llParams;
    @Bind(R.id.sv_container)
    ScrollView svContainer;
    @Bind(R.id.tv_total_time)
    TextView tvTotalTime;
    @Bind(R.id.lv_scores)
    ListView lvScores;
    @Bind(R.id.btn_begin)
    Button btnBegin;
    @Bind(R.id.sp_dev_num)
    Spinner spDevNum;


    private final int TIME_RECEIVE = 1;
    private final int UPDATE_SCORES = 2;
    private final int STOP_TRAINING = 4;
    @Bind(R.id.tv_device_list)
    TextView tvDeviceList;
    @Bind(R.id.img_help)
    ImageView imgHelp;
    @Bind(R.id.img_save)
    ImageView imgSave;
    @Bind(R.id.tv_delay_time)
    TextView tvDelayTime;
    @Bind(R.id.img_delay_time_sub)
    ImageView imgDelayTimeSub;
    @Bind(R.id.bar_delay_time)
    SeekBar barDelayTime;
    @Bind(R.id.img_delay_time_add)
    ImageView imgDelayTimeAdd;
    @Bind(R.id.tv_over_time)
    TextView tvOverTime;
    @Bind(R.id.img_over_time_sub)
    ImageView imgOverTimeSub;
    @Bind(R.id.bar_over_time)
    SeekBar barOverTime;
    @Bind(R.id.img_over_time_add)
    ImageView imgOverTimeAdd;
    @Bind(R.id.btn_on)
    Button btnOn;
    @Bind(R.id.btn_off)
    Button btnOff;
    @Bind(R.id.img_blink_mode_none)
    ImageView imgBlinkModeNone;
    @Bind(R.id.img_blink_mode_slow)
    ImageView imgBlinkModeSlow;
    @Bind(R.id.img_blink_mode_fast)
    ImageView imgBlinkModeFast;
    @Bind(R.id.img_level_sub)
    ImageView imgLevelsub;
    @Bind(R.id.img_level_add)
    ImageView imgLevelAdd;
    @Bind(R.id.bar_level)
    SeekBar barLevel;
    @Bind(R.id.tv_level)
    TextView tvLevel;
    @Bind(R.id.ll_level)
    LinearLayout lLevel;

    private Device device;
    //所选设备个数，分组数
    private int totalNum, groupNum;
    private DGroupListViewAdapter dGroupListViewAdapter;
    private DribblingGameAdapter dribblingGameAdapter;
    //感应模式和灯光模式集合
    private CheckBox actionModeCheckBox, lightModeCheckBox,blinkModeCheckBox;
    //是否训练标志
    private boolean trainningFlag = false;
    //计时器
    private Timer timer;
    //训练总时间,延迟时间,超时时间  单位是毫秒
    private int trainingTime, delayTime, overTime;
    //成绩统计
    private int[] scores;
    //每次开的三个灯组号和设备编号
    private char[] deviceNums;
    //每组设备灯亮起的时间
    private long[] duration;
    //训练开始时间
    private long startTime;
    private ArrayList<Integer> listRand = new ArrayList<>();


    private int level=2;

    private Handler handler = new Handler() {
        //处理接收过来的数据的方法
        @Override
        public void handleMessage(Message msg) {
            String data = msg.obj.toString();
            //比赛结束则不接收任何数据
            if (!trainningFlag)
                return;
            switch (msg.what) {
                //更新计时
                case Timer.TIMER_FLAG:
                    tvTotalTime.setText(data);
                    //判断结束
                    if (timer.time >= trainingTime) {
                        Message message = Message.obtain();
                        message.what = STOP_TRAINING;
                        message.obj = "";
                        handler.sendMessage(message);
                    }
                    break;
                //接收到返回的时间
                case TIME_RECEIVE:
                    if (data != null && data.length() > 7)
                        analyseData(data);
                    for (int i = 0; i < groupNum; i++) {
                        if (duration[i] != 0 && System.currentTimeMillis() - duration[i] > overTime) {
                            char deviceNum = Device.DEVICE_LIST.get(listRand.get(i)).getDeviceNum();
                            duration[i] = 0;
                            turnOffLight(deviceNum);
                            turnOnLight(i);
                        }
                    }
                    break;
                //更新成绩
                case UPDATE_SCORES:
                    dribblingGameAdapter.notifyDataSetChanged();
                    break;
                case STOP_TRAINING:
                    stopTraining();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbling_game);
        ButterKnife.bind(this);
        level = getIntent().getIntExtra("level", 0);
        device = new Device(this);
        //更新设备连接列表
        device.createDeviceList(this);
        //判断协调器是否插入
        if (device.devCount > 0) {
            //连接
            device.connect(this);
            //设备初始化
            device.initConfig();
        }
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        imgSave.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        device.turnOffAllTheLight();
        ReceiveThread.stopThread();
        if (device.devCount > 0)
            device.disconnect();
    }

    public void initView() {
        imgSave.setVisibility(View.VISIBLE);
        imgHelp.setVisibility(View.VISIBLE);
        if (level == 4){
            lLevel.setVisibility(View.GONE);
            tvTitle.setText("多人混战");
        } else{
            tvTitle.setText("运球比赛");
            lLevel.setVisibility(View.VISIBLE);
        }

        dribblingGameAdapter = new DribblingGameAdapter(this);
        lvScores.setAdapter(dribblingGameAdapter);
        //初始化训练强度拖动条
        barLevel.setOnSeekBarChangeListener(new MySeekBarListener(barTrainingTime,tvLevel, 2));
        imgLevelsub.setOnTouchListener(new AddOrSubBtnClickListener(barLevel, 0));
        imgLevelAdd.setOnTouchListener(new AddOrSubBtnClickListener(barLevel, 1));
//        if (level != 0) {
//            switch (level) {
//                case 1:
//                    level = 2;
//                    break;
//                case 2:
//                    level = 4;
//                    break;
//                case 3:
//                    level = 10;
//                    break;
//            }
//            barTrainingTime.setProgress(level);
//        }
//        barTrainingTime.setProgress(level);

        //初始化分组listview
        dGroupListViewAdapter = new DGroupListViewAdapter(DribblingGameActivity.this);
        lvGroup.setAdapter(dGroupListViewAdapter);

        //解决listview与scrollview的滑动冲突
        lvGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                //从listView 抬起时将控制权还给scrollview
                if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    svContainer.requestDisallowInterceptTouchEvent(false);
                    //requestDisallowInterceptTouchEvent（true）方法是用来子View告诉父容器不要拦截我们的事件的
                else
                    svContainer.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        //初始化得分listview
        dribblingGameAdapter = new DribblingGameAdapter(this);
        lvScores.setAdapter(dribblingGameAdapter);

        //选择设备个数spinner
        String[] num = new String[Device.DEVICE_LIST.size()];
        for (int i = 0; i < num.length; i++) {
            num[i] = (i + 1) + "个";
        }
        spDevNum.setOnItemSelectedListener(new SpinnerItemSelectedListener(this, spDevNum, num) {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                super.onItemSelected(adapterView, view, i, l);
                totalNum = i + 1;

                String str = "";
                for (int j = 0; j < totalNum; j++)
                    str += Device.DEVICE_LIST.get(j).getDeviceNum() + "  ";
                tvDeviceList.setText(str);
//                Toast.makeText(DribblingGameActivity.this, "所选择的设备个数是" + totalNum + "个", Toast.LENGTH_SHORT).show();
            }
        });

        //训练分组spinner
        String[] groupCount = new String[]{" ", "一组", "两组", "三组"};
        trainingSubgroups.setOnItemSelectedListener(new SpinnerItemSelectedListener(this, trainingSubgroups, groupCount) {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                groupNum = i;
                if (totalNum < groupNum) {
                    Toast.makeText(DribblingGameActivity.this, "当前设备数量为" + Device.DEVICE_LIST.size() + ",不能分成" + i + "组!",
                            Toast.LENGTH_LONG).show();
                    trainingSubgroups.setSelection(0);
                    groupNum = 0;
                }
                dGroupListViewAdapter.setGroupNum(groupNum);
                dGroupListViewAdapter.notifyDataSetChanged();
            }
        });

        //初始化训练时间拖动条
        //拖动进度条的事件监听  第一个：显示时间tectview，第二个：最大值
        barTrainingTime.setOnSeekBarChangeListener(new MySeekBarListener(tvTrainingTime, 10));
        //直接在触摸屏进行按住和松开事件的操作
        imgTrainingTimeAdd.setOnTouchListener(new AddOrSubBtnClickListener(barTrainingTime, 1));
        imgTrainingTimeSub.setOnTouchListener(new AddOrSubBtnClickListener(barTrainingTime, 0));
        //初始化延迟时间拖动条
        barDelayTime.setOnSeekBarChangeListener(new MySeekBarListener(tvDelayTime, 10));
        imgDelayTimeAdd.setOnTouchListener(new AddOrSubBtnClickListener(barDelayTime, 1));
        imgDelayTimeSub.setOnTouchListener(new AddOrSubBtnClickListener(barDelayTime, 0));
        //初始化超时时间拖动条
        barOverTime.setOnSeekBarChangeListener(new MySeekBarListener(tvOverTime, 28, 2));
        imgOverTimeAdd.setOnTouchListener(new AddOrSubBtnClickListener(barOverTime, 1));
        imgOverTimeSub.setOnTouchListener(new AddOrSubBtnClickListener(barOverTime, 0));

        //设定感应模式checkBox组合的点击事件
        ImageView[] views = new ImageView[]{imgActionModeLight, imgActionModeTouch, imgActionModeTogether};
        actionModeCheckBox = new CheckBox(1, views);
        new CheckBoxClickListener(actionModeCheckBox);
        //设定灯光模式checkBox组合的点击事件
        ImageView[] views1 = new ImageView[]{imgLightModeBeside, imgLightModeCenter, imgLightModeAllr};
        lightModeCheckBox = new CheckBox(1, views1);
        new CheckBoxClickListener(lightModeCheckBox);
        //设定闪烁模式checkBox组合的点击事件
        ImageView[] views2 = new ImageView[]{imgBlinkModeNone, imgBlinkModeSlow, imgBlinkModeFast};
        blinkModeCheckBox = new CheckBox(1, views2);
        new CheckBoxClickListener(blinkModeCheckBox);
    }

    @OnClick({R.id.layout_cancel, R.id.btn_begin, R.id.img_help, R.id.btn_on, R.id.btn_off, R.id.img_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_cancel:
                this.finish();
                device.turnOffAllTheLight();
                break;
            case R.id.btn_begin:
                //检测设备
                if (!device.checkDevice(this))
                    return;
                if (groupNum <= 0) {
                    Toast.makeText(this, "未选择分组,不能开始!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (trainningFlag)
                    stopTraining();
                else
                    startTraining();
                break;
            case R.id.img_help:
                Intent intent = new Intent(this, HelpActivity.class);
                if (level == 4)
                    intent.putExtra("flag", 6);
                else
                    intent.putExtra("flag", 5);
                startActivity(intent);
                break;
            case R.id.btn_on:
                //totalNum组数，1：每组设备个数，1：类型
                device.turnOnButton(totalNum, 1, 0);
                break;
            case R.id.btn_off:
                for (int i = 0; i < totalNum; i++)
                    turnOffLight(Device.DEVICE_LIST.get(i).getDeviceNum());
                break;
            case R.id.img_save:
                Intent it = new Intent(this, SaveActivity.class);
                Bundle bundle = new Bundle();
                //trainingCategory 1:折返跑 2:纵跳摸高 3:仰卧起坐 5:运球比赛、多人混战 ...
                bundle.putString("trainingCategory", "5");
                if (level == 4) {
                    bundle.putString("trainingName", "多人混战");
                } else {
                    bundle.putString("trainingName", "运球比赛");
                }
                //训练总时间
                bundle.putInt("trainingTime", trainingTime);
                //设备个数
                bundle.putInt("DeviceNum", totalNum);
                //每组得分
                bundle.putIntArray("scores", scores);
                it.putExtras(bundle);
                startActivity(it);
                break;
        }
    }

    //开始训练
    public void startTraining() {
        btnBegin.setText("停止");
        trainningFlag = true;
        //trainingTime是总时间
        trainingTime = (int) (new Double(tvTrainingTime.getText().toString()) * 60 * 1000);
        delayTime = new Integer(tvDelayTime.getText().toString()) * 1000;
        overTime = new Integer(tvOverTime.getText().toString()) * 1000;

        deviceNums = new char[groupNum];
        duration = new long[groupNum];
        scores = new int[groupNum];
        listRand.clear();
        //初始化
        for (int i = 0; i < groupNum; i++) {
            scores[i] = 0;
        }
        dribblingGameAdapter.setScores(scores);
        dribblingGameAdapter.notifyDataSetChanged();
        //创建随机队列
        createRandomNumber();
        Log.i("listRand-----------", "" + listRand.size());
        //开启全部灯
        for (int i = 0; i < listRand.size(); i++) {
            device.sendOrder(Device.DEVICE_LIST.get(listRand.get(i)).getDeviceNum(),
                    Order.LightColor.values()[i + 1],
                    Order.VoiceMode.values()[cbVoice.isChecked() ? 1 : 0],
                    Order.BlinkModel.values()[blinkModeCheckBox.getCheckId()-1],
                    Order.LightModel.OUTER,
                    Order.ActionModel.values()[actionModeCheckBox.getCheckId()],
                    Order.EndVoice.values()[cbEndVoice.isChecked() ? 1 : 0]);
            //每次开灯的设备编号
            deviceNums[i] = Device.DEVICE_LIST.get(listRand.get(i)).getDeviceNum();
            //每组设备灯亮起的当前时间
            duration[i] = System.currentTimeMillis();
        }
        //清除串口数据
        new ReceiveThread(handler, device.ftDev, ReceiveThread.CLEAR_DATA_THREAD, 0).start();

        //开启接收设备返回时间的监听线程,返回时间和编号
        new ReceiveThread(handler, device.ftDev, ReceiveThread.TIME_RECEIVE_THREAD, TIME_RECEIVE).start();

        startTime = System.currentTimeMillis();
        //开启计时器
        timer = new Timer(handler, trainingTime);
        timer.setBeginTime(startTime);
        timer.start();
    }

    //停止训练
    public void stopTraining() {
        timer.stopTimer();
        btnBegin.setText("开始");
        imgSave.setEnabled(true);
        btnBegin.setEnabled(false);
        trainningFlag = false;
        for (int i = 0; i < groupNum; i++)
            turnOffLight(Device.DEVICE_LIST.get(listRand.get(i)).getDeviceNum());

        ReceiveThread.stopThread();
        Timer.sleep(100);
        endingMovie();
        //Timer.sleep(1000);
        btnBegin.setEnabled(true);

    }

    //解析数据
    public void analyseData(final String data) {
        //infos里有设备编号和返回时间
        List<TimeInfo> infos = DataAnalyzeUtils.analyzeTimeData(data);
        for (TimeInfo info : infos) {
            char deviceNum = info.getDeviceNum();
            for (int i = 0; i < groupNum; i++) {
                //i 就是对应的组号
                if (deviceNum == deviceNums[i]) {
                    scores[i]++;
                    turnOnLight(i);
                }
            }
        }
        //更新成绩
        Message msg = Message.obtain();
        msg.what = UPDATE_SCORES;
        msg.obj = "";
        handler.sendMessage(msg);
    }

    //开某一个组的灯
    public void turnOnLight(final int groupId) {
        //移除此列表中指定位置上的元素。
        listRand.remove(groupId);
        Random random = new Random();
        while (listRand.size() < groupNum) {
            int rand = random.nextInt(totalNum);
            if (!listRand.contains(rand)) {
                //将指定的元素插入此列表中的指定位置。
                listRand.add(groupId, rand);
                deviceNums[groupId] = Device.DEVICE_LIST.get(rand).getDeviceNum();
            }
        }
        Log.i("现在的随机序列是什么", "" + listRand);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Timer.sleep(200 + delayTime);
                //若训练结束则返回
                if (!trainningFlag)
                    return;
                device.sendOrder(Device.DEVICE_LIST.get(listRand.get(groupId)).getDeviceNum(),
                        Order.LightColor.values()[groupId + 1],
                        Order.VoiceMode.values()[cbVoice.isChecked() ? 1 : 0],
                        Order.BlinkModel.values()[blinkModeCheckBox.getCheckId()-1],
                        Order.LightModel.OUTER,
                        Order.ActionModel.values()[actionModeCheckBox.getCheckId()],
                        Order.EndVoice.values()[cbEndVoice.isChecked() ? 1 : 0]);
                duration[groupId] = System.currentTimeMillis();
            }
        }).start();
    }

    //关灯
    private void turnOffLight(final char deviceNum) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                device.sendOrder(deviceNum,
                        Order.LightColor.NONE,
                        Order.VoiceMode.NONE,
                        Order.BlinkModel.NONE,
                        Order.LightModel.TURN_OFF,
                        Order.ActionModel.TURN_OFF,
                        Order.EndVoice.NONE);
            }
        }).start();
    }

    //生成随机数
    private void createRandomNumber() {
        Random random = new Random();
        while (listRand.size() < groupNum) {
            int randomInt = random.nextInt(totalNum);
            //如果随机list里不包含产生的这个随机数，则将产生的这个随机数加入到随机list中
            if (!listRand.contains(randomInt)) {
                listRand.add(randomInt);
            } else {
                System.out.println("该数字已经被添加,不能重复添加");
            }
        }
    }

    //结束动画
    private void endingMovie() {
        int position = 0;
        int max = scores[0];
        for (int i = 1; i < scores.length; i++) {
            if (max < scores[i]) {
                max = scores[i];
                position = i;
            }
        }

        int temp = 0;
        while (temp < 5) {
            for (int i = 0; i < totalNum; i++)
                device.sendOrder(Device.DEVICE_LIST.get(i).getDeviceNum(),
                        Order.LightColor.values()[position + 1],
                        Order.VoiceMode.NONE,
                        Order.BlinkModel.NONE,
                        Order.LightModel.OUTER,
                        Order.ActionModel.NONE,
                        Order.EndVoice.NONE);

            Timer.sleep(100);
            for (int i = 0; i < totalNum; i++)
                device.sendOrder(Device.DEVICE_LIST.get(i).getDeviceNum(),
                        Order.LightColor.NONE,
                        Order.VoiceMode.NONE,
                        Order.BlinkModel.NONE,
                        Order.LightModel.TURN_OFF,
                        Order.ActionModel.NONE,
                        Order.EndVoice.NONE);
            Timer.sleep(100);
            temp++;
        }

    }
}
