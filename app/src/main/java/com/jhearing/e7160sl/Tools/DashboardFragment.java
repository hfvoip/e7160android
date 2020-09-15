package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;


import com.jhearing.e7160sl.COBLE.CobleFragment;
import com.jhearing.e7160sl.Connection.ConnectFragment;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;

import com.jhearing.e7160sl.MainActivity;
import com.jhearing.e7160sl.Params.ParameterFragment;
import com.jhearing.e7160sl.R;

import com.jhearing.e7160sl.adapter.GridItem;
import com.jhearing.e7160sl.adapter.GridViewAdapter;


import java.util.ArrayList;


public class DashboardFragment extends Fragment  implements  View.OnClickListener  {

    private static final String TAG = DashboardFragment.class.getSimpleName();

    private GridViewAdapter gridAdapter;
    private GridView  gridView;
    Fragment fragment;
    String fragmentTag;
    private int page1 =1;
    private int[] arr_funcs = new int[30];
    private String[] arr_titles = new String[30];
    private Button  btn_memory,btn_hearingaid,btn_tinnitus,btn_setting,btn_parameter,btn_fithelp;



    public DashboardFragment() {
        // Required empty public constructor
    }



    private void connectedView(HearingAidModel.Side side) {


    }

    private void unregister() {

    }

    private void register() {

    }

    @Override
    public void onResume() {
        super.onResume();
        register();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregister();
    }

    private boolean show_alert() {
        String msg = "本功能需要连接上助听器";
        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left) || Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
            return true;
        } else {
            Configuration.instance().alertDialog(msg, getActivity());
            return  false ;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_laucher, container, false);



        Bundle bundle = getArguments();
        if (bundle !=null) {
            page1 = bundle.getInt("page");
        }
        gridView = (GridView) view.findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(getActivity(), R.layout.laucher_grid_item, getData());
        gridView.setAdapter(gridAdapter);

        btn_setting = (Button)view.findViewById(R.id.btn_setting);
        btn_tinnitus = (Button)view.findViewById(R.id.btn_tinnitus);
        btn_hearingaid = (Button)view.findViewById(R.id.btn_hearingaid);
        btn_memory = (Button)view.findViewById(R.id.btn_memory);
        btn_parameter = (Button)view.findViewById(R.id.btn_parameter);
        btn_fithelp = (Button)view.findViewById(R.id.btn_fithelp);


        btn_fithelp.setOnClickListener( this);
        btn_parameter.setOnClickListener( this);
        btn_memory.setOnClickListener( this);
        btn_hearingaid.setOnClickListener( this);
        btn_tinnitus.setOnClickListener( this);
        btn_setting.setOnClickListener( this);

        return view;
    }
    private ArrayList<GridItem> getData() {
        final ArrayList<GridItem> imageItems = new ArrayList<>();

        int count = 0;

            arr_titles[0] ="Test\n连接助听器";
            arr_titles[1] ="Test\n 音量和场景";
            arr_titles[2] ="Test\n 验配参数";
            arr_titles[3] ="Test\n 工程模式";


           count = 4;



        for (int i = 0; i < count ; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.popup_bg);
            imageItems.add(new GridItem(bitmap, arr_titles[i]));
        }
        return imageItems;
    }


    @Override
    public void onClick(View v) {
        int id= v.getId();
        if (id==R.id.btn_setting) {
            fragment = new ParameterFragment();
            Bundle bundleL = new Bundle();
            bundleL.putString("page1","setting");
            fragmentTag = "setting" ;
            fragment.setArguments(bundleL);

        }
        if (id==R.id.btn_parameter) {
            fragment = new ParameterFragment();
            Bundle bundleL = new Bundle();
            bundleL.putString("page1","user");
            bundleL.putInt("hackmode",0);
            fragment.setArguments(bundleL);
            fragmentTag = "Dashboard2" ;

        }
        if (id==R.id.btn_fithelp) {
            fragment = new FithelpFragment();
            fragmentTag = "fithelp" ;

        }
        if (id==R.id.btn_memory) {
            fragment = new CobleFragment();
            fragmentTag = "coble" ;

        }
        if (id==R.id.btn_tinnitus) {
            fragment = new ParameterFragment();
            Bundle bundleL = new Bundle();
            bundleL.putString("page1","sg");
            fragment.setArguments(bundleL);
            fragmentTag = "tinnitus" ;

        }
        if (id==R.id.btn_hearingaid) {
            fragment = new ParameterFragment();
            Bundle bundleL = new Bundle();
            bundleL.putString("page1","audio");
            fragment.setArguments(bundleL);
            fragmentTag = "audio" ;

        }

        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }

    }
}
