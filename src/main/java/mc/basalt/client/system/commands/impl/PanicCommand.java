package mc.basalt.client.system.commands.impl;

import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import mc.basalt.client.ui.notifications.NotificationManager;

public class PanicCommand extends Command {
    public PanicCommand() {
        super("panic", "一键关闭所有已开启的模块", ".panic");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            CommandManager.addChatMessage("用法错误: " + getSyntax());
            return;
        }

        int disabled = 0;
        for (Module module : Modules.get().getAll()) {
            if (module.isEnabled()) {
                module.disable();
                disabled++;
            }
        }

        CommandManager.addChatMessage("已紧急关闭 " + disabled + " 个模块。");
        NotificationManager.warn("Panic", "已关闭全部模块");
    }
}
