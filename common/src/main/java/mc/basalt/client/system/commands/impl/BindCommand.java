package mc.basalt.client.system.commands.impl;

import mc.basalt.client.event.events.MouseEvent;
import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.Locale;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "为模块绑定按键（鼠标用 mouse1~mouse8）", ".bind <模块名> <按键>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            CommandManager.addChatMessage("用法错误: " + getSyntax());
            return;
        }

        String moduleName = args[0];
        String keyName = args[1];

        Module module = Modules.get().getByName(moduleName);
        if (module == null) {
            CommandManager.addChatMessage("未找到模块: " + moduleName);
            return;
        }

        int key = parseKeyCode(keyName);
        if (key == Integer.MIN_VALUE) {
            CommandManager.addChatMessage("未知按键: " + keyName);
            return;
        }

        module.setKey(key);
        CommandManager.addChatMessage("已将 " + module.getDisplayName() + " 绑定到 " + module.getKeyName());
    }

    private int parseKeyCode(String input) {
        String normalized = input.toLowerCase(Locale.ROOT);
        if ("none".equals(normalized) || "无".equals(normalized)) {
            return -1;
        }

        // 鼠标按键：mouse1~mouse8（1左键 2右键 3中键 4/5 侧键，与 Windows 编号一致）
        if (normalized.startsWith("mouse") || normalized.startsWith("鼠标")) {
            String digits = normalized.replaceAll("\\D", "");
            if (!digits.isEmpty()) {
                int n = Integer.parseInt(digits);
                if (n >= 1 && n <= 8) {
                    return MouseEvent.encode(n - 1);
                }
            }
            return Integer.MIN_VALUE;
        }

        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ignored) {
        }

        String[] candidates = {
                normalized,
                "key.keyboard." + normalized,
                "key.keyboard." + normalized.replace('_', '.')
        };

        for (String candidate : candidates) {
            try {
                return InputConstants.getKey(candidate).getValue();
            } catch (IllegalArgumentException ignored) {
            }
        }

        return Integer.MIN_VALUE;
    }
}
