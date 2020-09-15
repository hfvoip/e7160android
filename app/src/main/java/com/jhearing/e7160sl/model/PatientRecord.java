package com.jhearing.e7160sl.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jhearing.e7160sl.HA.HearingAidModel;

public class PatientRecord {
    private String firstname;
    private String lastname;
    private int age;
    private int sex;
    private String contactphone;
    private int illtype;
    private String str_ac_left="";
    private String str_bc_left="";
    private String str_ucl_left="";
    private String str_ac_right="";
    private String str_bc_right="";
    private String str_ucl_right="";

    public PatientRecord() {
        firstname = "";
        lastname ="";
        age = 1;
        sex=1;
        contactphone = "";
        illtype = 0;
        str_ac_left = str_bc_left =str_ucl_left = str_ac_right = str_bc_right  =str_ucl_right ="";

    }
    public void setSex(int sex) {
        this.sex = sex;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public void setContactphone(String contactphone) {
        this.contactphone = contactphone;
    }
    public void setIlltype(int illtype) {
        this.illtype = illtype;
    }
    public int getSex() {
       return  sex;
    }
    public int getAge() {
        return  age;
    }
    public String getFirstname( ) {
        return  firstname;
    }
    public String getLastname( ) {
         return  lastname;
    }
    public String getContactphone( ) {
         return contactphone;
    }
    public int getIlltype( ) {
       return illtype;
    }
    public void init_record(String firstname,String lastname,int age,int sex,String contactphone,int illtype) {
        this.firstname =firstname;
        this.lastname = lastname;
        this.age = age;
        this.sex= sex;
        this.contactphone = contactphone;
        this.illtype = illtype;
    }
    public void set_audiogram(HearingAidModel.Side side , String agtype, String str) {

        if (side == HearingAidModel.Side.Left ) {
            if (agtype =="ac")  this.str_ac_left = str;
            if (agtype =="bc")  this.str_bc_left = str;
            if (agtype =="ucl")  this.str_ucl_left = str;

        }
        if (side == HearingAidModel.Side.Right) {
            if (agtype =="ac")  this.str_ac_right = str;
            if (agtype =="bc")  this.str_bc_right = str;
            if (agtype =="ucl")  this.str_ucl_right = str;
        }
    }
    public String get_audiogram(HearingAidModel.Side side , String agtype) {
        if (side == HearingAidModel.Side.Left ) {
            if (agtype =="ac")   return this.str_ac_left  ;
            if (agtype =="bc")  return this.str_bc_left  ;
            if (agtype =="ucl")  return this.str_ucl_left  ;

        }
        if (side == HearingAidModel.Side.Right) {
            if (agtype =="ac")  return this.str_ac_right ;
            if (agtype =="bc")  return this.str_bc_right  ;
            if (agtype =="ucl")  return this.str_ucl_right  ;
        }
        return "INVALID SIDE";
    }

    private SharedPreferences ourPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    public void load_record_sp(Context context) {
        //从 share preference 中取值
        SharedPreferences preferences = ourPreferences(context);
        this.firstname = preferences.getString("firstname", "" );
        this.lastname = preferences.getString("lastname", "" );
        this.sex = preferences.getInt("sex",1 );
        this.age = preferences.getInt("age", 60);
        this.contactphone = preferences.getString("contactphone", "" );
        this.illtype = preferences.getInt("illtype", 0 );
        this.str_ac_left = preferences.getString("str_ac_left", "20|30|40|50|60|20|90|20|20" );
        this.str_bc_left = preferences.getString("str_bc_left", "20|20|20|20|20|20|20|20|20" );
        this.str_ucl_left = preferences.getString("str_ucl_left", "" );

        this.str_ac_right = preferences.getString("str_ac_right", "20|20|20|20|20|20|20|20|20" );
        this.str_bc_right = preferences.getString("str_bc_right", "20|20|20|20|20|20|20|20|20" );
        this.str_ucl_right = preferences.getString("str_ucl_right", "" );

    }
    public void save_record_sp(Context context) {
        //保存到sp

        SharedPreferences preferences = ourPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("firstname", this.firstname);
        editor.putString("lastname", this.lastname);
        editor.putInt("age", this.age);
        editor.putInt("sex", this.sex);
        editor.putString("contactphone", this.contactphone);
        editor.putInt("illtype", this.illtype);
        editor.putString("str_ac_left", this.str_ac_left);
        editor.putString("str_bc_left", this.str_bc_left);
        editor.putString("str_ucl_left", this.str_ucl_left);
        editor.putString("str_ac_right", this.str_ac_right);
        editor.putString("str_bc_right", this.str_bc_right);
        editor.putString("str_ucl_right", this.str_ucl_right);

        editor.apply();


    }
}
