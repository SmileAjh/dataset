package allen.tencnet.baidulocation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;


public class MainActivity extends Activity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    private LocationClient mLocationClient;//定位SDK的核心类
    private TextView LocationResult;
    private Button startLocation;
    public MyLocationListener mMyLocationListener;//定义监听类
    SensorManager sensorManager;
    /**
     * 往Excel表格内写入数据声明
     */
    private WritableWorkbook wwb2;
//    private String excelPath2;
//    private File excelFile2;
    private String textPath;
    private File textFile;
    private File  textFile1;
    private String textPath1;
    private FileOutputStream fos1,fos;
    private OutputStreamWriter osw1,osw;
    private BufferedWriter bw1,bw;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private float[] linearAccValues = new float[3];
    private Sensor accelerometer; // 加速度传感器
    private Sensor magnetic; // 地磁场传感器
    private Sensor linearAcc;//线性加速度传感器
    private MySensorEventListener mySensorEventListener;
    //车辆异常驾驶标记按钮
    private Button mStopVehicle,mChangeLane,mWheel,mUturn,mNormal,mStopEnd;
    private static final int NORMAL = 0;//没有异常
    private static final int STOP = 1;//刹车
    private static final int CHANGE_LANE = 2;//换道
    private static final int WHEEL = 3;//转弯
    private static final int UTURN = 4;///调头
    private static final int STOPEND = 5;//停车
    private static int ABNORMAL_TAG = 0;//默认是标准模式，没有异常
    private TextView mShowStatus;//显示状态
    private boolean isLocation = false;
    private boolean isNoFirst = false;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mySensorEventListener = new MySensorEventListener();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //初始化线性加速度传感器
        linearAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

//        excelPath2 = getExcelDir() + File.separator + "baidu" + System.currentTimeMillis() + ".xls";
//        excelFile2 = new File(excelPath2);
//        createExcel2(excelFile2);

