package top.zspaces.assistExcavation;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssistExcavation implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("assist-excavation");

    @Override
    public void onInitialize() {
        LOGGER.info("Assist Excavation mod initialized");
    }
}