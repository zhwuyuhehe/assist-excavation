package top.zspaces.assistExcavation.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.zspaces.assistExcavation.client.config.HotKey.AssistExcavationKeyBindings;
import top.zspaces.assistExcavation.client.excavation.ExcavationHandler;

public class AssistExcavationClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("assist-excavation");

    @Override
    public void onInitializeClient() {
        // 注册按键绑定
        AssistExcavationKeyBindings.registerKeyBindings();
        
        // 注册客户端tick事件来处理挖掘逻辑
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        
        LOGGER.info("Assist Excavation mod initialized - Enhancing your mining experience while respecting server rules");
        LOGGER.info("辅助挖掘模组加载完毕。");
    }

    /**
     * 客户端tick事件，用于处理挖掘逻辑
     */
    private void onClientTick(MinecraftClient client) {
        // 处理挖掘逻辑
        ExcavationHandler.handleExcavation();
    }
}