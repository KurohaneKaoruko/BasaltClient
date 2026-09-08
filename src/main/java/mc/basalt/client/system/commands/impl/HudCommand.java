package mc.basalt.client.system.commands.impl;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.gui.HudEditorScreen;
import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;

public class HudCommand extends Command {
    public HudCommand() {
        super("hud", "打开 HUD 编辑器", ".hud");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 0) {
            CommandManager.addChatMessage("用法错误: " + getSyntax());
            return;
        }

        if (BasaltClient.MC != null && BasaltClient.MC.player != null) {
            BasaltClient.MC.setScreen(new HudEditorScreen());
        }
    }
}
