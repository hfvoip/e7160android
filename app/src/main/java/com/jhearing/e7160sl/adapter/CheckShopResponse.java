package com.jhearing.e7160sl.adapter;

import java.util.List;

/**
 * Create by dongli
 * Create date 2020/8/5
 * desc：
 */
public class CheckShopResponse extends BaseResponse{


    /**
     * total_count : 30
     * per_page : 10
     * page_num : 2
     * items : [{"id":21,"lat":"112","lng":"25.0","name":"店名21","address":"地址21","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":22,"lat":"112","lng":"25.0","name":"店名22","address":"地址22","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":23,"lat":"112","lng":"25.0","name":"店名23","address":"地址23","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":24,"lat":"112","lng":"25.0","name":"店名24","address":"地址24","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":25,"lat":"112","lng":"25.0","name":"店名25","address":"地址25","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":26,"lat":"112","lng":"25.0","name":"店名26","address":"地址26","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":27,"lat":"112","lng":"25.0","name":"店名27","address":"地址27","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":28,"lat":"112","lng":"25.0","name":"店名28","address":"地址28","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":29,"lat":"112","lng":"25.0","name":"店名29","address":"地址29","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1},{"id":30,"lat":"112","lng":"25.0","name":"店名30","address":"地址30","content":"简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内","featured":1}]
     */

    private int total_count;
    private int per_page;
    private String page_num;
    private List<ItemsBean> items;

    public int getTotal_count() {
        return total_count;
    }

    public void setTotal_count(int total_count) {
        this.total_count = total_count;
    }

    public int getPer_page() {
        return per_page;
    }

    public void setPer_page(int per_page) {
        this.per_page = per_page;
    }

    public String getPage_num() {
        return page_num;
    }

    public void setPage_num(String page_num) {
        this.page_num = page_num;
    }

    public List<ItemsBean> getItems() {
        return items;
    }

    public void setItems(List<ItemsBean> items) {
        this.items = items;
    }

    public static class ItemsBean {
        /**
         * id : 21
         * lat : 112
         * lng : 25.0
         * name : 店名21
         * address : 地址21
         * content : 简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍简要介绍 联系方式0311-1234567，3行字以内
         * featured : 1
         */

        private int id;
        private String lat;
        private String lng;
        private String name;
        private String address;
        private String content;
        private int featured;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getFeatured() {
            return featured;
        }

        public void setFeatured(int featured) {
            this.featured = featured;
        }
    }
}
