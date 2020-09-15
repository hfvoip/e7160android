package com.jhearing.e7160sl.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


import com.jhearing.e7160sl.R;


public class DialogInput extends Dialog {

    public DialogInput(Context context, int theme) {
        super(context, theme);
    }

    public DialogInput(Context context) {
        super(context);
    }

    public static class Builder {
        private Context context;
        private TextView left_btn, right_btn;
        private OnClickListener leftClickListener, rightClickListener;
        private OnRangeListener rangeListener;
        private EditText rangeEt;


        public Builder(Context context) {
            this.context = context;
        }


        public Builder setLeftClickListener(OnClickListener listener) {
            this.leftClickListener = listener;
            return this;
        }

        public Builder setRightClickListener(OnClickListener listener) {
            this.rightClickListener = listener;
            return this;
        }

        public Builder setRangeListener(OnRangeListener rangeListener) {
            this.rangeListener = rangeListener;
            return this;
        }

        public DialogInput create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final DialogInput dialog = new DialogInput(context, R.style.dialog);
            dialog.getWindow().setBackgroundDrawable(null);
            View layout = inflater.inflate(R.layout.view_dialog_input, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            rangeEt = ((EditText) layout.findViewById(R.id.range_et));
            left_btn = ((TextView) layout.findViewById(R.id.cancel_tv));
            right_btn = ((TextView) layout.findViewById(R.id.confirm_tv));

            if (leftClickListener != null) {
                left_btn.setVisibility(View.VISIBLE);
                left_btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        leftClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                });
            }


            if (rightClickListener != null) {
                right_btn.setVisibility(View.VISIBLE);
                right_btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String trim = rangeEt.getText().toString().trim();
                        if (!TextUtils.isEmpty(trim)){
                            if (Integer.parseInt(trim) >= 0 && Integer.parseInt(trim) <= 60 ){
                                if (rangeListener != null){
                                    rangeListener.getRange(trim);
                                    rightClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                                    dialog.dismiss();
                                }
                                dialog.dismiss();
                            }else {
                             //   ToastUtils.show("输入格式有误");
                            }

                        }else {
                           // ToastUtils.show("输入不能为空");
                        }

                    }
                });
            }




            dialog.setContentView(layout);
            return dialog;
        }

        public interface OnRangeListener{
            void getRange(String value);
        }

    }



}

