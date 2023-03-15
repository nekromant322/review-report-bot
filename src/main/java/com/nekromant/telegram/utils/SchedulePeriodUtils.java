package com.nekromant.telegram.utils;

import java.util.HashMap;
import java.util.Map;

public class SchedulePeriodUtils {
    private static Map<String, Long> period = new HashMap<>();
    static {
        period.put("start", 18L);
        period.put("end", 3L);
    }

    public static Long getStart() {
        return period.get("start");
    }

    public static Long getEnd() {
        return period.get("end");
    }

    public static void setStart(Long start) {
        period.put("start", start);
    }

    public static void setEnd(Long end) {
        period.put("end", end);
    }
}
