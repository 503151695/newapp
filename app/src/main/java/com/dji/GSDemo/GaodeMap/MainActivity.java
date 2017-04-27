package com.dji.GSDemo.GaodeMap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.String;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMap.OnMapClickListener;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;

import com.amap.api.maps2d.CoordinateConverter;

import com.amap.api.maps2d.LocationSource;

import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.dji.GSDemo.GaodeMap.excel.ExcelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import db.DBHelper;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.hotpoint.HotpointHeading;
import dji.common.mission.hotpoint.HotpointMission;
import dji.common.mission.hotpoint.HotpointStartPoint;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.CommonCallbacks;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.common.error.DJIError;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.Mission;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.timeline.actions.GoHomeAction;
import dji.sdk.mission.timeline.actions.GoToAction;
import dji.sdk.mission.timeline.actions.HotpointAction;
import dji.sdk.mission.timeline.actions.RecordVideoAction;
import dji.sdk.mission.timeline.actions.TakeOffAction;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;



public class MainActivity extends FragmentActivity implements View.OnClickListener,
        OnMapClickListener,LocationSource,AMapLocationListener,SeekBar.OnSeekBarChangeListener,TextWatcher{

    protected static final String TAG = "MainActivity";
    private static final double PI = 3.1415926535897932384626;
    private static final double C_EARTH = 6378137.0;

    private MapView mapView;
    private AMap aMap;

    private Button locate, add, clear;
    //private Button config, upload, start, stop;
    private TextView mydatashow;

    private boolean isAdd = false;

    private double droneLocationLat = 181, droneLocationLng = 181;
    private double droneLocationLatW = 181, droneLocationLngW = 181;
    private LatLng WGS84_pos,GCJ02_pos;
    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();
    private Marker droneMarker = null;

    private float altitude = 100.0f;
    private float mSpeed = 10.0f;

    private List<Waypoint> waypointList = new ArrayList<>();

    //private WaypointMissionOperator waypointMissionOperator;
    //private WaypointMission mission;
    //private WaypointMissionOperatorListener listener;
    public static WaypointMission.Builder waypointMissionBuilder;
    private FlightController mFlightController;
    private WaypointMissionOperator instance;
    private WaypointMissionFinishedAction mFinishedAction = WaypointMissionFinishedAction.NO_ACTION;
    private WaypointMissionHeadingMode mHeadingMode = WaypointMissionHeadingMode.AUTO;
    private UiSettings mUiSettings;
    //定位
    private OnLocationChangedListener mLocationChangeListener;
    public AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private Marker locationMarker;
    private Spinner mission_type;

    private EditText jingdu,weidu,mission_name,mission_addr,
            hv_high,hv_interval,hv_numbers,hv_time,
            mv_high,mv_interval,mv_speed,
            hs_high,hs_numbers,hs_radius,hs_time,
            ms_high,ms_radius,ms_speed;
    private Button btn_smap,btn_weixing,to_option2,to_option3,setPoint,setplane,btn_qf;

    private String task_name,task_addr;
    private ImageView to_option1,backto_option2;
    private ScrollView option1,option2,option3;
    private SeekBar seek_hv_high,seek_hv_interval,seek_hv_numbers,seek_hv_time,
            seek_mv_high,seek_mv_interval,seek_mv_speed,
            seek_hs_high,seek_hs_numbers,seek_hs_radius,seek_hs_time,
            seek_ms_high,seek_ms_radius,seek_ms_speed;

    //--mtr
    private MissionControl missionControl;
    MoveSurroundMode movsud = new MoveSurroundMode();
    private MissionMode mMissionMode = MissionMode.FREEMODE;
    private double BasicPointLat = 22;
    private double BasicPointLng = 113;//--WGS84
    protected double homeLatitude = 181;
    protected double homeLongitude = 181;
    protected  LatLng homelatlng;
    private LinearLayout hvmode,mvmode,hsmode,msmode;

    //--data
    private File file;
    private String[] title = { "日期", "食物支出", "日用品项", "交通话费", "旅游出行", "穿着支出", "医疗保健", "人情客往", "宝宝专项", "房租水电", "其它支出", "备注说明" };
    private String[] saveData;
    private DBHelper mDbHelper;
    private ArrayList<ArrayList<String>>bill2List;

    //--
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
        initFlightController();
        initDataTransmission();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        removeListener();
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * @Description : RETURN Button RESPONSE FUNCTION
     */
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUI() {

        locate = (Button) findViewById(R.id.locate);
        add = (Button) findViewById(R.id.add);
        clear = (Button) findViewById(R.id.clear);
        //config = (Button) findViewById(R.id.config);
        //upload = (Button) findViewById(R.id.upload);
        //start = (Button) findViewById(R.id.start);
        //stop = (Button) findViewById(R.id.stop);

        mydatashow = (TextView) findViewById(R.id.datashow);
        mission_type = (Spinner) findViewById(R.id.mission_type);
        to_option2 = (Button) findViewById(R.id.to_option2);
        jingdu = (EditText) findViewById(R.id.jingdu);
        weidu = (EditText) findViewById(R.id.weidu);
        setPoint = (Button) findViewById(R.id.setPoint);
        setplane = (Button) findViewById(R.id.setplane);
        to_option3 = (Button) findViewById(R.id.to_option3);
        btn_qf = (Button) findViewById(R.id.btn_qf);
        to_option1 = (ImageView) findViewById(R.id.to_option1);
        backto_option2 = (ImageView) findViewById(R.id.backto_option2);
        option1 = (ScrollView) findViewById(R.id.option1);
        option2 = (ScrollView) findViewById(R.id.option2);
        option3 = (ScrollView) findViewById(R.id.option3);
        mission_name = (EditText) findViewById(R.id.mission_name);
        mission_addr = (EditText) findViewById(R.id.mission_addr);

        hv_high = (EditText) findViewById(R.id.hv_high);
        hv_interval = (EditText) findViewById(R.id.hv_interval);
        hv_numbers = (EditText) findViewById(R.id.hv_numbers);
        hv_time = (EditText) findViewById(R.id.hv_time);

        mv_high = (EditText) findViewById(R.id.mv_high);
        mv_interval = (EditText) findViewById(R.id.mv_interval);
        mv_speed = (EditText) findViewById(R.id.mv_speed);

        hs_high = (EditText) findViewById(R.id.hs_high);
        hs_numbers = (EditText) findViewById(R.id.hs_numbers);
        hs_radius = (EditText) findViewById(R.id.hs_radius);
        hs_time = (EditText) findViewById(R.id.hs_time);

        ms_high = (EditText) findViewById(R.id.ms_high);
        ms_radius = (EditText) findViewById(R.id.ms_radius);
        ms_speed = (EditText) findViewById(R.id.ms_speed);

        seek_hv_high = (SeekBar) findViewById(R.id.seek_hv_high);
        seek_hv_interval = (SeekBar) findViewById(R.id.seek_hv_interval);
        seek_hv_numbers = (SeekBar) findViewById(R.id.seek_hv_numbers);
        seek_hv_time = (SeekBar) findViewById(R.id.seek_hv_time);

        seek_mv_high = (SeekBar) findViewById(R.id.seek_mv_high);
        seek_mv_interval = (SeekBar) findViewById(R.id.seek_mv_interval);
        seek_mv_speed = (SeekBar) findViewById(R.id.seek_mv_speed);

        seek_hs_high = (SeekBar) findViewById(R.id.seek_hs_high);
        seek_hs_numbers = (SeekBar) findViewById(R.id.seek_hs_numbers);
        seek_hs_radius = (SeekBar) findViewById(R.id.seek_hs_radius);
        seek_hs_time = (SeekBar) findViewById(R.id.seek_hs_time);

        seek_ms_high = (SeekBar) findViewById(R.id.seek_ms_high);
        seek_ms_radius = (SeekBar) findViewById(R.id.seek_ms_radius);
        seek_ms_speed = (SeekBar) findViewById(R.id.seek_ms_speed);

        hvmode = (LinearLayout) findViewById(R.id.Lin_hvmode);
        mvmode = (LinearLayout) findViewById(R.id.Lin_mvmode);
        hsmode = (LinearLayout) findViewById(R.id.Lin_hsmode);
        msmode = (LinearLayout) findViewById(R.id.Lin_msmode);

        hv_high.addTextChangedListener(this);
        hv_interval.addTextChangedListener(this);
        hv_numbers.addTextChangedListener(this);
        hv_time.addTextChangedListener(this);

        mv_high.addTextChangedListener(this);
        mv_interval.addTextChangedListener(this);
        mv_speed.addTextChangedListener(this);

        hs_high.addTextChangedListener(this);
        hs_numbers.addTextChangedListener(this);
        hs_radius.addTextChangedListener(this);
        hs_time.addTextChangedListener(this);

        ms_high.addTextChangedListener(this);
        ms_radius.addTextChangedListener(this);
        ms_speed.addTextChangedListener(this);

        seek_hv_high.setOnSeekBarChangeListener(this);
        seek_hv_interval.setOnSeekBarChangeListener(this);
        seek_hv_numbers.setOnSeekBarChangeListener(this);
        seek_hv_time.setOnSeekBarChangeListener(this);

        seek_mv_high.setOnSeekBarChangeListener(this);
        seek_mv_interval.setOnSeekBarChangeListener(this);
        seek_mv_speed.setOnSeekBarChangeListener(this);

        seek_hs_high.setOnSeekBarChangeListener(this);
        seek_hs_numbers.setOnSeekBarChangeListener(this);
        seek_hs_radius.setOnSeekBarChangeListener(this);
        seek_hs_time.setOnSeekBarChangeListener(this);

        seek_ms_high.setOnSeekBarChangeListener(this);
        seek_ms_radius.setOnSeekBarChangeListener(this);
        seek_ms_speed.setOnSeekBarChangeListener(this);

        backto_option2.setOnClickListener(this);
        to_option1.setOnClickListener(this);
        to_option3.setOnClickListener(this);
        btn_qf.setOnClickListener(this);
        setPoint.setOnClickListener(this);
        setplane.setOnClickListener(this);
        to_option2.setOnClickListener(this);
        locate.setOnClickListener(this);
        add.setOnClickListener(this);
        clear.setOnClickListener(this);
        //config.setOnClickListener(this);
        //upload.setOnClickListener(this);
        //start.setOnClickListener(this);
        //stop.setOnClickListener(this);
        mission_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                switch(pos){
                    case 1:
                        mMissionMode = MissionMode.VERTICAL_HOVER;
                        hvmode.setVisibility(View.VISIBLE);
                        mvmode.setVisibility(View.GONE);
                        hsmode.setVisibility(View.GONE);
                        msmode.setVisibility(View.GONE);
                        break;
                    case 2:
                        mMissionMode = MissionMode.VERTICAL_MOVE;
                        hvmode.setVisibility(View.GONE);
                        mvmode.setVisibility(View.VISIBLE);
                        hsmode.setVisibility(View.GONE);
                        msmode.setVisibility(View.GONE);
                        break;
                    case 3:
                        mMissionMode = MissionMode.SURROUND_HOVER;
                        hvmode.setVisibility(View.GONE);
                        mvmode.setVisibility(View.GONE);
                        hsmode.setVisibility(View.VISIBLE);
                        msmode.setVisibility(View.GONE);
                        break;
                    case 4:
                        mMissionMode = MissionMode.SURROUND_MOVE;
                        hvmode.setVisibility(View.GONE);
                        mvmode.setVisibility(View.GONE);
                        hsmode.setVisibility(View.GONE);
                        msmode.setVisibility(View.VISIBLE);
                        break;
                    case 5:
                        mMissionMode = MissionMode.HORIZONTAL_HOVER;
                        break;
                    case 6:
                        mMissionMode = MissionMode.HORIZONTAL_MOVE;
                        break;
                    case 7:
                        mMissionMode = MissionMode.SINGLEPOINT;
                        break;
                    case 8:
                        mMissionMode = MissionMode.FREEMODE;
                        break;
                    case 9:
                        mMissionMode = MissionMode.SPECIAL_MODE;
                        break;
                    default:
                        break;
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
    }

    private void initMapView() {

        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.setOnMapClickListener(this);// add the listener for click for amap object
        }
        mUiSettings = aMap.getUiSettings();
        aMap.setLocationSource(this);// 设置定位监听
        mUiSettings.setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false


//        LatLng shenzhen = new LatLng(22.5362, 113.9454);
//        aMap.addMarker(new MarkerOptions().position(shenzhen).title("Marker in Shenzhen"));
//        aMap.moveCamera(CameraUpdateFactory.newLatLng(shenzhen));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        initMapView();
        initUI();
        addListener();
        //--data
        mDbHelper = new DBHelper(this);
        mDbHelper.open();
        bill2List=new ArrayList<ArrayList<String>>();

    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();
        //--mtr
        initDataTransmission();
        //--
    }

    private void initFlightController() {

        BaseProduct product = DJIDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {

            mFlightController.setStateCallback(
                    new FlightControllerState.Callback() {
                        @Override
                        public void onUpdate(FlightControllerState
                                                     djiFlightControllerCurrentState) {
                            droneLocationLatW = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                            droneLocationLngW = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                            //droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                            //droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                            GCJ02_pos = GCJ2WGS.getGCJ02Location(new LatLng(droneLocationLatW,droneLocationLngW));
                            droneLocationLat = GCJ02_pos.latitude;
                            droneLocationLng = GCJ02_pos.longitude;

                            updateDroneLocation();
                        }
                    });

        }
    }

    //--mtr
    private void initDataTransmission(){
        BaseProduct product = DJIDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        setResultToToast("DataTransmission init");

        if (mFlightController != null) {
            mFlightController.setOnboardSDKDeviceDataCallback(new FlightController.OnboardSDKDeviceDataCallback() {
                @Override
                public void onReceive(byte[] bytes) {
                    String str = new String(bytes);
                        String aa[] = str.split("\\|");
                    ContentValues values = new ContentValues();
                    values.put("food", aa[0].substring(7));
                    values.put("use" , aa[1]);
                    //values.put("lat" , aa[2]);
                    //values.put("lng" , aa[3]);
                    long insert = mDbHelper.insert("family_bill", values);
                    if (insert > 0) {
                        initData();
                    }
                    mydatashow.setText("实时值：" + aa[0].substring(7) + "V/m");
                    //Toast.makeText(MainActivity.this,str , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    //--

    //Add Listener for WaypointMissionOperator
    private void addListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().addListener(eventNotificationListener);
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeListener(eventNotificationListener);
        }
    }

    private WaypointMissionOperatorListener eventNotificationListener = new WaypointMissionOperatorListener() {
        @Override
        public void onDownloadUpdate(WaypointMissionDownloadEvent downloadEvent) {

        }

        @Override
        public void onUploadUpdate(WaypointMissionUploadEvent uploadEvent) {

        }

        @Override
        public void onExecutionUpdate(WaypointMissionExecutionEvent executionEvent) {

        }

        @Override
        public void onExecutionStart() {

        }

        @Override
        public void onExecutionFinish(@Nullable final DJIError error) {
            setResultToToast("Execution finished: " + (error == null ? "Success!" : error.getDescription()));
        }
    };

    public WaypointMissionOperator getWaypointMissionOperator() {
        if (instance == null) {
            instance = DJISDKManager.getInstance().getMissionControl().getWaypointMissionOperator();
        }
        return instance;
    }

    @Override
    public void onMapClick(LatLng point) {
        if (isAdd == true){
            markWaypoint(point);
            //--mtr
            weidu.setText(point.latitude+"");
            jingdu.setText(point.longitude+"");

            WGS84_pos = GCJ2WGS.getWGS84Location(new LatLng(point.latitude,point.longitude));
            BasicPointLat = WGS84_pos.latitude;
            BasicPointLng = WGS84_pos.longitude;


            //--
            /*
            Waypoint mWaypoint = new Waypoint(point.latitude, point.longitude, altitude);
            //Add Waypoints to Waypoint arraylist;
            if (waypointMissionBuilder != null) {
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            }else
            {
                waypointMissionBuilder = new WaypointMission.Builder();
                waypointList.add(mWaypoint);
                waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());
            }
            */
        }else{
            setResultToToast("Cannot Add Waypoint");
        }
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation(){

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(droneLocationLat, droneLocationLng)) {
                    droneMarker = aMap.addMarker(markerOptions);
                }
            }
        });
    }

    private void markWaypoint(LatLng point){
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = aMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(), marker);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate:{
                updateDroneLocation();
                cameraUpdate(); // Locate the drone's place
                break;
            }
            case R.id.add:{
                enableDisableAdd();
                break;
            }
            case R.id.clear: {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        aMap.clear();
                    }

                });
                waypointList.clear();
                waypointMissionBuilder.waypointList(waypointList);
                updateDroneLocation();
                break;
            }
            /*
            case R.id.config:{
                configTimeline();
                //configMission();
                //showSettingDialog();
                break;
            }
            case R.id.upload:{
                getHomePoint();
                //uploadWayPointMission();
                break;
            }
            case R.id.start:{
                startTimeline();
                //startWaypointMission();
                break;
            }
            case R.id.stop:{
                stopWaypointMission();
                break;
            }*/
            case R.id.to_option2:
                task_name = mission_name.getText().toString();
                task_addr = mission_addr.getText().toString();
                if(task_name.isEmpty() || task_addr.isEmpty()){
                    Toast.makeText(this,"任务名称或地址不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                option1.setVisibility(View.GONE);
                option2.setVisibility(View.VISIBLE);
                option3.setVisibility(View.GONE);
//                setMapClickListener();
                break;
            case R.id.setPoint:
                String w = weidu.getText().toString();
                String j = jingdu.getText().toString();

                if(j.isEmpty() || w.isEmpty()){
                    Toast.makeText(this,"经纬度不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                LatLng latLng = new LatLng(Double.parseDouble(w),Double.parseDouble(j));

                //WGS84_pos = GCJ2WGS.getWGS84Location(latLng);
                WGS84_pos = GCJ2WGS.GCJ022GPS84(latLng);

                aMap.addMarker(new MarkerOptions().position(latLng).title(task_name));
                movsud.setBasicPoint(WGS84_pos.latitude,WGS84_pos.longitude);
                //markWaypoint(latLng);

                break;
            case R.id.setplane:
                //LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
                weidu.setText(droneLocationLat+"");
                jingdu.setText(droneLocationLng+"");
                break;
            case R.id.to_option3:
                setMission();
                option1.setVisibility(View.GONE);
                option2.setVisibility(View.GONE);
                option3.setVisibility(View.VISIBLE);
//                aMap.setOnMapClickListener(null);
                break;
            case R.id.to_option1:
                option1.setVisibility(View.VISIBLE);
                option2.setVisibility(View.GONE);
                option3.setVisibility(View.GONE);
//                aMap.setOnMapClickListener(null);
                break;
            case R.id.backto_option2:
                option1.setVisibility(View.GONE);
                option2.setVisibility(View.VISIBLE);
                option3.setVisibility(View.GONE);
//                setMapClickListener();
                break;
            case R.id.btn_qf:
                startMission();
                break;
            default:
                break;
        }
    }

    private void cameraUpdate(){
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        aMap.moveCamera(cu);

    }

    private void enableDisableAdd(){
        if (isAdd == false) {
            isAdd = true;
            add.setText("退出选择模式");
        }else{
            isAdd = false;
            add.setText("从地图选择基点");
        }
    }


    String nulltoIntegerDefalt(String value){
        if(!isIntValue(value)) value="0";
        return value;
    }

    boolean isIntValue(String val)
    {
        try {
            val=val.replace(" ","");
            Integer.parseInt(val);
        } catch (Exception e) {return false;}
        return true;
    }

    private void configWayPointMission(){

        if (waypointMissionBuilder == null){

            waypointMissionBuilder = new WaypointMission.Builder().finishedAction(mFinishedAction)
                                                                  .headingMode(mHeadingMode)
                                                                  .autoFlightSpeed(mSpeed)
                                                                  .maxFlightSpeed(mSpeed)
                                                                  .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }else
        {
            waypointMissionBuilder.finishedAction(mFinishedAction)
                    .headingMode(mHeadingMode)
                    .autoFlightSpeed(mSpeed)
                    .maxFlightSpeed(mSpeed)
                    .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        }

        if (waypointMissionBuilder.getWaypointList().size() > 0){

            for (int i=0; i< waypointMissionBuilder.getWaypointList().size(); i++){
                waypointMissionBuilder.getWaypointList().get(i).altitude = altitude;
            }

            setResultToToast("Set Waypoint attitude successfully");
        }

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("loadWaypoint succeeded");
        } else {
            setResultToToast("loadWaypoint failed " + error.getDescription());
        }

    }

    private void uploadWayPointMission(){

        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("Mission upload successfully!");
                } else {
                    setResultToToast("Mission upload failed, error: " + error.getDescription() + " retrying...");
                    getWaypointMissionOperator().retryUploadMission(null);
                }
            }
        });

    }

    private void startWaypointMission(){

        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Start: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });

    }

    private void stopWaypointMission(){

        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Stop: " + (error == null ? "Successfully" : error.getDescription()));
            }
        });

    }

    private void configMoveVerticalMode(){

        waypointMissionBuilder = new WaypointMission.Builder();
        waypointList.clear();

        final Waypoint startWaypoint = new Waypoint(movsud.BasicLat,movsud.BasicLng,
                movsud.mv_high);
        startWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, 1000));
        waypointList.add(startWaypoint);
        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());

        final Waypoint endWaypoint = new Waypoint(movsud.BasicLat,movsud.BasicLng,
                movsud.mv_high + movsud.mv_interval);
        endWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, 1000));
        waypointList.add(endWaypoint);
        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());

        //LatLng pos = new LatLng(droneLocationLatW, droneLocationLngW);
        final Waypoint homeWaypoint = new Waypoint(droneLocationLatW,droneLocationLngW,
                movsud.mv_high + movsud.mv_interval);
        endWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, 1000));
        waypointList.add(homeWaypoint);

        waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());




        waypointMissionBuilder.finishedAction(WaypointMissionFinishedAction.GO_HOME)
                .headingMode(WaypointMissionHeadingMode.AUTO)
                .autoFlightSpeed(movsud.mv_speed)
                .maxFlightSpeed(10)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("loadWaypoint succeeded");
        } else {
            setResultToToast("loadWaypoint failed " + error.getDescription());
        }
    }

    private void configHoverVerticalMode(){

        waypointMissionBuilder = new WaypointMission.Builder();
        waypointList.clear();

        for(int i=0; i < movsud.hv_numbers; i++){

            final Waypoint eachWaypoint = new Waypoint(movsud.BasicLat,movsud.BasicLng,
                    movsud.hv_high + movsud.hv_interval * i);
            eachWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, (int)movsud.hv_time * 1000));
            waypointList.add(eachWaypoint);
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());

            //setResultToToast(String.valueOf(BasicPointLat)+String.valueOf(BasicPointLng));
            //setResultToToast(String.valueOf(waypointList.size()));
        }
        //waypointMissionBuilder.waypointList(waypointList).waypointCount( TestPoints);


        waypointMissionBuilder.finishedAction(WaypointMissionFinishedAction.GO_HOME)
                .headingMode(WaypointMissionHeadingMode.AUTO)
                .autoFlightSpeed(5f)
                .maxFlightSpeed(10f)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("loadWaypoint succeeded");
        } else {
            setResultToToast("loadWaypoint failed " + error.getDescription());
        }

    }

    private void configHoverSurroundMode(){

        double lati,longi,optAngle;
        LatLng pos1;
        waypointMissionBuilder = new WaypointMission.Builder();
        waypointList.clear();
        LatLng pos = new LatLng(droneLocationLatW, droneLocationLngW);

        optAngle = Math.atan2((pos.longitude - movsud.BasicLng),(pos.latitude - movsud.BasicLat));


        for(int i=0; i < movsud.hs_numbers; i++){

            lati = movsud.BasicLat + Math.toDegrees(movsud.hs_radius * Math.cos(i * 2 *PI / movsud.hs_numbers + optAngle)/C_EARTH);
            longi = movsud.BasicLng + Math.toDegrees(movsud.hs_radius * Math.sin(i * 2 *PI / movsud.hs_numbers + optAngle)/C_EARTH);

            final Waypoint eachWaypoint = new Waypoint(lati,longi,
                    movsud.hs_high);
            eachWaypoint.addAction(new WaypointAction(WaypointActionType.STAY, (int)movsud.hs_time * 1000));
            waypointList.add(eachWaypoint);
            waypointMissionBuilder.waypointList(waypointList).waypointCount(waypointList.size());

            pos1 =  GCJ2WGS.getGCJ02Location(new LatLng(lati,longi));
            aMap.addMarker(new MarkerOptions().position(pos1).title("test point"+i));
            //setResultToToast(String.valueOf(lati)+"and"+String.valueOf(longi));
            //setResultToToast(String.valueOf(waypointList.size()));
        }
        //waypointMissionBuilder.waypointList(waypointList).waypointCount( TestPoints);


        waypointMissionBuilder.finishedAction(WaypointMissionFinishedAction.GO_HOME)
                .headingMode(WaypointMissionHeadingMode.AUTO)
                .autoFlightSpeed(5f)
                .maxFlightSpeed(10f)
                .flightPathMode(WaypointMissionFlightPathMode.NORMAL);

        DJIError error = getWaypointMissionOperator().loadMission(waypointMissionBuilder.build());
        if (error == null) {
            setResultToToast("loadWaypoint succeeded");
        } else {
            setResultToToast("loadWaypoint failed " + error.getDescription());
        }
    }


    private void configTimeline() {
        //if (!GeneralUtils.checkGpsCoordinate(homeLatitude, homeLongitude)) {
        //    ToastUtils.setResultToToast("No home point!!!");
        //    return;
        //}

        List<TimelineElement> elements = new ArrayList<>();

        missionControl = MissionControl.getInstance();
        final TimelineEvent preEvent = null;
        MissionControl.Listener listener = new MissionControl.Listener() {
            @Override
            public void onEvent(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {
                updateTimelineStatus(element, event, error);
            }
        };

        //Step 1: takeoff from the ground
        setResultToToast("Step 1: takeoff from the ground");
        elements.add(new TakeOffAction());


        //Step 2: start a hotpoint mission
        setResultToToast("Step 2: start a hotpoint mission to surround 360 degree");
        HotpointMission hotpointMission = new HotpointMission();
        hotpointMission.setHotpoint(new LocationCoordinate2D(movsud.BasicLat,movsud.BasicLng));
        hotpointMission.setAltitude(movsud.ms_high);
        hotpointMission.setRadius(movsud.ms_radius);
        hotpointMission.setAngularVelocity(movsud.ms_speed);
        HotpointStartPoint startPoint = HotpointStartPoint.NEAREST;
        hotpointMission.setStartPoint(startPoint);
        HotpointHeading heading = HotpointHeading.TOWARDS_HOT_POINT;
        hotpointMission.setHeading(heading);
        elements.add(new HotpointAction(hotpointMission, 362));

        //Step 3: Go 10 meters from home point
    //    setResultToToast("Step 3: Go 10 meters from home point");
    //    elements.add(new GoToAction(new LocationCoordinate2D(homeLatitude, homeLongitude), 20));
        //Step 4: go back home
        setResultToToast("Step 3: go back home");
        elements.add(new GoHomeAction());

        if (missionControl.scheduledCount() > 0) {
            missionControl.unscheduleEverything();
            missionControl.removeAllListeners();
        }

        missionControl.scheduleElements(elements);
        missionControl.addListener(listener);
    }
    private void startTimeline() {
        if (MissionControl.getInstance().scheduledCount() > 0) {
            MissionControl.getInstance().startTimeline();
        } else {
            setResultToToast("Init the timeline first by clicking the Init button");
        }
    }
    private void updateTimelineStatus(@Nullable TimelineElement element, TimelineEvent event, DJIError error) {

        if (element != null) {
            if (element instanceof Mission) {
                setResultToToast((((Mission) element).getMissionObject().getClass().getSimpleName()
                        + " event is "
                        + event.toString()
                        + " "
                        + (error == null ? "" : error.getDescription())));
            } else {
                setResultToToast((element.getClass().getSimpleName()
                        + " event is "
                        + event.toString()
                        + " "
                        + (error == null ? "" : error.getDescription())));
            }
        } else {
            setResultToToast(("Timeline Event is " + event.toString() + " " + (error == null
                    ? ""
                    : "Failed:"
                    + error.getDescription())));
        }

    }

    private void getHomePoint(){

        final CountDownLatch cdl = new CountDownLatch(1);

        mFlightController.getHomeLocation(new CommonCallbacks.CompletionCallbackWith<LocationCoordinate2D>() {
            @Override
            public void onSuccess(LocationCoordinate2D locationCoordinate2D) {
                homeLatitude = locationCoordinate2D.getLatitude();
                homeLongitude = locationCoordinate2D.getLongitude();
                setResultToToast("home point latitude: "
                        + homeLatitude
                        + "\nhome point longitude: "
                        + homeLongitude);


                homelatlng = GCJ2WGS.getWGS84Location(new LatLng(homeLatitude,homeLongitude));
                setResultToToast("home point latitude: "
                        + homelatlng.latitude
                        + "\nhome point longitude: "
                        + homelatlng.longitude);

                LatLng latlng1 = GCJ2WGS.getGCJ02Location(homelatlng);
                setResultToToast("home point latitude: "
                        + latlng1.latitude
                        + "\nhome point longitude: "
                        + latlng1.longitude);
            }

            @Override
            public void onFailure(DJIError djiError) {
                cdl.countDown();
            }
        });

    }


    private void setMission(){

        switch (mMissionMode){
            case VERTICAL_HOVER:
                configHoverVerticalMode();
                uploadWayPointMission();
                break;
            case VERTICAL_MOVE:
                configMoveVerticalMode();
                uploadWayPointMission();
                break;
            case SURROUND_HOVER:
                configHoverSurroundMode();
                uploadWayPointMission();
                break;
            case SURROUND_MOVE:
                configTimeline();
                break;
            case HORIZONTAL_HOVER:
                break;
            case HORIZONTAL_MOVE:
                break;
            case SINGLEPOINT:
                break;
            case FREEMODE:
                break;
            case SPECIAL_MODE:
                break;
            default:
                break;
        }
    }

    private  void startMission(){
        switch (mMissionMode){
            case VERTICAL_HOVER:
                startWaypointMission();
                break;
            case VERTICAL_MOVE:
                startWaypointMission();
                break;
            case SURROUND_HOVER:
                startWaypointMission();
                break;
            case SURROUND_MOVE:
                startTimeline();
                break;
            case HORIZONTAL_HOVER:
                break;
            case HORIZONTAL_MOVE:
                break;
            case SINGLEPOINT:
                break;
            case FREEMODE:
                break;
            case SPECIAL_MODE:
                break;
            default:
                break;
        }
    }



    /**
     * 激活定位
     * @param onLocationChangedListener
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationChangeListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mLocationClient.setLocationListener(this);
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
            mLocationOption.setHttpTimeOut(20000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
            mLocationOption.setInterval(5000);//可选，设置定位间隔。默认为2秒
            mLocationOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
            mLocationOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
            mLocationOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
            mLocationOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
            // 设置定位参数
            mLocationClient.setLocationOption(mLocationOption);

            mLocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mLocationChangeListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mLocationChangeListener != null && aMapLocation != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                //定位信息：
//                location.setText("经度：" + aMapLocation.getLatitude() +
//                        "纬度：" + aMapLocation.getLongitude() +
//                        "地址：" + aMapLocation.getCountry() + "," + aMapLocation.getProvince()
//                        + "," + aMapLocation.getCity() + "," + aMapLocation.getAddress());
                mission_addr.setText(aMapLocation.getProvince()+aMapLocation.getCity()+aMapLocation.getAddress());
                LatLng latLng = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());

                //添加Marker显示定位位置
                if (locationMarker == null) {
                    locationMarker = aMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker)));
                } else {
                    locationMarker.setPosition(latLng);
                }
                //然后可以移动到定位点,使用animateCamera就有动画效果
//                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
//                mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
//                mLocationChangeListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                Toast.makeText(MainActivity.this,"定位失败，" + aMapLocation.getErrorInfo(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.seek_hv_high:
                hv_high.setText(progress+"");
                movsud.sethv_high(progress);
                break;
            case R.id.seek_hv_interval:
                hv_interval.setText(progress+"");
                movsud.sethv_interval(progress);
                break;
            case R.id.seek_hv_numbers:
                hv_numbers.setText(progress+"");
                movsud.sethv_numbers(progress);
                break;
            case R.id.seek_hv_time:
                hv_time.setText(progress+"");
                movsud.sethv_time(progress);
                break;

            case R.id.seek_mv_high:
                mv_high.setText(progress+"");
                movsud.setmv_high(progress);
                break;
            case R.id.seek_mv_interval:
                mv_interval.setText(progress+"");
                movsud.setmv_interval(progress);
                break;
            case R.id.seek_mv_speed:
                mv_speed.setText(progress+"");
                movsud.setmv_speed(progress);
                break;

            case R.id.seek_hs_high:
                hs_high.setText(progress+"");
                movsud.seths_high(progress);
                break;
            case R.id.seek_hs_numbers:
                hs_numbers.setText(progress+"");
                movsud.seths_numbers(progress);
                break;
            case R.id.seek_hs_radius:
                hs_radius.setText(progress+"");
                movsud.seths_radius(progress);
                break;
            case R.id.seek_hs_time:
                hs_time.setText(progress+"");
                movsud.seths_time(progress);
                break;

            case R.id.seek_ms_high:
                ms_high.setText(progress+"");
                movsud.setms_high(progress);
                break;
            case R.id.seek_ms_radius:
                ms_radius.setText(progress+"");
                movsud.setms_radius(progress);
                break;
            case R.id.seek_ms_speed:
                ms_speed.setText(progress+"");
                movsud.setms_speed(progress);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    //--data
    public void initData() {
        file = new File(getSDPath() + "/Family");
        makeDir(file);
        ExcelUtils.initExcel(file.toString() + "/bill.xls", title);
        ExcelUtils.writeObjListToExcel(getBillData(), getSDPath() + "/Family/bill.xls", this);
        setResultToToast(""+ file);/////
    }
    private ArrayList<ArrayList<String>> getBillData() {
        Cursor mCrusor = mDbHelper.exeSql("select * from family_bill");
        while (mCrusor.moveToNext()) {
            ArrayList<String> beanList=new ArrayList<String>();
            beanList.add(mCrusor.getString(1));
            beanList.add(mCrusor.getString(2));
            beanList.add(mCrusor.getString(3));
            beanList.add(mCrusor.getString(4));
            beanList.add(mCrusor.getString(5));
            beanList.add(mCrusor.getString(6));
            beanList.add(mCrusor.getString(7));
            beanList.add(mCrusor.getString(8));
            beanList.add(mCrusor.getString(9));
            beanList.add(mCrusor.getString(10));
            beanList.add(mCrusor.getString(11));
            beanList.add(mCrusor.getString(12));
            bill2List.add(beanList);
        }
        mCrusor.close();
        return bill2List;
    }

    public static void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        String dir = sdDir.toString();
        return dir;

    }


}
