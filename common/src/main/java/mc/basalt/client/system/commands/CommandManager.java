package mc.basalt.client.system.commands;

import mc.basalt.client.system.commands.impl.BindCommand;
import mc.basalt.client.system.commands.impl.HelpCommand;
import mc.basalt.client.system.commands.impl.HudCommand;
import mc.basalt.client.system.commands.impl.PanicCommand;
import mc.basalt.client.system.commands.impl.ToggleCommand;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private static final CommandManager INSTANCE = new CommandManager();
    private final List<Command> commands = new ArrayList<>();
    public static final String PREFIX = ".";

    private CommandManager() {
        init();
    }

    public static CommandManager get() {
        return INSTANCE;
    }

    private void init() {
        add(new BindCommand());
        add(new ToggleCommand());
        add(new PanicCommand());
        add(new HelpCommand());
        add(new HudCommand());
    }

    private void add(Command command) {
        commands.add(command);
    }

    public List<Command> getCommands() {
        return commands;
    }

    /**
     * 聊天消息拦截入口（由 MixinClientPacketListener 调用）。
     * 返回 true 表示消息已被命令系统消费，应取消发送。
     */
    public static boolean tryHandle(String message) {
        if (!message.startsWith(PREFIX)) {
            return false;
        }
        INSTANCE.run(message);
        return true;
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
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            client.player.displayClientMessage(
                    Component.literal("[Basalt] ").withStyle(ChatFormatting.GRAY)
                            .append(Component.literal(message).withStyle(ChatFormatting.WHITE)),
                    false
            );
        }
    }
}
