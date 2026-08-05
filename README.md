# HoldYourCamera —— 按主手物品自动切换视角（Forge 1.20.1 客户端模组）

手持指定物品时自动切换视角，**规则全程可视化点选**：把物品拿在手里按 `J`，选个视角就完事，玩家不需要知道任何物品 ID 或语法。

---

## 一、项目结构

```
HoldYourCamera/
└── src/main/
    ├── java/com/kimik3znttey/holdyourcamera/
    │   ├── HoldYourCamera.java        主类：Dist 隔离，把初始化安全交给客户端
    │   ├── client/
    │   │   ├── ClientInit.java        注册配置文件 + 模组列表 Config 按钮
    │   │   ├── HYCKeyMappings.java    J 键注册（mod 总线事件）
    │   │   ├── CameraRuleHandler.java 核心：换手切视角 + J 键处理
    │   │   └── gui/
    │   │       ├── RulesScreen.java   规则管理界面（列表/开关/编辑入口）
    │   │       └── RuleEditScreen.java 添加/编辑规则弹窗（点选范围和视角，编辑模式含删除）
    │   └── config/
    │       └── HYCConfig.java         配置定义 + 规则解析 + GUI 写回
    └── resources/
        ├── META-INF/mods.toml         mod 元数据（占位符由 MDK 构建时替换）
        ├── pack.mcmeta                资源包元数据（1.20.1 用 pack_format 15）
        └── assets/holdyourcamera/lang/
            ├── zh_cn.json             中文界面文本
            └── en_us.json             英文界面文本
```

## 二、合入你的 MDK（3 步）

1. **复制源码**：把 `src/` 合进 MDK 工程（删掉 MDK 自带的 `com.example.examplemod`）。
2. **改 `gradle.properties`**（其余行保持 MDK 原样）：

   ```properties
   mod_id=holdyourcamera
   mod_name=HoldYourCamera
   mod_license=MIT
   mod_version=1.0.0
   mod_group_id=com.kimik3znttey.holdyourcamera
   mod_authors=你的名字
   mod_description=手持物品自动切换视角，规则可视化配置的客户端小工具
   ```

3. **构建**：`./gradlew build`，成品在 `build/libs/`（不带 `-sources` 的 jar）。

## 三、改名跑不起来？自查表（按嫌疑度排序）

改包名/modid 后跑不起来，九成是下面之一：

1. **包名以 `java.` 开头** —— 这是最常见的坑！`java.*` 是 JVM 保留包名，类加载器会直接抛 `SecurityException: Prohibited package name`。`src/main/java/` 里的 `java` 只是源码根目录，**不属于包名**。正确：`com.kimik3znttey.holdyourcamera`。
2. **modid 四处不一致**：`@Mod("holdyourcamera")`、`@Mod.EventBusSubscriber(modid = ...)`、`mods.toml` 的 `modId`、`gradle.properties` 的 `mod_id`，四个地方必须一字不差。
3. **package 声明与目录不一致**：每个 `.java` 第一行的 `package` 必须和它在 `java/` 下的相对路径完全对应。
4. **`mods.toml` 位置不对**：必须在 `src/main/resources/META-INF/` 下，不是 `resources/` 根目录。
5. **`pack.mcmeta` 缺失或 `pack_format` 不对**：1.20.1 用 `15`。
6. **构建缓存**：改完结构后跑一次 `./gradlew clean`，IDE 里重新 import Gradle 工程。
7. **看报错**：`run/logs/latest.log` 第一处 ERROR 通常直接点名问题（找不到 mods.toml、modid 重复、类加载失败等）。

## 四、玩家怎么用（易用性设计）

| 操作 | 效果 |
| --- | --- |
| 拿着物品按 `J` | 弹出添加窗口：物品图标直接渲染（悬停显示名字），点选 `仅此物品 / 整个模组` + 三种视角，保存即生效 |
| 空手按 `J` | 打开规则管理界面 |
| 模组列表 → Config 按钮 | 同样打开管理界面（Forge 官方入口） |

管理界面里可以：勾选两个总开关（恢复原视角 / 强制锁定）、点「编辑」进入规则编辑页（编辑页内含删除按钮）、滚动浏览列表。手写 TOML 的老路也保留：`config/holdyourcamera-client.toml`，保存即热重载。

### 规则行为（默认）

- 命中规则 → 切换视角；不再命中 → 恢复你被接管前的视角（接管时自动记住）
- 手持时仍可 F5 手动覆盖（开了"强制锁定"才会每 tick 锁死）
- 规则按列表顺序匹配，第一条命中的生效

## 五、发布 Checklist

- [ ] 仓库放 `LICENSE` 文件（与 `mod_license` 一致）
- [ ] `mods.toml` 填 `issueTrackerURL`
- [ ] CurseForge / Modrinth 页面勾选 **Client-side only**
- [ ] 介绍页第一句写卖点："拿在手里按 J，点一下就配好"
- [ ] 装/不装拔刀剑两个环境各冒烟一次；NeoForge 1.20.1 也试一次（大概率直接能跑）

## 六、V3 点子

- 可搜索的物品浏览器（给"物品不在手上"的场景）
- 规则上下移动排序（优先级调整）
- 每条规则独立的相机偏移 / 距离
- 平滑运镜过渡
