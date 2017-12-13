package com.jcc.common.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;



public class JDeviceUtils {

	public static final String TAG = "DeviceUtils";

	/**
	 * 获取设备唯一标示
	 * @param context
	 */
	public static String getDeviceId(Context context) {
        String deviceId = null;
        boolean is6 = isAndroid6();
        boolean hasPermission =
                JPermissionUtil.checkPermission(context, Manifest.permission.READ_PHONE_STATE);
        if (hasPermission) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (is6) {
                deviceId = tm.getDeviceId();
            } else {
                try {
                    deviceId = tm.getDeviceId();
                } catch (SecurityException se) {
                    // 防止低版本用户关闭权限。
                    hasPermission = false;
                }
            }
        }
        if (!hasPermission) {
            deviceId = "";
        } else if (deviceId == null) {
            deviceId = "";
        }
        return deviceId;
	}

	public static boolean isNetworkConnected(Context context) {

		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		return null != info && info.isAvailable();
	}

	/**
	 * wifi是否连接可用
	 */
	public static boolean isWifiConnected(Context context) {

		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return info != null && info.isConnected();
	}

	/**
	 * 得到当前的手机网络类型
	 */
	public static String getCurNetType(Context context) {

		String type = "";
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) {
			type = "null";
		} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			type = "wifi";
		} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			int subType = info.getSubtype();
			if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS
					|| subType == TelephonyManager.NETWORK_TYPE_EDGE) {
				type = "2g";
			} else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA
					|| subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
					|| subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
				type = "3g";
			} else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
				type = "4g";
			}
		}
		return type;
	}

	/**
	 * 获取系统基带版本
	 * @return String 基带版本，如果获取失败返回空字符串
	 */
	public static String getBaseband() {

		String baseBand = "";
		try {
			Class cl = Class.forName("android.os.SystemProperties");
			Object invoker = cl.newInstance();
			Method m = cl.getMethod("get", new Class[]{String.class, String.class});
			Object result = m.invoke(invoker, new Object[]{"gsm.version.baseband", ""});
			baseBand = (String) result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baseBand;
	}

	/**
	 * 获取CPU_ABI
	 */
	public static String getCPU_ABI() {

		String CPU_ABI = android.os.Build.CPU_ABI;
		if (!TextUtils.isEmpty(CPU_ABI)) {
			return CPU_ABI;
		} else {
			return "";
		}
	}

	/**
	 * 获得cpu名称
	 * @return
	 */
	public static String getCpuName() {

		FileReader fr = null;
		BufferedReader br = null;
		String text;
		try {
			fr = new FileReader("/proc/cpuinfo");
			br = new BufferedReader(fr);
			text = br.readLine();
			String[] array = text.split(":\\s+", 2);
			return array[1];
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	public static String getCpuInfo() {
		String str1 = "/proc/cpuinfo";
		String str2="";
		String[] cpuInfo={"",""};
		String[] arrayOfString;
		try {
			FileReader fr = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
			str2 = localBufferedReader.readLine();
			if(str2 == null){
				return "";
			}
			arrayOfString = str2.split("\\s+");
			for (int i = 2; i < arrayOfString.length; i++) {
				cpuInfo[0] = cpuInfo[0] + arrayOfString[i] + " ";
			}
			str2 = localBufferedReader.readLine();
			if(str2 == null){
				return "";
			}
			arrayOfString = str2.split("\\s+");
			cpuInfo[1] += arrayOfString[2];
			localBufferedReader.close();
		} catch (IOException e) {
		}
		return cpuInfo[0];
	}

	/**
	 * 获得内存大小
	 * @return
	 */
	public static String getTotalMemory() {

		FileReader fr = null;
		BufferedReader br = null;
		String text;
		try {
			fr = new FileReader("/proc/meminfo");
			br = new BufferedReader(fr, 8);
			text = br.readLine();
			String[] array = text.split("\\s+");
			//转换为GB显示
			float memory = Float.valueOf(array[1]) / 1024 / 1024;
			//设置两位有效数字
			DecimalFormat decimalFormat = new DecimalFormat("######0.00");
			String p = decimalFormat.format(memory);
			return p + "G";

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	/**
	 * SD卡存储 兼容低版本
	 * @return
	 */
	public static String getSdSize() {
		File path = Environment.getExternalStorageDirectory();
        try {
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return ((blockSize * availableBlocks)/ 1024 / 1024 / 1024) + "G";
        } catch (IllegalArgumentException e) {
        }
        return "0G";
	}

	/**
	 * 机身存储 兼容低版本
	 * @return
	 */

	public static long getRomTotalSize() {
		File path = Environment.getDataDirectory();
        try {
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return blockSize * totalBlocks;
        } catch (IllegalArgumentException e) {
        }
        return 0L;
	}


	public static boolean isAndroid6() {

		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}

	public static boolean isAndroid51() {

		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
	}

	public static boolean isAndroid5_0() {

		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	public static boolean isAPPInstall(Context context, String packagename){
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
		}catch (PackageManager.NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		if(packageInfo ==null){
			//System.out.println("没有安装");
			return false;
		}else{
			//System.out.println("已经安装");
			return true;
		}
	}

}
