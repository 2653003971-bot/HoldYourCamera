package com.kimik3znttey.holdyourcamera.client;

import com.kimik3znttey.holdyourcamera.HoldYourCamera;
import com.kimik3znttey.holdyourcamera.client.gui.RuleEditScreen;
import com.kimik3znttey.holdyourcamera.client.gui.RulesScreen;
import com.kimik3znttey.holdyourcamera.config.HYCConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 核心逻辑：
 * 1) 每 tick 比对主手物品命中的第一条规则，仅在"命中规则变化"时操作相机；
 * 2) 处理 J 键：拿物品 -> 添加规则窗口，空手 -> 规则管理界面。
 * 平时每 tick 的成本 = 一次注册表查询 + 几次引用比较（纳秒级）。
 */
@Mod.EventBusSubscriber(modid = HoldYourCamera.MODID, value = Dist.CLIENT)
public final class CameraRuleHandler {

    /** 当前生效的规则目标视角；null = 没有规则接管相机 */
    private static CameraType activeTarget = null;
    /** 被规则接管前玩家的视角，用于恢复 */
    private static CameraType userType = CameraType.FIRST_PERSON;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return; // ClientTickEvent 每 tick 触发两次，只取一次

        Minecraft mc = Minecraft.getInstance();

        // ---- 按键：添加规则 / 打开管理界面 ----
        while (HYCKeyMappings.ADD_RULE.consumeClick()) {
            if (mc.player != null && mc.screen == null) {
                ItemStack held = mc.player.getMainHandItem();
                if (held.isEmpty()) {
                    mc.setScreen(new RulesScreen(null));
                } else {
                    mc.setScreen(RuleEditScreen.add(null, held));
                }
            }
        }

        if (mc.player == null) return; // 主菜单等界面无玩家

        // ---- 视角规则 ----
        CameraType target = findTarget(mc.player.getMainHandItem());

        if (target != null) {
            if (activeTarget == null) {
                userType = mc.options.getCameraType(); // 首次被规则接管，记住用户原本视角
            }
            if (HYCConfig.ENFORCE.get()) {
                // 强制锁定：每 tick 校正，F5 也切不走
                if (mc.options.getCameraType() != target) mc.options.setCameraType(target);
            } else if (target != activeTarget) {
                // 仅在换手瞬间切一次，之后允许玩家手动 F5 覆盖
                mc.options.setCameraType(target);
            }
            activeTarget = target;
        } else if (activeTarget != null) {
            // 规则刚刚失效：恢复用户视角（或保持现状，看配置）
            if (HYCConfig.RESTORE_PREVIOUS.get()) mc.options.setCameraType(userType);
            activeTarget = null;
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        activeTarget = null; // 离开存档时复位状态
    }

    private static CameraType findTarget(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (HYCConfig.Rule rule : HYCConfig.rules()) {
            if (rule.matches(id)) return rule.type();
        }
        return null;
    }

    private CameraRuleHandler() {
    }
}
