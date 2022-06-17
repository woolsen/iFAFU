package cn.ifafu.ifafu.util;

import androidx.annotation.ColorInt;

import java.util.HashMap;
import java.util.Map;

/**
 * 颜色池，用于有规律地分配颜色
 */
public class LightColorPool implements ColorPool {
    private final int[] lightColorList = new int[]{
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

    private final Map<Integer, Integer> pool = new HashMap<>();

    @ColorInt
    @Override
    public int getColor(Object o) {
        int hashCode = o.hashCode();
        Integer color = pool.get(hashCode);
        if (color == null) {
            color = lightColorList[pool.size() % lightColorList.length];
            pool.put(hashCode, color);
        }
        return color;
    }

}