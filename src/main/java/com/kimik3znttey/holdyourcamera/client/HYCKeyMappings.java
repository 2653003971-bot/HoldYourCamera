package com.kimik3znttey.holdyourcamera.client;

import com.kimik3znttey.holdyourcamera.HoldYourCamera;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 按键注册：默认 J 键。
 * 拿着物品按 J -> 直接弹出"添加规则"窗口；空手按 J -> 打开规则管理界面。
 */
@Mod.EventBusSubscriber(modid = HoldYourCamera.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HYCKeyMappings {

    public static final KeyMapping ADD_RULE = new KeyMapping(
            "key.holdyourcamera.add_rule",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "key.categories.holdyourcamera");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ADD_RULE);
    }

    private HYCKeyMappings() {
    }
}
