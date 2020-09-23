/*
----------------------------------------------------------------------------
Copyright (c) 2016 Semiconductor Components Industries, LLC
(d/b/a ON Semiconductor). All Rights Reserved.

This code is the property of ON Semiconductor and may not be redistributed
in any form without prior written permission from ON Semiconductor. The
terms of use and warranty for this code are covered by contractual
agreements between ON Semiconductor and the licensee.
----------------------------------------------------------------------------
VolumeFragment.java
- Fragment to support volume control 
----------------------------------------------------------------------------
$Revision: 1.11 $
$Date: 2017/08/02 15:38:25 $
----------------------------------------------------------------------------
*/

package com.jhearing.e7160sl.COBLE.Memory;


import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.ark.ArkException;
import com.ark.ParameterSpace;
import com.google.android.material.tabs.TabLayout;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.MainActivity;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;


/**
 * Main View for controlling the hearing aids memory select
 * A simple {@link Fragment} subclass.
 */

public class MemoryFragment extends Fragment implements DialogInterface.OnDismissListener {

    private static final boolean NEXT_MEMORY = true;
    private static final boolean PREVIOUS_MEMORY = false;

    private final String TAG = MemoryFragment.class.getName();

    private int viewCurrentMemory;
    private HearingAidModel masterHA;
    private int numberOfMemories;

