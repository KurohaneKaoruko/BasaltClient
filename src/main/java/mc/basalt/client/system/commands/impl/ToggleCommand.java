package mc.basalt.client.system.commands.impl;

import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import net.minecraft.util.Formatting;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module", ".toggle <module>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            CommandManager.addChatMessage("Invalid syntax. Usage: " + getSyntax());
            return;
        }

        String moduleName = args[0];
        Module module = Modules.get().getByName(moduleName);

        if (module == null) {
            CommandManager.addChatMessage("Module '" + moduleName + "' not found.");
            return;
        }

        module.toggle();
        CommandManager.addChatMessage("Toggled " + module.getDisplayName() + (module.isEnabled() ? " §aON" : " §cOFF"));
    }
}
