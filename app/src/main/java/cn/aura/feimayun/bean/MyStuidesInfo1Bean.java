package cn.aura.feimayun.bean;

import java.util.List;

/**
 * 描述：我的学习-我的视频
 */
public class MyStuidesInfo1Bean {

    /**
     * status : 1
     * msg : success
     * data : {"teach_type":3,"pkid":832,"data":[{"name":"直播1029","lid":"880","bg_url":"https://img01.feimayun.com/wx/manage/defset/2018/2018-09/20180913090837_78838_288x152.jpg","start_ts":"2018-10-30 09:37:00","end_ts":"2018-10-30 13:05:00","webinar_id":"884621680","type":1,"liveStatus":5,"stat":"可查看回放"},{"lid":"826","name":"PMP录播","bg_url":"https://img01.feimayun.com/wx/manage/defset/2018/2018-09/20180913090837_78838_288x152.jpg","type":2,"total":2,"learned":2,"rate":"100%","stat":1,"typer":"继续观看"}]}
     */

    private int status;
    private String msg;
    private DataBeanX data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBeanX getData() {
        return data;
    }

    public void setData(DataBeanX data) {
        this.data = data;
    }

    public static class DataBeanX {
        /**
         * teach_type : 3
         * pkid : 832
         * data : [{"name":"直播1029","lid":"880","bg_url":"https://img01.feimayun.com/wx/manage/defset/2018/2018-09/20180913090837_78838_288x152.jpg","start_ts":"2018-10-30 09:37:00","end_ts":"2018-10-30 13:05:00","webinar_id":"884621680","type":1,"liveStatus":5,"stat":"可查看回放"},{"lid":"826","name":"PMP录播","bg_url":"https://img01.feimayun.com/wx/manage/defset/2018/2018-09/20180913090837_78838_288x152.jpg","type":2,"total":2,"learned":2,"rate":"100%","stat":1,"typer":"继续观看"}]
         */

        private int teach_type;
        private int pkid;
        private List<DataBean> data;

        public int getTeach_type() {
            return teach_type;
        }

        public void setTeach_type(int teach_type) {
            this.teach_type = teach_type;
        }

        public int getPkid() {
            return pkid;
        }

        public void setPkid(int pkid) {
            this.pkid = pkid;
        }

        public List<DataBean> getData() {
            return data;
        }

        public void setData(List<DataBean> data) {
            this.data = data;
        }

        public static class DataBean {
            /**
             * name : 直播1029
             * lid : 880
             * bg_url : https://img01.feimayun.com/wx/manage/defset/2018/2018-09/20180913090837_78838_288x152.jpg
             * start_ts : 2018-10-30 09:37:00
             * end_ts : 2018-10-30 13:05:00
             * webinar_id : 884621680
             * type : 1
             * liveStatus : 5
             * stat : 可查看回放
             * total : 2
             * learned : 2
             * rate : 100%
             * typer : 继续观看
             */

            private String name;
            private String lid;
            private String bg_url;
            private String start_ts;
            private String end_ts;
            private String webinar_id;
            private int type;
            private int liveStatus;
            private String stat;
            private int total;
            private int learned;
            private String rate;
            private String typer;
            //面授加的字段
            private String address;
            private int teach_type;
            private String lesson_time;

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public int getTeach_type() {
                return teach_type;
            }

            public void setTeach_type(int teach_type) {
                this.teach_type = teach_type;
            }

            public String getLesson_time() {
                return lesson_time;
            }

            public void setLesson_time(String lesson_time) {
                this.lesson_time = lesson_time;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getLid() {
                return lid;
            }

            public void setLid(String lid) {
                this.lid = lid;
            }

            public String getBg_url() {
                return bg_url;
            }

            public void setBg_url(String bg_url) {
                this.bg_url = bg_url;
            }

            public String getStart_ts() {
                return start_ts;
            }

            public void setStart_ts(String start_ts) {
                this.start_ts = start_ts;
            }

            public String getEnd_ts() {
                return end_ts;
            }

            public void setEnd_ts(String end_ts) {
                this.end_ts = end_ts;
            }

            public String getWebinar_id() {
                return webinar_id;
            }

            public void setWebinar_id(String webinar_id) {
                this.webinar_id = webinar_id;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }

            public int getLiveStatus() {
                return liveStatus;
            }

            public void setLiveStatus(int liveStatus) {
                this.liveStatus = liveStatus;
            }

            public String getStat() {
                return stat;
            }

            public void setStat(String stat) {
                this.stat = stat;
            }

            public int getTotal() {
                return total;
            }

            public void setTotal(int total) {
                this.total = total;
            }

            public int getLearned() {
                return learned;
            }

            public void setLearned(int learned) {
                this.learned = learned;
            }

            public String getRate() {
                return rate;
            }

            public void setRate(String rate) {
                this.rate = rate;
            }

            public String getTyper() {
                return typer;
            }

            public void setTyper(String typer) {
                this.typer = typer;
            }
        }
    }
}
