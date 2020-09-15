package com.jhearing.e7160sl.Tools;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import com.github.mikephil.charting.data.Entry;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.widget.SingleTapLineChart;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;


public class AgActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = AgActivity.class.getSimpleName();
    private SingleTapLineChart leftEarStlc ;
    private SingleTapLineChart rightEarStlc ;


   // TitleBar listeningStatusTitle;
  //  @BindView(R.id.left_Hz_250_tv)
    private TextView leftHz250Tv;
   // @BindView(R.id.left_Hz_250_ll)
   private LinearLayout leftHz250Ll;
   // @BindView(R.id.left_Hz_500_tv)
   private TextView leftHz500Tv;
   // @BindView(R.id.left_Hz_500_ll)
   private  LinearLayout leftHz500Ll;
   // @BindView(R.id.left_Hz_750_tv)
   private TextView leftHz750Tv;
   // @BindView(R.id.left_Hz_750_ll)
   private LinearLayout leftHz750Ll;
    //@BindView(R.id.left_Hz_1000_tv)
    private TextView leftHz1000Tv;
    //@BindView(R.id.left_Hz_1000_ll)
    private LinearLayout leftHz1000Ll;
    //@BindView(R.id.left_Hz_1500_tv)
    private TextView leftHz1500Tv;
   // @BindView(R.id.left_Hz_1500_ll)
   private LinearLayout leftHz1500Ll;
    //@BindView(R.id.left_Hz_2000_tv)
    private TextView leftHz2000Tv;
    //@BindView(R.id.left_Hz_2000_ll)
    private LinearLayout leftHz2000Ll;
    //@BindView(R.id.left_Hz_4000_tv)
    private TextView leftHz4000Tv;
    //@BindView(R.id.left_Hz_4000_ll)
    private LinearLayout leftHz4000Ll;
    //@BindView(R.id.left_Hz_6000_tv)
    private TextView leftHz6000Tv;
    //@BindView(R.id.left_Hz_6000_ll)
    private LinearLayout leftHz6000Ll;
    //@BindView(R.id.left_Hz_8000_tv)
    private TextView leftHz8000Tv;
    //@BindView(R.id.left_Hz_8000_ll)
    private LinearLayout leftHz8000Ll;

    //@BindView(R.id.right_Hz_250_tv)
    private TextView rightHz250Tv;
   // @BindView(R.id.right_Hz_250_ll)
   private LinearLayout rightHz250Ll;
   // @BindView(R.id.right_Hz_500_tv)
   private TextView rightHz500Tv;
    //@BindView(R.id.right_Hz_500_ll)
    LinearLayout rightHz500Ll;
    //@BindView(R.id.right_Hz_750_tv)
    private  TextView rightHz750Tv;
   // @BindView(R.id.right_Hz_750_ll)
   private  LinearLayout rightHz750Ll;
   // @BindView(R.id.right_Hz_1000_tv)
   private TextView rightHz1000Tv;
   // @BindView(R.id.right_Hz_1000_ll)
   private  LinearLayout rightHz1000Ll;
   // @BindView(R.id.right_Hz_1500_tv)
   private TextView rightHz1500Tv;
  //  @BindView(R.id.right_Hz_1500_ll)
  private LinearLayout rightHz1500Ll;
   // @BindView(R.id.right_Hz_2000_tv)
   private  TextView rightHz2000Tv;
   // @BindView(R.id.right_Hz_2000_ll)
   private LinearLayout rightHz2000Ll;
  //  @BindView(R.id.right_Hz_4000_tv)
  private  TextView rightHz4000Tv;
  //  @BindView(R.id.right_Hz_4000_ll)
  private LinearLayout rightHz4000Ll;
  //  @BindView(R.id.right_Hz_6000_tv)
  private  TextView rightHz6000Tv;
  //  @BindView(R.id.right_Hz_6000_ll)
  private  LinearLayout rightHz6000Ll;
  //  @BindView(R.id.right_Hz_8000_tv)
  private   TextView rightHz8000Tv;
   // @BindView(R.id.right_Hz_8000_ll)
   private   LinearLayout rightHz8000Ll;
    private ArrayList<String> list = new ArrayList<>();

    private ArrayList<Entry> values;
    private ArrayList<Entry> values2;

    private String flag = "";

    private OptionsPickerView pvOptions;
    private OptionsPickerView pvOptions2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ag);

        leftHz250Tv = findViewById(R.id.left_Hz_250_tv);
        leftHz250Ll = findViewById(R.id.left_Hz_250_ll);
        leftHz500Tv =  findViewById(R.id.left_Hz_500_tv);
        leftHz500Ll =  findViewById(R.id.left_Hz_500_ll);

        leftHz750Tv =  findViewById(R.id.left_Hz_750_tv);
        leftHz750Ll =  findViewById(R.id.left_Hz_750_ll);

        leftHz1000Tv =  findViewById(R.id.left_Hz_1000_tv);
        leftHz1000Ll =  findViewById(R.id.left_Hz_1000_ll);

        leftHz1500Tv =  findViewById(R.id.left_Hz_1500_tv);
        leftHz1500Ll =  findViewById(R.id.left_Hz_1500_ll);

        leftHz2000Tv =  findViewById(R.id.left_Hz_2000_tv);
        leftHz2000Ll =  findViewById(R.id.left_Hz_2000_ll);


        leftHz4000Tv =  findViewById(R.id.left_Hz_4000_tv);
        leftHz4000Ll =  findViewById(R.id.left_Hz_4000_ll);

        leftHz6000Tv =  findViewById(R.id.left_Hz_6000_tv);
        leftHz6000Ll =  findViewById(R.id.left_Hz_6000_ll);

        leftHz8000Tv =  findViewById(R.id.left_Hz_8000_tv);
        leftHz8000Ll =  findViewById(R.id.left_Hz_8000_ll);

        rightHz250Tv = findViewById(R.id.right_Hz_250_tv);
        rightHz250Ll = findViewById(R.id.right_Hz_250_ll);
        rightHz500Tv =  findViewById(R.id.right_Hz_500_tv);
        rightHz500Ll =  findViewById(R.id.right_Hz_500_ll);

        rightHz750Tv =  findViewById(R.id.right_Hz_750_tv);
        rightHz750Ll =  findViewById(R.id.right_Hz_750_ll);

        rightHz1000Tv =  findViewById(R.id.right_Hz_1000_tv);
        rightHz1000Ll =  findViewById(R.id.right_Hz_1000_ll);

        rightHz1500Tv =  findViewById(R.id.right_Hz_1500_tv);
        rightHz1500Ll =  findViewById(R.id.right_Hz_1500_ll);

        rightHz2000Tv =  findViewById(R.id.right_Hz_2000_tv);
        rightHz2000Ll =  findViewById(R.id.right_Hz_2000_ll);


        rightHz4000Tv =  findViewById(R.id.right_Hz_4000_tv);
        rightHz4000Ll =  findViewById(R.id.right_Hz_4000_ll);

        rightHz6000Tv =  findViewById(R.id.right_Hz_6000_tv);
        rightHz6000Ll =  findViewById(R.id.right_Hz_6000_ll);

        rightHz8000Tv =  findViewById(R.id.right_Hz_8000_tv);
        rightHz8000Ll =  findViewById(R.id.right_Hz_8000_ll);

        leftEarStlc = findViewById(R.id.left_ear_stlc);
        rightEarStlc = findViewById(R.id.right_ear_stlc);

        leftHz250Ll.setOnClickListener(this);
        leftHz500Ll.setOnClickListener(this);
        leftHz750Ll.setOnClickListener(this);
        leftHz1000Ll.setOnClickListener(this);
        leftHz1500Ll.setOnClickListener(this);
        leftHz2000Ll.setOnClickListener(this);
        leftHz4000Ll.setOnClickListener(this);
        leftHz6000Ll.setOnClickListener(this);
        leftHz8000Ll.setOnClickListener(this);

        rightHz250Ll.setOnClickListener(this);
        rightHz500Ll.setOnClickListener(this);
        rightHz750Ll.setOnClickListener(this);
        rightHz1000Ll.setOnClickListener(this);
        rightHz1500Ll.setOnClickListener(this);
        rightHz2000Ll.setOnClickListener(this);
        rightHz4000Ll.setOnClickListener(this);
        rightHz6000Ll.setOnClickListener(this);
        rightHz8000Ll.setOnClickListener(this);


        leftEarStlc = findViewById(R.id.left_ear_stlc);
        rightEarStlc = findViewById(R.id.right_ear_stlc);

        initDb();


        leftEarStlc.setOnSingleTapListener(new SingleTapLineChart.OnSingleTapListener() {
            @Override
            public void onSingleTap(int x, float y) {
                Log.i(TAG, "x = " + x + " y = " + Math.floor(y));
                int left = (int) (Math.floor(y)*5-10);
                if (left <= -10){
                    left = -10;
                }else if (left >= 120){
                    left = 120;
                }else {
                    left = (int) (Math.floor(y)*5-10);;
                }


                switch (x) {
                    case 1:
                        leftHz250Tv.setText(String.valueOf(left) + "db");
                        break;
                    case 2:
                        leftHz500Tv.setText(String.valueOf(left) + "db");
                        break;
                    case 3:
                        leftHz750Tv.setText(String.valueOf(left) + "db");
                        break;
                    case 4:
                        leftHz1000Tv.setText(String.valueOf(left) + "db");
                        break;
                    case 5:
                        leftHz1500Tv.setText(String.valueOf(left) + "db");
                        break;
                    case 6:
                        leftHz2000Tv.setText(String.valueOf(left) + "db");
                        break;
                    case 7:
                        leftHz4000Tv.setText(String.valueOf(left) + "db");
                        break;
                    case 8:
                        leftHz6000Tv.setText(String.valueOf(left) + "db");
                        break;
                    case 9:
                        leftHz8000Tv.setText(String.valueOf(left) + "db");
                        break;
                }
            }
        });

        rightEarStlc.setOnSingleTapListener(new SingleTapLineChart.OnSingleTapListener() {
            @Override
            public void onSingleTap(int x, float y) {
                Log.i(TAG, "rx = " + x + " ry = " + Math.floor(y));
                int right = (int) (Math.floor(y)*5-10);
                if (right <= -10){
                    right = -10;
                }else if (right >= 120){
                    right = 120;
                }else {
                    right = (int) (Math.floor(y)*5-10);
                }
                switch (x) {
                    case 1:
                        rightHz250Tv.setText(String.valueOf(right) + "db");
                        break;
                    case 2:
                        rightHz500Tv.setText(String.valueOf(right) + "db");
                        break;
                    case 3:
                        rightHz750Tv.setText(String.valueOf(right) + "db");
                        break;
                    case 4:
                        rightHz1000Tv.setText(String.valueOf(right) + "db");
                        break;
                    case 5:
                        rightHz1500Tv.setText(String.valueOf(right) + "db");
                        break;
                    case 6:
                        rightHz2000Tv.setText(String.valueOf(right) + "db");
                        break;
                    case 7:
                        rightHz4000Tv.setText(String.valueOf(right) + "db");
                        break;
                    case 8:
                        rightHz6000Tv.setText(String.valueOf(right) + "db");
                        break;
                    case 9:
                        rightHz8000Tv.setText(String.valueOf(right) + "db");
                        break;


                }
            }
        });
        values = new ArrayList<>();
        for (int i =0;i<10;i++) {
            Entry new_entry =new Entry(i,6);
            values.add(new_entry);
        }


        leftEarStlc.setChartData(values);
     //   leftEarStlc.changeTouchEntry();

        values2 = new ArrayList<>();
        for (int i =0;i<10;i++) {
            Entry new_entry =new Entry(i,6);
            values2.add(new_entry);
        }

        rightEarStlc.setChartData(values2);

        String def_dbtext ="20db";

        leftHz250Tv.setText(def_dbtext);
        leftHz500Tv.setText(def_dbtext);
        leftHz750Tv.setText(def_dbtext);
        leftHz1000Tv.setText(def_dbtext);
        leftHz1500Tv.setText(def_dbtext);
        leftHz2000Tv.setText(def_dbtext);
        leftHz4000Tv.setText(def_dbtext);
        leftHz6000Tv.setText(def_dbtext);
        leftHz8000Tv.setText(def_dbtext);

        rightHz250Tv.setText(def_dbtext);
        rightHz500Tv.setText(def_dbtext);
        rightHz750Tv.setText(def_dbtext);
        rightHz1000Tv.setText(def_dbtext);
        rightHz1500Tv.setText(def_dbtext);
        rightHz2000Tv.setText(def_dbtext);
        rightHz4000Tv.setText(def_dbtext);
        rightHz6000Tv.setText(def_dbtext);
        rightHz8000Tv.setText(def_dbtext);



    }

    @Override
    public void onClick(View view) {

    //}
   // public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.left_Hz_250_ll:
                flag = "left_Hz_250";
                String trim = leftHz250Tv.getText().toString().trim();
                if (!trim.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;
            case R.id.left_Hz_500_ll:
                flag = "left_Hz_500";
                String trim1 = leftHz500Tv.getText().toString().trim();
                if (!trim1.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim1));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;
            case R.id.left_Hz_750_ll:
                flag = "left_Hz_750";
                String trim2 = leftHz750Tv.getText().toString().trim();
                if (!trim2.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim2));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;
            case R.id.left_Hz_1000_ll:
                flag = "left_Hz_1000";
                String trim3 = leftHz1000Tv.getText().toString().trim();
                if (!trim3.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim3));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;
            case R.id.left_Hz_1500_ll:
                flag = "left_Hz_1500";
                String trim4 = leftHz1500Tv.getText().toString().trim();
                if (!trim4.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim4));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;
            case R.id.left_Hz_2000_ll:
                flag = "left_Hz_2000";
                String trim5 = leftHz2000Tv.getText().toString().trim();
                if (!trim5.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim5));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;
            case R.id.left_Hz_4000_ll:
                flag = "left_Hz_4000";
                String trim6 = leftHz4000Tv.getText().toString().trim();
                if (!trim6.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim6));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;
            case R.id.left_Hz_6000_ll:
                flag = "left_Hz_6000";
                String trim7 = leftHz6000Tv.getText().toString().trim();
                if (!trim7.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim7));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;
            case R.id.left_Hz_8000_ll:
                flag = "left_Hz_8000";
                String trim8 = leftHz8000Tv.getText().toString().trim();
                if (!trim8.equals("")) {
                    pvOptions.setSelectOptions(getPosition(trim8));
                } else {
                    pvOptions.setSelectOptions(0);
                }
                pvOptions.show();
                break;


            case R.id.right_Hz_250_ll:
                flag = "right_Hz_250";
                String trim9 = rightHz250Tv.getText().toString().trim();
                if (!trim9.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim9));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
            case R.id.right_Hz_500_ll:
                flag = "right_Hz_500";
                String trim10 = rightHz500Tv.getText().toString().trim();
                if (!trim10.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim10));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
            case R.id.right_Hz_750_ll:
                flag = "right_Hz_750";
                String trim11 = rightHz750Tv.getText().toString().trim();
                if (!trim11.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim11));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
            case R.id.right_Hz_1000_ll:
                flag = "right_Hz_1000";
                String trim12 = rightHz1000Tv.getText().toString().trim();
                if (!trim12.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim12));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
            case R.id.right_Hz_1500_ll:
                flag = "right_Hz_1500";
                String trim13 = rightHz1500Tv.getText().toString().trim();
                if (!trim13.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim13));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
            case R.id.right_Hz_2000_ll:
                flag = "right_Hz_2000";
                String trim14 = rightHz2000Tv.getText().toString().trim();
                if (!trim14.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim14));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
            case R.id.right_Hz_4000_ll:
                flag = "right_Hz_4000";
                String trim15 = rightHz4000Tv.getText().toString().trim();
                if (!trim15.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim15));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
            case R.id.right_Hz_6000_ll:
                flag = "right_Hz_6000";
                String trim16 = rightHz6000Tv.getText().toString().trim();
                if (!trim16.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim16));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
            case R.id.right_Hz_8000_ll:
                flag = "right_Hz_8000";
                String trim17 = rightHz8000Tv.getText().toString().trim();
                if (!trim17.equals("")) {
                    pvOptions2.setSelectOptions(getPosition(trim17));
                } else {
                    pvOptions2.setSelectOptions(0);
                }
                pvOptions2.show();
                break;
        }
    }


    private void initDb() {
        for (int i = -10; i <= 120; i += 5) {
            list.add(i + "db");
        }

        //创建
        pvOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {

                if (flag.equals("left_Hz_250")) {
                    leftHz250Tv.setText(list.get(options1));
                    Entry entry = values.get(1);
                    entry.setY(options1);
                    values.set(1,entry);
                    leftEarStlc.changeTouchEntry();
                } else if (flag.equals("left_Hz_500")) {
                    leftHz500Tv.setText(list.get(options1));
                    Entry entry = values.get(2);
                    entry.setY(options1);
                    values.set(2,entry);
                    leftEarStlc.changeTouchEntry();
                }else if (flag.equals("left_Hz_750")) {
                    leftHz750Tv.setText(list.get(options1));
                    Entry entry = values.get(3);
                    entry.setY(options1);
                    values.set(3,entry);
                    leftEarStlc.changeTouchEntry();
                }else if (flag.equals("left_Hz_1000")) {
                    leftHz1000Tv.setText(list.get(options1));
                    Entry entry = values.get(4);
                    entry.setY(options1);
                    values.set(4,entry);
                    leftEarStlc.changeTouchEntry();
                }else if (flag.equals("left_Hz_1500")) {
                    leftHz1500Tv.setText(list.get(options1));
                    Entry entry = values.get(5);
                    entry.setY(options1);
                    values.set(5,entry);
                    leftEarStlc.changeTouchEntry();
                }else if (flag.equals("left_Hz_2000")) {
                    leftHz2000Tv.setText(list.get(options1));
                    Entry entry = values.get(6);
                    entry.setY(options1);
                    values.set(6,entry);
                    leftEarStlc.changeTouchEntry();
                }else if (flag.equals("left_Hz_4000")) {
                    leftHz4000Tv.setText(list.get(options1));
                    Entry entry = values.get(7);
                    entry.setY(options1);
                    values.set(7,entry);
                    leftEarStlc.changeTouchEntry();
                }else if (flag.equals("left_Hz_6000")) {
                    leftHz6000Tv.setText(list.get(options1));
                    Entry entry = values.get(8);
                    entry.setY(options1);
                    values.set(8,entry);
                    leftEarStlc.changeTouchEntry();
                }else if (flag.equals("left_Hz_8000")) {
                    leftHz8000Tv.setText(list.get(options1));
                    Entry entry = values.get(9);
                    entry.setY(options1);
                    values.set(9,entry);
                    leftEarStlc.changeTouchEntry();
                }
            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();

        pvOptions2 = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3, View v) {

                if (flag.equals("right_Hz_250")) {
                    rightHz250Tv.setText(list.get(options1));
                    Entry entry = values2.get(1);
                    entry.setY(options1);
                    values2.set(1,entry);
                    rightEarStlc.changeTouchEntry();
                } else if (flag.equals("right_Hz_500")) {
                    rightHz500Tv.setText(list.get(options1));
                    Entry entry = values2.get(2);
                    entry.setY(options1);
                    values2.set(2,entry);
                    rightEarStlc.changeTouchEntry();
                }else if (flag.equals("right_Hz_750")) {
                    rightHz750Tv.setText(list.get(options1));
                    Entry entry = values2.get(3);
                    entry.setY(options1);
                    values2.set(3,entry);
                    rightEarStlc.changeTouchEntry();
                }else if (flag.equals("right_Hz_1000")) {
                    rightHz1000Tv.setText(list.get(options1));
                    Entry entry = values2.get(4);
                    entry.setY(options1);
                    values2.set(4,entry);
                    rightEarStlc.changeTouchEntry();
                }else if (flag.equals("right_Hz_1500")) {
                    rightHz1500Tv.setText(list.get(options1));
                    Entry entry = values2.get(5);
                    entry.setY(options1);
                    values2.set(5,entry);
                    rightEarStlc.changeTouchEntry();
                }else if (flag.equals("right_Hz_2000")) {
                    rightHz2000Tv.setText(list.get(options1));
                    Entry entry = values2.get(6);
                    entry.setY(options1);
                    values2.set(6,entry);
                    rightEarStlc.changeTouchEntry();
                }else if (flag.equals("right_Hz_4000")) {
                    rightHz4000Tv.setText(list.get(options1));
                    Entry entry = values2.get(7);
                    entry.setY(options1);
                    values2.set(7,entry);
                    rightEarStlc.changeTouchEntry();
                }else if (flag.equals("right_Hz_6000")) {
                    rightHz6000Tv.setText(list.get(options1));
                    Entry entry = values2.get(8);
                    entry.setY(options1);
                    values2.set(8,entry);
                    rightEarStlc.changeTouchEntry();
                }else if (flag.equals("right_Hz_8000")) {
                    rightHz8000Tv.setText(list.get(options1));
                    Entry entry = values2.get(9);
                    entry.setY(options1);
                    values2.set(9,entry);
                    rightEarStlc.changeTouchEntry();
                }
            }
        }).setSelectOptions(0)//设置选择第一个
                .setOutSideCancelable(false)//点击背的地方不消失
                .build();

//      把数据绑定到控件上面
//      把数据绑定到控件上面
        pvOptions.setPicker(list);
        pvOptions2.setPicker(list);
    }

    public int getPosition(String s) {
        int flag = 0;
        for (int i = 0; i < list.size(); i++) {
            if (s.equals(list.get(i))) {
                flag = i;
                return flag;
            }

        }
        return flag;
    }


}

