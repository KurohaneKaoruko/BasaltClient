package mc.basalt.client.system.commands.impl;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "显示所有命令", ".help");
    }

    @Override
    public void execute(String[] args) {
        CommandManager.addChatMessage("玄武岩客户端 " + BasaltClient.VERSION + " 命令列表:");
        for (Command command : BasaltClient.INSTANCE.getCommandManager().getCommands()) {
            CommandManager.addChatMessage("." + command.getName() + " - " + command.getDescription() + " (" + command.getSyntax() + ")");
        }
    }
}
