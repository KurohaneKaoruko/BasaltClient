package mc.basalt.client.system.commands;

import mc.basalt.client.system.commands.impl.BindCommand;
import mc.basalt.client.system.commands.impl.HelpCommand;
import mc.basalt.client.system.commands.impl.HudCommand;
import mc.basalt.client.system.commands.impl.PanicCommand;
import mc.basalt.client.system.commands.impl.ToggleCommand;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final List<Command> commands = new ArrayList<>();
    public static final String PREFIX = ".";

    public CommandManager() {
        init();
    }

    private void init() {
        add(new BindCommand());
        add(new ToggleCommand());
        add(new PanicCommand());
        add(new HelpCommand());
        add(new HudCommand());

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.startsWith(PREFIX)) {
                run(message);
                return false;
            }
            return true;
        });
    }

    private void add(Command command) {
        commands.add(command);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void run(String message) {
        String[] args = message.substring(PREFIX.length()).split(" ");
        if (args.length == 0) {
            return;
        }

        String commandName = args[0];
        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(commandName)) {
                String[] commandArgs = new String[args.length - 1];
                System.arraycopy(args, 1, commandArgs, 0, args.length - 1);
                try {
                    command.execute(commandArgs);
                } catch (Exception e) {
                    addChatMessage("命令执行出错: " + e.getMessage());
                    e.printStackTrace();
                }
                return;
            }
        }

        addChatMessage("未知命令: " + commandName + "，输入 .help 查看命令列表。");
    }

    public static void addChatMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("[Basalt] ").formatted(Formatting.GRAY)
                            .append(Text.literal(message).formatted(Formatting.WHITE)),
                    false
            );
        }
    }
}