    private TextView memoryTextView;
    private TextView memoryNoteView;
    private ViewGroup textLayout;
    private Slide slide;
    private Switch switchAutomemory;
    private ImageView imgview;
    private TabLayout tab_memorylist;
    private Button btn_switchmemory;
    private String arr_memnote[] = new String[32];
    private int arr_icons[] = new int[5];

    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            updateViewsBasedOnConnection();
            onConfigurationChanged(HearingAidModel.Side.Right, data.address);
            onConfigurationChanged(HearingAidModel.Side.Left, data.address);

        }
    };


    private void updateViewsBasedOnConnection() {
        boolean isEnabled = false;
        //double check if one of them is connected
        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left) || Configuration.instance().isHAAvailable(HearingAidModel.Side.Right))
            isEnabled = true;

        if (switchAutomemory!=null)
            switchAutomemory.setEnabled(isEnabled);

        btn_switchmemory.setEnabled(isEnabled);
     //   memoryTextView.setEnabled(isEnabled);
     //   enableImageView(arrowRight, isEnabled);
      //  enableImageView(arrowLeft, isEnabled);
    }


    private void enableImageView(ImageView imageView, boolean isEnabled) {
        imageView.setEnabled(isEnabled);
    }


    private void onConfigurationChanged(HearingAidModel.Side side, String address) {

        if (Configuration.instance().isHANotNull(side)) {
            if (getHearingAid(side).address.equals(address)) {
                if (viewCurrentMemory != getHearingAid(side).currentMemory) {
                    viewCurrentMemory = getHearingAid(side).currentMemory;
                    updateTextViewForNotification();
                }
            }
        }
    }


    private void writeEventTrigger(HearingAidModel.Side side, boolean isNext) {
        if (Configuration.instance().isHAAvailable(side)) {
            try {
                getHearingAid(side).wirelessControl.changeMemory(isNext);
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }

        }
    }

    private void updateTextViewForNotification() {
        updateTextView();
        animateTextViewForNotification();
    }


    private void animateTextViewForNotification() {
        final float startSize = 12;
        final float endSize = 20;
        final int animationDuration = 600;
        ValueAnimator animator = ValueAnimator.ofFloat(startSize, endSize);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

            }
        });
        animator.start();
    }

    private void updateTextView() {

        tab_memorylist.getTabAt(viewCurrentMemory).select();
        memoryNoteView.setText( arr_memnote[viewCurrentMemory] );
        imgview.setImageResource(arr_icons[viewCurrentMemory]);


    }


    private void animateTextViewOnButtonPressed(HearingAidModel.Side side) {
      //  textLayout.removeView(memoryTextView);
        if (side == HearingAidModel.Side.Left)
            slide.setSlideEdge(Gravity.START);
        else
            slide.setSlideEdge(Gravity.END);
        TransitionManager.beginDelayedTransition(textLayout, slide);
     //   textLayout.addView(memoryTextView);
    }

    private HearingAidModel getHearingAid(HearingAidModel.Side side) {
        return Configuration.instance().getDescriptor(side);
    }


    private void updateCurrentMemoryValue() {
        if (!Configuration.instance().isConfigEmpty()) {
            if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left)) {
                viewCurrentMemory = (getHearingAid(HearingAidModel.Side.Left).currentMemory);
            } else if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
                viewCurrentMemory = (getHearingAid(HearingAidModel.Side.Right).currentMemory);
            }
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ((MainActivity) getActivity()).changeNavigationSelected(R.id.nav_coble);
        View rootView;
        int showexitbtn = 0;
        Bundle bundle = getArguments();
        if (bundle !=null )
             showexitbtn= bundle.getInt("showexitbtn");

        if (showexitbtn ==1) {
        //    rootView = inflater.inflate(R.layout.switchmemory_fragment, container, false);
        } else {
        //    rootView = inflater.inflate(R.layout.memory_fragment, container, false);
        }
        rootView = inflater.inflate(R.layout.switchmemory_fragment, container, false);
        textLayout = rootView.findViewById(R.id.textViewLayout);
        memoryTextView = rootView.findViewById(R.id.memoryTextView);

        memoryNoteView = rootView.findViewById(R.id.tv_memorynote);
        imgview = rootView.findViewById(R.id.img_memory);
        switchAutomemory = rootView.findViewById(R.id.switchAutomemory);
        btn_switchmemory = rootView.findViewById(R.id.btn_switchmemory);
        btn_switchmemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                changeMemory();

            }
        });

        for (int i=0;i<8;i++) {
            arr_memnote[i] = "";
        }
        arr_memnote[0] = "室内";
        arr_memnote[1] = "交通";
        arr_memnote[2] = "餐厅";
        arr_memnote[3] = "风噪";
        arr_memnote[4] = "音乐";
        arr_memnote[5] = "户外";
        arr_memnote[6] = "餐厅";
        arr_memnote[7] = "其他";
        arr_memnote[8] = "其他2";


        slide = new Slide();
       // setImageButtons(rootView);
        Log.i(TAG, " view Memory value in onCreate" + viewCurrentMemory);
        updateViewsBasedOnConnection();


        arr_icons[0] = R.drawable.memory_room;
        arr_icons[1] = R.drawable.memory_car;
        arr_icons[2] = R.drawable.memory_dinning;
        arr_icons[3] = R.drawable.memory_wind;

        arr_icons[4] = R.drawable.memory_music;


        tab_memorylist = rootView.findViewById(R.id.tab_memorylist);
        for (int i=0;i<5;i++) {
            TabLayout.Tab  newtab = tab_memorylist.newTab();

            newtab.setIcon(arr_icons[i]);
            newtab.setText(arr_memnote[i]);
            tab_memorylist.addTab(newtab);

        }

        tab_memorylist.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
               int pos =  tab.getPosition();
               //pos 就是memory idx
                memoryNoteView.setText(arr_memnote[pos]);
                viewCurrentMemory = pos;
                imgview.setImageResource(arr_icons[viewCurrentMemory]);


            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        return rootView;
    }

    private ParameterSpace getPs(int index) {

        ParameterSpace ps = ParameterSpace.kNvmMemory0;
        if (index == 0)  ps = ParameterSpace.kNvmMemory0 ;
        if (index == 1)  ps = ParameterSpace.kNvmMemory1 ;
        if (index == 2)  ps = ParameterSpace.kNvmMemory2 ;
        if (index == 3)  ps = ParameterSpace.kNvmMemory3 ;
        if (index == 4)  ps = ParameterSpace.kNvmMemory4 ;
        if (index == 5)  ps = ParameterSpace.kNvmMemory5 ;
        if (index == 6)  ps = ParameterSpace.kNvmMemory6 ;
        if (index == 7)  ps = ParameterSpace.kNvmMemory7 ;

        return ps;


    }

    private void onImageViewClicked(HearingAidModel.Side side) {

        if (HearingAidModel.Side.Right == side)
            viewCurrentMemory++;
        else
            viewCurrentMemory--;

        resetValue();
        updateTextView();
        animateTextViewOnButtonPressed(side);
        writeToHAs((side == HearingAidModel.Side.Right) ? NEXT_MEMORY : PREVIOUS_MEMORY);
    }

    private void resetValue() {

        if (viewCurrentMemory > numberOfMemories)
            viewCurrentMemory = 1;
        else if (viewCurrentMemory < 1)
            viewCurrentMemory = numberOfMemories;
    }

    private void setImageButtons(final View rootView) {


    }

    private void writeToHAs(boolean eventValue) {
        writeEventTrigger(HearingAidModel.Side.Left, eventValue);
        writeEventTrigger(HearingAidModel.Side.Right, eventValue);
    }

    private void changeMemory() {
        ParameterSpace ps = getPs(viewCurrentMemory);

        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left)) {
            try {
                getHearingAid(HearingAidModel.Side.Left).wirelessControl.setCurrentMemory(ps);
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }

        }
        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
            try {
                getHearingAid(HearingAidModel.Side.Right).wirelessControl.setCurrentMemory(ps);
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }

        }


    }

    private void setMaster(HearingAidModel.Side side) {
        masterHA = getHearingAid(side);
        viewCurrentMemory = getHearingAid(side).currentMemory;
        numberOfMemories = getHearingAid(side).numberOfMemories;
    }

    private void onResumeUpdates() {

        if (!Configuration.instance().isConfigEmpty()) {

            if (Configuration.instance().isHANotNull(HearingAidModel.Side.Left)) {
                setMaster(HearingAidModel.Side.Left);
            } else if (Configuration.instance().isHANotNull(HearingAidModel.Side.Right)) {
                setMaster(HearingAidModel.Side.Right);
            }
        }
        updateTextView();
        updateViewsBasedOnConnection();
    }

    @Override
    public void onResume() {
        super.onResume();
        onResumeUpdates();
        updateCurrentMemoryValue();
        updateTextView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregister();
    }


    private void unregister() {
        EventBus.unregisterReceiver(configurationChangedEventHandler);
    }

    private void register() {
        EventBus.registerReceiver(configurationChangedEventHandler, ConfigurationChangedEvent.class.getName());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        updateTextViewForNotification();
    }
}