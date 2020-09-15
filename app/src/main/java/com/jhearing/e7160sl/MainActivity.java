package com.jhearing.e7160sl;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.ark.ArkException;
import com.jhearing.e7160sl.COBLE.CobleFragment;
import com.jhearing.e7160sl.COBLE.Memory.MemoryFragment;
import com.jhearing.e7160sl.COBLE.Volume.VolumeFragment;
import com.jhearing.e7160sl.Connection.ConnectFragment;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.Im.JWebSocketClientService;
import com.jhearing.e7160sl.Params.ParameterFragment;
import com.jhearing.e7160sl.Tools.AgActivity;
import com.jhearing.e7160sl.Tools.ChatFragment;
import com.jhearing.e7160sl.Tools.Dashboard2Fragment;
import com.jhearing.e7160sl.Tools.Dashboard3Fragment;
import com.jhearing.e7160sl.Tools.DashboardFragment;
import com.jhearing.e7160sl.Tools.FithelpFragment;
import com.jhearing.e7160sl.Tools.NearshopFragment;
import com.jhearing.e7160sl.Tools.OptionsActivity;
import com.jhearing.e7160sl.Tools.PersonalFragment;
import com.jhearing.e7160sl.Tools.PuretoneFragment;
import com.jhearing.e7160sl.Tools.AgFragment;
import com.jhearing.e7160sl.Tools.TinnitusFragment;
import com.jhearing.e7160sl.Tools.UsageFragment;
import com.jhearing.e7160sl.Utils.ParseUrl;
import com.jhearing.e7160sl.adapter.Adapter_ChatMessage;
import com.jhearing.e7160sl.adapter.GridItem;
import com.jhearing.e7160sl.adapter.GridViewAdapter;
import com.jhearing.e7160sl.model.ChatMessage;
import com.jhearing.e7160sl.Tools.OptionsActivity;

