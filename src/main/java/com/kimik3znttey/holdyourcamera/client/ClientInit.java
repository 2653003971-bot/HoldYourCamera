package com.kimik3znttey.holdyourcamera.client;

import com.kimik3znttey.holdyourcamera.client.gui.RulesScreen;
import com.kimik3znttey.holdyourcamera.config.HYCConfig;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 仅客户端执行的初始化入口（由主类通过 DistExecutor 安全调用）。
 */
public final class ClientInit {

    public static void register() {
        ModLoadingContext ctx = ModLoadingContext.get();

        // 客户端配置文件：config/holdyourcamera-client.toml
        ctx.registerConfig(ModConfig.Type.CLIENT, HYCConfig.SPEC);

        // 模组列表里的 Config 按钮 -> 打开规则管理界面
        ctx.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new RulesScreen(parent)));
    }

    private ClientInit() {
    }
}
