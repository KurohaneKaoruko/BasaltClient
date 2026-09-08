package mc.basalt.client.system.commands.impl;

import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.config.PresetManager;
import mc.basalt.client.ui.notifications.NotificationManager;

import java.util.List;

/**
 * 预设管理：.preset save/load/list/delete <名称>
 */
public class PresetCommand extends Command {
    public PresetCommand() {
        super("preset", "配置预设管理（不同场景快速切换）",
                ".preset save|load|list|delete <名称>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            CommandManager.addChatMessage("用法: " + getSyntax());
            CommandManager.addChatMessage("示例: .preset save pvp | .preset load pvp | .preset list");
            return;
        }

        String action = args[0].toLowerCase(java.util.Locale.ROOT);

        switch (action) {
            case "list" -> {
                List<String> presets = PresetManager.list();
                if (presets.isEmpty()) {
                    CommandManager.addChatMessage("暂无预设，使用 .preset save <名称> 创建。");
                } else {
                    String active = PresetManager.getActivePreset();
                    StringBuilder sb = new StringBuilder("预设列表: ");
                    for (int i = 0; i < presets.size(); i++) {
                        if (i > 0) {
                            sb.append(", ");
                        }
                        sb.append(presets.get(i));
                        if (presets.get(i).equals(active)) {
                            sb.append("(当前)");
                        }
                    }
                    CommandManager.addChatMessage(sb.toString());
                }
            }
            case "save", "load", "apply", "delete" -> {
                if (args.length != 2) {
                    CommandManager.addChatMessage("用法: .preset " + action + " <名称>");
                    return;
                }
                String name = args[1];
                if (!PresetManager.isValidName(name)) {
                    CommandManager.addChatMessage("预设名仅支持字母/数字/下划线/连字符/中文（1-32位）。");
                    return;
                }

                switch (action) {
                    case "save" -> {
                        if (PresetManager.save(name)) {
                            NotificationManager.success("预设", "已保存: " + name);
                            CommandManager.addChatMessage("预设 [" + name + "] 已保存。");
                        } else {
                            CommandManager.addChatMessage("保存失败，详见日志。");
                        }
                    }
                    case "load", "apply" -> {
                        if (PresetManager.apply(name)) {
                            NotificationManager.success("预设", "已切换: " + name);
                            CommandManager.addChatMessage("已切换到预设 [" + name + "]。");
                        } else {
                            CommandManager.addChatMessage("预设 [" + name + "] 不存在。");
                        }
                    }
                    case "delete" -> {
                        if (PresetManager.delete(name)) {
                            NotificationManager.info("预设", "已删除: " + name);
                            CommandManager.addChatMessage("预设 [" + name + "] 已删除。");
                        } else {
                            CommandManager.addChatMessage("预设 [" + name + "] 不存在。");
                        }
                    }
                    default -> {
                    }
                }
            }
            default -> CommandManager.addChatMessage("未知子命令: " + action + "，用法: " + getSyntax());
        }
    }
}
