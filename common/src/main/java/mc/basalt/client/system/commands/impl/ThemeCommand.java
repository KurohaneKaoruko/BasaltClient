package mc.basalt.client.system.commands.impl;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.gui.ThemeScreen;
import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;

public class ThemeCommand extends Command {
    public ThemeCommand() {
        super("theme", "打开主题设置界面", ".theme");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            CommandManager.addChatMessage("用法错误: " + getSyntax());
            return;
        }

        if (BasaltClient.MC != null && BasaltClient.MC.player != null) {
            BasaltClient.MC.setScreen(new ThemeScreen(null));
        }
    }
}
