/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.gui.book;

import de.verdox.vcore.impl.gameserver.paper.utils.util.BukkitBookUtil;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class DialogGUI {
    private JavaPlugin vCorePlugin;
    private final Player player;
    private final String headLine;
    private final String text;
    private final Map<TextComponent, Consumer<Player>> buttons = new LinkedHashMap<>();

    public DialogGUI(@NotNull JavaPlugin vCorePlugin, @NotNull Player player, @NotNull String headLine, @NotNull String text) {
        this.vCorePlugin = vCorePlugin;
        this.player = player;
        this.headLine = headLine;
        this.text = text;
    }

    public TextComponent addButton(@NotNull TextComponent textComponent, @NotNull Consumer<Player> consumer) {
        buttons.put(textComponent, consumer);
        return textComponent;
    }

    public void openDialog() {
        createBookGUI(player).openBook();
    }

    public List<Component> getContent() {
        return createBookGUI(player).getPages();
    }

    public BookGUI createBookGUI(@NotNull Player player) {
        BookGUI bookGUI = new BookGUI(vCorePlugin, player);

        List<TextComponent> responsiveButtons = buildResponsiveButtons(bookGUI);
        bookGUI.provideBook(() -> {
            Book.Builder builder = Book.builder();
            TextComponent textComponent = Component.text("");
            if (!headLine.isEmpty()) {
                textComponent = textComponent.append(Component.text(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', headLine))).decoration(TextDecoration.UNDERLINED, true).color(TextColor.fromHexString("#44ffd2")))
                        .append(Component.newline())
                        .append(Component.newline());
            }
            if (!text.isEmpty()) {
                List<String> lines = BukkitBookUtil.getLines(text);

                for (int i = 0; i < lines.size(); i++) {
                    String line = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines.get(i)));
                    textComponent = textComponent.append(Component.newline().append(Component.text(line)).color(TextColor.fromHexString("#774936")));

                    if (i != 0 && i % 12 == 0) {
                        builder.addPage(textComponent);
                        textComponent = Component.text("");
                    }
                    // Page is full
                    if (i == lines.size() - 1)
                        textComponent = addButtonsToEnd(textComponent, responsiveButtons);
                }
            } else
                textComponent = addButtonsToEnd(textComponent, responsiveButtons);
            builder.addPage(textComponent);
            return builder.build();
        });
        return bookGUI;
    }

    private TextComponent addButtonsToEnd(@NotNull TextComponent page, @NotNull List<TextComponent> responsiveButtons) {
        for (int j = 0; j < responsiveButtons.size(); j++) {
            TextComponent responsiveButton = responsiveButtons.get(j);
            page = page.append(Component.newline()).append(Component.newline().append(Component.text("<" + (j + 1) + "> ")).color(responsiveButton.color()).append(responsiveButton));
        }
        return page;
    }

    private List<TextComponent> buildResponsiveButtons(@NotNull BookGUI bookGUI) {
        return buttons.entrySet()
                .stream()
                .map(textComponentConsumerEntry -> {
                    TextComponent textComponent = bookGUI.createResponsiveCallbackText(textComponentConsumerEntry.getKey(), textComponentConsumerEntry.getValue());
                    textComponent = textComponent.hoverEvent(HoverEvent.showText(Component.text("Klicke mich").color(TextColor.fromHexString("#ffbfa0"))));
                    return textComponent;
                })
                .distinct()
                .collect(Collectors.toList());
    }
}
