package mc.basalt.client.event.events;

import mc.basalt.client.event.Event;

/**
 * 鼠标按键事件（MouseHandler#onPress 注入）。
 */
public class MouseEvent extends Event {
    private final int button;
    private final int action;
    private final int mods;

    public MouseEvent(int button, int action, int mods) {
        this.button = button;
        this.action = action;
        this.mods = mods;
    }

    public int getButton() {
        return button;
    }

    public int getAction() {
        return action;
    }

    public int getMods() {
        return mods;
    }

    /** 鼠标按键的模块绑定编码（负数区，避免与键盘键码冲突）。 */
    public static int encode(int button) {
        return -(100 + button);
    }
}
