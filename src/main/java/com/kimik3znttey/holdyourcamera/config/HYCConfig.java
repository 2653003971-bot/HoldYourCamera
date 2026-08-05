package com.kimik3znttey.holdyourcamera.config;

import com.kimik3znttey.holdyourcamera.HoldYourCamera;
import net.minecraft.client.CameraType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 配置定义、规则解析与写回。
 * 配置文件：config/holdyourcamera-client.toml。
 * 玩家可以在 GUI 里点选规则（推荐），也可以直接编辑 TOML，保存后自动热重载。
 */
@Mod.EventBusSubscriber(modid = HoldYourCamera.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HYCConfig {

    public static final ForgeConfigSpec SPEC;

    /** 规则列表，自上而下第一条命中的生效 */
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RULES;
    /** 规则不再命中时，恢复被接管前的视角 */
    public static final ForgeConfigSpec.BooleanValue RESTORE_PREVIOUS;
    /** 规则命中期间每 tick 强制锁定视角 */
    public static final ForgeConfigSpec.BooleanValue ENFORCE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("HoldYourCamera - 按主手物品自动切换视角").push("camera");

        RULES = b.comment(
                "每条规则格式：命名空间:物品ID=视角   或   命名空间:*=视角（匹配该模组全部物品）",
                "视角取值：first_person / third_person_back / third_person_front",
                "多条规则按顺序匹配，第一条命中的生效。",
                "提示：游戏内拿着物品按 J 键可以可视化添加规则，无需手写")
                .defineList("itemRules",
                        List.of("slashblade:*=third_person_back"),
                        o -> o instanceof String);

        RESTORE_PREVIOUS = b.comment(
                "true  = 规则不再命中时，恢复到你被接管前使用的视角；",
                "false = 保持当前视角不变（自己按 F5 调整）")
                .define("restorePreviousPerspective", true);

        ENFORCE = b.comment(
                "true  = 规则命中期间每 tick 强制锁定视角，按 F5 也切不走；",
                "false = 只在换手瞬间切一次视角，之后仍可手动 F5 覆盖（推荐）")
                .define("enforcePerspective", false);

        b.pop();
        SPEC = b.build();
    }

    // ------------------------------------------------------------------
    // 规则模型
    // ------------------------------------------------------------------

    /** 一条已解析的规则；path 为 null 表示整个命名空间通配 */
    public record Rule(String namespace, String path, CameraType type) {

        public boolean matches(ResourceLocation id) {
            return id.getNamespace().equals(namespace) && (path == null || id.getPath().equals(path));
        }

        /** 序列化回配置文本，如 slashblade:*=third_person_back */
        public String serialize() {
            return namespace + ":" + (path == null ? "*" : path) + "=" + type.name().toLowerCase(Locale.ROOT);
        }

        /** 从手持物品生成一条草稿规则（默认第三人称背面） */
        public static Rule fromItem(ItemStack stack) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return new Rule(id.getNamespace(), id.getPath(), CameraType.THIRD_PERSON_BACK);
        }
    }

    private static volatile List<Rule> parsedRules = List.of();

    public static List<Rule> rules() {
        return parsedRules;
    }

    /** GUI 写回入口：把规则列表存进配置文件并立即生效 */
    public static void saveRules(List<Rule> rules) {
        List<String> raw = new ArrayList<>();
        for (Rule r : rules) {
            raw.add(r.serialize());
        }
        RULES.set(raw); // 写回并保存到 TOML
        reparse();      // 立即重新解析，不用等文件监听
    }

    // ------------------------------------------------------------------
    // 解析
    // ------------------------------------------------------------------

    public static void reparse() {
        List<Rule> out = new ArrayList<>();
        for (String raw : RULES.get()) {
            try {
                out.add(parse(raw));
            } catch (Exception e) {
                HoldYourCamera.LOGGER.warn("[HoldYourCamera] 规则 \"{}\" 解析失败（{}），已跳过", raw, e.getMessage());
            }
        }
        parsedRules = List.copyOf(out);
        HoldYourCamera.LOGGER.info("[HoldYourCamera] 已加载 {} 条视角规则", parsedRules.size());
    }

    private static Rule parse(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        int eq = s.indexOf('=');
        if (eq <= 0 || eq == s.length() - 1) {
            throw new IllegalArgumentException("缺少 '=' 或视角");
        }

        CameraType type = CameraType.valueOf(s.substring(eq + 1).trim().toUpperCase(Locale.ROOT));

        String item = s.substring(0, eq).trim();
        int colon = item.indexOf(':');
        if (colon <= 0) {
            throw new IllegalArgumentException("物品 ID 缺少命名空间");
        }
        String path = item.substring(colon + 1);
        if (path.isEmpty() || path.equals("*")) {
            path = null; // 整族通配
        }
        return new Rule(item.substring(0, colon), path, type);
    }

    // ------------------------------------------------------------------
    // 配置文件加载 / 热重载
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(HoldYourCamera.MODID)) reparse();
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(HoldYourCamera.MODID)) reparse();
    }

    private HYCConfig() {
    }
}
