package com.example.q.cs496_week3;

/**
 * Created by q on 2017-07-14.
 */

public class UserInfo {
    private static String id;
    private static String nickname;
    private static double lat;
    private static double lng;

    public static void setIdStr(String s) {
        id = s;
    }

    public static String getIdStr() {
        return id;
    }

    public static void setNickname(String s) {
        nickname = s;
    }

    public static String getNickname() {
        return nickname;
    }

    public static void setLatv(double d) {
        lat = d;
    }

    public static double getLatv() {
        return lat;
    }

    public static void setLngv(double d) {
        lng = d;
    }

    public static double getLngv() {
        return lng;
    }
}
