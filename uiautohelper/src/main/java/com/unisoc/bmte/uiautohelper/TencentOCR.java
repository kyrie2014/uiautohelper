package com.unisoc.bmte.uiautohelper;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.List;

import static com.unisoc.bmte.uiautohelper.Common.print;
import static com.unisoc.bmte.uiautohelper.Common.takeScreenshot;

/**
 * Created by kyrie.liu on 2018/8/23.
 */

public class TencentOCR {


    public static List getUisByOcr(String text) throws Exception {

        if(!Common.isNetConnection())
            throw new Exception("Currently no data service!!");
        takeScreenshot("ocrshot");
        StringBuffer path = new StringBuffer(Environment.getExternalStorageDirectory().getPath());
        path.append("/screenshot/ocrshot.png");
        String result = getTextFromImage(path.toString());
        JSONObject ocrResult = new JSONObject(result);
        List<UI> uis = new ArrayList();
        JSONArray items = ocrResult.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            int sameCount = 0;
            JSONObject item = items.getJSONObject(i);
            String itemString = item.getString("itemstring");
            if (itemString.contains(text)) {
                print("itemString-->" + itemString);
                JSONObject itemcoord = item.getJSONObject("itemcoord");
                UI ui = new UI(itemcoord.getInt("x"), itemcoord.getInt("y"),
                        itemcoord.getInt("x") + itemcoord.getInt("width"),
                        itemcoord.getInt("y") + itemcoord.getInt("height"),
                        itemcoord.getInt("width"), itemcoord.getInt("height"));
                uis.add(ui);
            }
        }
        return uis;
    }
    /**
     * 查找指定text的UI位置
     *
     * @param text 需要查找的text
     * @return 返回UI位置
     */
    public static UI getUiByOcr(String text) throws Exception {
        List<UI> uilist = getUisByOcr(text);
        if(!uilist.isEmpty())
            return uilist.get(0);
        return null;
    }

    public static UI getUiByOcr(String text, int index) throws Exception {
        List<UI> uilist = getUisByOcr(text);
        if(!uilist.isEmpty())
            return uilist.get(index);
        return null;
    }

    /**
     * 识别图中文字信息
     *
     * @param imagePath 图片路径
     * @return 返回ocr识别结果
     */
    public static String getTextFromImage(String imagePath) throws Exception {
        if (TextUtils.isEmpty(imagePath))
            return null;
        long expired = System.currentTimeMillis() / 1000 + 2592000;
        //得到Authorization
        String sign = TencentSign.appSign(YouTuHttpContants.APP_ID,
                YouTuHttpContants.SECRET_ID,
                YouTuHttpContants.SECRET_KEY,
                expired);
        String image = image2Base64(imagePath);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("app_id", String.valueOf(YouTuHttpContants.APP_ID));
        jsonObject.put("session_id", "");
        jsonObject.put("image", image);
        String result = HttpUtils.post(YouTuHttpContants.OCR_URL, sign,
                jsonObject.toString());
        return result;
    }

    /**
     * 将图中进行base64格式编码
     *
     * @param imagePath 图片路径
     * @return
     */
    public static String image2Base64(String imagePath) {

        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(imagePath);
            data = new byte[is.available()];
            is.read(data);
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (IOException e) {
            print(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static class UI {

        private int x1;
        private int y1;
        private int x2;
        private int y2;
        private int width;
        private int height;
        private String text;

        public UI(int x1, int y1, int x2, int y2, int width, int height) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.width = width;
            this.height = height;
            this.text = null;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void click() {
            int x = x1 + width / 2;
            int y = y1 + height / 2;
            print("Click: [%d,%d]", x, y);
            Shell.SH.run(String.format("input tap %d %d", x, y));
        }
    }
}
