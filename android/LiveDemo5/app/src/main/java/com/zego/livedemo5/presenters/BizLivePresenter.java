package com.zego.livedemo5.presenters;


import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.zego.livedemo5.ZegoApiManager;
import com.zego.livedemo5.ZegoApplication;
import com.zego.livedemo5.interfaces.OnUpdateRoomListListener;
import com.zego.livedemo5.utils.PreferenceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * des: 为了更好的展示zego的直播技术, 我们开发了简单的业务测功能, 主要用于管理用户信息、 流信息,
 * 类似于zego客户的业务逻辑.
 */
public class BizLivePresenter {

    private static BizLivePresenter sInstance;

    private OnUpdateRoomListListener mUpdateRoomListListener;

    /**
     * 线程池.
     */
    private ExecutorService mExecutorService;


    private BizLivePresenter() {
        mExecutorService = Executors.newFixedThreadPool(4);
    }

    public static BizLivePresenter getInstance() {
        if (sInstance == null) {
            synchronized (BizLivePresenter.class) {
                if (sInstance == null) {
                    sInstance = new BizLivePresenter();
                }
            }
        }
        return sInstance;
    }

    /**
     * 设置房间列表监听.
     *
     * @param updateRoomListListener
     */
    public void setUpdateRoomListListener(OnUpdateRoomListListener updateRoomListListener) {
        mUpdateRoomListListener = updateRoomListListener;
    }


    /**
     * 获取房间列表.
     */
    public void getRoomList() {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                RequestQueue mQueue = Volley.newRequestQueue(ZegoApplication.sApplicationContext);
                long appID = ZegoApiManager.getInstance().getAppID();
                String domain = ZegoApiManager.getInstance().isInternationalProduct(appID) ? "zegocloud.com" : "zego.im";
                String url = String.format("https://liveroom%d-api.%s/demo/roomlist?appid=%s", appID, domain, appID);
                StringRequest request = new StringRequest(url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Gson gson = new Gson();
                                RoomInfoEx roomInfoEx = gson.fromJson(response, RoomInfoEx.class);

                                if(roomInfoEx != null && roomInfoEx.data != null){
                                    if(mUpdateRoomListListener != null){
                                        mUpdateRoomListListener.onUpdateRoomList(roomInfoEx.data.room_list);
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                mQueue.add(request);
            }
        });
    }
}