package cn.aura.feimayun.bean;

import java.util.List;

/**
 * 描述：我的学习-我的班级
 */
public class MyStuidesInfo3Bean {

    /**
     * status : 1
     * msg : success
     * data : [{"lessons":"PMP®远程","timer":"本班课程学习已结束"},{"lessons":"PMP®远程,live test","timer":"本班课程学习已结束"},{"id":"377","name":"PMP远程过期测试班","stime":"2018-10-05","etime":"2018-11-30","lessons":"PMP®远程","timer":"离班级课程结束还有22天"},{"id":"96","name":"PMP远程1101内部测试班","stime":"2018-10-18","etime":"2018-11-30","lessons":"PMP®远程","timer":"离班级课程结束还有22天"}]
     */

    private int status;
    private String msg;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * lessons : PMP®远程
         * timer : 本班课程学习已结束
         * id : 377
         * name : PMP远程过期测试班
         * stime : 2018-10-05
         * etime : 2018-11-30
         */

        private String lessons;
        private String timer;
        private String id;
        private String name;
        private String stime;
        private String etime;

        public String getLessons() {
            return lessons;
        }

        public void setLessons(String lessons) {
            this.lessons = lessons;
        }

        public String getTimer() {
            return timer;
        }

        public void setTimer(String timer) {
            this.timer = timer;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStime() {
            return stime;
        }

        public void setStime(String stime) {
            this.stime = stime;
        }

        public String getEtime() {
            return etime;
        }

        public void setEtime(String etime) {
            this.etime = etime;
        }
    }
}
