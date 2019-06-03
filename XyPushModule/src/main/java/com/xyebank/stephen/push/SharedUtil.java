package com.xyebank.stephen.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedUtil {

	private static final String SHARED_PATH = "app_share";

	public static SharedPreferences getDefaultSharedPreferences(Context context) {
		return context.getSharedPreferences(SHARED_PATH, Context.MODE_PRIVATE);
	}
	
	public static void putInt(Context context,String key, int value) {
		SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putInt(key, value);
		edit.commit();
	}

	public static int getInt(Context context,String key) {
		try {
			SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
			return sharedPreferences.getInt(key,0);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

    public static void putLong(Context context,String key, long value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        Editor edit = sharedPreferences.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    public static long getLong(Context context,String key) {
		try {
			SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
			return sharedPreferences.getLong(key,-1l);
		} catch (Exception e) {
			e.printStackTrace();
			return -1l;
		}
	}

	public static void putString(Context context,String key, String value) {
		SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putString(key, value);
		edit.commit();
	}

	public static String getString(Context context,String key) {
		SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
		return sharedPreferences.getString(key,null);
	}
	
	public static void putBoolean(Context context,String key, boolean value) {
		SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
		Editor edit = sharedPreferences.edit();
		edit.putBoolean(key, value);
		edit.commit();
	}

	public static boolean getBoolean(Context context,String key,boolean defValue) {
		SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean(key,defValue);
	}

	public static boolean contains(Context context,String key) {
		SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
		return sharedPreferences.contains(key);
	}

    public static void remove(Context context,String... keys) {
        for (String key : keys) {
            SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
            Editor edit = sharedPreferences.edit();
            edit.remove(key);
            edit.commit();
        }//end of if
    }
}
