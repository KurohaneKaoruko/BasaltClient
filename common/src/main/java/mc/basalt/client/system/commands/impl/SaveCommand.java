package mc.basalt.client.system.commands.impl;

import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.config.ConfigManager;
import mc.basalt.client.ui.notifications.NotificationManager;

public class SaveCommand extends Command {
    public SaveCommand() {
        super("save", "手动保存配置", ".save");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            CommandManager.addChatMessage("用法错误: " + getSyntax());
            return;
        }
        ConfigManager.save();
        NotificationManager.success("配置", "已保存");
    }
}
