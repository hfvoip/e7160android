package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.ark.ArkException;
import com.ark.AsyncResult;
import com.ark.CommunicationAdaptor;
import com.ark.DeviceInfo;
import com.ark.Graph;
import com.ark.GraphDefinition;
import com.ark.GraphDefinitionList;
import com.ark.GraphId;
import com.ark.IndexedList;
import com.ark.IndexedTextList;
import com.ark.InputSignalType;
import com.ark.LogType;
import com.ark.Parameter;
import com.ark.ParameterList;
import com.ark.ParameterSpace;
import com.ark.ParameterType;
import com.ark.Product;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;
import com.jhearing.e7160sl.Im.JWebSocketClient;
import com.jhearing.e7160sl.Im.JWebSocketClientService;
import com.jhearing.e7160sl.MainActivity;

import com.jhearing.e7160sl.Params.ParameterFragmentItem;
import com.jhearing.e7160sl.R;
import com.jhearing.e7160sl.Utils.Events.ConfigurationChangedEvent;
import com.jhearing.e7160sl.Utils.Events.WsmessageEvent;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventBus;
import com.jhearing.e7160sl.Utils.EventsHadlers.EventReceiver;
import com.jhearing.e7160sl.Utils.ParseUrl;
import com.jhearing.e7160sl.adapter.Adapter_ChatMessage;
import com.jhearing.e7160sl.model.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChatFragment extends Fragment {

    private static final String TAG = ChatFragment.class.getSimpleName();

    private static final int DISCONNECTING = 0;
    private static final int DISCONNECTED = 1;
    private static final int CONNECTING = 2;
    private static final int CONNECTED = 3;

    private static final int DETECT_DEVICE = 0;
    private static final int INITIALIZE_DEVICE = 1;
    private static final int READ_DEVICE = 2;
    private static final int WRITE_TO_DEVICE = 3;
    private static final int BURN_TO_DEVICE = 5;
    private static final int READ_DEVICE_SYS = 4;

    private static HearingAidModel.Side side;
    private static boolean isActive = true;
    private static AsyncTask<Void, Integer, Void> initializeSDKParameters;
    private boolean isBusy = false;

    private int current_memory_idx;

    private ImageView imageRight, imageLeft;
    private Button btnSend;
    private EditText etContent;
    private ListView listView;
    private List<ChatMessage> chatMessageList = new ArrayList<>();//消息列表
    private Adapter_ChatMessage adapter_chatMessage;
    private ChatMessageReceiver chatMessageReceiver;
    private Context mContext;
    private JWebSocketClient client;
    private JWebSocketClientService jWebSClientService;
    private JWebSocketClientService.JWebSocketClientBinder binder;
    private String hwid="abc";

    private ParameterSpace  current_ParameterSpace  = ParameterSpace.kActiveMemory;



    private EventReceiver<ConfigurationChangedEvent> configurationChangedEventEventHandler = new EventReceiver<ConfigurationChangedEvent>() {
        @Override
        public void onEvent(String name, ConfigurationChangedEvent data) {
            onConfigurationChanged(HearingAidModel.Side.Left, data.address);
            onConfigurationChanged(HearingAidModel.Side.Right, data.address);
        }
    };
    private ParameterSpace get_parameterspace(int mem_idx) {
        ParameterSpace  tmp_ps =null;
        ParameterSpace  arr_paramspace[] = {ParameterSpace.kNvmMemory0,ParameterSpace.kNvmMemory1,
                ParameterSpace.kNvmMemory2,ParameterSpace.kNvmMemory3,ParameterSpace.kNvmMemory4,
                ParameterSpace.kNvmMemory5,ParameterSpace.kNvmMemory6,ParameterSpace.kNvmMemory7,
                ParameterSpace.kNvmMemory5,ParameterSpace.kNvmMemory6,ParameterSpace.kNvmMemory7,
                ParameterSpace.kNvmMemory8,ParameterSpace.kNvmMemory9,ParameterSpace.kNvmMemory10,
                ParameterSpace.kNvmMemory11,ParameterSpace.kNvmMemory12,ParameterSpace.kNvmMemory13,
                ParameterSpace.kNvmMemory14,ParameterSpace.kNvmMemory15
        };
        if (mem_idx ==-3 ) {

            tmp_ps = ParameterSpace.kSystemNvmMemory;
        }
        if (mem_idx == -2) {

            tmp_ps = ParameterSpace.kSystemActiveMemory;

        }
        if (mem_idx ==-1) {
            tmp_ps = ParameterSpace.kActiveMemory;

        }
        if (mem_idx >= 0 && mem_idx < 16) {
            tmp_ps = arr_paramspace[mem_idx];

        }
        return tmp_ps;




    }


    public ChatFragment() {
        // Required empty public constructor
    }

    private void onConfigurationChanged(HearingAidModel.Side side, String address) {
        if (Configuration.instance().isHANotNull(side)) {
            if (address.equals(getHearingAid(side).address)) {
                Log.i(TAG, "Connection Status" + getHearingAid(side).connectionStatus);
                connectedView(side);
            }
        }
    }

    private HearingAidModel getHearingAid(HearingAidModel.Side side) {
        return Configuration.instance().getDescriptor(side);
    }

    private void connectedView(HearingAidModel.Side side) {


    }

    private void check_parameterspace() {
        try {
            Log.d(TAG, "L92:wirelesscontrol:" + getHearingAidModel(side).wirelessControl.getCurrentMemory());
            Log.d(TAG, "L92:current memory:" + getHearingAidModel(side).product.getCurrentMemory());
            ParameterSpace tmp_ps = getHearingAidModel(side).wirelessControl.getCurrentMemory();
            if (tmp_ps == ParameterSpace.kNvmMemory0) current_memory_idx = 0;
            if (tmp_ps == ParameterSpace.kNvmMemory1) current_memory_idx = 1;
            if (tmp_ps == ParameterSpace.kNvmMemory2) current_memory_idx = 2;
            if (tmp_ps == ParameterSpace.kNvmMemory3) current_memory_idx = 3;
            if (tmp_ps == ParameterSpace.kNvmMemory4) current_memory_idx = 4;
            if (tmp_ps == ParameterSpace.kNvmMemory5) current_memory_idx = 5;
            if (tmp_ps == ParameterSpace.kNvmMemory6) current_memory_idx = 6;
            if (tmp_ps == ParameterSpace.kNvmMemory7) current_memory_idx = 7;
            getHearingAidModel(side).product.setCurrentMemory(current_memory_idx);
        } catch (ArkException e ) {

        }
    }
    private void unregister() {
        EventBus.unregisterReceiver(configurationChangedEventEventHandler);


    }

    private void register() {
        EventBus.registerReceiver(configurationChangedEventEventHandler, ConfigurationChangedEvent.class.getName());

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

    /**
     * 绑定服务
     */
    private void bindService_my() {
        Intent bindIntent = new Intent(mContext, JWebSocketClientService.class);
        mContext.bindService(bindIntent, serviceConnection, mContext.BIND_AUTO_CREATE);

    }

    /**
     * 启动服务（websocket客户端服务）
     */
    private void startJWebSClientService_my() {
        Intent intent = new Intent(mContext, JWebSocketClientService.class);
        mContext.startService(intent);
    }
    private void doRegisterReceiver() {
        chatMessageReceiver = new ChatMessageReceiver();
        IntentFilter filter = new IntentFilter("com.jhearing.e7160sl.content");
        mContext.registerReceiver(chatMessageReceiver, filter);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG, "服务与活动成功绑定");
            binder = (JWebSocketClientService.JWebSocketClientBinder) iBinder;
            jWebSClientService = binder.getService();
            client = jWebSClientService.client;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "服务与活动成功断开");
        }
    };



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        listView = view.findViewById(R.id.chatmsg_listView);
        imageLeft = view.findViewById(R.id.ear_left);
        imageRight = view.findViewById(R.id.ear_right);
        btnSend = view.findViewById(R.id.btn_voice_or_text);
        etContent = view.findViewById(R.id.et_content);
        mContext= ((MainActivity) getActivity());
        //启动服务
        startJWebSClientService_my();
        //绑定服务
        bindService_my();
        //注册广播
        doRegisterReceiver();



     //   ((MainActivity) getActivity()).changeNavigationSelected(R.id.nav_chat);
        //register receiver
        // 20200524 经测试没有触发到事件，索性注释掉，改用eventbus的做法


       /* imageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageTabPressed(HearingAidModel.Side.Left);

            }
        });

        imageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageTabPressed(HearingAidModel.Side.Right);
            }
        });

        */
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = etContent.getText().toString();
                String jsonstr = "textmsg:"+content;
                send_wsmsg(jsonstr);

                ChatMessage chatMessage=new ChatMessage();
                chatMessage.setContent(content);
                chatMessage.setIsMeSend(1);
                chatMessage.setIsRead(1);
                chatMessage.setTime(System.currentTimeMillis()+"");
                chatMessageList.add(chatMessage);
                initChatMsgListView();
                etContent.setText("");


            }
        });

        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setContent("正在进行验配... ");
        chatMessage.setIsMeSend(0);
        chatMessage.setIsRead(1);
        chatMessage.setTime(System.currentTimeMillis()+"");
        chatMessageList.add(chatMessage);
        initChatMsgListView();

        //发送当前active memory, active sysmeory 的数据，
        HearingAidModel ha0 = null;
        HearingAidModel ha1 = null;
        ha0 = Configuration.instance().getDescriptor(HearingAidModel.Side.Left);
        ha1 = Configuration.instance().getDescriptor(HearingAidModel.Side.Right);

        if (( ha0 ==null) && (ha1==null)) {
            addChatlist("警告，您的助听器没有连接");

        } else {
            if ((ha0 != null) && (ha1 != null)) {
                addChatlist("警告，您同时连接了两个助听器，当前版本只支持单个助听器验配，为您选择了左耳助听器");
                side = HearingAidModel.Side.Left;
            }
            if ((ha0 == null) && (ha1 != null)) side = HearingAidModel.Side.Right;
            if ((ha0 != null) && (ha1 == null)) side = HearingAidModel.Side.Left;


            if (Configuration.instance().isHAAvailable(side)) {
                if (!getHearingAidModel(side).isConfigured) {
                    initializeSDKParameters = new InitializeSDKParameters(side, DETECT_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                //    check_parameterspace();
                    initializeSDKParameters = new InitializeSDKParameters(side, READ_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
            }
        }




        return view;
    }
    private void initChatMsgListView(){
        adapter_chatMessage = new Adapter_ChatMessage( mContext, chatMessageList);
        listView.setAdapter(adapter_chatMessage);
        listView.setSelection(chatMessageList.size());
    }
    private void imageTabPressed(HearingAidModel.Side side) {
        String content ="返回数据，DEMO...";
        if (client != null && client.isOpen()) {
            jWebSClientService.sendMsg(content);

            //暂时将发送的消息加入消息列表，实际以发送成功为准（也就是服务器返回你发的消息时）
            ChatMessage chatMessage=new ChatMessage();
            chatMessage.setContent(content);
            chatMessage.setIsMeSend(1);
            chatMessage.setIsRead(1);
            chatMessage.setTime(System.currentTimeMillis()+"");
            chatMessageList.add(chatMessage);
            initChatMsgListView();

        } else {
            Toast.makeText(mContext, "连接失败", Toast.LENGTH_LONG).show();
        }

    }
    private EventReceiver<WsmessageEvent> WsmessageEventHandler = new EventReceiver<WsmessageEvent>() {
        @Override
        public void onEvent(String name, WsmessageEvent data) {
            //disable this function
        }
    };

    private class ChatMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message=intent.getStringExtra("message");

            /*
            ChatMessage chatMessage=new ChatMessage();
            chatMessage.setContent(message);
            chatMessage.setIsMeSend(0);
            chatMessage.setIsRead(1);
            chatMessage.setTime(System.currentTimeMillis()+"");
            chatMessageList.add(chatMessage);
            initChatMsgListView();

             */

            String strRequestKeyAndValues ="";

            Map<String, String> mapRequest = ParseUrl.parseURLParam(message);

            for(String strRequestKey: mapRequest.keySet()) {
                String strRequestValue=mapRequest.get(strRequestKey);
                strRequestKeyAndValues+="key:"+strRequestKey+",Value:"+strRequestValue+";";

            }
            Log.d(TAG,strRequestKeyAndValues);

            process_wsmsg(mapRequest);
        }
    }
    private void addChatlist(String msg) {
        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setContent(msg);
        chatMessage.setIsMeSend(0);
        chatMessage.setIsRead(1);
        chatMessage.setTime(System.currentTimeMillis()+"");
        chatMessageList.add(chatMessage);
        initChatMsgListView();
    }


    private int  process_wsmsg(  Map<String, String> mapRequest) {
      String op = mapRequest.get("op");
      switch (op) {
          case "textmsg":
              process_textmsg(mapRequest);
              break;
          case "activedata":
              addChatlist("正在读取验配参数...");
              process_activedata(mapRequest);
              break;
          case "batch_save_param":
              addChatlist("正在保存验配参数...");
              process_saveparam(mapRequest);
              break;
          case "authres":
              process_authres(mapRequest);

              break;
          case "datalog":
              process_datalog(mapRequest);
              break;
          case "memdata":
              process_memdata(mapRequest);
              break;
          case "save_memdata":

              process_savememdata(mapRequest);
              break;

      }


        return 1;
    }
    private void send_wsmsg(String content) {

        if (client != null && client.isOpen()) {
            jWebSClientService.sendMsg(content);

            //暂时将发送的消息加入消息列表，实际以发送成功为准（也就是服务器返回你发的消息时）
          /*  String content2 ="";
            ChatMessage chatMessage=new ChatMessage();
            chatMessage.setContent(content2);
            chatMessage.setIsMeSend(1);
            chatMessage.setIsRead(1);
            chatMessage.setTime(System.currentTimeMillis()+"");
            chatMessageList.add(chatMessage);
            initChatMsgListView();
            */
        } else {
            Toast.makeText(mContext, "连接失败", Toast.LENGTH_LONG).show();
        }

    }

    private int process_authres(Map<String, String> mapRequest) {
        String userid = mapRequest.get("userid");
        String password = mapRequest.get("password");
        Configuration.instance().userid = userid;
        Configuration.instance().password = password;


        return 1;
    }
    private int process_textmsg(Map<String, String> mapRequest) {
        String msg = mapRequest.get("msg");
        msg = msg.replace("/rand/","?rand=");
        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setContent(msg);
        chatMessage.setIsMeSend(0);
        chatMessage.setIsRead(1);
        chatMessage.setTime(System.currentTimeMillis()+"");
        chatMessageList.add(chatMessage);
        initChatMsgListView();
        return 1;

    }
    private int process_datalog(Map<String, String> mapRequest) {
        String str_ear_id = mapRequest.get("ear_id");
        int ear_id = Integer.parseInt(str_ear_id);
        Product product;
        LogType logtype;
        String out_string ="";
        HearingAidModel.Side tmp_side = HearingAidModel.Side.Left;
        if (ear_id == 1 )  tmp_side = HearingAidModel.Side.Right;

        product = Configuration.instance().getDescriptor(tmp_side).product;
        try {
            out_string = product.generateLog(LogType.kUsageTime);

        }catch(ArkException e){
            Log.e(TAG, e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String jsonstr = "datalog:"+out_string;
        send_wsmsg(jsonstr);
        return 1;

    }
    private int process_saveparam(Map<String, String> mapRequest) {
        //忽略memory_idx 参数，也就是memory_idx =-1的情况
        String str_ear_id = mapRequest.get("ear_id");
        int ear_id = Integer.parseInt(str_ear_id);
        String str_mem_idx = mapRequest.get("mem_idx");
        int mem_idx = Integer.parseInt(str_mem_idx);
        JSONArray jsonArray = null;
        //update_info 是一个java 结构
        String update_info = mapRequest.get("info");
        try
        {
            jsonArray = new JSONArray(update_info);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String str_param_id = mapRequest.get("param");
        String str_value = mapRequest.get("value");

        Product product;
        HearingAidModel.Side tmp_side = HearingAidModel.Side.Left;
        if (ear_id == 1 )  tmp_side = HearingAidModel.Side.Right;

        product = Configuration.instance().getDescriptor(tmp_side).product;

        current_ParameterSpace = get_parameterspace(mem_idx);

        ParameterList  parameters = Configuration.instance().getDescriptor(tmp_side).arr_parameters[mem_idx];


        for (int i = 0; i < jsonArray.length(); i++) {
            try {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String tmp_id = jsonObject.getString("name");
            String tmp_val = jsonObject.getString("value");

            Parameter tmp_param = parameters.getById(tmp_id);
            if (tmp_param == null) continue;
            ParameterType tmp_type = tmp_param.getType();

            if (tmp_type == ParameterType.kBoolean) {
                boolean tmp_value = Boolean.parseBoolean(tmp_val);
                tmp_param.setBooleanValue(tmp_value);
            } else if (tmp_type == ParameterType.kDouble) {
                double tmp_value = Double.parseDouble(tmp_val);
                tmp_param.setDoubleValue(tmp_value);

            } else {
                tmp_param.setValue(Integer.parseInt(tmp_val));
            }

            //     initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE,mem_idx).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch(ArkException e){
                Log.e(TAG, e.getMessage());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

       // String jsonstr = "saveparam:ok" ;
       // send_wsmsg(jsonstr);
        return 1;
    }
    private int send_errinfo(int code,int cat,String errinfo) {
        //res_left='{ "result": 0, "errcat": 101,"errinfo": "HiPro Error 202:  Error in OpenComm
        //COM port may be in use by another device. Try to change COMPort= in HIPRO.INI."}';
        Map<String,String> root= new HashMap<String,String>();
        root.put("result",String.valueOf(code));
        root.put("errcat",String.valueOf(cat));

        root.put( "errinfo", errinfo);
        JSONObject jsonObj=new JSONObject(root);

        String jsonstr = "errinfo:"+jsonObj.toString();
        send_wsmsg(jsonstr);
        return 1;
    }
    private int  process_activedata(  Map<String, String> mapRequest) {
        process_eardata(mapRequest);
        process_ha(mapRequest);
        process_sysparam(mapRequest);
        process_frgraph(mapRequest);
      //  process_iograph(mapRequest);
      //  process_noisegraph(mapRequest);

        return 1;
    }
    private int  process_eardata(  Map<String, String> mapRequest) {
        String str_ear_id = mapRequest.get("ear_id");
        int ear_id = Integer.parseInt(str_ear_id);
        HearingAidModel ha = null;
        if (ear_id == 0) {
            ha = Configuration.instance().getDescriptor(HearingAidModel.Side.Left);
        } else {
            ha = Configuration.instance().getDescriptor(HearingAidModel.Side.Right);
        }

        int m_LastOp = 3;
        int m_curAudioMemory = 0;
        Map<String,String> root= new HashMap<String,String>();
        root.put("earid",String.valueOf(ear_id));
        root.put("hwid",hwid);
        root.put( "remotefitting","1");
        root.put( "lastop",String.valueOf(m_LastOp));
        root.put( "active_memindex",String.valueOf(m_curAudioMemory));
        root.put( "active_custid","0");
        root.put( "active_earid","0");
        root.put( "total_channels","16");
        root.put( "total_memories","8");
        root.put( "memory_list","0|1|2|3|4|5|6|7");
        root.put( "display_name","e7160");
        root.put( "prod_name","7160");
        root.put( "page_tmpl","7160");
      //  root.put( "serialno",  ha.serialno);
      //  root.put("macaddress",ha.macaddress);
        root.put( "program_box","jinhaowireless");
        root.put( "enablelog","1");
        root.put( "lib_name","e7160");
        JSONObject jsonObj=new JSONObject(root);

        String jsonstr = "eardata:"+jsonObj.toString();
        send_wsmsg(jsonstr);
        return 1;
    }
    private int  process_frgraph(  Map<String, String> mapRequest) {
        String str_ear_id = mapRequest.get("ear_id");
        String str_mem_idx = mapRequest.get("mem_idx");
        str_mem_idx = "0";
        int ear_id = Integer.parseInt(str_ear_id);
        Product product;

        if (ear_id ==-1)  ear_id=0;

        if (ear_id == 0)  side = HearingAidModel.Side.Left;
        if (ear_id == 1)  side = HearingAidModel.Side.Right;

        GraphDefinitionList m_GraphDefinitionList ;
        GraphDefinition m_GraphDefinition  ;

        Graph m_Graph   = null;
        ParameterList graphSetting  ;
        Parameter  InputlevelSetting   = null;

        try {
            HearingAidModel ha = getHearingAidModel(side);

            if (ha == null) return 1;
            if (ha != null) {
                product = ha.product;


                m_GraphDefinitionList = product.getGraphs();
                m_GraphDefinition = m_GraphDefinitionList.getById(GraphId.kFrequencyResponseGraph
                );

                m_Graph = m_GraphDefinition.createGraph();
                product.setInputSignal(InputSignalType.kPureTone);

                graphSetting = m_Graph.getGraphSettings();
                int total_count = graphSetting.getCount();
                for (int i=0;i<total_count;i++) {
                    Parameter  tmp_pt = graphSetting.getItem(i);
                    Log.d(TAG,tmp_pt.getId());
                }

                InputlevelSetting = graphSetting.getById("InputLevel");
            }


        } catch (ArkException e ) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        int[] arr_levels = { 50, 65, 80 };
        String res ="[]";

        String s;
        int  inputlevel;

        double output;

        short chancount;

        String tmpstr = "";
        String  str_fr = "";
        JSONArray jsonArray = new JSONArray();



	    int BUF_LEN = 200;

	    int FREQDATA_LEN = 64;  //原来是80
        double[] freqpoints = new double[FREQDATA_LEN];
        double[] frequencyresponse = new double[FREQDATA_LEN];
        for (int j = 1; j <= FREQDATA_LEN; j++)
        {
            freqpoints[j-1] =  (125 * j);
        }
        for (int index = 0; index < 3; index++) {
            inputlevel = arr_levels[index];
            try {
                InputlevelSetting.setDoubleValue(inputlevel);
                m_Graph.setDomain(FREQDATA_LEN,freqpoints);
                m_Graph.calculatePoints(FREQDATA_LEN, frequencyresponse);

                // Copy 80 tones, evenly spaced from 100 Hz to 8000 Hz, to the clipboard.
                String resptext = "";
                str_fr = "";

                DecimalFormat df = new DecimalFormat("0.00");
                for (int i = 1; i <= FREQDATA_LEN; i++)
                {
                    tmpstr = df.format( frequencyresponse[i - 1]);

                    if (i < FREQDATA_LEN)
                        tmpstr +="|";

                    str_fr += tmpstr;
                }

                try {

                    JSONObject Person = new JSONObject();
                    Person.put("level", inputlevel);
                    Person.put("value", str_fr);
                    jsonArray.put(index,Person);
                } catch (JSONException e) {

                }


            } catch (ArkException e) {

            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

        JSONObject root = new JSONObject();
        try {
            root.put("ear_id", String.valueOf(ear_id));
            root.put("mem_idx", str_mem_idx);
            root.put("frgraph",jsonArray);
        }catch (JSONException e) {
            return -1;
        }

        res = root.toString();
        String jsonstr = "frgraph:"+res;
        send_wsmsg(jsonstr);
        return 1;
    }
    private int  process_iograph(  Map<String, String> mapRequest) {
        String str_ear_id = mapRequest.get("ear_id");
        int ear_id = Integer.parseInt(str_ear_id);
        Product product;

        if (ear_id ==-1)  ear_id=0;

        if (ear_id == 0)  side = HearingAidModel.Side.Left;
        if (ear_id == 1)  side = HearingAidModel.Side.Right;

        GraphDefinitionList m_GraphDefinitionList ;
        GraphDefinition m_GraphDefinition  ;

        Graph m_Graph   = null;
        ParameterList graphSetting  ;
        Parameter  InputlevelSetting   = null;

        try {
            HearingAidModel ha = getHearingAidModel(side);

            if (ha == null) return 1;
            if (ha != null) {
                product = ha.product;

                m_GraphDefinitionList = product.getGraphs();
                m_GraphDefinition = m_GraphDefinitionList.getById(GraphId.kFrequencyResponseGainGraph
                );

                m_Graph = m_GraphDefinition.createGraph();
                product.setInputSignal(InputSignalType.kPureTone);

                graphSetting = m_Graph.getGraphSettings();
                int total_count = graphSetting.getCount();
                for (int i=0;i<total_count;i++) {
                    Parameter  tmp_pt = graphSetting.getItem(i);
                    Log.d(TAG,tmp_pt.getId());
                }

                InputlevelSetting = graphSetting.getById("InputLevel");
            }


        } catch (ArkException e ) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        int[] arr_levels = { 50, 65, 80 };
        String res ="[]";

        String s;
        int  inputlevel;

        double output;

        short chancount;

        String tmpstr = "";
        String  str_fr = "";
        JSONArray jsonArray = new JSONArray();
        int BUF_LEN = 200;

        int FREQDATA_LEN = 64;  //原来是80
        double[] freqpoints = new double[FREQDATA_LEN];
        double[] frequencyresponse = new double[FREQDATA_LEN];
        for (int j = 1; j <= FREQDATA_LEN; j++)
        {
            freqpoints[j-1] =  (125 * j);
        }
        for (int index = 0; index < 3; index++) {
            inputlevel = arr_levels[index];
            try {
                InputlevelSetting.setDoubleValue(50);
                m_Graph.setDomain(FREQDATA_LEN,freqpoints);
                m_Graph.calculatePoints(FREQDATA_LEN, frequencyresponse);

                // Copy 80 tones, evenly spaced from 100 Hz to 8000 Hz, to the clipboard.
                String resptext = "";
                str_fr = "";

                DecimalFormat df = new DecimalFormat("0.00");
                for (int i = 1; i <= FREQDATA_LEN; i++)
                {
                    tmpstr = df.format( frequencyresponse[i - 1]);

                    if (i < FREQDATA_LEN)
                        tmpstr +="|";

                    str_fr += tmpstr;
                }

                try {
                    JSONObject Person = new JSONObject();
                    Person.put("level", inputlevel);
                    Person.put("value", str_fr);
                    jsonArray.put(index,Person);
                } catch (JSONException e) {

                }


            } catch (ArkException e) {

            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

        res = jsonArray.toString();
        String jsonstr = "iograph:"+res;
        send_wsmsg(jsonstr);
        return 1;

    }
    private int  process_noisegraph(  Map<String, String> mapRequest) {

        return 1;
    }
    private int  process_ha(  Map<String, String> mapRequest) {
        String str_ear_id = mapRequest.get("ear_id");
        String str_mem_idx = mapRequest.get("mem_idx");
        String info = mapRequest.get("info");
        int total_count = 0;
        str_mem_idx ="-1";

        String[] arr_selected_parms  = new String[1];
        if (info !="") {
            arr_selected_parms = info.split("\\|");
            total_count = arr_selected_parms.length;
        }

        int ear_id = Integer.parseInt(str_ear_id);



        if (ear_id ==-1)  ear_id=0;

        if (ear_id == 0)  side = HearingAidModel.Side.Left;
        if (ear_id == 1)  side = HearingAidModel.Side.Right;

        HearingAidModel ha = getHearingAidModel(side);
        if (ha == null)
            return 1;

        int mem_idx = Integer.parseInt(str_mem_idx);
        int m_LastOp = 3;
        int m_curAudioMemory = 0;
        if (mem_idx ==-1) {
             mem_idx = current_memory_idx;
        }



        ParameterList  parameters = ha.arr_parameters[mem_idx];
        IndexedList arr_list;
        IndexedTextList arr_textlist;

        JSONObject root= new JSONObject();
        try {
            root.put("ear_id", String.valueOf(ear_id));
            root.put("mem_idx", String.valueOf(mem_idx));

            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < total_count; i++) {
                String tmp_parmname = arr_selected_parms[i];
                JSONObject obj_parm = new JSONObject();
                try {
                    //Parameter tmp_pt = getParameter(side, mem_idx, "X_EQ_ChannelGain_dB[" + i + "]");
                    Parameter tmp_pt = parameters.getById(tmp_parmname);
                    if (tmp_pt ==null)  continue;

                 //   obj_parm.put("id", tmp_pt.getId());
                    obj_parm.put("name", tmp_pt.getId());
                    obj_parm.put("min", tmp_pt.getMin());
                    obj_parm.put("max", tmp_pt.getMax());
                    obj_parm.put("value", tmp_pt.getValue());
                    obj_parm.put("units", tmp_pt.getUnits());
                    JSONArray json_arrlist = new JSONArray();
                    try {
                        arr_list = tmp_pt.getListValues();
                        if (arr_list != null) {
                            int list_count = arr_list.getCount();
                            double[] arr_dlist = new double[list_count];
                            for (int list_i = 0; list_i < list_count; list_i++)
                                json_arrlist.put(list_i, arr_list.getItem(list_i));


                            obj_parm.put("arr_list", json_arrlist);
                        }
                    } catch (ArkException e) {

                    }
                    try {
                        arr_textlist = tmp_pt.getTextListValues();
                        if (arr_textlist != null) {
                            int list_count = arr_textlist.getCount();

                            for (int list_i = 0; list_i < list_count; list_i++)
                                json_arrlist.put(list_i, arr_textlist.getItem(list_i));


                            obj_parm.put("arr_textlist", json_arrlist);
                        }
                    }catch (ArkException e) {

                    }

                    jsonArray.put(obj_parm);


                } catch (ArkException e) {
                    Log.d(TAG,e.getMessage());
                    continue;
                }



            }
            root.put("param", jsonArray);


        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        String jsonstr = "param:"+ root.toString();
        send_wsmsg(jsonstr);


        return 1;
    }

    private  int process_memdata( Map<String, String> mapRequest)  {
        String str_ear_id = mapRequest.get("ear_id");
        String str_mem_idx = mapRequest.get("mem_idx");
        String info = mapRequest.get("info");
        int total_count = 0;

        String[] arr_selected_parms  = new String[1];
        if (info !="") {
            arr_selected_parms = info.split("\\|");
            total_count = arr_selected_parms.length;
        }

        int ear_id = Integer.parseInt(str_ear_id);

        if (ear_id ==-1)  ear_id=0;

        if (ear_id == 0)  side = HearingAidModel.Side.Left;
        if (ear_id == 1)  side = HearingAidModel.Side.Right;

        HearingAidModel ha = getHearingAidModel(side);
        if (ha == null) {
            send_errinfo(0,255,"NO CONNECTED HA");
            return 1;
        }

        int mem_idx = Integer.parseInt(str_mem_idx);
        int m_LastOp = 3;
        int m_curAudioMemory = 0;
        if (mem_idx ==-1) {
            check_parameterspace();
            mem_idx = current_memory_idx;

        }
        ParameterList parameters = null;
        if ((mem_idx ==-2) || (mem_idx ==-3)) {
            parameters  = ha.systemParameters;
        } else {
           parameters = ha.arr_parameters[mem_idx];
           addChatlist("查询助听器参数");
        }

        JSONObject root= new JSONObject();
        try {
            root.put("ear_id", String.valueOf(ear_id));
            root.put("mem_idx", String.valueOf(mem_idx));
            String str_ids = "";
            String str_values = "";


            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < total_count; i++) {
                String tmp_parmname = arr_selected_parms[i];
                JSONObject obj_parm = new JSONObject();
                try {

                    Parameter tmp_pt = parameters.getById(tmp_parmname);
                    if (tmp_pt ==null)  continue;
                    int tmp_val = tmp_pt.getValue();
                    String tmp_id = tmp_pt.getId();

                    str_ids +="|"+tmp_id;
                    str_values +="|"+tmp_val;


                } catch (ArkException e) {
                    Log.d(TAG,e.getMessage());
                    continue;
                }
            }
            root.put("ids",str_ids);
            root.put("values",str_values);


            // root.put("param", jsonArray);


        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        String jsonstr = "memdata:"+ root.toString();
        send_wsmsg(jsonstr);
        return 1;


    }

    private int process_savememdata(Map<String, String> mapRequest) {
        //忽略memory_idx 参数，也就是memory_idx =-1的情况
        String str_ear_id = mapRequest.get("ear_id");
        int ear_id = Integer.parseInt(str_ear_id);
        String str_mem_idx = mapRequest.get("mem_idx");
        int mem_idx = Integer.parseInt(str_mem_idx);
        JSONArray jsonArray = null;
        //update_info 是一个java 结构
        String update_info = mapRequest.get("info");
        try
        {
            jsonArray = new JSONArray(update_info);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String str_param_id = mapRequest.get("param");
        String str_value = mapRequest.get("value");

        Product product;
        HearingAidModel.Side tmp_side = HearingAidModel.Side.Left;
        if (ear_id == 1 )  tmp_side = HearingAidModel.Side.Right;

        HearingAidModel ha = getHearingAidModel(tmp_side);
        if (ha == null) {
            send_errinfo(0,255,"NO CONNECTED HA");
            return 1;
        }

        product = ha.product;
        //-3 -2 -1


        current_ParameterSpace =  get_parameterspace(mem_idx);
        ParameterList parameters = ha.parameters;
        if (mem_idx ==-3 ) {
            parameters= Configuration.instance().getDescriptor(tmp_side).systemParameters;

        }
        if (mem_idx == -2) {
            parameters= Configuration.instance().getDescriptor(tmp_side).systemParameters;
        }
        if (mem_idx ==-1) {
            check_parameterspace();
            parameters = Configuration.instance().getDescriptor(tmp_side).arr_parameters[current_memory_idx];
        }
        if (mem_idx >= 0 && mem_idx < 16) {

            addChatlist("保存助听器参数");
            parameters = Configuration.instance().getDescriptor(tmp_side).arr_parameters[mem_idx];
        }



        for (int i = 0; i < jsonArray.length(); i++) {
            String target_paramname ="";

            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String tmp_id = jsonObject.getString("name");
                String tmp_val = jsonObject.getString("value");
                target_paramname = tmp_id;

                Parameter tmp_param = parameters.getById(tmp_id);
                if (tmp_param == null) continue;
                ParameterType tmp_type = tmp_param.getType();

                if (tmp_type == ParameterType.kBoolean) {
                    boolean tmp_value = Boolean.parseBoolean(tmp_val);
                    tmp_param.setBooleanValue(tmp_value);
                } else if (tmp_type == ParameterType.kDouble) {
                    double tmp_value = Double.parseDouble(tmp_val);
                    tmp_param.setDoubleValue(tmp_value);

                } else {
                    tmp_param.setValue(Integer.parseInt(tmp_val));
                }

                //     initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE,mem_idx).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } catch(ArkException e){
                Log.e(TAG, target_paramname+":"+ e.getMessage());
                send_errinfo(0,200,target_paramname+":"+ e.getMessage());
            }
            catch (Exception e)
            {
                e.printStackTrace();

            }
        }

        initializeSDKParameters = new InitializeSDKParameters(side, WRITE_TO_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return 1;
    }

    private int  process_sysparam(  Map<String, String> mapRequest) {
        String str_ear_id = mapRequest.get("ear_id");
        String str_mem_idx = mapRequest.get("mem_idx");
        int ear_id = Integer.parseInt(str_ear_id);

        String info = mapRequest.get("info");
        int total_count = 0;


        String[] arr_selected_parms  = new String[1];
        if (info !="") {
            arr_selected_parms = info.split("\\|");
            total_count = arr_selected_parms.length;
        }


        if (ear_id ==-1)  ear_id=0;

        if (ear_id == 0)  side = HearingAidModel.Side.Left;
        if (ear_id == 1)  side = HearingAidModel.Side.Right;

        HearingAidModel ha = getHearingAidModel(side);
        if (ha == null) return 1;
        IndexedList arr_list;
        IndexedTextList arr_textlist;

        ParameterList  parameters = ha.systemParameters;


        JSONObject root= new JSONObject();
        try {
            root.put("ear_id", String.valueOf(ear_id));

            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < total_count; i++) {
                JSONObject obj_parm = new JSONObject();
                try {
                    String tmp_parmname = arr_selected_parms[i];
                    Parameter tmp_pt = parameters.getById(tmp_parmname);
                    if (tmp_pt == null ) continue;

                    obj_parm.put("name", tmp_pt.getId());
                    obj_parm.put("min", tmp_pt.getMin());
                    obj_parm.put("max", tmp_pt.getMax());
                    obj_parm.put("value", tmp_pt.getValue());
                    obj_parm.put("units", tmp_pt.getUnits());

                    JSONArray json_arrlist = new JSONArray();
                    try {
                        arr_list = tmp_pt.getListValues();
                        if (arr_list != null) {
                            int list_count = arr_list.getCount();

                            for (int list_i = 0; list_i < list_count; list_i++)
                                json_arrlist.put(list_i, arr_list.getItem(list_i));


                            obj_parm.put("arr_list", json_arrlist);
                        }
                    } catch (ArkException e ) {

                    }
                    try {
                        arr_textlist = tmp_pt.getTextListValues();
                        if (arr_textlist != null) {
                            int list_count = arr_textlist.getCount();

                            for (int list_i = 0; list_i < list_count; list_i++)
                                json_arrlist.put(list_i, arr_textlist.getItem(list_i));


                            obj_parm.put("arr_textlist", json_arrlist);
                        }

                    }catch (ArkException e) {

                    }
                    jsonArray.put(obj_parm);

                } catch (ArkException e) {

                }



            }
            root.put("sysparam", jsonArray);


        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        String jsonstr = "sysparam:"+ root.toString();
        send_wsmsg(jsonstr);


        return 1;
    }

    private int  process_memfunc(  Map<String, String> mapRequest,int op ) {
        String str_ear_id = mapRequest.get("ear_id");
        String str_mem_idx = mapRequest.get("mem_idx");
        String str_mem_idx2 = mapRequest.get("mem_idx2");

        int ear_id = Integer.parseInt(str_ear_id);
        int mem_idx = Integer.parseInt(str_mem_idx);
        int mem_idx2 = Integer.parseInt(str_mem_idx2);

        Product product;
        HearingAidModel.Side tmp_side = HearingAidModel.Side.Left;
        if (ear_id == 1 )  tmp_side = HearingAidModel.Side.Right;

        product = Configuration.instance().getDescriptor(tmp_side).product;

        current_ParameterSpace =  get_parameterspace(mem_idx);


        if (op ==201) {
            //reset factory
        }
        if (op ==202) {
            //switch ear and memory,要不要执行下读操作
            try {
                product.setCurrentMemory(mem_idx);
            }catch (ArkException e) {

            }

        }
        if (op ==203) {
            //copy memory

        }
        if (op ==204) {
            //reload memory
        }
        if (op == 205) {
            //revert back
        }
        if (op == 206) {
            //burn memory ,这个要测试,改为同步
            isBusy = true;
            try {
               product.writeParameters(current_ParameterSpace);
            }catch (ArkException e) {

            }
            Configuration.instance().showMessage("Burn to Device Complete", getActivity());
            isBusy = false;

        }

        return 1;
    }




    private HearingAidModel getHearingAidModel(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
    }


    private class InitializeSDKParameters extends AsyncTask<Void, Integer, Void> {
        AsyncResult res;
        DeviceInfo deviceInfo;
        private HearingAidModel.Side side;
        private CommunicationAdaptor communicationAdaptor;
        private Product product;
        private int command;

        InitializeSDKParameters(HearingAidModel.Side side, int command) {
            this.side = side;
            this.command = command;


            communicationAdaptor = Configuration.instance().getDescriptor(this.side).communicationAdaptor;
            product = Configuration.instance().getDescriptor(this.side).product;

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                switch (command) {
                    case DETECT_DEVICE:
                        res = communicationAdaptor.beginDetectDevice();
                        break;
                    case INITIALIZE_DEVICE:
                        res = product.beginInitializeDevice(communicationAdaptor);
                        break;
                    case READ_DEVICE:
                        res = product.beginReadParameters(current_ParameterSpace);
                        break;
                    case WRITE_TO_DEVICE:
                        res = product.beginWriteParameters(current_ParameterSpace);
                        break;
                    case BURN_TO_DEVICE:
                        res = product.beginWriteParameters(current_ParameterSpace);
                        break;
                }
                isBusy = true;
                while (!res.isIsFinished()) {
                    publishProgress(res.getProgressValue());
                }
                res.getResult();

            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());

            }
            return null;

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            switch (command) {
                case DETECT_DEVICE:
                  //  progressBarTextView.setText(getString(R.string.msg_param_progress_detect) + " " + side.name() + ".....");
                    break;
                case INITIALIZE_DEVICE:
                 //   progressBarTextView.setText(R.string.msg_param_progress_init);
                    break;
                case READ_DEVICE:
                  //  progressBarTextView.setText(R.string.msg_param_progress_reading);
                    break;
                case WRITE_TO_DEVICE:
                  //  progressView(View.VISIBLE);
                  //  progressBarTextView.setText("Writing to Device");
                    break;
                case BURN_TO_DEVICE:
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                switch (command) {
                    case DETECT_DEVICE:
                        deviceInfo = communicationAdaptor.endDetectDevice(res);
                        initializeSDKParameters = new  InitializeSDKParameters(side, INITIALIZE_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case INITIALIZE_DEVICE:
                        product.endInitializeDevice(res);
                        initializeSDKParameters = new  InitializeSDKParameters(side, READ_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        break;
                    case READ_DEVICE:
                        Configuration.instance().showMessage("Initialization Complete", getActivity());
                        isBusy = false;
                        getHearingAidModel(side).productInitialized = true;
                        getHearingAidModel(side).isConfigured = true;
                        break;
                    case WRITE_TO_DEVICE:
                        Configuration.instance().showMessage("参数成功写入助听器", getActivity());
                        isBusy = false;

                        break;
                    case BURN_TO_DEVICE:
                        Configuration.instance().showMessage("Burn to Device Complete", getActivity());
                        isBusy = false;
                        int ear_id=0;
                        if (side == HearingAidModel.Side.Left)  ear_id=0;
                        if (side == HearingAidModel.Side.Right)  ear_id=1;

                        JSONObject root= new JSONObject();
                        try {
                            root.put("earid", String.valueOf(ear_id));

                            root.put("status", "Burn to Device Complete");

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String jsonstr = "status:"+ root.toString();
                        send_wsmsg(jsonstr);

                        break;

                }
            } catch (ArkException e) {
                Log.e(TAG, e.getMessage());
            }
        }



        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }


    }




}
