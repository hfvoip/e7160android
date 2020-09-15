package com.jhearing.e7160sl.Connection.ScanningDevice;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jhearing.e7160sl.R;

import java.util.Collection;
import java.util.List;


public class BlEAdapter extends ArrayAdapter<BLEDeviceWrapper> {


    public BlEAdapter(@NonNull Context context, @NonNull List<BLEDeviceWrapper> objects) {
        super(context, 0, objects);
    }


    @Override
    public void add(@Nullable BLEDeviceWrapper object) {
        super.add(object);

    }

    @Override
    public void addAll(@NonNull Collection<? extends BLEDeviceWrapper> collection) {
        super.addAll(collection);
    }

    @Override
    public void addAll(BLEDeviceWrapper... items) {
        super.addAll(items);
    }

    @Override
    public void insert(@Nullable BLEDeviceWrapper object, int index) {
        super.insert(object, index);
    }

    @Override
    public void remove(@Nullable BLEDeviceWrapper object) {
        super.remove(object);
    }


    @Nullable
    @Override
    public BLEDeviceWrapper getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        BLEDeviceWrapper bleDeviceWrapper = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_element, parent, false);
        }

        TextView macAddress = convertView.findViewById(R.id.address);
        TextView name = convertView.findViewById(R.id.name);
        TextView rssi = convertView.findViewById(R.id.rssi);
        String address = bleDeviceWrapper.getAddress();
        if (!address.equals(null))
            macAddress.setText(address);
        name.setText(bleDeviceWrapper.getName());
        rssi.setText(bleDeviceWrapper.getRssi() + "");

        return convertView;
    }


}