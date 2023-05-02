package de.verdox.vcore.impl.gameserver.paper.data;

import org.bukkit.NamespacedKey;

import java.util.Set;

public record PlayerAdvancementProgress(NamespacedKey advancementKey, Set<String> awardedCriteria) {
}
