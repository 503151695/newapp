package com.dji.GSDemo.GaodeMap;

/**
 * Created by 111112 on 2017/4/21.
 */

public class MoveSurroundMode {


    public double BasicLat,BasicLng;
    public float qsgd_ptr; //起始高度
    public float gdjg_ptr;//高度间隔
    public float jcds_ptr;//监测点数
    public float ddcjsj_ptr;//单点采集时间
    public float jcgd_ptr;//监测高度
    public float jcfxsd_ptr;//监测飞行速度
    public float hrbj_ptr;//环绕半径

    public float hv_high;
    public float hv_interval;
    public float hv_numbers;
    public float hv_time;

    public float mv_high;
    public float mv_interval;
    public float mv_speed;

    public float hs_high;
    public float hs_numbers;
    public float hs_radius;
    public float hs_time;

    public float ms_high;
    public float ms_radius;
    public float ms_speed;

    public float hh_high;
    public float hh_interval;
    public float hh_numbers;
    public float hh_time;

    public float mh_high;
    public float mh_interval;
    public float mh_speed;



    protected void setBasicPoint(double lat,double lng){
        BasicLat = lat;
        BasicLng = lng;
    }
    protected void setQsgd_ptr(float ptr){
        qsgd_ptr = ptr;
    }
    protected void setGdjg_ptr(float ptr){
        gdjg_ptr = ptr;
    }
    protected void setJcds_ptr(float ptr){
        jcds_ptr = ptr;
    }
    protected void setDdcjsj_ptr(float ptr){
        ddcjsj_ptr = ptr;
    }
    protected void setJcgd_ptr(float ptr){
        jcgd_ptr = ptr;
    }
    protected void setJcfxsd_ptr(float ptr){
        jcfxsd_ptr = ptr;
    }
    protected void setHrbj_ptr(float ptr){
        hrbj_ptr = ptr;
    }

    protected void sethv_high(float ptr){
        hv_high = ptr;
    }
    protected void sethv_interval(float ptr){
        hv_interval = ptr;
    }
    protected void sethv_numbers(float ptr){
        hv_numbers = ptr;
    }
    protected void sethv_time(float ptr){
        hv_time = ptr;
    }

    protected void setmv_high(float ptr){
        mv_high = ptr;
    }
    protected void setmv_interval(float ptr){
        mv_interval = ptr;
    }
    protected void setmv_speed(float ptr){
        mv_speed = ptr;
    }

    protected void seths_high(float ptr){
        hs_high = ptr;
    }
    protected void seths_numbers(float ptr){
        hs_numbers = ptr;
    }
    protected void seths_radius(float ptr){
        hs_radius = ptr;
    }
    protected void seths_time(float ptr){
        hs_time = ptr;
    }

    protected void setms_high(float ptr){
        ms_high = ptr;
    }
    protected void setms_radius(float ptr){
        ms_radius = ptr;
    }
    protected void setms_speed(float ptr){
        ms_speed = ptr;
    }

}
