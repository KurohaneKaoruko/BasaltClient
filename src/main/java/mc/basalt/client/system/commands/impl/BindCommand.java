package mc.basalt.client.system.commands.impl;

import mc.basalt.client.system.commands.Command;
import mc.basalt.client.system.commands.CommandManager;
import mc.basalt.client.system.modules.Module;
import mc.basalt.client.system.modules.Modules;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Bind a module to a key", ".bind <module> <key>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 2) {
            CommandManager.addChatMessage("Invalid syntax. Usage: " + getSyntax());
            return;
        }

        String moduleName = args[0];
        String keyName = args[1].toUpperCase(Locale.ROOT);

        Module module = Modules.get().getByName(moduleName);
        if (module == null) {
            CommandManager.addChatMessage("Module '" + moduleName + "' not found.");
            return;
        }

        int key;
        if (keyName.equalsIgnoreCase("NONE")) {
            key = -1;
        } else {
            try {
                // Try to parse as integer first (legacy support)
                key = Integer.parseInt(keyName);
            } catch (NumberFormatException e) {
                // Parse key name using InputUtil
                try {
                    key = InputUtil.fromTranslationKey("key.keyboard." + keyName.toLowerCase()).getCode();
                } catch (Exception ex) {
                    // Fallback to simple GLFW lookup or error
                    // Since InputUtil might be tricky with "key.keyboard.", let's try a simpler
                    // approach if possible.
                    // Actually, InputUtil.fromTranslationKey expects translation keys like
                    // "key.keyboard.a".
                    // A better way is InputUtil.getType(InputUtil.Type.KEYSYM, name).getCode() but
                    // name must be valid.

                    // Let's iterate GLFW keys or use a helper if we had one.
                    // For now, let's assume the user types the GLFW key name like "R" or
                    // "RIGHT_SHIFT".
                    // But standard users type "R".

                    // Simplified: Use InputUtil to get key from name.
                    // "key.keyboard.r" -> R

                    try {
                        key = InputUtil.fromTranslationKey("key.keyboard." + keyName.toLowerCase()).getCode();
                    } catch (IllegalArgumentException ie) {
                        CommandManager.addChatMessage("Unknown key: " + keyName);
                        return;
                    }
                }
            }
        }

        module.setKey(key);
        CommandManager.addChatMessage("Bound " + module.getDisplayName() + " to " + keyName);
    }
}
