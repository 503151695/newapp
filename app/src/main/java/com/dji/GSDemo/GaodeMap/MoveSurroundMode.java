package com.dji.GSDemo.GaodeMap;

/**
 * Created by 111112 on 2017/4/21.
 */

public class MoveSurroundMode {

    public double BasicLat,BasicLng;

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


    protected void setBasicPoint(double lat,double lng){
        BasicLat = lat;
        BasicLng = lng;
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
