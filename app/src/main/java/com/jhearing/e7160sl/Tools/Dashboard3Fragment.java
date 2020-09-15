package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.jhearing.e7160sl.COBLE.CobleFragment;
import com.jhearing.e7160sl.Connection.ConnectFragment;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.Params.ParameterFragment;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.adapter.GridItem;
import com.jhearing.e7160sl.adapter.GridViewAdapter;

import java.util.ArrayList;


public class Dashboard3Fragment extends Fragment    {

    private static final String TAG = Dashboard3Fragment.class.getSimpleName();

    private GridViewAdapter gridAdapter;
    private GridView  gridView;
    Fragment fragment;
    String fragmentTag;
    private int page1 =3;
    private int[] arr_funcs = new int[30];
    private String[] arr_titles = new String[30];



    public Dashboard3Fragment() {
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


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                int func_id = 0;
                func_id =arr_funcs[position];

                if (position == 0) {
                    fragment = new ChatFragment();
                    fragmentTag = getString(R.string.remotefitting);
                }


                if (position == 1) {
                    fragment = new FithelpFragment();
                    fragmentTag = getString(R.string.fit_help);
                }


                if (position == 2) {
                    fragment = new NearshopFragment();
                    fragmentTag = getString(R.string.nearshop);
                }
                if (position == 3) {
                    Intent intent = new Intent();
                    String url="http://www.jhearing.com";
                    intent.setData(Uri.parse(url));//Url 就是你要打开的网址
                    intent.setAction(Intent.ACTION_VIEW);
                    startActivity(intent); //启动浏览器
                    return;

                }
                if (fragment != null) {
                    getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
                }
            }
        });



        return view;
    }
    private ArrayList<GridItem> getData() {
        final ArrayList<GridItem> imageItems = new ArrayList<>();

        int count = 4;


            arr_titles[0] ="远程求助";

            arr_titles[1] ="帮助手册";
        arr_titles[2] ="附近验配店";
            arr_titles[3] ="联系我们";




        for (int i = 0; i < count ; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.popup_bg);
            imageItems.add(new GridItem(bitmap, arr_titles[i]));
        }
        return imageItems;
    }







}
