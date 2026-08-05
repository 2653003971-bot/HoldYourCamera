# HoldYourCamera —— 按主手物品自动切换视角（Forge 1.20.1 客户端模组）

手持指定物品时自动切换视角，**规则全程可视化点选**：把物品拿在手里按 `J`，在默认的三个视角中选取一个，无需手动输入nbt标签或矫正语法格式。

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

## 二、易用性设计

| 操作 | 效果 |
| --- | --- |
| 拿着物品按 `J` | 弹出添加窗口：物品图标直接渲染（悬停显示名字），点选 `仅此物品 / 整个模组` + 三种视角，保存即生效 |
| 空手按 `J` | 打开规则管理界面 |
| 模组列表 → Config 按钮 | 同样打开管理界面（Forge 官方入口） |

管理界面里可以：勾选两个总开关（恢复原视角 / 强制锁定）、点「编辑」进入规则编辑页（编辑页内含删除按钮）、滚动浏览列表。支持传统手写 TOML：`config/holdyourcamera-client.toml`，保存即可热重载。

### 规则行为（默认）

- 命中规则 → 切换视角；不再命中 → 恢复你被接管前的视角（接管时自动记住）
- 手持时仍可 F5 手动覆盖（开了"强制锁定"才会每 tick 锁死）
- 规则按列表顺序匹配，第一条命中的生效

# HoldYourCamera — Store Page (English)

> Paste-ready for CurseForge / Modrinth. Suggested summary line first, then the long description.

## Summary (short, one-liner)

Hold an item, press J, pick a camera — per-item perspective rules with zero config-file knowledge required.

## Description (long)

**HoldYourCamera** is a tiny client-side QoL mod that switches your camera perspective automatically based on what you're holding. First person for pickaxes, third-person-back for swords, third-person-front for showing off your skin — set it once, and it just works every time you swap items.

### Point-and-click rules — no IDs, no TOML

- **Hold any item and press J**: an editor pops up with the item's icon rendered right there. Pick a scope — *this item only* or *the whole mod* — pick one of three perspectives (first person / third person back / third person front), save, done.
- **Empty-hand J** (or the **Config** button in Forge's mod list) opens the rule manager: global toggles, edit entries, and deletion (inside each rule's edit page).
- Rules are matched top-to-bottom; first match wins.

### Sensible defaults

- When a rule matches, your camera switches; when it stops matching, your **previous perspective is restored** automatically (it remembers what you had before taking over).
- You can still override manually with F5 at any time — unless you enable the global **lock** toggle, which holds the perspective every tick while a rule matches.
- Everything is client-side and per-player: rules live in `config/holdyourcamera-client.toml`, hot-reloaded on save if you prefer hand-editing.

### Requirements

- Minecraft 1.20.1, Forge (expected to run on NeoForge 1.20.1 47.1.x)
- **Client-side only** — safe to use on any server; nothing is sent, nothing is required server-side.

Open source under MIT. Issues and PRs welcome on GitHub.

