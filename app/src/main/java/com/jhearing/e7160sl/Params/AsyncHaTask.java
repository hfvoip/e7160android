package com.jhearing.e7160sl.Params;

import android.os.AsyncTask;
import android.util.Log;

import com.ark.ArkException;
import com.ark.AsyncResult;
import com.ark.CommunicationAdaptor;
import com.ark.DeviceInfo;
import com.ark.ParameterSpace;
import com.ark.Product;
import com.jhearing.e7160sl.HA.Configuration;
import com.jhearing.e7160sl.HA.HearingAidModel;




public  class AsyncHaTask extends AsyncTask<Void, Integer, Void> {
    private static final String TAG = AsyncHaTask.class.getSimpleName();
    AsyncResult res;
    DeviceInfo deviceInfo;
    private HearingAidModel.Side side;
    private CommunicationAdaptor communicationAdaptor;
    private Product product;
    private int command;
    private ParameterSpace tmp_ps ;

    private static final int DETECT_DEVICE = 0;
    private static final int INITIALIZE_DEVICE = 1;
    private static final int READ_DEVICE = 2;
    private static final int READ_DEVICE_SYS = 4;
    private static final int WRITE_TO_DEVICE = 3;


    private HearingAidModel getHearingAidModel(HearingAidModel.Side side) {

        return Configuration.instance().getDescriptor(side);
    }
    AsyncHaTask(HearingAidModel.Side side, int command) {
        this.side = side;
        this.command = command;
        communicationAdaptor = Configuration.instance().getDescriptor(this.side).communicationAdaptor;
        product = Configuration.instance().getDescriptor(this.side).product;


    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            tmp_ps = getHearingAidModel(side).wirelessControl.getCurrentMemory();
            switch (command) {
                case DETECT_DEVICE:
                    res = communicationAdaptor.beginDetectDevice();
                    break;
                case INITIALIZE_DEVICE:
                    res = product.beginInitializeDevice(communicationAdaptor);
                    break;
                case READ_DEVICE:
                    res = product.beginReadParameters(tmp_ps);
                    break;
                case READ_DEVICE_SYS:
                    res = product.beginReadParameters(ParameterSpace.kSystemActiveMemory);
                    break;
                case WRITE_TO_DEVICE:
                    res = product.beginWriteParameters(tmp_ps);
                    break;
            }

            if (res ==null) {
                Log.e(TAG,"启动异步任务失败,任务类型:"+command);
                return null;
            }
            while (!res.isIsFinished()) {
                publishProgress(res.getProgressValue());
            }
            res.getResult();

        } catch (ArkException e) {


        }
        return null;

    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        switch (command) {
            case DETECT_DEVICE:

                break;
            case INITIALIZE_DEVICE:

                break;
            case READ_DEVICE:


                break;
            case READ_DEVICE_SYS:

                break;

            case WRITE_TO_DEVICE:


                break;
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (res == null ) {
            //      Configuration.instance().showMessage("启动异步任务失败", getActivity());
            //        progressDlg.hide();
            return ;
        }
        try {
            switch (command) {
                case DETECT_DEVICE:
                    deviceInfo = communicationAdaptor.endDetectDevice(res);
                     new  AsyncHaTask(side, INITIALIZE_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case INITIALIZE_DEVICE:
                    product.endInitializeDevice(res);
                     new  AsyncHaTask(side, READ_DEVICE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    break;
                case READ_DEVICE:
                     new  AsyncHaTask(side, READ_DEVICE_SYS).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                    break;
                case READ_DEVICE_SYS:

                    getHearingAidModel(side).productInitialized = true;
                    getHearingAidModel(side).isConfigured = true;

                case WRITE_TO_DEVICE:

                    break;
            }
        } catch (ArkException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        //     progressBar.setProgress(values[0]);
    }


}
