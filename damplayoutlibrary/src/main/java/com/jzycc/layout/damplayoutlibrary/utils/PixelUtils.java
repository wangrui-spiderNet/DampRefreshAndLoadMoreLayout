package com.jzycc.layout.damplayoutlibrary.utils;

import android.content.Context;

/**
 * author Jzy(Xiaohuntun)
 * date 18-9-18
 */
public class PixelUtils {
    public static int dp2px(Context context, float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }
}
