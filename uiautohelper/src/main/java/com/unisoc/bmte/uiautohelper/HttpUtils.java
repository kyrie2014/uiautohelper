package com.unisoc.bmte.uiautohelper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


/**
 * Created by kyrie.liu on 2018/8/13.
 */

public class HttpUtils {
   
    /**
     * post方式请求服务器(https协议)
     *
     * @param url
     *            请求地址
     * @param content
     *            参数
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws IOException
     */
    public static String post(String url, String sign, String content)
            throws Exception {
        URL console = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) console.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(true);
        //文档要求填写的Header参数
        conn.addRequestProperty("Content-Type", "text/json");
        conn.setRequestProperty("Authorization", sign);
        conn.connect();
        DataOutputStream out = new DataOutputStream(conn.getOutputStream());
        out.write(content.getBytes("UTF-8"));
        // 刷新、关闭
        out.flush();
        out.close();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
            throw new Exception("Request url failed ...");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null)
            result += getLine;
        in.close();
        return result;
    }

}
