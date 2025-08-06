package top.zspaces.assistExcavation.client.config.HotKey;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.text.Text;
import top.zspaces.assistExcavation.client.config.GUI.AssistExcavationConfigScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class AssistExcavationKeyBindings {
    public static final Logger LOGGER = LoggerFactory.getLogger("assist-excavation");
    
    // 挖掘开关按键 Z键
    public static KeyBinding toggleExcavationKey;
    
    // 配置界面按键 V键
    public static KeyBinding openConfigKey;
    
    // 辅助挖掘是否启用
    private static boolean excavationEnabled = false;
    
    // 记录上次按键状态，用于检测按键的按下和释放
    private static boolean wasToggleKeyPressed = false;
    
    public static void registerKeyBindings() {
        // 注册挖掘开关按键 (默认Z键)
        toggleExcavationKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.assist-excavation.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "key.assist-excavation.category"
        ));
        
        // 注册配置界面按键 (默认V键)
        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.assist-excavation.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.assist-excavation.category"
        ));
        
        // 注册按键事件监听
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 处理挖掘开关按键 - 只在按下时切换一次
            boolean isToggleKeyPressed = toggleExcavationKey.isPressed();
            if (isToggleKeyPressed && !wasToggleKeyPressed) {
                // 按键刚刚被按下
                excavationEnabled = !excavationEnabled;
                if (client.player != null) {
                    client.player.sendMessage(
                        Text.translatable(
                            "message.assist-excavation.toggle",
                            Text.translatable(excavationEnabled ? "message.assist-excavation.enabled" : "message.assist-excavation.disabled")
                        ),
                        true
                    );
                }
                LOGGER.info("Excavation toggled: {}", excavationEnabled ? "enabled" : "disabled");
            }
            wasToggleKeyPressed = isToggleKeyPressed;
            
            // 处理配置界面按键
            while (openConfigKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new AssistExcavationConfigScreen(client.currentScreen));
                    LOGGER.info("Config screen opened");
                }
            }
        });
        
        LOGGER.info("Key bindings registered: toggle (Z), config (V)");
    }
    
    /**
     * 获取辅助挖掘是否启用
     * @return 如果启用返回true，否则返回false
     */
    public static boolean isExcavationEnabled() {
        return excavationEnabled;
    }
}