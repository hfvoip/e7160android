package com.jhearing.e7160sl.Tools;

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import android.text.TextWatcher;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ark.AsyncResult;


import com.jhearing.e7160sl.model.shopInfo;

import com.jhearing.e7160sl.MainActivity;
import com.jhearing.e7160sl.R;

import com.jhearing.e7160sl.adapter.ShopAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NearshopFragment  extends Fragment     {

    private static final String TAG = UsageFragment.class.getSimpleName();

    private RecyclerView checkShopRv;
    private SwipeRefreshLayout checkShopSfrv;
    private ShopAdapter shopAdapter;
    private int page_num = 0;
    private boolean  loading = false;
    private boolean isRefresh = false;

    private List<shopInfo> shoplist = new ArrayList<>();
    private EditText shopNameTv;
    private String shopname;

    public NearshopFragment() {
        // Required empty public constructor
    }



    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nearshop, container, false);


       shopNameTv  = view.findViewById((R.id.shop_name_tv));
        checkShopRv = view.findViewById(R.id.check_shop_rv);

        ((MainActivity) getActivity()).changeNavigationSelected(R.id.nav_Param);

          initView();



        return view;
    }

    protected void initView() {


        checkShopRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        shopAdapter = new ShopAdapter();
        checkShopRv.setAdapter(shopAdapter);
        page_num=0;
        new FetchUrlTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        shopAdapter.addData(shoplist);
        shopAdapter.notifyDataSetChanged();
        checkShopRv.addOnScrollListener( new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //如果page_num >3  ,不执行了,也可通过其他方法判断，例如items为空则不执行了
                if (page_num >=3)  return ;
                if( !loading && !recyclerView.canScrollVertically(1)){
                    loading = true;
                    page_num++;

                    new FetchUrlTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }


        });
        shopNameTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                shopname = s.toString();
                page_num = 0;
                new FetchUrlTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });




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
            shopAdapter.addData(shoplist);
            shopAdapter.notifyDataSetChanged();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }
        private void load_jsondata() {
            String baseurl = "http://www.jhearing.com/ajax_shops.php?lat=112&lng=25.0&page_num"+page_num+"&shopname="+shopname;
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(baseurl)
                        .build();

                Response responses = null;

                responses = client.newCall(request).execute();
                String jsonData = responses.body().string();

                JSONObject Jobject = new JSONObject(jsonData);

                JSONArray Jarray = Jobject.getJSONArray("items");

                for (int i = 0; i < Jarray.length(); i++) {

                    JSONObject object = Jarray.getJSONObject(i);
                    shopInfo tmpinfo = new shopInfo();
                    tmpinfo.setTitle(object.getString("name"));
                    tmpinfo.setAddress(object.getString("address"));
                    tmpinfo.setContent(object.getString("content"));
                    shoplist.add(tmpinfo);
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            loading = false;
        }



    }







}
