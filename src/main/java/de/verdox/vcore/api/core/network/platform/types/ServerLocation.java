package de.verdox.vcore.api.core.network.platform.types;

import de.verdox.vcore.api.core.network.VCoreServer;

public record ServerLocation(VCoreServer server, GameLocation gameLocation) {
}
