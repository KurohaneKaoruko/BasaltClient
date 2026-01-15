package mc.basalt.client.system.modules;

public enum Category {
    Combat("战斗"),
    Movement("移动"),
    Render("渲染"),
    Player("玩家"),
    World("世界"),
    Misc("杂项");

    public final String name;

    Category(String name) {
        this.name = name;
    }
}
