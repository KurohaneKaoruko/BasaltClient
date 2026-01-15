package mc.basalt.client.system.commands.impl;

import mc.basalt.client.BasaltClient;
import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "Lists all commands", ".help");
    }

    @Override
    public void execute(String[] args) {
        CommandManager.addChatMessage("§bBasalt Client " + BasaltClient.VERSION + " Commands:");
        for (Command command : BasaltClient.INSTANCE.getCommandManager().getCommands()) {
            CommandManager.addChatMessage("§7" + command.getName() + " §8- §f" + command.getDescription() + " §7("
                    + command.getSyntax() + ")");
        }
    }
}
