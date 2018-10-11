package com.unisoc.bmte.uiautohelper;
import android.os.SystemClock;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.unisoc.bmte.uiautohelper.Common.mDevice;
import static com.unisoc.bmte.uiautohelper.Common.print;

/**
 * Created by kyrie.liu on 2018/8/24.
 * <br>
 * uiautomator优化
 *
 * @version 1.0
 */
public class UiAutoDump {

    static final String PATH = "/sdcard/uixml/";
    ArrayList<UI> uiList;
    ArrayList<Integer> point;


    public UiAutoDump() {

        File file = new File("/sdcard/uixml");
        if (!file.exists())
            file.mkdir();
        else
            Shell.SH.run("rm -r /sdcard/uixml/*.xml");
    }

    private static Document parse(String fileName, boolean isCovered) throws UiAutoDumpException {
        String filePath = PATH + fileName;
        File file = new File(filePath);
        if (!file.exists() || isCovered) {
            int i = 3;
            do {
                try {
                    mDevice.dumpWindowHierarchy(file);
                    break;
                } catch (IOException e) {
                    print(e.getMessage());
                }
                i--;
            } while (i > 0);
        }
        Document document;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(file);
        } catch (DocumentException e) {
            throw new UiAutoDumpException(e.getMessage());
        }
        return document;
    }

    public UI getUiByAttr(String value) throws UiAutoDumpException {
        return getUiByAttr(value, 0, true);
    }

    public UI getUiByAttr(String value, boolean isCovered) throws UiAutoDumpException {
        return getUiByAttr(value, 0, isCovered);
    }

    public UI getUiByAttr(String value, int index, boolean isCovered) throws UiAutoDumpException {
        uiList = new ArrayList();
        String fileName = value.replaceAll("\\W*", "");
        if(fileName.length() > 15)
            fileName = value.substring(0,15);
        Element node = parse(fileName + ".xml", isCovered).getRootElement();
        List<UI> list = getUIsByAttr(node, value);
        return list.size() > 0 ? list.get(index) : null;
    }

    public ArrayList<UI> getUIsByAttr(Element node, String value) {
        point = new ArrayList();
        boolean flag = false;
        boolean isScrollable = false;
        boolean isClickable = false;
        String bounds = null;
        String targetValue;
        // 获取当前节点的所有属性节点
        List<Attribute> list = node.attributes();

        // 遍历属性节点
        for (Attribute attr : list) {
            if (attr.getName().equals("bounds")) {
                bounds = attr.getValue();
            }
            if (attr.getName().equals("clickable")) {
                isClickable = Boolean.valueOf(attr.getValue());
            }
            if (attr.getName().equals("scrollable")) {
                isScrollable = Boolean.valueOf(attr.getValue());
            }
            targetValue = attr.getValue();
            if (targetValue.contains(value)) {
                print("Find attrubite \"%s\"!!!", targetValue);
                flag = true;
            }
        }
        if (flag & bounds != null) {
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(bounds);
            while (matcher.find()) {
                point.add(Integer.parseInt(matcher.group()));
            }
            uiList.add(new UI(point.get(0), point.get(1), point.get(2), point.get(3),
                    isClickable,
                    isScrollable));
            point = null;
        }
        // 当前节点下面子节点迭代器
        for (Iterator<Element> it = node.elementIterator(); it.hasNext(); ) {
            // 获取某个子节点对象
            Element element = it.next();
            // 对子节点进行遍历
            getUIsByAttr(element, value);
        }
        return uiList;
    }

    public static class UI {

        private int x1;
        private int y1;
        private int x2;
        private int y2;
        public boolean isClickable;
        public boolean isScrollable;


        public UI(int x1, int y1, int x2, int y2, boolean isClickable, boolean isScrollable) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.isClickable = isClickable;
            this.isScrollable = isScrollable;
        }

        public void click() throws DocumentException {
            int x = x1 + (x2 - x1) / 2;
            int y = y1 + (y2 - y1) / 2;
            print("Click: [%d,%d]", x, y);
            try {
                mDevice.executeShellCommand(String.format("input tap %d %d", x, y));
            } catch (IOException e) {
                throw new DocumentException("Click failed!!");
            }
            SystemClock.sleep(500);
        }

        public void swipe(int x, int y) throws DocumentException {
            int x0 = x1 + (x2 - x1) / 2;
            int y0 = y1 + (y2 - y1) / 2;
            print("Swipe to: [%d,%d]", x0, y0);
            try {
                mDevice.executeShellCommand(String.format("input tap %d %d", x0, y0));
            } catch (IOException e) {
                throw new DocumentException("Swipe failed!!");
            }
            SystemClock.sleep(500);
        }
    }
}