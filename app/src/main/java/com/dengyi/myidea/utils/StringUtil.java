package com.dengyi.myidea.utils;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by deng on 2015/11/27.
 */
public class StringUtil {
    /**
     * 中文乱码
     *
     * 暂时解决大部分的中文乱码 但是还有部分的乱码无法解决 .
     *
     * 如果您有好的解决方式 请联系我
     * 我会很乐意向您请教 谢谢您
     *
     * @return
     */
    public static final String recode(String str) {
        String formart = "";

        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder()
                    .canEncode(str);
            if (ISO) {
                formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
                Log.i("1234      ISO8859-1", formart);
            } else {
                formart = str;
                Log.i("1234      stringExtra", str);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return formart;
    }
}
