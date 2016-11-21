package com.advmovile.noote;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created by jhlee on 11/20/16.
 */

public class NooteHelper {

    public static String formatDT(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        System.out.println(formattedDate);
        return formattedDate;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static <T> T[] concatenate (T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
