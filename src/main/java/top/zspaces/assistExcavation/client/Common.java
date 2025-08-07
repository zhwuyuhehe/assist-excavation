package top.zspaces.assistExcavation.client;

public class Common {
    // 定义范围常量
    public static final int MIN_DELAY_TICKS = 0;      // 延迟最小值
    public static final int MAX_DELAY_TICKS = 40;     // 延迟最大值
    public static final int MIN_REACH = 1;            // 手长最小值
    public static final int MAX_REACH = 6;            // 手长最大值
    public static final int MIN_EXCAVATION_MODE = 0;  // 挖掘模式最小值
    public static final int MAX_EXCAVATION_MODE = 2;  // 挖掘模式最大值
    
    private static Integer delayTicks = 0; // 挖掘等待延迟的默认值
    private static Integer reach = 1;     // 默认手长
    private static Integer excavationMode = 0; // 默认使用正方形范围
    
    // delayTicks的getter和setter
    public static Integer getDelayTicks() {
        return delayTicks;
    }
    
    public static void setDelayTicks(Integer delayTicks) {
        if (delayTicks != null) {
            // 限制delayTicks在有效范围内
            Common.delayTicks = Math.max(MIN_DELAY_TICKS, Math.min(MAX_DELAY_TICKS, delayTicks));
        }
    }
    
    // reach的getter和setter
    public static Integer getReach() {
        return reach;
    }
    
    public static void setReach(Integer reach) {
        if (reach != null) {
            // 限制reach在有效范围内
            Common.reach = Math.max(MIN_REACH, Math.min(MAX_REACH, reach));
        }
    }
    
    // excavationMode的getter和setter
    public static Integer getExcavationMode() {
        return excavationMode;
    }
    
    public static void setExcavationMode(Integer excavationMode) {
        if (excavationMode != null) {
            // 限制excavationMode在有效范围内
            Common.excavationMode = Math.max(MIN_EXCAVATION_MODE, Math.min(MAX_EXCAVATION_MODE, excavationMode));
        }
    }
}