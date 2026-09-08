# Basalt Client (玄武岩客户端)

一个 Minecraft Java 版的客户端辅助 MOD，同时支持 **Fabric** 与 **NeoForge** 加载器。

- **游戏版本**: Minecraft 1.21.1
- **加载器**: Fabric Loader 0.16.14+ / NeoForge 21.1.x
- **Java**: 21+

## 项目结构（多加载器单仓库）

```
common/    共享源码（事件系统、模块、GUI、HUD、mixin）——使用 Mojang 官方映射
fabric/    Fabric 模块（Loom 构建，入口适配器 + fabric.mod.json + Access Widener）
neoforge/  NeoForge 模块（ModDevGradle 构建，@Mod 入口 + neoforge.mods.toml + Access Transformer）
```

两个加载器模块都把 `common` 并入各自的源码集，基于同一份代码分别编译打包。
原版调用集中在 mixin 与渲染工具层，模块逻辑只依赖自建事件总线，便于后续多版本移植。

## 构建

```bash
./gradlew build
# 产物：
#   fabric/build/libs/basalt-client-fabric-<版本>.jar
#   neoforge/build/libs/basalt-client-neoforge-<版本>.jar
```

开发运行：

```bash
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```

## 使用

| 操作 | 说明 |
| --- | --- |
| `Right Shift` | 打开 / 关闭 ClickGUI |
| `H` | 打开 HUD 编辑器（拖拽移动、右键显示/隐藏，退出自动保存） |
| `.help` | 查看聊天命令列表 |
| `.toggle <模块>` | 开关模块 |
| `.bind <模块> <按键>` | 绑定按键 |
| `.panic` | 一键关闭所有模块 |
| `.hud` | 打开 HUD 编辑器 |

ClickGUI 内：**左键** 开关模块，**右键** 打开设置，**中键** 直接绑定按键，直接打字即可搜索。

## 功能列表

### Combat（战斗）
- **Killaura** 杀戮光环 — 自动攻击最近目标（旋转视角 / 透墙 / 实体过滤）
- **Velocity** 反击退 — 取消击退与爆炸位移
- **AutoTotem** 自动图腾 — 低血量自动切换副手图腾
- **AutoArmor** 自动穿甲 — 自动装备最优护甲
- **AutoCrystal** 自动水晶 — 自动放置与破坏末影水晶
- **Criticals** 刀刀暴击 — 攻击时自动暴击（Packet/Jump）
- **AutoClicker** 自动连点 — 按住攻击键自动连点（CPS 区间）

### Movement（移动）
- **Flight** 飞行 — 生存飞行，可调速度
- **Speed** 速度 — Vanilla 蹦跳 / Strafe 平移加速
- **NoFall** 无摔落 — 免摔落伤害
- **Step** 自动上台阶 — 无需跳跃走上高处
- **Jesus** 水上行走
- **Scaffold** 自动铺路 — 脚下自动放置方块
- **SafeWalk** 安全行走 — 边缘不再掉落
- **EntityControl** 实体控制 — 骑乘实体飞行控制

### Render（渲染）
- **ESP** 实体透视 — 实体方框/填充/连线，穿墙可见
- **Tracers** 实体连线 — 屏幕中心追踪线
- **Nametags** 名牌显示 — 透墙名牌 + 血量/距离
- **Fullbright** 夜视
- **XRay** 矿物透视 — 只显示矿物
- **Freecam** 自由视角 — 相机脱离身体，身体原地冻结

### Player（玩家）
- **FastPlace** 快速放置
- **ChestStealer** 自动拿取箱子
- **Blink** 瞬移 — 暂停发包
- **AutoEat** 自动进食 — 饥饿时自动吃食物
- **AutoRespawn** 自动重生
- **AutoTool** 自动切工具 — 挖掘时切换最快工具

### Misc（杂项）
- **Timer** 变速齿轮 — 改变游戏运行速度
- **AntiAFK** 防挂机 — 定时动作避免被踢

### HUD
- 水印 / 模块列表 / 坐标与 FPS / 护甲状态，锚点定位分辨率自适应，可拖拽并持久化
- 右下角弹出通知（模块开关、命令反馈）

## 配置

配置保存在 `.minecraft/basalt/config.json`（两种加载器共用同一位置与格式），包含模块开关、按键、设置与 HUD 布局，退出游戏自动保存。

## 多版本开发约定

- `main` 始终跟随最新支持的 MC 版本；旧版本使用 `MC<x.y.z>` 维护分支，仅通过 cherry-pick 同步修复
- **版本号规则 `vA.B.C`**：`A` 大版本仅在明确要求时迭代；`B` 仅在新增功能时递增；`C` 在修复 bug 时递增
- 发布版本号格式 `模组版本+MC版本`（如 `0.3.3+1.21.4`），与 tag 一致；`+MC版本` 后缀仅供发布渠道识别，**GUI 内显示不带后缀**
- 升级 MC 版本：切新分支 → 更新 `gradle.properties` 中版本四件套 → 修复编译（集中在 `common/.../mixin/` 与渲染工具）→ 双加载器 `runClient` 验证 → `main` 跟进

## 自动发布（GitHub Actions）

- **推送 `v*` 标签**（如 `v0.3.3+1.21.4`）自动触发 `release` 工作流：构建双加载器 jar 并创建 GitHub Release 附上文件，用户直接下载放入 `mods` 即可使用
- 日常推送 / PR 由 `build` 工作流做编译验证，产物可在 Action 页面下载
- 发布流程：本地验证 → 更新版本号 → 提交 → `git tag v<版本>+<MC版本> && git push --tags`

## 声明

本项目仅供学习与研究客户端 MOD 开发使用。
