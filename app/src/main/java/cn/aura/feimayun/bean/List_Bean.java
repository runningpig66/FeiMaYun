package cn.aura.feimayun.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class List_Bean implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Map<String, String>> list;
    private Map<String, String> map;
    private List<String> stringList;
    private List<String[]> listString;

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public List<String[]> getListString() {
        return listString;
    }

    public void setListString(List<String[]> listString) {
        this.listString = listString;
    }

    public List<Map<String, String>> getList() {
        return list;
    }

    public void setList(List<Map<String, String>> list) {
        this.list = list;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}