//创建text文件
//        Log.i(TAG, getExcelDir());
        textPath = getExcelDir() + File.separator + "baidu" + System.currentTimeMillis() + ".txt";
        textFile = new File(textPath);
        textPath1 = getExcelDir() + File.separator + "orientation" + System.currentTimeMillis() + ".txt";
        textFile1 = new File(textPath1);


        //声明LocationClient类
        mLocationClient = new LocationClient(getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        //注册监听
        mLocationClient.registerLocationListener(mMyLocationListener);
        //显示结果
        LocationResult = (TextView) findViewById(R.id.textView1);
        logMsg("请点击开始定位！");
        startLocation = (Button) findViewById(R.id.addfence);
        startLocation.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                InitLocation();//初始化
                if (startLocation.getText().equals("开始定位")) {
                    try {
                        fos1 = new FileOutputStream(textFile1,true);
                        osw1 = new OutputStreamWriter(fos1);
                        bw1 = new BufferedWriter(osw1);
                        fos = new FileOutputStream(textFile,true);
                        osw = new OutputStreamWriter(fos);
                        bw = new BufferedWriter(osw);
                        String s = "time,latitude,lontitude,speed,direction,timestamp\r\n";
                        String s1 = "lacc x,lacc y,lacc z,acc x,acc y,acc z,ori x,ori y,ori z,timestamp,time,tag\r\n";
                        char sValue[] = s.toCharArray();
                        char s1Value[] = s1.toCharArray();
                        if(!isNoFirst) {
                            bw.write(sValue);
                            bw1.write(s1Value);
                            isNoFirst=true;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sensorManager.registerListener(mySensorEventListener,
                            accelerometer, 200000);
                    sensorManager.registerListener(mySensorEventListener,
                            magnetic, 200000);
                    sensorManager.registerListener(mySensorEventListener,
                            linearAcc,200000);
                    mLocationClient.start();
                    isLocation = true;
//                    setColor();
                    mShowStatus.setText("目前模式：正常驾驶");
                    mNormal.setBackgroundColor(Color.parseColor("#EE5C42"));
                    startLocation.setText("停止定位");
                    logMsg("正在定位！");
                } else {
                    mLocationClient.stop();
                    setColor();
                    ABNORMAL_TAG = NORMAL;
                    isLocation = false;
                    startLocation.setText("开始定位");
                    logMsg("停止定位！");
                    // 解除注册
                    sensorManager.unregisterListener(mySensorEventListener);
                    try {
                        bw1.flush();
                        bw1.close();
                        osw1.close();
                        fos1.close();
                        bw.flush();
                        bw.close();
                        osw.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        initButton();
    }

private void initButton() {
    mStopVehicle = (Button) findViewById(R.id.stop);
    mChangeLane = (Button) findViewById(R.id.changelanes);
    mWheel = (Button) findViewById(R.id.wheel);
    mUturn = (Button) findViewById(R.id.uturn);
    mShowStatus = (TextView) findViewById(R.id.showStatus);
    mNormal = (Button) findViewById(R.id.normal);
    mStopEnd = (Button) findViewById(R.id.stopend);
    mStopVehicle.setOnClickListener(this);
    mChangeLane.setOnClickListener(this);
    mWheel.setOnClickListener(this);
    mUturn.setOnClickListener(this);
    mNormal.setOnClickListener(this);
    mStopEnd.setOnClickListener(this);
    mShowStatus.setText("目前模式：待开始");
}

    private void setColor(){

            if (ABNORMAL_TAG == NORMAL) {
                mNormal.setBackgroundColor(getResources().getColor(R.color.buttonbackgroud));
            }
            if (ABNORMAL_TAG == CHANGE_LANE) {
                mChangeLane.setBackgroundColor(getResources().getColor(R.color.buttonbackgroud));
            }
            if (ABNORMAL_TAG == WHEEL) {
                mWheel.setBackgroundColor(getResources().getColor(R.color.buttonbackgroud));
            }
            if (ABNORMAL_TAG == STOP) {
                mStopVehicle.setBackgroundColor(getResources().getColor(R.color.buttonbackgroud));
            }
            if (ABNORMAL_TAG == UTURN) {
                mUturn.setBackgroundColor(getResources().getColor(R.color.buttonbackgroud));
            }
            if (ABNORMAL_TAG == STOPEND) {
                mStopEnd.setBackgroundColor(getResources().getColor(R.color.buttonbackgroud));
            }
    }
    @Override
    public void onClick(View v) {
        if (!isLocation) {
            Toast.makeText(getApplicationContext(), "请点击开始定位！", Toast.LENGTH_SHORT).show();
        } else {
            switch (v.getId()) {
                case R.id.stop:
                    setColor();
                    ABNORMAL_TAG = STOP;
                    mStopVehicle.setBackgroundColor(Color.parseColor("#d15fee"));
                    mShowStatus.setText("目前模式：刹车");
                    break;
                case R.id.changelanes:
                    setColor();
                    ABNORMAL_TAG = CHANGE_LANE;
                    mChangeLane.setBackgroundColor(Color.parseColor("#EE9A49"));
                    mShowStatus.setText("目前模式：换道");
                    break;
                case R.id.wheel:
                    setColor();
                    ABNORMAL_TAG = WHEEL;
                    mWheel.setBackgroundColor(Color.parseColor("#B3EE3A"));
                    mShowStatus.setText("目前模式：转弯");
                    break;
                case R.id.uturn:
                    setColor();
                    ABNORMAL_TAG = UTURN;
                    mUturn.setBackgroundColor(Color.parseColor("#9F79EE"));
                    mShowStatus.setText("目前模式：调头");
                    break;
                case R.id.normal:
                    setColor();
                    ABNORMAL_TAG = NORMAL;
                    mNormal.setBackgroundColor(Color.parseColor("#EE5C42"));
                    mShowStatus.setText("目前模式：正常驾驶");
                    break;
                case R.id.stopend:
                    setColor();
                    ABNORMAL_TAG = STOPEND;
                    mStopEnd.setBackgroundColor(Color.parseColor("#7CCD7C"));
                    mShowStatus.setText("目前模式：停车");
                    break;
                default:
                    setColor();
                    ABNORMAL_TAG = NORMAL;
                    mNormal.setBackgroundColor(Color.parseColor("#EE5C42"));
                    mShowStatus.setText("目前模式：正常驾驶");
                    break;
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bw1.flush();
            bw1.close();
            osw1.close();
            fos1.close();
            bw.flush();
            bw.close();
            osw.close();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }




    @Override
    protected void onStop() {
//        mLocationClient.stop();
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /**
     * 获取所在位置经纬度及详细地址
     */
    private void InitLocation() {
        //声明定位参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy);//设置高精度定位定位模式
        option.setCoorType("gcj02");//设置百度经纬度坐标系格式
        option.setScanSpan(1000);//设置发起定位请求的间隔时间为1000ms
        option.setLocationNotify(true);
        option.setOpenGps(true);//设置可以使用GPS
        option.setIsNeedAddress(true);//反编译获得具体位置，只有网络定位才可以
        mLocationClient.setLocOption(option);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }



    /**
     * 实现定位回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        public void onReceiveLocation(BDLocation location) {
//            location.setLocType(BDLocation.TypeGpsLocation);
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());//获得当前时间
            sb.append("\nerror code : ");//61代表GPS定位
            sb.append(location.getLocType());//获得erro code得知定位现状
            String mLatitude = String.valueOf(location.getLatitude());
            if (mLatitude.equals("4.9E-324")) {
                mLatitude = "-1";//-1代表无法定位
            }
            String mLongitude = String.valueOf(location.getLongitude());
            if (mLongitude.equals("4.9E-324")) {
                mLongitude = "-1";//-1代表无法定位
            }
            sb.append("\nlatitude : ");
            sb.append(mLatitude);//获得纬度
            sb.append("\nlontitude : ");
            sb.append(mLongitude);//获得经度
            sb.append("\nradius : ");
            sb.append(location.getRadius());
//            writeToExcel2(location.getTime(),String.valueOf(location.getLatitude()),
//                    String.valueOf(location.getLongitude()),
//                    String.valueOf(location.getSpeed()),String.valueOf(location.getDirection()));
            if (location.getLocType() == BDLocation.TypeGpsLocation) {//通过GPS定位
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());//获得速度
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\ndirection : ");
                sb.append(location.getDirection());//获得方位
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());//获得当前地址‘

                writeToTxt(location.getTime(), mLatitude,
                        mLongitude,
                        String.valueOf(location.getSpeed()), String.valueOf(location.getDirection()),
                        String.valueOf(System.currentTimeMillis()));
//                writeToExcel2(location.getTime(), mLatitude,
//                        mLongitude,
//                        String.valueOf(location.getSpeed()), String.valueOf(location.getDirection()),
//                        String.valueOf(System.currentTimeMillis()));

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {//通过网络连接定位
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());//获得当前地址
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());//获得经营商？
                writeToTxt(location.getTime(), mLatitude,
                        mLongitude,
                        "-2", "-2",
                        String.valueOf(System.currentTimeMillis()));
//                writeToExcel2(location.getTime(), mLatitude,
//                        mLongitude,
//                        "-2", "-2",
//                        String.valueOf(System.currentTimeMillis()));
            }
//            logMsg(sb.toString());
            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }

    /**
     * 显示请求字符串
     *
     *
     *
     *
     * @param str
     */
    public void logMsg(String str) {
        try {
            if (LocationResult != null)
                LocationResult.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 创建excel表.
//    public void createExcel2(File file) {
//        WritableSheet ws = null;
//        try {
//            if (!file.exists()) {
//                wwb2 = Workbook.createWorkbook(file);
//
//                ws = wwb2.createSheet("sheet1", 0);
//
////                ws = wwb.createSheet("sheet2", 1);
//                // 在指定单元格插入数据
//                Label lbl1 = new Label(0, 0, "time");
//                Label bll2 = new Label(1, 0, "latitude");
//                Label bll3 = new Label(2, 0, "lontitude");
//                Label bll4 = new Label(3, 0, "speed");
//                Label bll5 = new Label(4, 0, "direction");
//                Label bll6 = new Label(5, 0, "timestamp");
//
//                ws.addCell(lbl1);
//                ws.addCell(bll2);
//                ws.addCell(bll3);
//                ws.addCell(bll4);
//                ws.addCell(bll5);
//                ws.addCell(bll6);
//
//
//                // 从内存中写入文件中
//                wwb2.write();
//                wwb2.close();
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public void writeToTxt(String time, String latitude, String lontitude, String speed, String direction, String timpstamp) {

        String s = time+","+latitude+","+lontitude+","+speed+","+direction+","+timpstamp+"\r\n";
        try {
            bw.append(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void writeToExcel2(String time, String latitude, String lontitude, String speed, String direction, String timpstamp) {
//
//        try {
//            Workbook oldWwb = Workbook.getWorkbook(excelFile2);
//            wwb2 = Workbook.createWorkbook(excelFile2,
//                    oldWwb);
//            WritableSheet ws = wwb2.getSheet(0);
//            // 当前行数
//            int row = ws.getRows();
//            Label lbl1 = new Label(0, row, time);
//            Label bll2 = new Label(1, row, latitude);
//            Label bll3 = new Label(2, row, lontitude);
//            Label bll4 = new Label(3, row, speed);
//            Label bll5 = new Label(4, row, direction);
//            Label bll6 = new Label(5, row, timpstamp);
//
//            ws.addCell(lbl1);
//            ws.addCell(bll2);
//            ws.addCell(bll3);
//            ws.addCell(bll4);
//            ws.addCell(bll5);
//            ws.addCell(bll6);
//
//            // 从内存中写入文件中,只能刷一次.
//            wwb2.write();
//            wwb2.close();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    // 获取Excel文件夹
    public String getExcelDir() {
        // SD卡指定文件夹
        String sdcardPath = Environment.getExternalStorageDirectory()
                .toString();
        File dir = new File(sdcardPath + File.separator + "Excel"
                + File.separator + "Person");

        if (dir.exists()) {
            return dir.toString();

        } else {
            dir.mkdirs();
            Log.d("BAG", "保存路径不存在,");
            return dir.toString();
        }
    }
    // 计算方向
    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues,
                magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);
        long currtime = System.currentTimeMillis();
        String time = formatData("yyyy-MM-dd hh:mm:ss",currtime);
        String s1 = linearAccValues[0]+","+linearAccValues[1]+","+linearAccValues[2]+","+accelerometerValues[0]+","+accelerometerValues[1]+","+accelerometerValues[2]+","
                +values[0] + "," + values[1] + "," + values[2] + "," + currtime+","+time+","+ABNORMAL_TAG+"\r\n";
//        ABNORMAL_TAG = NORMAL;
        //        char[] value = s.toCharArray();
//        LocationResult.setText(linearAccValues[0]+"\n"+linearAccValues[1]+"\n"+linearAccValues[2]);
        try {
            bw1.append(s1);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    boolean islineacc = false;
    boolean isacc = false;
    boolean ismagic = false;
    class MySensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
                isacc=true;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
                ismagic = true;
            }
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                linearAccValues = event.values;
                islineacc = true;
            }
            if(islineacc&&isacc&&ismagic) {
                calculateOrientation();
                islineacc=false;
                isacc=false;
                ismagic=false;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    }
    static String result="";

    public static String formatData(String dataFormat,long timeStamp){
        if(timeStamp ==0){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(dataFormat);
        result = sdf.format(new Date(timeStamp));
        return result;
    }

}
