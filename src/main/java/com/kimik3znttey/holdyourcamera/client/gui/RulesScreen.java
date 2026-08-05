package com.kimik3znttey.holdyourcamera.client.gui;

import com.kimik3znttey.holdyourcamera.config.HYCConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * 规则管理界面：
 * - 顶部：两个总开关（恢复原视角 / 强制锁定）
 * - 中间：规则列表（物品图标 + 规则文本 + 视角 + 编辑入口，支持滚动；删除在编辑页内）
 * - 底部：添加手持物品 / 完成
 * 打开方式：模组列表 Config 按钮，或空手按 J。
 */
public class RulesScreen extends Screen {

    private final Screen parent;
    private RuleList ruleList;

    public RulesScreen(Screen parent) {
        super(Component.translatable("holdyourcamera.screen.rules.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        // 顶部两个总开关（1.20.1 的 Checkbox 没有 builder，用匿名子类监听勾选变化）
        this.addRenderableWidget(this.makeCheckbox(cx - 165, 32, "holdyourcamera.restore",
                HYCConfig.RESTORE_PREVIOUS.get(), HYCConfig.RESTORE_PREVIOUS::set));
        this.addRenderableWidget(this.makeCheckbox(cx + 15, 32, "holdyourcamera.enforce",
                HYCConfig.ENFORCE.get(), HYCConfig.ENFORCE::set));

        // 规则列表
        this.ruleList = new RuleList(Minecraft.getInstance(), this.width, this.height, 58, this.height - 54, 26);
        this.addWidget(this.ruleList);
        this.refresh();

        // 底部按钮
        this.addRenderableWidget(Button.builder(Component.translatable("holdyourcamera.add_held"), b -> this.openAddHeld())
                .bounds(cx - 155, this.height - 42, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> this.onClose())
                .bounds(cx + 5, this.height - 42, 150, 20).build());
    }

    /**
     * 1.20.1 的 Checkbox 没有 builder()（那是 1.20.2 新增的 API），
     * 用构造器 + 匿名子类重写 onPress 来实现"勾选变化时写回配置"。
     */
    private Checkbox makeCheckbox(int x, int y, String langKey, boolean initial, Consumer<Boolean> onChange) {
        Component label = Component.translatable(langKey);
        int width = 24 + this.font.width(label); // 20px 勾选框 + 间距 + 文字宽度
        return new Checkbox(x, y, width, 20, label, initial) {
            @Override
            public void onPress() {
                super.onPress(); // 先让原版完成勾选状态翻转
                onChange.accept(this.selected());
            }
        };
    }

    /** 重新从配置读取规则并刷新列表 */
    void refresh() {
        this.ruleList.setRules(HYCConfig.rules());
    }

    private void openAddHeld() {
        if (this.minecraft.player != null) {
            ItemStack held = this.minecraft.player.getMainHandItem();
            if (!held.isEmpty()) {
                this.minecraft.setScreen(RuleEditScreen.add(this, held));
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        this.ruleList.render(g, mouseX, mouseY, partial);
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        if (this.ruleList.children().isEmpty()) {
            g.drawCenteredString(this.font, Component.translatable("holdyourcamera.empty"),
                    this.width / 2, 92, 0xAAAAAA);
        }
        super.render(g, mouseX, mouseY, partial);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    // ------------------------------------------------------------------
    // 可滚动的规则列表
    // ------------------------------------------------------------------

    class RuleList extends ContainerObjectSelectionList<RulesScreen.RuleList.RuleEntry> {

        RuleList(Minecraft mc, int width, int height, int y0, int y1, int itemHeight) {
            super(mc, width, height, y0, y1, itemHeight);
        }

        /** 重新填充规则。clearEntries()/addEntry() 是基类的 protected 方法，只能在列表类内部调用 */
        void setRules(List<HYCConfig.Rule> rules) {
            this.clearEntries();
            for (HYCConfig.Rule rule : rules) {
                this.addEntry(new RuleEntry(rule));
            }
        }

        @Override
        public int getRowWidth() {
            return Math.min(360, this.width - 50);
        }

        class RuleEntry extends ContainerObjectSelectionList.Entry<RuleEntry> {

            private final HYCConfig.Rule rule;
            private final ItemStack icon;
            private final Button editBtn;

            RuleEntry(HYCConfig.Rule rule) {
                this.rule = rule;
                this.icon = rule.path() == null
                        ? ItemStack.EMPTY
                        : new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(rule.namespace(), rule.path())));
                this.editBtn = Button.builder(Component.translatable("holdyourcamera.edit"), b -> this.editRule())
                        .bounds(0, 0, 66, 20).build();
            }

            private void editRule() {
                Minecraft.getInstance().setScreen(RuleEditScreen.edit(RulesScreen.this, this.rule));
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovered, float partial) {
                var font = Minecraft.getInstance().font;

                // 物品图标；整族规则画一个星标
                if (!this.icon.isEmpty()) {
                    g.renderItem(this.icon, left + 2, top + 5);
                } else {
                    g.drawString(font, "*", left + 7, top + 9, 0xFFCC55);
                }

                g.drawString(font, this.rule.serialize(), left + 24, top + 4, 0xFFFFFF);
                g.drawString(font,
                        Component.translatable("holdyourcamera.perspective."
                                + this.rule.type().name().toLowerCase(Locale.ROOT)),
                        left + 24, top + 14, 0xAAAAAA);

                this.editBtn.setX(left + width - 70);
                this.editBtn.setY(top + 3);
                this.editBtn.render(g, mouseX, mouseY, partial);
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return List.of(this.editBtn);
            }

            @Override
            public List<? extends NarratableEntry> narratables() {
                return List.of(this.editBtn);
            }
        }
    }
}
