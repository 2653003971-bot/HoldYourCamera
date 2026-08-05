package com.kimik3znttey.holdyourcamera;

import com.kimik3znttey.holdyourcamera.client.ClientInit;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * HoldYourCamera 主类。
 * 相机是客户端专属逻辑，用 DistExecutor 隔离：
 * 即使有人误把 jar 装进专用服务器，也不会加载任何客户端类导致崩溃。
 */
@Mod(HoldYourCamera.MODID)
public final class HoldYourCamera {

    public static final String MODID = "holdyourcamera";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HoldYourCamera() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientInit::register);
    }
}
