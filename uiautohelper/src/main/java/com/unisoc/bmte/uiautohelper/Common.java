package com.unisoc.bmte.uiautohelper;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.Direction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;

import com.bmte.modemtest.itestapi.ModemITestAPI;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by kyrie.liu on 2018/6/21.
 */

public class Common {
    static Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    public static Context context = InstrumentationRegistry.getTargetContext();
    public static UiDevice mDevice = UiDevice.getInstance(instrumentation);
    static TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    static ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    public final static int VERTICAL = 0;
    public final static int HORIZONTAL = 1;
    public final static int ACTION = 2;
    public final static int ACTIVITY = 3;
    private final static int FDD_MAX_BAND = 25;
    private final static int FLING_STEPS = 10;
    private final static int MAX_SEARCHE_SWIPES = 30;
    public final static String KEY_LTE_OPT = "lte_operator";
    public final static String KEY_LTE_TYPE = "lte_type";
    public final static String KEY_EARFCN_DL = "earfcn_dl";
    public final static String GOFU_GMS_BUILD = "gofu_gms";
    public final static String GMS_BUILD = "gms";
    public final static String NATIVE_BUILD = "native";
    private final static String SYSTEM_PROPERTIE = "android.os.SystemProperties";
    private final static String TDD = "TDD_LTE";
    private final static String FDD = "FDD_LTE";
    private final static String ITEST_PAC = "com.spreadtrum.itestapp";
    private final static String CMD_GET_SERVING = "SendATCmd AT+SPENGMD=0,6,0 ";


