package com.unisoc.bmte.uiautohelper;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
//import com.android.internal.telephony.Phone;
import com.android.sprd.telephony.RadioInteractor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.content.Context.TELEPHONY_SERVICE;

public class OperatorInfo {

    private final static String TAG = "OperatorInfo";
    public static int PHONE_NUM = 2;
    static Class<?> mClass = getTelephonyObject().getClass();

    private static Object getTelephonyObject() {
        Object telephony = null;
        try {
            // 初始化iTelephony
            Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, new Object[]{TELEPHONY_SERVICE});
            telephony = ITelephony.Stub.asInterface(binder);
            method.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return telephony;
    }
    public static boolean isMultisim(Context context) {
        if (Build.VERSION.RELEASE.contains("4.4")) {
            return reflectIsMultisim("android.telephony.TelephonyManager");
        } else if (OperatorInfo.isAndroidL() || OperatorInfo.isAndroidM() || OperatorInfo.isAndroidN()) {
            TelephonyManager tMger = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return reflectIsMultisim(tMger);
        } else {
            return reflectIsMultisim("com.android.internal.telephony.PhoneFactory");
        }
    }

    public static boolean reflectIsMultisim(String className) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("isMultiSim");
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            Boolean abc = (Boolean) method.invoke(mClass);
            Log.d(TAG, "reflectIsMultisim:" + abc);
            return abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static int getPhoneCount(TelephonyManager telMsg) {
        return telMsg.getPhoneCount();
//        Method method = null;
//        try {
//            Class<?> demo = Class.forName(className);
//            method = demo.getDeclaredMethod("getPhoneCount");
//        } catch (Exception e) {
//            Log.e(TAG, "getDeclaredMethod error");
//            e.printStackTrace();
//        }
//
//        try {
//            method.setAccessible(true);
//            int abc = (Integer) method.invoke(mClass);
//            return abc;
//        } catch (IllegalArgumentException e) {
//            Log.e(TAG, "Illegal Argument");
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            Log.e(TAG, "Illegal Access");
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            Log.e(TAG, "Invocation Target");
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return 0;
    }

