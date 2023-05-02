/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.gui;

import de.verdox.vcore.impl.gameserver.paper.utils.gui.event.PlayerOpenVCoreGUIEvent;
import net.wesjd.anvilgui.AnvilGUI;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class VCoreGUI<T> implements CustomGUI {
    private final Player player;

    private final Map<Integer, InventoryContent<T>> itemCache;
    private final Consumer<Player> closeListener;
    private final Function<VCoreGUIClick<T>, Response<?, ?>> onItemClick;
    private final Function<GUIClick, Response<?, ?>> onPlayerInventoryClick;
    private final BiFunction<Player, List<ItemStack>, Response<?, ?>> completeFunction;
    private final boolean updater;
    private final Consumer<ContentBuilder<T>> consumer;
    private final boolean enableDoubleClick;
    private final Predicate<Player> openPredicate;
    private final boolean preventClose;
    private final VCoreGUIListener listener;
    private final String inventoryTitle;
    private final int size;
    private final InventoryType inventoryType;
    private final JavaPlugin plugin;
    private boolean open;
    private Inventory inventory;

    private BukkitTask guiUpdater;

    public VCoreGUI(JavaPlugin plugin
            , Player player
            , String inventoryTitle
            , int size
            , InventoryType inventoryType
            , boolean preventClose
            , Map<Integer, InventoryContent<T>> itemCache
            , Consumer<Player> closeListener
            , Function<VCoreGUIClick<T>, Response<?, ?>> onItemClick
            , Function<GUIClick, Response<?, ?>> onPlayerInventoryClick, BiFunction<Player, List<ItemStack>, Response<?, ?>> completeFunction
            , boolean updater
            , Consumer<ContentBuilder<T>> consumer
            , boolean enableDoubleClick
            , Predicate<Player> openPredicate
    ) {
        this.onPlayerInventoryClick = onPlayerInventoryClick;
        this.completeFunction = completeFunction;
        this.updater = updater;
        this.consumer = consumer;
        this.enableDoubleClick = enableDoubleClick;
        this.openPredicate = openPredicate;
        this.listener = new VCoreGUIListener();
        this.plugin = plugin;
        this.player = player;
        this.inventoryTitle = inventoryTitle;
        this.size = size;
        this.inventoryType = inventoryType;
        this.preventClose = preventClose;
        this.itemCache = itemCache;
        this.closeListener = closeListener;
        this.onItemClick = onItemClick;

        openInventory();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void openInventory() {

        PlayerOpenVCoreGUIEvent playerOpenVCoreGUIEvent = new PlayerOpenVCoreGUIEvent(player, this);
        Bukkit.getPluginManager().callEvent(playerOpenVCoreGUIEvent);
        if (playerOpenVCoreGUIEvent.isCancelled())
            return;

        if (this.openPredicate != null && !openPredicate.test(player))
            return;
        Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
        if (this.size == 0) {
            if (inventoryType == null)
                throw new IllegalArgumentException("Please specify either an InventoryType or an InventorySize");
            this.inventory = Bukkit.createInventory(player, inventoryType, ChatColor.translateAlternateColorCodes('&', inventoryTitle));
        } else {
            this.inventory = Bukkit.createInventory(player, size, ChatColor.translateAlternateColorCodes('&', inventoryTitle));
        }

        itemCache.forEach((slot, inventoryContent) -> {
            if (inventory.getSize() <= inventoryContent.slot)
                return;
            if (inventoryContent.slot < 0)
                return;
            inventory.setItem(inventoryContent.slot, inventoryContent.stack);
        });

        this.open = true;
        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inventory));

        if (updater) {
            this.guiUpdater = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateGUI, 20L, 20L);
        }
    }

    private synchronized void executeGUIUpdate() {
        if (inventory.getViewers().size() <= 0)
            guiUpdater.cancel();

        ContentBuilder<T> contentBuilder = new ContentBuilder<T>();
        consumer.accept(contentBuilder);

        // Removing every item that was changed in contentBuilder since last update
        this.itemCache.forEach((integer, inventoryContent) -> {
            if (contentBuilder.getItemCache().containsValue(inventoryContent))
                return;
            removeItem(integer);
        });

        contentBuilder.getItemCache().forEach((itemSlot, inventoryContent) -> {
            if (inventoryContent.slot < 0)
                return;

            if (inventory.getSize() <= inventoryContent.slot)
                return;

            if (itemCache.containsKey(itemSlot))
                if (itemCache.get(itemSlot).equals(inventoryContent))
                    return;

            itemCache.put(itemSlot, inventoryContent);
            inventory.setItem(inventoryContent.slot, inventoryContent.stack);
        });
        if (openPredicate != null && !openPredicate.test(player))
            Bukkit.getScheduler().runTask(plugin, this::closeInventory);
    }

    public void updateGUI() {
        //if(Bukkit.isPrimaryThread())
        //    VCorePaper.getInstance().async(this::executeGUIUpdate);
        //else
        executeGUIUpdate();
    }

    private VCoreGUI<T> copy() {
        return new VCoreGUI<>(plugin
                , player
                , inventoryTitle
                , size
                , inventoryType
                , preventClose
                , itemCache
                , closeListener
                , onItemClick
                , onPlayerInventoryClick, completeFunction
                , updater
                , consumer
                , enableDoubleClick
                , openPredicate
        );
    }

    private void closeInventory() {
        if (open) {
            open = false;
            player.closeInventory();
            HandlerList.unregisterAll(this.listener);
            if (this.completeFunction != null)
                this.completeFunction.apply(player, Arrays.stream(inventory.getContents())
                                                          .collect(Collectors.toList()));
            if (this.closeListener != null)
                closeListener.accept(player);
        }
    }

    public void removeItem(int slot) {
        InventoryContent<T> content = itemCache.remove(slot);
        inventory.remove(content.stack);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public static class Builder<T> {
        private Consumer<Player> closeListener;
        private boolean preventClose;
        private Map<Integer, InventoryContent<T>> itemCache;
        private Function<VCoreGUIClick<T>, Response<?, ?>> onItemClick;
        private Function<GUIClick, Response<?, ?>> onPlayerInventoryClick;
        private BiFunction<Player, List<ItemStack>, Response<?, ?>> completeFunction;
        private JavaPlugin plugin;
        private String title = "VCore GUI";
        private Consumer<ContentBuilder<T>> consumer;
        private int size = 0;
        private InventoryType inventoryType;
        private boolean update;
        private boolean enableDoubleClick;
        private Predicate<Player> openPredicate;

        public Builder<T> update() {
            this.update = true;
            return this;
        }

        public Builder<T> openCondition(Predicate<Player> predicate) {
            this.openPredicate = predicate;
            return this;
        }

        public Builder<T> enableDoubleClick() {
            this.enableDoubleClick = true;
            return this;
        }

        public Builder<T> preventClose() {
            this.preventClose = true;
            return this;
        }

        public Builder<T> size(int size) {
            this.size = size;
            return this;
        }

        public Builder<T> onClose(Consumer<Player> closeListener) {
            Validate.notNull(closeListener, "closeListener cannot be null");
            this.closeListener = closeListener;
            return this;
        }

        public Builder<T> content(Consumer<ContentBuilder<T>> consumer) {
            Validate.notNull(consumer, "supplier cannot be null");
            ContentBuilder<T> contentBuilder = new ContentBuilder<>();
            consumer.accept(contentBuilder);
            this.itemCache = contentBuilder.getItemCache();
            this.consumer = consumer;
            return this;
        }

        public Builder<T> onItemClick(Function<VCoreGUIClick<T>, Response<?, ?>> onItemClick) {
            Validate.notNull(onItemClick, "biFunction cannot be null");
            this.onItemClick = onItemClick;
            return this;
        }

        public Builder<T> onPlayerInventoryClick(Function<GUIClick, Response<?, ?>> onPlayerInventoryClick) {
            Validate.notNull(onPlayerInventoryClick, "onPlayerInventoryClick cannot be null");
            this.onPlayerInventoryClick = onPlayerInventoryClick;
            return this;
        }

        public Builder<T> plugin(JavaPlugin plugin) {
            Validate.notNull(plugin, "Plugin cannot be null");
            this.plugin = plugin;
            return this;
        }

        public Builder<T> title(String title) {
            Validate.notNull(title, "title cannot be null");
            this.title = title;
            return this;
        }

        public Builder<T> type(InventoryType inventoryType) {
            Validate.notNull(inventoryType, "inventoryType cannot be null");
            this.inventoryType = inventoryType;
            return this;
        }

        public Builder<T> completeFunction(BiFunction<Player, List<ItemStack>, Response<?, ?>> completeFunction) {
            Validate.notNull(completeFunction, "completeFunction cannot be null");
            this.completeFunction = completeFunction;
            return this;
        }

        public VCoreGUI<T> open(Player player) {
            Validate.notNull(this.plugin, "Plugin cannot be null");
            Validate.notNull(player, "Player cannot be null");
            if (update)
                Validate.notNull(consumer, "Content must be set!");
            if (inventoryType == null && size <= 0)
                size = 9;
            return new VCoreGUI<>(plugin, player, title, size, inventoryType, preventClose, itemCache, closeListener, onItemClick, onPlayerInventoryClick, completeFunction, update, consumer, enableDoubleClick, openPredicate);
        }

    }

    public static class ContentBuilder<T> {
        private final Map<Integer, InventoryContent<T>> itemCache = new ConcurrentHashMap<>();

        //TODO: Direkt Consumer für Click Callback einfügen
        public ContentBuilder<T> addContent(int slot, ItemStack stack, T object) {
            Validate.notNull(stack, "Stack cannot be null");
            stack.editMeta(meta -> meta.getPersistentDataContainer()
                                       .set(new NamespacedKey("vcore", "vcore_gui_slot"), PersistentDataType.INTEGER, slot));
            itemCache.put(slot, new InventoryContent<>(stack, slot, object));
            return this;
        }

        public ContentBuilder<T> addContent(int slot, ItemStack stack) {
            return addContent(slot, stack, null);
        }

        public ContentBuilder<T> removeItem(int slot) {
            itemCache.remove(slot);
            return this;
        }

        public ContentBuilder<T> fillRowWithItem(int row, ItemStack vCoreItem, T object) {
            for (int i = row * 9; i < (row * 9) + 9; i++)
                addContent(i, vCoreItem, object);
            return this;
        }

        public ContentBuilder<T> fillLeftRightItem(int row, ItemStack vCoreItem, T object) {
            int rowIndex = row * 9;
            addContent(rowIndex, vCoreItem, object);
            addContent(rowIndex + 8, vCoreItem, object);
            return this;
        }

        public ContentBuilder<T> createBorder(int guiSize, ItemStack vCoreItem, T object) {
            int rows = guiSize / 9;

            fillRowWithItem(0, vCoreItem, object);
            fillRowWithItem(rows - 1, vCoreItem, object);

            for (int i = 1; i < rows - 1; i++)
                fillLeftRightItem(i, vCoreItem, object);
            return this;
        }

        public ContentBuilder<T> createBorder(int guiSize) {
            return createBorder(guiSize, new ItemStack(Material.BLACK_STAINED_GLASS_PANE), null);
        }


        Map<Integer, InventoryContent<T>> getItemCache() {
            return itemCache;
        }
    }

    public static class Response<T, R> {
        private final static String CLOSE = "close";
        private final static String NOTHING = "nothing";
        private final static String CONFIRMATION = "confirmation";
        private final static String INPUT = "input";
        private final String code;
        private final Function<T, R> callback;

        private Response(String code, Function<T, R> callback) {
            this.code = code;
            this.callback = callback;
        }

        public static Response<?, ?> close() {
            return new Response<>(CLOSE, null);
        }

        public static Response<?, ?> nothing() {
            return new Response<>(NOTHING, null);
        }

        public static Response<Boolean, AnvilGUI.Response> confirmation(Function<Boolean, AnvilGUI.Response> callback) {
            return new Response<>(CONFIRMATION, callback);
        }

        public static Response<String, AnvilGUI.Response> input(Function<String, AnvilGUI.Response> callback) {
            return new Response<>(INPUT, callback);
        }

        public Function<T, R> getCallback() {
            return callback;
        }
    }

    public static class InventoryContent<T> {
        private final ItemStack stack;
        private final int slot;
        private final T object;

        private InventoryContent(ItemStack stack, int slot, T object) {
            this.stack = stack;
            this.slot = slot;
            this.object = object;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InventoryContent)) return false;
            InventoryContent<?> that = (InventoryContent<?>) o;
            return Objects.equals(stack, that.stack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack);
        }
    }

    public static class GUIClick {
        private final int slot;
        private final ClickType clickType;
        private final ItemStack clickedItem;
        private final Inventory clickedInventory;

        public GUIClick(int slot, ClickType clickType, ItemStack clickedItem, Inventory clickedInventory) {
            this.slot = slot;
            this.clickType = clickType;
            this.clickedItem = clickedItem;
            this.clickedInventory = clickedInventory;
        }

        public int getSlot() {
            return slot;
        }

        public Inventory getClickedInventory() {
            return clickedInventory;
        }

        public ItemStack getClickedItem() {
            return clickedItem;
        }

        public ClickType getClickType() {
            return clickType;
        }
    }

    public static class VCoreGUIClick<T> extends GUIClick {
        private final T dataInItemStack;
        private final VCoreGUI<T> clickedGUI;

        public VCoreGUIClick(int slot, ClickType clickType, ItemStack clickedItem, T dataInItemStack, VCoreGUI<T> clickedGUI) {
            super(slot, clickType, clickedItem, clickedGUI.getInventory());
            this.dataInItemStack = dataInItemStack;
            this.clickedGUI = clickedGUI;
        }

        public VCoreGUI<T> getClickedGUI() {
            return clickedGUI;
        }

        public T getDataInItemStack() {
            return dataInItemStack;
        }
    }

    private class VCoreGUIListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (!e.getInventory().equals(VCoreGUI.this.inventory))
                return;
            e.setCancelled(true);
            player.updateInventory();
            if (!enableDoubleClick && e.getClick().equals(ClickType.DOUBLE_CLICK))
                return;
            if (e.getCurrentItem() == null || e.getCurrentItem().equals(Material.AIR))
                return;
            var clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().equals(Material.AIR))
                return;

            Response<?, ?> response;

            if (e.getClickedInventory().equals(e.getView().getTopInventory())) {
                InventoryContent<T> content = VCoreGUI.this.itemCache.get(e.getSlot());
                if (VCoreGUI.this.onItemClick == null)
                    return;
                try {
                    VCoreGUIClick<T> vCoreGUIClick = new VCoreGUIClick<>(e.getSlot(), e.getClick(), clickedItem, content.object, VCoreGUI.this);
                    response = VCoreGUI.this.onItemClick.apply(vCoreGUIClick);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    return;
                }
            } else {
                if (VCoreGUI.this.onPlayerInventoryClick == null)
                    return;
                //VCoreItem, ClickType, Inventory, Response
                try {
                    GUIClick guiClick = new GUIClick(e.getSlot(), e.getClick(), clickedItem, e.getClickedInventory());
                    response = VCoreGUI.this.onPlayerInventoryClick.apply(guiClick);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    return;
                }
            }

            if (response.code.equals(Response.CLOSE))
                VCoreGUI.this.closeInventory();
            else if (response.code.equals(Response.CONFIRMATION)) {
                new AnvilGUI.Builder()
                        .plugin(getPlugin())
                        .title(ChatColor.translateAlternateColorCodes('&', "&cBestätige mit Ja"))
                        .itemLeft(new ItemStack(Material.BLACK_STAINED_GLASS_PANE))
                        .onLeftInputClick(player -> copy())
                        .onComplete((player, text) -> {
                            Function<Boolean, AnvilGUI.Response> func = (Function<Boolean, AnvilGUI.Response>) response.getCallback();
                            boolean input = text.equalsIgnoreCase("Ja")
                                    || text.equalsIgnoreCase("yes");
                            return func.apply(input);
                        })
                        .onClose(player -> copy())
                        .open(getPlayer());
            } else if (response.code.equals(Response.INPUT)) {
                new AnvilGUI.Builder()
                        .plugin(getPlugin())
                        .title(ChatColor.translateAlternateColorCodes('&', inventoryTitle))
                        .itemLeft(new ItemStack(Material.RED_STAINED_GLASS_PANE))
                        .onLeftInputClick(player -> copy())
                        .onComplete((player1, s) -> {
                            Function<String, AnvilGUI.Response> func = (Function<String, AnvilGUI.Response>) response.getCallback();
                            return func.apply(s);
                        })
                        .onClose(player -> copy())
                        .open(getPlayer());
            }
        }

        @EventHandler
        public void offHandSwitch(PlayerSwapHandItemsEvent e) {
            if (e.getPlayer().getOpenInventory().getTopInventory().equals(VCoreGUI.this.inventory)) {
                e.setCancelled(true);
                player.updateInventory();
            }
        }

        @EventHandler
        public void onInventoryDrag(InventoryDragEvent e) {
            if (e.getInventory().equals(VCoreGUI.this.inventory)) {
                e.setCancelled(true);
                player.updateInventory();
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (VCoreGUI.this.open && e.getInventory().equals(VCoreGUI.this.inventory)) {
                if (VCoreGUI.this.preventClose)
                    Bukkit.getScheduler().runTask(VCoreGUI.this.getPlugin(), VCoreGUI.this::copy);
                else if (VCoreGUI.this.closeListener != null)
                    VCoreGUI.this.closeListener.accept((Player) e.getPlayer());
            }
        }

    }
}
