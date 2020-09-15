package com.jhearing.e7160sl.adapter;


/**
 * Created by Administrator on 2016/4/25.
 */
public class BaseResponse {

    private int flag;
    private String message;
    /**
     * data : {"msg":"用户名或密码错误"}
     * status : -1
     */

    private int status;


    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public static class DataBean {

        /**
         * msg : 用户名或密码错误
         */


    }
}