    public static String reflectGetServiceName(String className, String param1, int param2) {
        Method method = null;
        try {
            Class<?> demo = Class.forName(className);
            method = demo.getDeclaredMethod("getServiceName", String.class, int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        Object abc;
        try {
            method.setAccessible(true);
            abc = method.invoke(mClass, param1, param2);
            return (String) abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return "";
    }

    public static boolean reflectIsMultisim(TelephonyManager telMgr1) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("isMultiSimEnabled");
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            Boolean abc = (Boolean) method.invoke(telMgr1);
            return abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return false;
    }

     // 获取数据业务状态，打开or关闭
    public static boolean getDataEnable(TelephonyManager telMgr1, int phoneId) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("getDataEnabled", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            Boolean abc = (Boolean) method.invoke(telMgr1, phoneId);
            return abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return false;
    }
    
    // 打开数据业务
    public static void setDataEnable(TelephonyManager telMgr1, int subId, boolean isOpen) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("setDataEnabled", int.class, boolean.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            method.invoke(telMgr1, subId, isOpen);
            return;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
    }

    public static boolean reflectIsMultisim(TelephonyManager telMgr1, String className, String methodName, int subId) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName(className);
            method = demo.getDeclaredMethod(methodName, int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            Boolean abc = (Boolean) method.invoke(telMgr1, subId);
            return abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return false;
    }

    // 获取当前的运营商
    public static String getOperator(TelephonyManager telMgr1, String className, String methodName, int subId) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName(className);
            method = demo.getDeclaredMethod(methodName, int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            String abc = (String) method.invoke(telMgr1, subId);
            return abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return null;
    }

    public static SmsManager getSmsManager(int subId) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.SmsManager");
            method = demo.getDeclaredMethod("getSmsManagerForSubscriptionId", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            SmsManager abc = (SmsManager) method.invoke(mClass, subId);
            return abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return null;
    }

//    public static int getSubId(Phone phone) {
//        Class<?> demo = null;
//        Method method = null;
//        try {
//            demo = Class.forName("com.android.internal.telephony.Phone");
//            method = demo.getDeclaredMethod("getSubId");
//        } catch (Exception e) {
//            Log.e(TAG, "getDeclaredMethod error");
//            e.printStackTrace();
//        }
//
//        try {
//            method.setAccessible(true);
//            int abc = (Integer) method.invoke(phone);
//            return abc;
//        } catch (IllegalArgumentException e) {
//            Log.e(TAG, "Illegal Argument");
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            Log.e(TAG, "Illegal Access");
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            Log.e(TAG, "Invocation Target");
//            e.printStackTrace();
//        }
//        return 0;
//    }

    public static int[] getSubId(SubscriptionManager subM) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.SubscriptionManager");
            method = demo.getDeclaredMethod("getActiveSubscriptionIdList");
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        Object abc;
        try {
            method.setAccessible(true);
            abc = method.invoke(subM);
            return (int[]) abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return null;
    }

    public static int getPhoneId(int parm) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.SubscriptionManager");
            method = demo.getDeclaredMethod("getPhoneId", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        Object abc;
        try {
            method.setAccessible(true);
            abc = method.invoke(mClass, parm);
            return (Integer) abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return 0;
    }

    public static int getNetworkType(TelephonyManager tMager, int subId) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("getVoiceNetworkType", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        Object abc;
        try {
            method.setAccessible(true);
            abc = method.invoke(tMager, subId);
            return (Integer) abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return 0;
    }

    public static int getDefaultSim(TelephonyManager tMager) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("getDefaultSim");
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        Object abc;
        try {
            method.setAccessible(true);
            abc = method.invoke(tMager);
            return (Integer) abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return 0;
    }

    public static void setSimStandby(TelephonyManager tMager, int id, boolean open) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("setSimStandby", int.class, boolean.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }
        try {
            method.setAccessible(true);
            method.invoke(tMager, id, open);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
    }

    public static int getDefaultDataPhoneId(SubscriptionManager subM) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.SubscriptionManager");
            method = demo.getDeclaredMethod("getDefaultDataPhoneId");
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        Object abc;
        try {
            method.setAccessible(true);
            abc = method.invoke(subM);
            return (int) abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return 0;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public static int getPrimaryCard(Context context) {
        int phoneId = getDefaultDataPhoneId(SubscriptionManager.from(context));
        Log.d(TAG, "getPrimaryCard: " + phoneId);
        return phoneId;
    }


    public static int getPrimaryCard(TelephonyManager tMager) {
        Method method = null;
        try {
            Class<?> demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("getPrimaryCard");
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }
        Object abc;
        try {
            method.setAccessible(true);
            abc = method.invoke(tMager);
            return (Integer) abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return 0;
    }

    public static void setDefaultDataSubId(SubscriptionManager subM, int subid) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.SubscriptionManager");
            method = demo.getDeclaredMethod("setDefaultDataSubId", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        Object abc;
        try {
            method.setAccessible(true);
            method.invoke(subM, subid);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
    }

    public static void setDisplayName(SubscriptionManager subM, String displayName, int subid) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.SubscriptionManager");
            method = demo.getDeclaredMethod("setDisplayName", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }
        try {
            method.setAccessible(true);
            method.invoke(subM, subid);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
    }


    public static void setPrimaryCard(TelephonyManager tMager, int id) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("setPrimaryCard", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            method.invoke(tMager, id);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
    }

    public static void setLteEnabled(TelephonyManager tMager, boolean enable) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("setLteEnabled", boolean.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            method.invoke(tMager, enable);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
    }

    public static void setLteEnabled(RadioInteractor tMager, boolean enable, int id) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("com.android.sprd.telephony.RadioInteractor");
            method = demo.getDeclaredMethod("setLteEnabled", boolean.class, int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            method.invoke(tMager, enable, id);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
    }

    public static String getNetworkOperator(TelephonyManager telMgr1, int phoneId) {
        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("getNetworkOperatorForPhone", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }

        try {
            method.setAccessible(true);
            String abc = (String) method.invoke(telMgr1, phoneId);
            return abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return "";
    }

    public static int getPreferredNetworkType(TelephonyManager telMgr1, int subid) {

        Class<?> demo = null;
        Method method = null;
        try {
            demo = Class.forName("android.telephony.TelephonyManager");
            method = demo.getDeclaredMethod("getPreferredNetworkType", int.class);
        } catch (Exception e) {
            Log.e(TAG, "getDeclaredMethod error");
            e.printStackTrace();
        }
        Object abc;
        try {
            method.setAccessible(true);
            abc = method.invoke(telMgr1, subid);
            return (int) abc;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Illegal Access");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Invocation Target");
            e.printStackTrace();
        }
        return -1;

    }

    public static boolean isAndroidK() {
        return Build.VERSION.RELEASE.contains("4");
    }

    public static boolean isAndroidL() {
        return Build.VERSION.RELEASE.contains("5");
    }

    public static boolean isAndroidM() {
        return Build.VERSION.RELEASE.contains("6");
    }

    public static boolean isAndroidN() {
        return Build.VERSION.RELEASE.contains("7");
    }
}
