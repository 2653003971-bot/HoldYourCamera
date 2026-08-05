package com.kimik3znttey.holdyourcamera.client.gui;

import com.kimik3znttey.holdyourcamera.config.HYCConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 添加 / 编辑单条规则的窗口：
 * - 顶部渲染物品图标（悬停显示名字），整族规则显示命名空间文本
 * - 作用范围二选一：仅此物品 / 整个模组
 * - 视角三选一：第一人称 / 第三人称背面 / 第三人称正面
 * 全程点选，玩家不需要知道物品 ID 或任何语法。
 */
public class RuleEditScreen extends Screen {

    private static final CameraType[] TYPES = {
            CameraType.FIRST_PERSON, CameraType.THIRD_PERSON_BACK, CameraType.THIRD_PERSON_FRONT
    };

    private final Screen parent;
    private final HYCConfig.Rule original;   // null = 新增模式
    private final String namespace;
    private final String concretePath;       // 已知的具体物品路径（编辑整族规则时可能为 null）
    private final ItemStack icon;

    private String path;                     // 编辑结果；null = 整个模组
    private CameraType selected;

    private Button scopeItemBtn;
    private Button scopeModBtn;
    private final Button[] typeBtns = new Button[TYPES.length];

    /** 新增模式：从手持物品创建草稿 */
    public static RuleEditScreen add(Screen parent, ItemStack held) {
        return new RuleEditScreen(parent, null, HYCConfig.Rule.fromItem(held));
    }

    /** 编辑模式：修改已有规则 */
    public static RuleEditScreen edit(Screen parent, HYCConfig.Rule rule) {
        return new RuleEditScreen(parent, rule, rule);
    }

    private RuleEditScreen(Screen parent, HYCConfig.Rule original, HYCConfig.Rule draft) {
        super(Component.translatable(original == null
                ? "holdyourcamera.screen.edit.title.add"
                : "holdyourcamera.screen.edit.title.edit"));
        this.parent = parent;
        this.original = original;
        this.namespace = draft.namespace();
        this.concretePath = draft.path();
        this.path = draft.path();
        this.selected = draft.type();
        this.icon = draft.path() == null
                ? ItemStack.EMPTY
                : new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(draft.namespace(), draft.path())));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 2 - 64;

        // 作用范围（二选一）
        this.scopeItemBtn = this.addRenderableWidget(Button.builder(Component.empty(), b -> {
            this.path = this.concretePath;
            this.refreshButtons();
        }).bounds(cx - 112, top + 40, 106, 20).build());
        this.scopeModBtn = this.addRenderableWidget(Button.builder(Component.empty(), b -> {
            this.path = null;
            this.refreshButtons();
        }).bounds(cx + 6, top + 40, 106, 20).build());
        this.scopeItemBtn.active = this.concretePath != null; // 整族规则没有具体物品可切回

        // 视角（三选一）
        for (int i = 0; i < TYPES.length; i++) {
            final CameraType type = TYPES[i];
            this.typeBtns[i] = this.addRenderableWidget(Button.builder(Component.empty(), b -> {
                this.selected = type;
                this.refreshButtons();
            }).bounds(cx - 168 + i * 113, top + 84, 106, 20).build());
        }

        // 保存 / 返回；编辑模式额外给一个"删除"（与列表行上的删除按钮等效）
        if (this.original != null) {
            this.addRenderableWidget(Button.builder(Component.translatable("holdyourcamera.save"), b -> this.save())
                    .bounds(cx - 168, top + 124, 106, 20).build());
            this.addRenderableWidget(Button.builder(Component.translatable("holdyourcamera.delete"), b -> this.delete())
                    .bounds(cx - 53, top + 124, 106, 20).build());
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> this.onClose())
                    .bounds(cx + 62, top + 124, 106, 20).build());
        } else {
            this.addRenderableWidget(Button.builder(Component.translatable("holdyourcamera.save"), b -> this.save())
                    .bounds(cx - 112, top + 124, 106, 20).build());
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> this.onClose())
                    .bounds(cx + 6, top + 124, 106, 20).build());
        }

        this.refreshButtons();
    }

    /** 刷新所有选项按钮的选中态显示（选中的加 > < 标记） */
    private void refreshButtons() {
        this.scopeItemBtn.setMessage(this.mark(this.path != null, "holdyourcamera.scope.item"));
        this.scopeModBtn.setMessage(this.mark(this.path == null, "holdyourcamera.scope.mod"));
        for (int i = 0; i < TYPES.length; i++) {
            this.typeBtns[i].setMessage(this.mark(this.selected == TYPES[i],
                    "holdyourcamera.perspective." + TYPES[i].name().toLowerCase(Locale.ROOT)));
        }
    }

    private Component mark(boolean on, String key) {
        Component c = Component.translatable(key);
        return on ? Component.literal("> ").append(c).append(" <") : c;
    }

    private void save() {
        HYCConfig.Rule updated = new HYCConfig.Rule(this.namespace, this.path, this.selected);
        List<HYCConfig.Rule> list = new ArrayList<>(HYCConfig.rules());
        if (this.original == null) {
            list.add(updated);
        } else {
            int idx = list.indexOf(this.original);
            if (idx >= 0) {
                list.set(idx, updated);
            } else {
                list.add(updated);
            }
        }
        HYCConfig.saveRules(list);
        this.onClose();
    }

    /** 编辑页内直接删除本规则（与列表行的删除按钮同一条数据通路） */
    private void delete() {
        List<HYCConfig.Rule> list = new ArrayList<>(HYCConfig.rules());
        list.remove(this.original);
        HYCConfig.saveRules(list);
        this.onClose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        this.renderBackground(g);
        int cx = this.width / 2;
        int top = this.height / 2 - 64;

        g.drawCenteredString(this.font, this.title, cx, top - 18, 0xFFFFFF);

        // 物品图标 + 悬停名字；整族规则显示 命名空间:*
        if (!this.icon.isEmpty()) {
            g.renderItem(this.icon, cx - 8, top);
            if (mouseX >= cx - 8 && mouseX < cx + 8 && mouseY >= top && mouseY < top + 16) {
                g.renderTooltip(this.font, this.icon, mouseX, mouseY);
            }
        } else {
            g.drawCenteredString(this.font, this.namespace + ":*", cx, top + 4, 0xFFCC55);
        }

        g.drawCenteredString(this.font, Component.translatable("holdyourcamera.scope"), cx, top + 26, 0xAAAAAA);
        g.drawCenteredString(this.font, Component.translatable("holdyourcamera.perspective"), cx, top + 70, 0xAAAAAA);

        super.render(g, mouseX, mouseY, partial);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
