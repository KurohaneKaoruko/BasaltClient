# Basalt Client (玄武岩客户端)

一个 Minecraft Java 版的客户端辅助 MOD（Fabric），包含多项作弊/辅助功能。

- **游戏版本**: Minecraft 1.21.1
- **加载器**: Fabric Loader 0.16.14+
- **依赖**: Fabric API

## 构建

```bash
./gradlew build
```

产物位于 `build/libs/`。

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
- **XRay** 矿物透视 — 仅显示矿物
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
- 水印 / 模块列表 / 坐标与 FPS / 护甲状态，位置可通过 HUD 编辑器自由拖拽并持久化
- 右下角弹出通知（模块开关、命令反馈）

## 配置

配置保存在 `.minecraft/basalt/config.json`，包含模块开关、按键、设置与 HUD 布局，退出游戏自动保存。

## 声明

本项目仅供学习与研究客户端 MOD 开发使用。
