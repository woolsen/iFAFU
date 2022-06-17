package cn.ifafu.ifafu.util;

import android.content.Context;

import androidx.annotation.ColorRes;

import java.util.Random;

/**
 * shape选择器生成工具
 * Created by mnnyang on 17-10-22.
 */
public class ColorUtils {
    public static final int[] lightColorList = new int[]{
            0xFF8AD297,
            0xFFF9A883,
            0xFF88CFCC,
            0xFFF19C99,
            0xFFF7C56B,
            0xFFD2A596,
            0xFF67BDDE,
            0xFF9CCF5A,
            0xFF9AB4CF,
            0xFFE593AD,
            0xFFE2C38A,
            0xFFB29FD2,
            0xFFE2C490,
            0xFFE2C490,
    };

    public static final int[] darkColorList = new int[]{
            0xFF5ABF6C,
            0xFFF79060,
            0xFF63C0BD,
            0xFFED837F,
            0xFFF5B94E,
            0xFFCA9483,
            0xFF31A6D3,
            0xFF8BC73D,
            0xFF87A6C6,
            0xFFDF7999,
            0xFFD6A858,
            0xFF997FC3,
            0xFFDDB97B,
            0xFFd3dEe5};

    private static final Random random = new Random();

    public static int getRandomLightColor() {
        return lightColorList[random.nextInt(20) % lightColorList.length];
    }

    public static int getRandomLightColor(Object o) {
        return lightColorList[o.hashCode() % lightColorList.length];
    }

    public static int getColor(Context context, @ColorRes int id) {
        return context.getResources().getColor(id);
    }

    public static int getDarkRandomColor() {
        return darkColorList[random.nextInt(20) % lightColorList.length];
    }

}


