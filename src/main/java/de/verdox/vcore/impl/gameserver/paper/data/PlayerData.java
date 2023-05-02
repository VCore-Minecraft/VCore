package de.verdox.vcore.impl.gameserver.paper.data;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData implements Serializable {
    public final long saveDate;
    public boolean isFlying;
    public float flySpeed;
    public float walkSpeed;
    public int totalExperiencePoints;
    public int wardenWarningCooldown;
    public int wardenWarningLevel;
    public int wardenTimeSinceLastWarning;
    public GameMode playerGameMode;
    public int foodLevel;
    public float foodExhaustion;
    public double currentHealth;
    public int fireTicks;
    public int ticksLived;
    public int freezeTicks;
    public int noDamageTicks;
    public int maximumNoDamageTicks;
    public int maximumAir;
    public int currentAir;
    public int arrowCooldown;
    public int arrowsInBody;
    public int arrowsStuck;
    public int saturatedRegenRate;
    public int unsaturatedRegenRate;
    public int starvationRate;
    public boolean sleepingIgnored;
    public boolean healthScaled;
    public double healthScale;
    public boolean affectsSpawning;
    public int viewDistance;
    public int sendViewDistance;
    public int simulationDistance;
    public Inventory playerInventory;
    public Inventory enderchestInventory;
    private boolean isInvisible;
    private boolean isInvulnerable;
    private boolean canPickupItems;
    private boolean allowFlight;
    private int beeStingersInBody;
    private double absorptionAmount;
    public Set<NamespacedKey> discoveredRecipes = new HashSet<>();
    public List<PotionEffect> activePotionEffects;
    private final Map<Statistic, Integer> statistics = new ConcurrentHashMap<>();
    private final Map<Statistic, Map<Material, Integer>> materialStatistics = new ConcurrentHashMap<>();
    private final Map<Statistic, Map<EntityType, Integer>> entityStatistics = new ConcurrentHashMap<>();
    private final Map<Attribute, Double> attributeBaseValues = new ConcurrentHashMap<>();
    private final Map<Attribute, Set<AttributeModifier>> attributeModifiers = new ConcurrentHashMap<>();
    private final Set<PlayerAdvancementProgress> advancementProgress = new HashSet<>();

    public static PlayerData getPlayerData(Player player, boolean includeStatistics, boolean includeAttributes, boolean includeAdvancements, boolean includeRecipes) {
        return new PlayerData(player, includeStatistics, includeAttributes, includeAdvancements, includeRecipes);
    }

    private PlayerData(Player player, boolean includeStatistics, boolean includeAttributes, boolean includeAdvancements, boolean includeRecipes) {
        this.saveDate = System.currentTimeMillis();
        this.isFlying = player.isFlying();
        this.flySpeed = player.getFlySpeed();
        this.walkSpeed = player.getWalkSpeed();
        this.totalExperiencePoints = player.getTotalExperience();
        this.wardenWarningCooldown = player.getWardenWarningCooldown();
        this.wardenWarningLevel = player.getWardenWarningLevel();
        this.wardenTimeSinceLastWarning = player.getWardenTimeSinceLastWarning();
        this.playerGameMode = player.getGameMode();

        this.foodLevel = player.getFoodLevel();
        this.foodExhaustion = player.getExhaustion();
        this.currentHealth = player.getHealth();
        this.fireTicks = player.getFireTicks();
        this.ticksLived = player.getTicksLived();
        this.freezeTicks = player.getFreezeTicks();
        this.noDamageTicks = player.getNoDamageTicks();
        this.maximumNoDamageTicks = player.getMaximumNoDamageTicks();
        this.maximumAir = player.getMaximumAir();
        this.currentAir = player.getRemainingAir();
        this.arrowCooldown = player.getArrowCooldown();
        this.arrowsInBody = player.getArrowsInBody();
        this.arrowsStuck = player.getArrowsStuck();
        this.saturatedRegenRate = player.getSaturatedRegenRate();
        this.unsaturatedRegenRate = player.getUnsaturatedRegenRate();
        this.starvationRate = player.getStarvationRate();
        this.sleepingIgnored = player.isSleepingIgnored();
        this.healthScaled = player.isHealthScaled();
        if (this.healthScaled)
            this.healthScale = player.getHealthScale();
        this.affectsSpawning = player.getAffectsSpawning();
        this.viewDistance = player.getViewDistance();
        this.sendViewDistance = player.getSendViewDistance();
        this.simulationDistance = player.getSimulationDistance();

        this.playerInventory = player.getInventory();
        this.enderchestInventory = player.getEnderChest();

        this.activePotionEffects = new LinkedList<>(player.getActivePotionEffects());

        this.isInvisible = player.isInvisible();
        this.isInvulnerable = player.isInvulnerable();
        this.beeStingersInBody = player.getBeeStingersInBody();
        this.canPickupItems = player.getCanPickupItems();
        this.allowFlight = player.getAllowFlight();
        this.absorptionAmount = player.getAbsorptionAmount();

        if (includeRecipes) {
            this.discoveredRecipes = player.getDiscoveredRecipes();
        }

        if (includeAdvancements)
            Bukkit.advancementIterator().forEachRemaining(advancement -> {
                var progress = player.getAdvancementProgress(advancement);
                if (progress.getAwardedCriteria().isEmpty())
                    return;
                advancementProgress.add(new PlayerAdvancementProgress(advancement.getKey(), new HashSet<>(progress.getAwardedCriteria())));
            });

        if (includeStatistics) {
            Arrays.stream(Statistic.values()).forEach(value -> {
                if (value.getType().equals(Statistic.Type.UNTYPED)) {
                    try {
                        var statisticValue = player.getStatistic(value);
                        if (statisticValue == 0)
                            return;
                        statistics.put(value, statisticValue);
                    } catch (IllegalArgumentException ignored) {
                    }
                } else if (value.getType().equals(Statistic.Type.ITEM) || value.getType()
                                                                               .equals(Statistic.Type.BLOCK)) {
                    Arrays.stream(Material.values()).forEach(material -> {
                        try {
                            var statisticValue = player.getStatistic(value, material);
                            if (statisticValue == 0)
                                return;
                            materialStatistics.computeIfAbsent(value, statistic -> new ConcurrentHashMap<>())
                                              .put(material, statisticValue);
                        } catch (IllegalArgumentException ignored) {
                        }
                    });
                } else if (value.getType().equals(Statistic.Type.ENTITY)) {
                    Arrays.stream(EntityType.values()).forEach(entityType -> {
                        try {
                            var statisticValue = player.getStatistic(value, entityType);

                            if (statisticValue == 0)
                                return;
                            entityStatistics.computeIfAbsent(value, statistic -> new ConcurrentHashMap<>())
                                            .put(entityType, statisticValue);
                        } catch (IllegalArgumentException ignored) {
                        }
                    });
                }
            });
        }

        if (includeAttributes) {
            Arrays.stream(Attribute.values()).forEach(attribute -> {
                var attributeInstance = player.getAttribute(attribute);
                if (attributeInstance == null)
                    return;
                attributeBaseValues.put(attribute, attributeInstance.getBaseValue());
                attributeModifiers.put(attribute, new HashSet<>(attributeInstance.getModifiers()));
            });
        }
    }

    public void applyToPlayer(Player player) {
        tryOrThrow(() -> player.getInventory().setContents(this.playerInventory.getContents()));
        tryOrThrow(() -> player.getEnderChest().setContents(this.enderchestInventory.getContents()));
        tryOrThrow(() -> player.setFlying(this.isFlying));
        tryOrThrow(() -> player.setFlySpeed(this.flySpeed));
        tryOrThrow(() -> player.setWalkSpeed(this.walkSpeed));
        tryOrThrow(() -> player.setTotalExperience(this.totalExperiencePoints));
        tryOrThrow(() -> player.setWardenWarningCooldown(this.wardenWarningCooldown));
        tryOrThrow(() -> player.setWardenWarningLevel(this.wardenWarningLevel));
        tryOrThrow(() -> player.setWardenTimeSinceLastWarning(this.wardenTimeSinceLastWarning));
        tryOrThrow(() -> player.setGameMode(this.playerGameMode));
        tryOrThrow(() -> player.discoverRecipes(this.discoveredRecipes));
        tryOrThrow(() -> player.setFoodLevel(this.foodLevel));
        tryOrThrow(() -> player.setExhaustion(this.foodExhaustion));
        tryOrThrow(() -> player.setExhaustion(this.foodExhaustion));
        tryOrThrow(() -> player.setHealth(this.currentHealth));
        tryOrThrow(() -> player.setFireTicks(this.fireTicks));
        tryOrThrow(() -> player.setTicksLived(this.ticksLived));
        tryOrThrow(() -> player.setFreezeTicks(this.freezeTicks));
        tryOrThrow(() -> player.setNoDamageTicks(this.noDamageTicks));
        tryOrThrow(() -> player.setMaximumNoDamageTicks(this.maximumNoDamageTicks));
        tryOrThrow(() -> player.setMaximumAir(this.maximumAir));
        tryOrThrow(() -> player.setRemainingAir(this.currentAir));
        tryOrThrow(() -> player.setArrowCooldown(this.arrowCooldown));
        tryOrThrow(() -> player.setArrowsInBody(this.arrowsInBody));
        tryOrThrow(() -> player.setArrowsInBody(this.arrowsStuck));
        tryOrThrow(() -> player.setSaturatedRegenRate(this.saturatedRegenRate));
        tryOrThrow(() -> player.setUnsaturatedRegenRate(this.unsaturatedRegenRate));
        tryOrThrow(() -> player.setStarvationRate(this.starvationRate));
        tryOrThrow(() -> player.setSleepingIgnored(this.sleepingIgnored));
        tryOrThrow(() -> player.setHealthScaled(this.healthScaled));
        if (this.healthScaled)
            tryOrThrow(() -> player.setHealthScale(this.healthScale));
        tryOrThrow(() -> player.setAffectsSpawning(this.affectsSpawning));
        tryOrThrow(() -> player.setViewDistance(this.viewDistance));
        tryOrThrow(() -> player.setSendViewDistance(this.sendViewDistance));
        tryOrThrow(() -> player.setSimulationDistance(this.simulationDistance));

        tryOrThrow(() -> player.setInvisible(this.isInvisible));
        tryOrThrow(() -> player.setInvulnerable(this.isInvulnerable));
        tryOrThrow(() -> player.setBeeStingersInBody(this.beeStingersInBody));
        tryOrThrow(() -> player.setCanPickupItems(this.canPickupItems));
        tryOrThrow(() -> player.setAllowFlight(this.allowFlight));
        tryOrThrow(() -> player.setAbsorptionAmount(this.absorptionAmount));


        tryOrThrow(() -> {
            for (PotionEffect potionEffect : this.activePotionEffects)
                player.addPotionEffect(potionEffect);
        });

        tryOrThrow(() -> this.statistics.forEach(player::setStatistic));
        tryOrThrow(() -> this.materialStatistics.forEach((statistic, materialIntegerMap) -> materialIntegerMap.forEach((material, integer) -> player.setStatistic(statistic, material, integer))));
        tryOrThrow(() -> this.entityStatistics.forEach((statistic, entityTypeIntegerMap) -> entityTypeIntegerMap.forEach((entityType, integer) -> player.setStatistic(statistic, entityType, integer))));

        tryOrThrow(() -> {
            this.attributeBaseValues.forEach((attribute, aDouble) -> {
                player.registerAttribute(attribute);
                var instance = player.getAttribute(attribute);
                if (instance == null)
                    return;
                instance.setBaseValue(aDouble);

                for (AttributeModifier attributeModifier : new HashSet<>(instance.getModifiers()))
                    instance.removeModifier(attributeModifier);

                for (AttributeModifier attributeModifier : this.attributeModifiers.getOrDefault(attribute, new HashSet<>()))
                    instance.addModifier(attributeModifier);
            });
        });

        tryOrThrow(() -> {
            this.advancementProgress.forEach(playerAdvancementProgress -> {
                var key = playerAdvancementProgress.advancementKey();
                var advancement = Bukkit.getAdvancement(key);
                if (advancement == null)
                    return;
                var progress = player.getAdvancementProgress(advancement);
                progress.getAwardedCriteria().forEach(criteria -> {
                    if (!playerAdvancementProgress.awardedCriteria().contains(criteria))
                        progress.revokeCriteria(criteria);
                });
                playerAdvancementProgress.awardedCriteria().forEach(criteria -> {
                    if (progress.getDateAwarded(criteria) != null)
                        return;
                    progress.awardCriteria(criteria);
                });
            });
        });
    }

    private void tryOrThrow(Runnable runnable) {
        try {
            VCoreNetwork.getInstance().getPlatform().performServerActionThreadSafe(() -> {
                runnable.run();
                return null;
            });
        } catch (Throwable e) {
            e.printStackTrace();
            ;
        }
    }

    public long getSaveDate() {
        return saveDate;
    }
}