import com.ark.Parameter;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final int BLE_ENABLE_CODE = 2;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String[] LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;
    Fragment fragment;
    String fragmentTag;
    private NavigationView navigationView;
    private ChatMessageReceiver chatMessageReceiver;
    private List<ChatMessage> chatMessageList = new ArrayList<>();//消息列表
    private Adapter_ChatMessage adapter_chatMessage;
    private Context mContext;
    private GridViewAdapter gridAdapter;
    private GridView  gridView;

    public MainActivity() {
        super();
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mContext=MainActivity.this;
        getSupportActionBar().hide();
     //   Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    //    toolbar.setVisibility(View.GONE);
    //    toolbar.inflateMenu(R.menu.toolbar_menu);
        //这行语句会导致从laucher 调用失败
       // setSupportActionBar(toolbar);



        if (savedInstanceState == null && !getIntent().getAction().equals(Intent.ACTION_APPLICATION_PREFERENCES) && !getIntent().getAction().equals(Intent.ACTION_SHUTDOWN)) {
            try {
                Configuration.instance().initialiseConfiguration(this);
            } catch (ArkException e) {
                Log.i(TAG, e.getMessage());
            }
        }


        try {
            Configuration.instance().initialiseConfiguration(this);
        } catch (ArkException e) {
            Log.i(TAG, e.getMessage());
        }

        if (!Configuration.instance().isSavedPreferenceConfigEmpty() && !getIntent().getAction().equals(Intent.ACTION_APPLICATION_PREFERENCES))
            Configuration.instance().load(this);

        checkScanningPermissions();


        BottomNavigationView navView = (BottomNavigationView)  findViewById(R.id.navigation);
        navView.setOnNavigationItemSelectedListener(this);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.



        if (getFragmentManager().getBackStackEntryCount() == 0) {

            // Add the COBLE as the first view
         /*   if (Configuration.instance().isConfigEmpty()) {
                fragment = new ConnectFragment();
                fragmentTag = getString(R.string.connect);
                navigationView.setCheckedItem(R.id.nav_connect);
            } else {
                fragment = new CobleFragment();
                fragmentTag = getString(R.string.coble);
                navigationView.setCheckedItem(R.id.nav_coble);
            }

          */
            fragment = new DashboardFragment();
            fragmentTag ="dashboard";
        //    navigationView.setCheckedItem(R.id.nav_manage);

            getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();

          if (getIntent().getAction().equals(Intent.ACTION_APPLICATION_PREFERENCES)) {
                createService(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else if (getIntent().getAction().equals(Intent.ACTION_MAIN)) {
                createService(INITIAL_REQUEST);

            } else if (getIntent().getAction().equals(Intent.ACTION_SHUTDOWN)) {

                finishAndRemoveTask();
            }


        }


    }

    // Prepare some dummy data for gridview
    private ArrayList<GridItem> getData() {
        final ArrayList<GridItem> imageItems = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.popup_bg);
            imageItems.add(new GridItem(bitmap, "Image#" + i));
        }
        return imageItems;
    }



    public void createService(int flag) {
        Intent intent = new Intent(this, BLESync.class);
        intent.setFlags(flag);
        startService(intent);
    }

    //by YANG
    public void createWebsocketService() {

        //register receiver
        chatMessageReceiver = new ChatMessageReceiver();
        IntentFilter filter = new IntentFilter("com.jhearing.e7160sl.content");
        registerReceiver(chatMessageReceiver, filter);

        Intent intent = new Intent(this, JWebSocketClientService.class);
        startService(intent);
    }


    private void checkScanningPermissions() {
        if (!canAccessLocation()) {
            requestPermissions(LOCATION_PERMS, INITIAL_REQUEST);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BLE_ENABLE_CODE:
                if (resultCode != RESULT_OK) {
                    Configuration.instance().showMessage(getString(R.string.bluetooth_disabled), this);
                    finish();
                } else if (resultCode == RESULT_OK) {
                    Configuration.instance().showMessage(getString(R.string.bluetooth_enabled), this);
                }
        }
    }


    public void changeNavigationSelected(int id) {

    }

    @Override
    public void onBackPressed() {
     /*yang   DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (getFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressed();
        }
        */
     Log.d(TAG,"back pressed");
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_chat) {

            fragmentTag = getString(R.string.remotefitting);
            fragment = new ChatFragment();
            if (fragment != null) {
                getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
            }
        }

        if (id==R.id.menu_switchmemory) {

            fragmentTag = "memoryswitch";
            fragment = new MemoryFragment();
            Bundle bundleL = new Bundle();
            bundleL.putInt("showexitbtn",1);
            fragment.setArguments(bundleL);

            if (fragment != null) {
                getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
            }
        }


        if (id== R.id.nav_connect) {

            fragment = new ConnectFragment();
            fragmentTag = getString(R.string.connect);
            if (fragment != null) {
                getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
            }
        }
        if (id== R.id.menu_shop) {

            fragmentTag = "nearshop";
            fragment = new NearshopFragment();

            if (fragment != null) {
                getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
            }
        }
        if (id== R.id.menu_remotefitting) {

            fragmentTag = getString(R.string.remotefitting);
            fragment = new ChatFragment();

            if (fragment != null) {
                getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
            }
        }
        if (id== R.id.menu_mylog) {

            fragmentTag = getString(R.string.settings);
            fragment = new UsageFragment();

            if (fragment != null) {
                getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
            }
        }
        if (id== R.id.nav_profile) {
            fragmentTag = "Personal";
            fragment = new PersonalFragment();

            if (fragment != null) {
                getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }

        if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left) || Configuration.instance().isHAAvailable(HearingAidModel.Side.Right))
            return ifKeyIsVolumeButton(keyCode, getResources().getStringArray(R.array.fragments_names)[0]);

        return super.onKeyDown(keyCode, event);
    }

    private boolean ifKeyIsVolumeButton(int keyCode, String fragmentName) {

        VolumeFragment volumeFragment = ((VolumeFragment) getFragmentManager().findFragmentByTag(fragmentName));

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (volumeFragment != null)
                if (volumeFragment.onKeyDown(keyCode))
                    return true;
        }
        return false;

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String msg = "本功能需要连接上助听器";
        if (id==R.id.nav_dashboard1) {
            fragment = new DashboardFragment();
            fragmentTag = "Dashboard1" ;

        }
        else if (id==R.id.nav_profile) {
            fragment = new PersonalFragment();
            fragmentTag = "Personal" ;

        }
        else if (id == R.id.nav_connect) {
            fragment = new ConnectFragment();
            fragmentTag = getString(R.string.connect);
        } else if (id == R.id.nav_coble) {
            if (Configuration.instance().isHAAvailable(HearingAidModel.Side.Left) || Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
                // Handle the coble action
                fragment = new CobleFragment();
                fragmentTag = getString(R.string.coble);
            } else {
                Configuration.instance().alertDialog(msg, this);
            }
        } else if ( id == R.id.nav_Param) {
            if (  Configuration.instance().isHAAvailable(HearingAidModel.Side.Left) || Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
                fragmentTag = getString(R.string.params);
                fragment = new ParameterFragment();
                Bundle bundleL = new Bundle();
                bundleL.putInt("hackmode",0);
                fragment.setArguments(bundleL);
            } else {
                Configuration.instance().alertDialog(msg, this);
            }
        }
        else if ( id == R.id.nav_allparameters) {
            if (  Configuration.instance().isHAAvailable(HearingAidModel.Side.Left) || Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
                fragmentTag = getString(R.string.params);
                fragment = new ParameterFragment();
                Bundle bundleL = new Bundle();
                bundleL.putInt("hackmode",1);
                fragment.setArguments(bundleL);
            } else {
                Configuration.instance().alertDialog(msg, this);
            }
        }

      //    else if (id == R.id.menu_mylog) {
       //     fragmentTag = getString(R.string.settings);
       //     fragment = new UsageFragment();
       // }

        else if (id == R.id.nav_manage) {
            fragmentTag = getString(R.string.settings);
            fragment = new SettingsFragment();
        } else if (id == R.id.nav_puretone_test) {
            fragmentTag = getString(R.string.puretone_test);
            fragment = new PuretoneFragment();
        }
        else if (id == R.id.nav_ag_input) {
            fragmentTag = getString(R.string.ag_diagram);
            fragment = new AgFragment();
        }
        else if (id == R.id.nav_chat) {

             fragmentTag = getString(R.string.remotefitting);
            fragment = new ChatFragment();
        }
        else if (id == R.id.nav_tinnitus) {
            Configuration.instance().alertDialog(msg, this);
            fragmentTag = getString(R.string.tinnitus);
            fragment = new TinnitusFragment();
        }
        else if (id == R.id.nav_usage_report) {
            fragmentTag = "usagereport";
            fragment = new UsageFragment();
        }
     //   else if (id == R.id.nav_nearshop) {
    //        fragmentTag = "nearshop";
    //        fragment = new NearshopFragment();
    //    }
          else if (id == R.id.nav_helpfit) {
           fragmentTag = "Fithelp";
           fragment = new FithelpFragment();
        }


        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }

      /*  DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
*/
        return true;
    }

    private boolean canAccessLocation() {
        return (hasPermission(LOCATION_PERMS[0], LOCATION_PERMS[1]));
    }

    private boolean hasPermission(String perm, String perm2) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm)) && (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm2));
    }
    public void gotoparameter() {
        fragmentTag = getString(R.string.params);
        fragment = new ParameterFragment();
        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }

    }

    public void gotocoble() {
        fragmentTag = getString(R.string.coble);
        fragment = new CobleFragment();
        if (fragment != null) {
            getFragmentManager().beginTransaction().replace(R.id.Main_frame, fragment, fragmentTag).addToBackStack(fragmentTag).commit();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case LOCATION_REQUEST:
                if (canAccessLocation()) {
                    Configuration.instance().showMessage(getString(R.string.msg_permission_granted), this);
                }
                break;

        }
    }

    private class ChatMessageReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {


            String message=intent.getStringExtra("message");
            Log.d(TAG,message);

            Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
            HearingAidModel.Side side = HearingAidModel.Side.Left;
            HearingAidModel ha = null;
            Parameter parameter = null;
            String strRequestKeyAndValues="";
            Map<String, String> mapRequest = ParseUrl.parseURLParam(message);

            for(String strRequestKey: mapRequest.keySet()) {
                String strRequestValue=mapRequest.get(strRequestKey);
                strRequestKeyAndValues+="key:"+strRequestKey+",Value:"+strRequestValue+";";

            }
            Log.d(TAG,strRequestKeyAndValues);

            try {
                ha = Configuration.instance().getDescriptor(side);
                if (ha !=null) {
                    parameter = ha.parameters.getById(getString(R.string.sdk_mid_param_id));
                    String modulename = parameter.getLongModuleName();
                    Toast.makeText(mContext, modulename, Toast.LENGTH_LONG).show();
                }
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }
          //  ChatMessage chatMessage=new ChatMessage();
           // chatMessage.setContent(message);
           // chatMessage.setIsMeSend(0);
           // chatMessage.setIsRead(1);
           // chatMessage.setTime(System.currentTimeMillis()+"");
           // chatMessageList.add(chatMessage);

           // adapter_chatMessage = new Adapter_ChatMessage(mContext, chatMessageList);
         //   listView.setAdapter(adapter_chatMessage);
          //  listView.setSelection(chatMessageList.size());
        }
    }
}
