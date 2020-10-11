package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.ark.ArkException;
import com.ark.AsyncResult;
import com.ark.Parameter;
import com.ark.ParameterList;
import com.ark.ParameterType;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.jhearing.e7160sl.adapter.CheckAssistantAdapter;
import com.jhearing.e7160sl.model.AssistantInfo;

import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.MainActivity;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;
import com.jhearing.e7160sl.model.shopInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FithelpFragment extends Fragment   {

    private static final String TAG = UsageFragment.class.getSimpleName();

    private static final int DISCONNECTING = 0;
    private static final int DISCONNECTED = 1;
    private static final int CONNECTING = 2;
    private static final int CONNECTED = 3;

    RecyclerView checkAssistantRv;
    WebView webview;
    private List<AssistantInfo> assistantInfos = new ArrayList<>();

    String[] titles = {"助听器啸叫","不能听见轻小言语声" ,"安静环境下太多噪音",
            "助听器不清楚","远处声音听起来比近处更好" ,"助听器听起来声音小",
            "自己说话声音大，回声，闷","狗叫声太响" ,"冰箱声太响",
            "水滴声太响","交通噪音太响" ,"气导太响",
            "餐具和陶器声太响","鸟叫声太响" ,"纸张沙沙声太响","声音太尖"
    };

    String[] contents = {"降低高频轻声增益","增加轻声增益" ,"降低非常小声增益",
            "减少高频的压缩比","提升大声增益，减少CR" ,"提升大声增益，减少CR",
            "降低低频大声增益，增加CR","降低低频大声增益，提升低频拐点，降低非常小声放大" ,"降低低频大声增益，降低低频增益",
            "降低低频大声增益，降低低频增益","降低低频大声增益，降低低频增益" ,"降低低频大声增益",
            "降低MPO，降低大声增益，增加高频通道拐点","降低MPO，降低大声增益，增加高频增益" ,"降低MPO，降低大声增益，增加高频增益","降低MPO，降低大声增益"
    };

    EditText shopNameTv;
    private ImageView imageRight, imageLeft;
    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            onConfigurationChanged(HearingAidModel.Side.Left, data.address);
            onConfigurationChanged(HearingAidModel.Side.Right, data.address);
        }
    };

    public FithelpFragment() {
        // Required empty public constructor
    }

    private void onConfigurationChanged(HearingAidModel.Side side, String address) {

    }

    private HearingAidModel getHearingAid(HearingAidModel.Side side) {
        return Configuration.instance().getDescriptor(side);
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

    }

    @Override
    public void onPause() {
        super.onPause();

    }


    private void createUsageFragment(HearingAidModel.Side side) {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fithelp, container, false);

        imageLeft = view.findViewById(R.id.ear_left);
        imageRight = view.findViewById(R.id.ear_right);
       shopNameTv  = view.findViewById((R.id.shop_name_tv));
       webview = view.findViewById((R.id.help_webview));
       webview.loadUrl("http://yp.kalllove.com/demo.html");
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                //返回值为true时，在app内打开网页，flase为手机自带浏览器打开。
                return true;
            }
        });

        new FetchUrlTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

       /*
        checkAssistantRv = view.findViewById(R.id.check_assistant_rv);


        loadData();
        checkAssistantRv.setLayoutManager(new LinearLayoutManager(getActivity() ));
        CheckAssistantAdapter adapter = new CheckAssistantAdapter();
        checkAssistantRv.setAdapter(adapter);
        adapter.addData(assistantInfos);
        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                Intent intent = new Intent(getActivity(), AssistDetailActivity.class);
                intent.putExtra("data",(AssistantInfo)adapter.getItem(position));
                startActivity(intent);
            }
        });
        */



        return view;
    }

    private void imageTabPressed(HearingAidModel.Side side) {


    }
    public void  loadData(){

        for (int i = 0; i < titles.length; i++) {
            AssistantInfo assistantInfo = new AssistantInfo();
            assistantInfo.setTitle(titles[i]);
            assistantInfo.setContent(contents[i]);
            assistantInfos.add(assistantInfo);
        }

    }

    private class FetchUrlTask extends AsyncTask<Void, Integer, Void> {
        AsyncResult res;
        FetchUrlTask( ) {
        }

        @Override
        protected Void doInBackground(Void... params) {
            load_jsondata();
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
        private void load_jsondata() {
           MediaType mt_JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject  reportdata  = report_eardata();
            String baseurl = "http://m.kalllove.com/ypapi/ajax_savehisa.php";
            try {

                OkHttpClient client = new OkHttpClient();
                //创建一个RequestBody(参数1：数据类型 参数2传递的json串)
                RequestBody requestBody = RequestBody.create(mt_JSON, String.valueOf(reportdata));
                //3 . 构建Request,将FormBody作为Post方法的参数传入

                Request request = new Request.Builder()
                        .url(baseurl)
                        .post(requestBody)
                        .build();

                Response responses = null;

                responses = client.newCall(request).execute();
                String jsonData = responses.body().string();

                JSONObject Jobject = new JSONObject(jsonData);

                JSONArray Jarray = Jobject.getJSONArray("items");

                for (int i = 0; i < Jarray.length(); i++) {


                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        }



    }



    private JSONObject  report_eardata(  ) {

        int ear_id = -1;
        HearingAidModel ha = null;
        if (  Configuration.instance().isHAAvailable(HearingAidModel.Side.Left)) {
            ear_id = 0;
            ha = Configuration.instance().getDescriptor(HearingAidModel.Side.Left);
        }
        if (  Configuration.instance().isHAAvailable(HearingAidModel.Side.Right)) {
            ear_id = 1;
            ha = Configuration.instance().getDescriptor(HearingAidModel.Side.Right);
        }
        JSONObject root = new JSONObject();
        if (ear_id ==-1) return root;

        JSONObject root2 = new JSONObject();
        JSONObject root_sysmem = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        ParameterList parameters = ha.arr_parameters[0];
        ParameterList sysparams = ha.systemParameters;
        int mem_idx = 0;




        try {
            String serial_uuid = android.os.Build.SERIAL;
            root.put("appid", serial_uuid);
            root.put("earid",String.valueOf(ear_id));
            root.put("ha_name",ha.name);
            root.put("ha_address",ha.address);
            root.put("libname","7160");
            root2.put("mem_idx", String.valueOf(mem_idx));


            int total_count = 0;
            String str_total ="";
            try {

                total_count = parameters.getCount();
            } catch (ArkException fe) {
                return root;
            }
            for (int i = 0; i < total_count; i++) {

                JSONObject obj_parm = new JSONObject();
                try {
                    //Parameter tmp_pt = getParameter(side, mem_idx, "X_EQ_ChannelGain_dB[" + i + "]");
                    Parameter tmp_pt = parameters.getItem(i);
                    if (tmp_pt ==null)  continue;
                    ParameterType type = tmp_pt.getType();
                    String strval ="";

                    if ( type == ParameterType.kByte || type == ParameterType.kInteger)
                        strval= ""+tmp_pt.getValue();
                    else if (type == ParameterType.kDouble)
                        strval =""+ tmp_pt.getDoubleValue();
                    else if (type == ParameterType.kBoolean)
                        strval =""+ tmp_pt.getValue();
                    else
                        strval =""+ tmp_pt.getValue();

                    if (str_total =="")
                        str_total = strval;
                    else
                        str_total +="|"+strval;

                } catch (ArkException e) {
                    Log.d(TAG,e.getMessage());
                    continue;
                }
            }
            root2.put("param", str_total);
            jsonArray.put(root2);


            root_sysmem.put("mem_idx", "-2");

            total_count = 0;
            str_total ="";
            try {

                total_count = sysparams.getCount();
            } catch (ArkException fe) {
                return root;
            }
            for (int i = 0; i < total_count; i++) {

                JSONObject obj_parm = new JSONObject();
                try {

                    Parameter tmp_pt = sysparams.getItem(i);
                    if (tmp_pt ==null)  continue;
                    ParameterType type = tmp_pt.getType();
                    String strval ="";

                    if ( type == ParameterType.kByte || type == ParameterType.kInteger)
                        strval= ""+tmp_pt.getValue();
                    else if (type == ParameterType.kDouble)
                        strval =""+ tmp_pt.getDoubleValue();
                    else if (type == ParameterType.kBoolean)
                        strval =""+ tmp_pt.getValue();
                    else
                        strval =""+ tmp_pt.getValue();

                    if (str_total =="")
                        str_total = strval;
                    else
                        str_total +="|"+strval;

                } catch (ArkException e) {
                    Log.d(TAG,e.getMessage());
                    continue;
                }
            }
            root_sysmem.put("param", str_total);
            jsonArray.put(root_sysmem);
            root.put("param",jsonArray);


        } catch(Exception fe ) {

        }


        return root;
    }



}