    /**
     * 屏幕解锁，只能解锁滑动锁屏方式
     * <p>This method requires the caller to hold the permission
     * {@link android.Manifest.permission#DISABLE_KEYGUARD}.
     */
    public static void unlockScreen() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.PARTIAL_WAKE_LOCK, "aprdtest");
        wl.acquire(50);
        try {
            mDevice.executeShellCommand("wm dismiss-keyguard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断屏幕是否被锁定
     */
    public final static boolean isScreenLocked() {
        KeyguardManager mKeyguardManager =
                (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return mKeyguardManager.isKeyguardLocked();
    }

    /**
     * 设置一个系统闹钟
     *
     * @参数 minutes  设置闹铃时间，多少分钟后振铃
     */
    public static void setAlarm(int minutes) throws ParseException {
        if (!DateFormat.is24HourFormat(context)) {
            print("Current time format is 12h,set time to 24h format!");
            Settings.System.putString(context.getContentResolver(),
                    Settings.System.TIME_12_24, "24");
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, minutes);
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "sprdtest");
        intent.putExtra(AlarmClock.EXTRA_DAYS, calendar.get(Calendar.DAY_OF_MONTH));
        intent.putExtra(AlarmClock.EXTRA_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
        intent.putExtra(AlarmClock.EXTRA_MINUTES, calendar.get(Calendar.MINUTE));
        context.startActivity(intent);
        print("Will Alert after %d mins, time: %s", minutes, format.format(calendar.getTime()));
    }

    /**
     * 取消系统闹钟
     *
     */
    public static void cancelAlarm() {
        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "sprdtest");
        context.startActivity(intent);
    }

    /**
     * 设置飞行模式
     *
     * @参数 isEnabled true代表设置飞行模式，false取消飞行模式
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void setAirplaneMode(boolean isEnabled) {
        print("setAirplaneModeOn: " + isEnabled);

        if (isAirplane() && isEnabled) {
            print("AirplaneMode open already");
            return;
        }
        if (!isAirplane() && !isEnabled) {
            print("AirplaneMode close already");
            return;
        }
        if (Build.VERSION.RELEASE.contains("4.4")) {
            Settings.Secure.putInt(context.getContentResolver(), "radio_operation", 1);
        }
        Settings.Global.putInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
                isEnabled ? 1 : 0);
    }


    /**
     * 判断当前是否为飞行模式
     *
     * @返回值 false：当前不是飞行模式，true：当前为飞行模式
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isAirplane() {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * 判断网络情况
     *
     * @返回值 false：表示没有网络， true：表示有网络
     */
    public static boolean isAvalibleNetwork() {
        if (connMgr == null) {
            return false;
        }
        NetworkInfo[] net_info = connMgr.getAllNetworkInfo();
        if (net_info != null) {
            for (int i = 0; i < net_info.length; i++) {
                if (net_info[i].getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置休眠时间
     *
     * @参数 second 休眠时间
     */
    public static void setScreenSleepTime(int second) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                    second * 1000);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    /**
     * 设置系统日期
     *
     * @参数 year 需要设置的年份
     * @参数 month 需要设置的月份
     * @参数 day 需要设置的天数
     */
    public static void setSysYear(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        //  long when = Math.max(c.getTimeInMillis(), MIN_DATE);
        long when = c.getTimeInMillis();
        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

    /**
     * 修改语言为指定语言
     *
     * @参数 language 需要设置语言的类型{@link Language}
     */
    @TargetApi(Build.VERSION_CODES.N)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setSysLanguage(Language language) {
        Locale locale = null;
        switch (language) {
            case CHINA:
                locale = Locale.CHINA;
                break;
            case PRC:  //繁体中文
                locale = Locale.PRC;
                break;
            case US:
                locale = Locale.US;
                break;
            case UK:
                locale = Locale.UK;
                break;
            case JAPAN:
                locale = Locale.JAPAN;
                break;
            case KOREA:
                locale = Locale.KOREA;
                break;
            case FRANCE:
                locale = Locale.FRANCE;
                break;
        }
        LocaleList localeList = new LocaleList(locale);
        if (!localeList.isEmpty()) {
            try {
                Class activityManagerNative = Class.forName("android.app.ActivityManagerNative");
                Method getDefault = activityManagerNative.getDeclaredMethod("getDefault");
                Object objIActivityManager = getDefault.invoke(activityManagerNative);
                Class iActivityManager = Class.forName("android.app.IActivityManager");
                Method getConfiguration = iActivityManager.getDeclaredMethod("getConfiguration");
                Configuration config = (Configuration) getConfiguration.invoke(objIActivityManager);
                config.setLocales(localeList);
                Class[] clzParams = {Configuration.class};
                Method updateConfiguration = iActivityManager.getDeclaredMethod("updatePersistentConfiguration", clzParams);
                updateConfiguration.invoke(objIActivityManager, config);
            } catch (Exception e) {
                print("changeSystemLanguage: " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * 情景模式中，设置指定文件为来电铃声
     *
     * @参数 path 音频文件路径
     */
    public void setRingtone(String path) {
        File sdfile = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, sdfile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, sdfile.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
        values.put(MediaStore.Audio.Media.IS_ALARM, false);
        values.put(MediaStore.Audio.Media.IS_MUSIC, false);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(sdfile.getAbsolutePath());
        Uri newUri = context.getContentResolver().insert(uri, values);
        RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, newUri);
        print("设置铃声成功：%s", path);
    }

    /**
     * 设备屏幕截图
     *
     * @参数 picName  截图命名
     */
    public static void takeScreenshot(String picName) {
        StringBuffer path = new StringBuffer(Environment.getExternalStorageDirectory().getPath());
        path.append("/screenshot/");
        File Dir = new File(path.toString());
        if (!Dir.exists()) {
            Dir.mkdir();
        }
        path.append(picName + ".png");
        mDevice.takeScreenshot(new File(path.toString()));
    }

    /**
     * 等待匹配给定标准文本值的UI控件
     *
     * @参数 text    UI控件所属的text值的子字符串
     * @参数 timeout 等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByTextContains(String text, long timeout) {
        return mDevice.wait(Until.findObject(By.textContains(text)), timeout);
    }

    public static boolean isExist(Typer typer, String regex) {
        boolean flag = false;
        Pattern pattern;
        switch (typer) {
            case TEXTCONTAINS:
                pattern = Pattern.compile("\\S*\\s*" + regex + "\\S*\\s*");
                flag = mDevice.hasObject(By.text(pattern));
                break;
            case DESCCONTAINS:
                pattern = Pattern.compile("\\S*\\s*" + regex + "\\S*\\s*");
                flag = mDevice.hasObject(By.desc(pattern));
                break;
            case TEXT:
                pattern = Pattern.compile(regex);
                flag = mDevice.hasObject(By.text(pattern));
                break;
            case DESC:
                pattern = Pattern.compile(regex);
                flag = mDevice.hasObject(By.desc(pattern));
                break;
            case RESID:
                pattern = Pattern.compile(regex);
                flag = mDevice.hasObject(By.res(pattern));
                break;
            case CLAZZ:
                pattern = Pattern.compile(regex);
                flag = mDevice.hasObject(By.clazz(pattern));
                break;
        }
        return flag;
    }

    /**
     * 等待正则匹配到同时满足给定ResourceId和Description值的UI控件
     *
     * @参数 resId    UI控件所属的ResourceId值的正则字符串
     * @参数 desc     UI控件所属的Description值的正则字符串
     * @参数 timeout  等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByResIdDesc(String resid, String desc, long timeout) {
        return mDevice.wait(Until.findObject(
                By.res(Pattern.compile("\\S*\\s*" + resid + "\\S*\\s*")).desc(
                        Pattern.compile("\\S*\\s*" + desc + "\\S*\\s*"))), timeout);
    }


    /**
     * 等待匹配到给定标准text值的UI控件
     *
     * @参数 text    UI控件所属的文本值的全字符串
     * @参数 timeout 等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */

    public static UiObject2 findByText(String text, long timeout) {
        return findByText(Pattern.compile("\\S*\\s*" + text + "\\S*\\s*"), timeout);
    }

    /**
     * 等待正则匹配到给定部分text值的UI控件
     *
     * @参数 regex    UI控件所属的部分text值的正则匹配模式
     * @参数 timeout 等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */

    public static UiObject2 findByText(Pattern regex, long timeout) {
        return mDevice.wait(Until.findObject(By.text(regex)), timeout);
    }

    /**
     * 等待匹配到给定className值的UI控件
     *s
     * @参数 className    UI控件所属的标准className值字符串
     * @参数 instance     查找clas对象序列标号
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByClazz(String clazz, int instance, long timeout) {
        List<UiObject2> obj = mDevice.wait(Until.findObjects(By.clazz(clazz)), timeout);
        return obj != null && obj.size() >= instance ? obj.get(instance) : null;
    }

    /**
     * 等待匹配指定位置下符合className值条件的UI控件
     *
     * @参数 className    UI控件所属的标准className值字符串
     * @参数 instance     匹配到的符合类名条件的所有UI控件列表的位置索引
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByResId(String res, int instance, long timeout) {
        List<UiObject2> obj = mDevice.wait(Until.findObjects(By.res(res)), timeout);
        return obj != null && obj.size() >= instance ? obj.get(instance) : null;
    }

    /**
     * 等待匹配指定位置下符合text值条件的UI控件
     *
     * @参数 text       UI控件所属的标准text值字符串
     * @参数 instance   匹配到的符合类名条件的所有UI控件列表的位置索引
     * @参数 timeout    等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByText(String text, int instance, long timeout) {
        List<UiObject2> obj = mDevice.wait(Until.findObjects(By.text(text)), timeout);
        return obj != null && obj.size() >= instance ? obj.get(instance) : null;
    }

    /**
     * 等待匹配给定className值条件的UI控件
     *
     * @参数 className    UI控件所属的标准className值字符串
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByClazz(String clazz, long timeout) {
        return findByClazz(Pattern.compile("\\S*\\s*" + clazz + "\\S*\\s*"), timeout);
    }

    /**
     * 等待匹配给定className值条件的UI控件
     *
     * @参数 className    UI控件所属的标准className值字符串
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByClazz(Pattern regex, long timeout) {
        return mDevice.wait(Until.findObject(By.clazz(regex)), timeout);
    }

    /**
     * 等待匹配同时满足给定的className值和text值的UI控件
     *
     * @参数 className    UI控件所属的classname值字符串
     * × @参数 text         UI控件所属的text值的子字符串
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByText(String clazz, String text, long timeout) {
        return mDevice.wait(Until.findObject(By.clazz(clazz).text(text)), timeout);
    }

    /**
     * 等待匹配给定的description值的UI控件
     *
     * @参数 desc         UI控件所属的description值字符串
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByDesc(String desc, long timeout) {
        return findByDesc(Pattern.compile("\\S*\\s*" + desc + "\\S*\\s*"), timeout);
    }

    /**
     * 等待正则模式匹配给定的部分description值的UI控件
     *
     * @参数 regex        UI控件所属的部分description值的正则匹配模式
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByDesc(Pattern regex, long timeout) {
        return mDevice.wait(Until.findObject(By.desc(regex)), timeout);
    }

    /**
     * 等待匹配到同时满足给定标准className和description值的UI控件
     *
     * @参数 className    UI控件所属的className值的字符串
     * × @参数 desc         I控件所属的description值字符串
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByDesc(String clazz, String desc, long timeout) {
        return mDevice.wait(Until.findObject(By.clazz(clazz).
                desc(Pattern.compile("\\S*\\s*" + desc + "\\S*\\s*"))), timeout);
    }


    /**
     * 等待匹配给定的标准的Resource-Id值的UI控件
     *
     * @参数 resId        UI控件所属的标准Resource-Id值的字符串
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByResId(String resId, long timeout) {
        return findByResId(Pattern.compile("\\S*\\s*" + resId + "\\S*\\s*"), timeout);
    }

    /**
     * 等待匹配同是满足给定的package名和标准的Resource-Id值的UI控件
     *
     * @参数 pkg          UI控件所属的package name的字符串
     * @参数 resId        UI控件所属的标准Resource-Id值的字符串
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByResId(String pkg, String resId, long timeout) {
        return mDevice.wait(Until.findObject(By.res(pkg, resId)), timeout);
    }

    /**
     * 等待正则模式匹配给定的部分Resource-Id值的UI控件
     *
     * @参数 regex        UI控件所属的部分description值的正则匹配模式
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByResId(Pattern regex, long timeout) {
        return mDevice.wait(Until.findObject(By.res(regex)), timeout);
    }

    /**
     * 等待匹配满足标准Resource-Id值和正则text值的UI控件
     *
     * @参数 resId       UI控件所属的Resource-Id值的字符串
     * @参数 text         UI控件所属的text值的子字符串
     * @参数 timeout      等待匹配的最长时间，单位毫秒
     * @返回值 如果匹配到返回对象实例，否则返回空
     */
    public static UiObject2 findByResIdText(String resId, String text, long timeout) {
        return mDevice.wait(Until.findObject(By.res(Pattern.compile("\\S*\\s*" + resId + "\\S*\\s*")).
                text(Pattern.compile("\\S*\\s*" + text + "\\S*\\s*"))), timeout);
    }

    /**
     * 将UI控件滚动至最尾端
     *
     * @参数 direction    滑动方向，垂直{@link #VERTICAL}，横向{@link #HORIZONTAL}
     * @参数 maxSwipes    滑动的步长，即需要多少次滑动到尾端
     * @返回值 如果找到到返回true，否则返回false
     */
    public static boolean scrollToEnd(int direction) {
        try {
            UiScrollable uiScrollable = new UiScrollable(new UiSelector().scrollable(true));
            switch (direction) {
                case VERTICAL:
                    uiScrollable.setAsVerticalList();
                    break;
                case HORIZONTAL:
                    uiScrollable.setAsHorizontalList();
                    break;
            }
            return uiScrollable.scrollToEnd(MAX_SEARCHE_SWIPES, FLING_STEPS);
        } catch (UiObjectNotFoundException e) {
            print(e.getMessage());
        }
        return false;
    }


    /**
     * 将UI控件滚动至最开端
     *
     * @参数 direction    滑动方向，垂直{@link #VERTICAL}，横向{@link #HORIZONTAL}
     * @参数 maxSwipes    滑动的步长，即需要多少次滑动到尾端
     * @返回值 如果找到到返回true，否则返回false
     */
    public static boolean scrollToBeginning(int direction) {
        try {
            UiScrollable uiScrollable = new UiScrollable(new UiSelector().scrollable(true));
            switch (direction) {
                case VERTICAL:
                    uiScrollable.setAsVerticalList();
                    break;
                case HORIZONTAL:
                    uiScrollable.setAsHorizontalList();
                    break;
            }
            return uiScrollable.scrollToBeginning(MAX_SEARCHE_SWIPES, FLING_STEPS);
        } catch (UiObjectNotFoundException e) {
            print(e.getMessage());
        }
        return false;
    }

    /**
     * 滚动匹配标准text值的UI控件
     *
     * @参数 typer        可滚动UI控件类型{@link Typer}
     * @参数 scrollValue  可滚动UI控件的属性值
     * @参数 findText     需要匹配的TEXT值
     * @参数 direction    滑动方向，垂直{@link #VERTICAL}，横向{@link #HORIZONTAL}
     * @返回值 如果找到到返回true，否则返回false
     */
    public static boolean scrollableFind(Typer typer, String scrollValue, String findText, int direction) {
        UiScrollable uiScrollable;
        try {

            switch (typer) {
                case RESID:
                    uiScrollable = new UiScrollable(new UiSelector().resourceId(scrollValue).scrollable(true));
                case DESC:
                    uiScrollable = new UiScrollable(new UiSelector().description(scrollValue).scrollable(true));
                    break;
                case TEXT:
                    uiScrollable = new UiScrollable(new UiSelector().text(scrollValue).scrollable(true));
                    break;
                case CLAZZ:
                    uiScrollable = new UiScrollable(new UiSelector().className(scrollValue).scrollable(true));
                    break;
                default:
                    uiScrollable = new UiScrollable(new UiSelector().scrollable(true));
                    break;
            }

            if (direction == VERTICAL)
                uiScrollable.setAsVerticalList();
            else if (direction == HORIZONTAL)
                uiScrollable.setAsHorizontalList();

            scrollToBeginning(direction);

            return uiScrollable.scrollIntoView(new UiSelector().textMatches("\\S*\\s*" + findText + "\\S*\\s*"));
        } catch (UiObjectNotFoundException e) {
            print(e.getMessage());
        }
        return false;
    }

    /**
     * 滚动匹配标准text值的UI控件
     *
     * @参数 findText     需要查找的TEXT值
     * @参数 direction    滑动方向，垂直{@link #VERTICAL}，横向{@link #HORIZONTAL}
     * @返回值 如果找到到返回true，否则返回false
     */

    public static boolean scrollableFindbyText(String findText, int direction) {
        try {
            UiScrollable uiScrollable = new UiScrollable(new UiSelector().scrollable(true));
            switch (direction) {
                case VERTICAL:
                    uiScrollable.setAsVerticalList();
                    break;
                case HORIZONTAL:
                    uiScrollable.setAsHorizontalList();
                    break;
            }
            scrollToBeginning(direction);
            return uiScrollable.scrollIntoView(new UiSelector().textMatches("\\S*\\s*" + findText + "\\S*\\s*"));
        } catch (UiObjectNotFoundException e) {
            print(e.getMessage());
        }
        return false;
    }

    /**
     * 仅为适配Android P应用列表中查找指定的应用
     *
     * @参数 appName 应用名，即控件属性text
     * @返回值 如果找到到返回true，否则返回false
     */
    public static boolean findApp(String appName){
        UiObject2 scroll = Common.findByResId("apps_list_view", 2000);
        if(scroll.isScrollable()){
            do{
                if(Common.findByText(appName, 1000) != null)
                    return true;
            }while (scroll.fling(Direction.DOWN));
        }
        return false;
    }

    /**
     * 通过app的activity或action启动应用界面
     *
     * @参数 param 启动action或activity对应的参数
     * @参数 action activity或action的字符串
     */
    public static void launchApp(int type, String... value) {
        Intent intent = new Intent();
        switch (type) {
            case ACTION:
                intent.setAction(value[0]);
                break;
            case ACTIVITY:
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName(value[0], value[1]);
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    /**
     * 停止app，并清除缓存
     *
     * @参数 pkg 需要终止app的包名
     */
    public static void clearApp(String pkg) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("pm clear ");
        sBuffer.append(pkg);
        try {
            String stdout = mDevice.executeShellCommand(sBuffer.toString());
            print(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查找UI控件
     *
     * @参数 type   查找控件的类型
     * @参数 regex  查找控件的属性值
     * @参数 timeout 查找控件的超时时间
     * @返回值 查到UI控件对象或者null
     */
    public static UiObject2 find(Typer type, String regex, int timeout) {
        UiObject2 obj = null;
        Pattern pattern;
        switch (type) {
            case DESCCONTAINS:
                pattern = Pattern.compile("\\S*\\s*" + regex + "\\S*\\s*");
                obj = findByDesc(pattern, timeout);
                break;
            case TEXTCONTAINS:
                pattern = Pattern.compile("\\S*\\s*" + regex + "\\S*\\s*");
                obj = findByText(pattern, timeout);
                break;
            case DESC:
                pattern = Pattern.compile(regex);
                obj = findByDesc(pattern, timeout);
                break;
            case TEXT:
                pattern = Pattern.compile(regex);
                obj = findByText(pattern, timeout);
                break;
            case RESID:
                pattern = Pattern.compile(regex);
                obj = findByResId(pattern, timeout);
                break;
            case CLAZZ:
                pattern = Pattern.compile(regex);
                obj = findByClazz(pattern, timeout);
                break;
            default:
                break;
        }
        return obj;
    }

    /**
     * 设置锁屏方式，滑动，pin码，密码，图案，无，目前仅支持Android O项目
     *
     * @参数 pattern LockScreenPattern 锁屏方式
     * @参数 args[] 密码字符串，当设置滑动或无锁时该参数不需要传入，只有设置pin码，密码，图案方式时为数字字符串，数字长度应大于4，
     * 当需要更新密码需要设置两个参数旧密码，新密码，请参考下面示例
     * @返回值 true设置成功，false为失败
     * <p>
     * 示例
     * 无锁：     setLockScreenPattern(LockScreenPattern.UNCLOCK);
     * 滑动：     setLockScreenPattern(LockScreenPattern.SWIPE);
     * 图案：     setLockScreenPattern(LockScreenPattern.LOCK_PATTERN, "1234");
     * 更新图案： setLockScreenPattern(LockScreenPattern.LOCK_PATTERN, "1234"，"2345");
     * 清除图案： setLockScreenPattern(LockScreenPattern.CLEAR, "1234");
     * 密码和pin码设置以及更新，同图案方式
     */
    public static boolean setLockScreenPattern(LockScreenPattern pattern, String... args)
            throws IOException {
        String cmd = null;
        String result = null;
        String pwd;
        switch (pattern) {
            case UNCLOCK:
                cmd = "locksettings set-disabled true";
                result = "Lock screen disabled set to true";
                break;
            case SWIPE:
                cmd = "locksettings set-disabled false";
                result = "Lock screen disabled set to false";
                break;
            case PIN:
                if (args.length > 1 && args[0].length() >= 4 && args[1].length() >= 4) {
                    cmd = String.format("locksettings set-pin --old %s %s", args[0], args[1]);
                    pwd = args[1];
                } else if (args.length == 1 && args[0].length() >= 4) {
                    cmd = "locksettings set-pin " + args[0];
                    pwd = args[0];
                } else {
                    print("The password set pin is null or password length is too short!");
                    return false;
                }
                result = String.format("Pin set to '%s'", pwd);
                break;
            case LOCK_PATTERN:
                if (args.length > 1 && args[0].length() >= 4 && args[1].length() >= 4) {
                    cmd = String.format("locksettings set-pattern --old %s %s", args[0], args[1]);
                    pwd = args[1];
                } else if (args.length == 1 && args[0].length() >= 4) {
                    cmd = "locksettings set-pattern " + args[0];
                    pwd = args[0];
                } else {
                    print("The password set pattern is null or password length is too short!");
                    return false;
                }
                result = String.format("Pattern set to '%s'", pwd);
                break;
            case PASSWORD:
                if (args.length > 1 && args[0].length() >= 4 && args[1].length() >= 4) {
                    cmd = String.format("locksettings set-password --old %s %s", args[0], args[1]);
                    pwd = args[1];
                } else if (args.length == 1 && args[0].length() >= 4) {
                    cmd = "locksettings set-password " + args[0];
                    pwd = args[0];
                } else {
                    print("The password set pattern is null or password length is too short!");
                    return false;
                }
                result = String.format("Pattern set to '%s'", pwd);
                break;
            case CLEAR:
             if (args.length == 1) {
                    cmd = "locksettings clear --old " + args[0];
                } else {
                    print("The password set pattern is null or password length is too short!");
                    return false;
                }
                result = "Lock credential cleared";
                break;
            default:
                if (args.length < 1) {
                    print("The password ed pattern is null!");
                    return false;
                }
                cmd = "locksettings clear --old " + args[0];
                result = "Lock credential ed";
                break;
        }
        if (mDevice.executeShellCommand(cmd).equals(result))
            return true;
        return false;
    }

    public enum LockScreenPattern {
        SWIPE, UNCLOCK, PIN, LOCK_PATTERN, PASSWORD, CLEAR;
    }

    /**
     * 清理所有后台任务
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void clearRecentApps() {
        if (Build.VERSION.SDK_INT < 28) {
            ActivityManager mActivityManager;
            Method mRemoveTask;
            try {

                Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
                Method method = ActivityThread.getMethod("currentActivityThread");
                Object currentActivityThread = method.invoke(ActivityThread);
                Method method2 = currentActivityThread.getClass().getMethod("getApplication");
                Context contextInstance = (Context) method2.invoke(currentActivityThread);
                Class<?> activityManagerClass = Class.forName("android.app.ActivityManager");
                mActivityManager = (ActivityManager) contextInstance.getSystemService(Context.ACTIVITY_SERVICE);
                print(" all recent tasks!!");
                mRemoveTask = activityManagerClass.getMethod("removeTask", int.class);
                mRemoveTask.setAccessible(true);

                List<ActivityManager.RecentTaskInfo> recents =
                        mActivityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
                for (ActivityManager.RecentTaskInfo recent : recents) {
                    ComponentName cn = recent.baseActivity;
                    if (cn == null) continue;
                    String pkg = cn.getPackageName();
                    if (pkg.contains("launcher")) continue;
                    print("--> Clear activity \"%s\"", pkg);
                    mRemoveTask.invoke(mActivityManager, recent.persistentId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                mDevice.pressRecentApps();
                UiObject2 tasks = findByDesc("近期没有任何内容|No recent items", 2000);
                if (tasks == null) {
                    scrollToBeginning(HORIZONTAL);
                    UiObject2 clear = findByText("全部清除|CLEAR ALL", 2000);
                    if (clear != null) {
                        print("clear all recent tasks!!");
                        clear.click();
                    }
                }
                mDevice.pressBack();
            } catch (RemoteException e) {
                print(e.getMessage());
            }
        }
    }

    /**
     * 向控制台打印log
     *
     * @参数 msg log信息
     * @参数 Object 字符格式参数
     * @参数 ...
     */
    public static void print(String msg, Object... args) {
        Bundle bundle = new Bundle();
        bundle.clear();
        bundle.putString("[SPRD_TEST]", String.format(msg, args));
        instrumentation.sendStatus(0, bundle);
        Log.d("[SPRD_TEST]", String.format(msg, args));
    }

    /**
     * 获取运营商代码，LTE类型，LTE频点起始值
     *
     * @参数 itest    ITestApp接口实例
     * @参数 phoneId  SIM卡id
     * @返回值 返回运营商代码，LTE类型，LTE频点起始值的Map实例
     * @see #KEY_LTE_OPT
     * @see #KEY_LTE_TYPE
     * @see #KEY_EARFCN_DL
     */
    public static Map getBandInfo(ModemITestAPI itest, int phoneId) {
        Map map = new HashMap();
        String result = itest.getCommandReturn(CMD_GET_SERVING + phoneId);
        if (result.contains("OK")) {
            String[] info = result.split("-");
            map.put(KEY_LTE_OPT, OperatorInfo.getNetworkOperator(telMgr, phoneId));
            int band = Integer.parseInt(info[0].replaceAll(",0", ""));
            if (band > 0) {
                map.put(KEY_LTE_TYPE, band <= FDD_MAX_BAND ? FDD : TDD);
                map.put(KEY_EARFCN_DL, info[1].replaceAll(",0", ""));
            }
        }
        return map;
    }

    /**
     * 支持user版本读取config.xml获取手机号码
     *
     * @返回值 手机号码数组
     */

    public List getSimNumber() throws IOException {
        List list = new ArrayList();
        String studout = mDevice.executeShellCommand("cat /data/local/tmp/config.xml");
        Pattern pattern = Pattern.compile("(?m)\\d{11}");
        Matcher m = pattern.matcher(studout);
        while (m.find()) {
            list.add(m.group(0));
        }
        return list;
    }

    /**
     * 启动itestapp
     */
    public static void launchITestApp() {
        launchApp(ACTIVITY, ITEST_PAC, ITEST_PAC + ".TestClientActivity");
    }

    /**
     * 获取手机的SN号
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName(SYSTEM_PROPERTIE);
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (serial.isEmpty()) {
            try {
                serial = mDevice.executeShellCommand("getprop ro.serialno");
            } catch (IOException e) {
                print(e.getMessage());
            }
        }
        return serial;
    }

    /**
     *  区分版本类型
     *
     *  @返回值
     *  {@link #GOFU_GMS_BUILD}
     *  {@link #GMS_BUILD}
     *  {@link #NATIVE_BUILD}
     *
     */
    public static String getBuildType() {
        try {
            Class<?> c = Class.forName(SYSTEM_PROPERTIE);
            Method get = c.getMethod("get", String.class);
            String gms = (String) get.invoke(c, "persist.sys.gms");
            String extrainfo = (String) get.invoke(c, "ro.sprd.extrainfo");
            if (gms.isEmpty()){
                String gofu = (String) get.invoke(c, "ro.com.google.gmsversion");
                if (!gofu.isEmpty()) {
                    print("Current extrainfo ---> " + extrainfo);
                    return GOFU_GMS_BUILD;
                }
            }else{
                print("Current extrainfo ---> "+ extrainfo);
                return GMS_BUILD;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NATIVE_BUILD;
    }

    /**
     * 开关数据业务
     */
    public static void setDataState(boolean isOpened) throws IOException {
        if (isOpened)
            mDevice.executeShellCommand("svc data enable");
        else
            mDevice.executeShellCommand("svc data disable");
    }

    /**
     * 开关数据业务
     */
    public static void setDataState(int simId, boolean isOpened) {
        OperatorInfo.setDataEnable(telMgr, simId, isOpened);
    }

    /**
     * 播放视频
     *
     * @param path
     * @return
     */
    public static void playVideo(String path) {
        if (!TextUtils.isEmpty(path)) {
            try {
                Uri uri = Uri.parse(path);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                print(uri.toString());
                intent.setDataAndType(uri, "video/*");
                intent.setClassName("com.android.gallery3d",
                        "com.android.gallery3d.app.MovieActivity");
                context.startActivity(intent);
            } catch (Exception e) {
                print(e.getMessage());
            }
        }
    }

    /**
     * 枚举控件类型
     */
    public enum Typer {
        DESCCONTAINS, DESC, TEXT, TEXTCONTAINS, RESID, CLAZZ, NULLTYPE;

        static Typer getType(String str) {
            try {
                return Typer.valueOf(str);
            } catch (Exception err) {
                return NULLTYPE;
            }
        }
    }

    /**
     * 枚举控件类型
     */
    public enum Language {
        US, CHINA, UK, KOREA, JAPAN, PRC, FRANCE
    }

    /**
     * 获取当前主卡网络类型，4/3/2G
     */
    public static String getNetworkType() {
        String strNetworkType = "unkown";

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String _strSubTypeName = networkInfo.getSubtypeName();

                Log.e("cocos2d-x", "Network getSubtypeName : " + _strSubTypeName);

                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;
                    default:
                        if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = "3G";
                        } else {
                            strNetworkType = _strSubTypeName;
                        }
                        break;
                }
                Log.e("cocos2d-x", "Network getSubtype : " + Integer.valueOf(networkType).toString());
            }
        }
        Log.e("cocos2d-x", "Network Type : " + strNetworkType);
        return strNetworkType;
    }

    /**
     * 设置屏幕锁屏,注意设置必须当前锁屏方式非“无”
     */
    public static void lockScreen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mDevice.executeShellCommand("input keyevent " +
                            KeyEvent.KEYCODE_POWER);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 设置声音大小
     *
     * @参数 type 设系统中声音类型{@link AudioType}
     * @参数 level 设系统中声音大小
     */
    public static void setVolumeLevel(AudioType type, int level) {
        int max = 0;
        AudioManager aMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        switch (type) {
            case STREAM_SYSTEM:
                max = aMgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
                print("The max system volume level is %d.", max);
                if (level > max)
                    print("The system volume level \"%d\" is invalid.", level);
                aMgr.setStreamVolume(AudioManager.STREAM_SYSTEM, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case STREAM_MUSIC:
                max = aMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                print("The max music volume level is %d.", max);
                if (level > max)
                    print("The music volume level \"%d\" is invalid.", level);
                aMgr.setStreamVolume(AudioManager.STREAM_MUSIC, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case STREAM_RING:
                max = aMgr.getStreamMaxVolume(AudioManager.STREAM_RING);
                print("The max ring volume level is %d.", max);
                if (level > max)
                    print("The ring volume level \"%d\" is invalid.", level);
                aMgr.setStreamVolume(AudioManager.STREAM_RING, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case STREAM_ALARM:
                max = aMgr.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                print("The alarm ring volume level is %d.", max);
                if (level > max)
                    print("The ring volume level \"%d\" is invalid.", level);
                aMgr.setStreamVolume(AudioManager.STREAM_ALARM, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case STREAM_NOTIFICATION:
                max = aMgr.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
                print("The notification volume level is %d.", max);
                if (level > max)
                    print("The notification volume level \"%d\" is invalid.", level);
                aMgr.setStreamVolume(AudioManager.STREAM_NOTIFICATION, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                break;
            case STREAM_ALL:
                aMgr.setStreamVolume(AudioManager.STREAM_SYSTEM, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                aMgr.setStreamVolume(AudioManager.STREAM_MUSIC, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                aMgr.setStreamVolume(AudioManager.STREAM_RING, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                aMgr.setStreamVolume(AudioManager.STREAM_ALARM, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                aMgr.setStreamVolume(AudioManager.STREAM_NOTIFICATION, level, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            default:
                break;
        }
    }

    public enum AudioType {
        STREAM_MUSIC, STREAM_RING, STREAM_ALARM, STREAM_SYSTEM, STREAM_NOTIFICATION, STREAM_ALL;
    }
    /**
     * 进度条滑动
     *
     * @参数 clazz   进度条控件class值
     * @参数 percent 滑动距离百分比
     */
    public static void progressBar(String clazz, float percent){
        UiObject2 progress = findByClazz(clazz, 2000);
        if (progress != null){
            Rect rect = progress.getVisibleBounds();
            mDevice.swipe(rect.left, rect.top,(int) (rect.right * percent), rect.bottom, 10);
        }
    }

    public static boolean isNetConnection() {
        Context mContext = instrumentation.getTargetContext();
        if(mContext != null) {
//            ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getSystemService("connectivity");
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            boolean connected = networkInfo.isConnected();
            if(networkInfo != null && connected) {
                if(networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }

                return false;
            }
        }
        return false;
    }
}
