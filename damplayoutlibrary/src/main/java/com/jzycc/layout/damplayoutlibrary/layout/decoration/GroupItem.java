package com.jzycc.layout.damplayoutlibrary.layout.decoration;

import java.util.HashMap;
import java.util.Map;

/**
 * author yuzhouxu
 * date 2018/11/12.
 */
public class GroupItem {
    private int startPosition;//起始position
    private Map<String,Object> dataMap;
    private int type;

    public GroupItem(int startPosition){
        this.startPosition = startPosition;
        dataMap = new HashMap<>();
    }

    public GroupItem(int startPosition, int type) {
        this.startPosition = startPosition;
        this.type = type;
        dataMap = new HashMap<>();
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setData(String key,Object data){
        dataMap.put(key,data);
    }

    public Object getData(String key){
        return dataMap.get(key);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
