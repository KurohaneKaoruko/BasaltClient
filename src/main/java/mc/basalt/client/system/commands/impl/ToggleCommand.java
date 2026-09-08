package mc.basalt.client.system.commands.impl;

import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "开关一个模块", ".toggle <模块名>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CommandManager.addChatMessage("用法错误: " + getSyntax());
            return;
        }

        String moduleName = args[0];
        Module module = Modules.get().getByName(moduleName);

        if (module == null) {
            CommandManager.addChatMessage("未找到模块: " + moduleName);
            return;
        }

        module.toggle();
        CommandManager.addChatMessage(module.getDisplayName() + (module.isEnabled() ? " 已开启" : " 已关闭"));
    }
}
