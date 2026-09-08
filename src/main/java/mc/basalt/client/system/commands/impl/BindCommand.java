package mc.basalt.client.system.commands.impl;

import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import net.minecraft.client.util.InputUtil;

import java.util.Locale;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "为模块绑定按键", ".bind <模块名> <按键>");
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
                return InputUtil.fromTranslationKey(candidate).getCode();
            } catch (IllegalArgumentException ignored) {
            }
        }

        return Integer.MIN_VALUE;
    }
}
