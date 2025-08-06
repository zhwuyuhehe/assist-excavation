package top.zspaces.assistExcavation.client.config.GUI;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import top.zspaces.assistExcavation.client.Common;

@Environment(EnvType.CLIENT)
public class AssistExcavationConfigScreen extends Screen {
    private final Screen parent;
    
    // 配置值
    private int delayTicks;
    private int reach;
    private int excavationMode; // 0=矩形, 1=圆形, 2=未实现
    
    // UI组件
    private DelayTicksSlider delayTicksSlider;
    private ReachSlider reachSlider;
    private ButtonWidget excavationModeButton;
    
    public AssistExcavationConfigScreen(Screen parent) {
        super(Text.translatable("screen.assist-excavation.config.title"));
        this.parent = parent;
        // 从Common类中获取当前配置值
        this.delayTicks = Common.getDelayTicks();
        this.reach = Common.getReach();
        this.excavationMode = Common.getExcavationMode();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        
        // delayTicks滑块 (范围0-40)
        delayTicksSlider = new DelayTicksSlider(
                centerX - buttonWidth / 2, 
                50, 
                buttonWidth, 
                buttonHeight, 
                Text.translatable("screen.assist-excavation.config.delayTicks", delayTicks), 
                (double) delayTicks / 40.0);
        
        // reach滑块 (范围1-6)
        reachSlider = new ReachSlider(
                centerX - buttonWidth / 2, 
                80, 
                buttonWidth, 
                buttonHeight, 
                Text.translatable("screen.assist-excavation.config.reach", reach), 
                (double) (reach - 1) / 5.0);
        
        // excavationMode按钮
        excavationModeButton = ButtonWidget.builder(
                getExcavationModeText(), 
                button -> {
                    excavationMode = (excavationMode + 1) % 3;
                    button.setMessage(getExcavationModeText());
                })
                .dimensions(centerX - buttonWidth / 2, 110, buttonWidth, buttonHeight)
                .build();
        
        // 添加重置按钮
        ButtonWidget resetButton = ButtonWidget.builder(
                Text.translatable("screen.assist-excavation.config.reset"), 
                button -> resetConfig())
                .dimensions(centerX - buttonWidth / 2, this.height - 60, buttonWidth, buttonHeight)
                .build();
        
        // 添加完成按钮
        ButtonWidget doneButton = ButtonWidget.builder(
                Text.translatable("gui.done"), 
                button -> {
                    // 保存配置到Common类
                    saveConfig();
                    this.close();
                })
                .dimensions(centerX - buttonWidth / 2, this.height - 35, buttonWidth, buttonHeight)
                .build();
        
        // 添加组件到屏幕
        addDrawableChild(delayTicksSlider);
        addDrawableChild(reachSlider);
        addDrawableChild(excavationModeButton);
        addDrawableChild(resetButton);
        addDrawableChild(doneButton);
    }
    
    private void saveConfig() {
        Common.setDelayTicks(delayTicks);
        Common.setReach(reach);
        Common.setExcavationMode(excavationMode);
    }
    
    private void resetConfig() {
        delayTicks = 0;
        reach = 1;
        excavationMode = 0;
        
        // 重新创建滑块以重置它们的值
        remove(delayTicksSlider);
        remove(reachSlider);
        
        int buttonWidth = 200;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        
        delayTicksSlider = new DelayTicksSlider(
                centerX - buttonWidth / 2, 
                50, 
                buttonWidth, 
                buttonHeight, 
                Text.translatable("screen.assist-excavation.config.delayTicks", delayTicks), 
                0.0);
        
        reachSlider = new ReachSlider(
                centerX - buttonWidth / 2, 
                80, 
                buttonWidth, 
                buttonHeight, 
                Text.translatable("screen.assist-excavation.config.reach", reach), 
                0.0);
        
        addDrawableChild(delayTicksSlider);
        addDrawableChild(reachSlider);
        excavationModeButton.setMessage(getExcavationModeText());
    }
    
    private MutableText getExcavationModeText() {
        return Text.translatable("screen.assist-excavation.config.excavationMode", 
                Text.translatable("screen.assist-excavation.config.excavationMode." + excavationMode));
    }
    
    @Override
    public void close() {
        this.client.setScreen(parent);
    }
    
    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        
        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, 
                Text.translatable("screen.assist-excavation.config.title"), 
                this.width / 2, 15, 0xFFFFFF);
        
        // 显示服务器实际可达距离信息
        if (this.client != null && this.client.player != null) {
            double realReach = this.client.player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("screen.assist-excavation.config.real_reach", String.format("%.2f", realReach)),
                    this.width / 2, this.height - 85, 0xAAAAAA);
        }
    }
    
    // 自定义滑块类以处理delayTicks
    private class DelayTicksSlider extends SliderWidget {
        public DelayTicksSlider(int x, int y, int width, int height, Text text, double value) {
            super(x, y, width, height, text, value);
            delayTicks = (int) (value * 40);
        }
        
        @Override
        protected void updateMessage() {
            delayTicks = (int) (this.value * 40);
            this.setMessage(Text.translatable("screen.assist-excavation.config.delayTicks", delayTicks));
        }
        
        @Override
        protected void applyValue() {
            // 应用值时的逻辑
        }
    }
    
    // 自定义滑块类以处理reach
    private class ReachSlider extends SliderWidget {
        public ReachSlider(int x, int y, int width, int height, Text text, double value) {
            super(x, y, width, height, text, value);
            reach = (int) (value * 5) + 1;
        }
        
        @Override
        protected void updateMessage() {
            reach = (int) (this.value * 5) + 1;
            // 获取当前可用的真实手长。

            this.setMessage(Text.translatable("screen.assist-excavation.config.reach", reach));
        }
        
        @Override
        protected void applyValue() {
            // 应用值时的逻辑
        }
    }
}