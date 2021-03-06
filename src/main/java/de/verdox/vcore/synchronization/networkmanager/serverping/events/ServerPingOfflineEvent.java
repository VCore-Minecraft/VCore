/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.synchronization.networkmanager.serverping.events;

import de.verdox.vcore.synchronization.networkmanager.server.ServerType;
import org.jetbrains.annotations.NotNull;

/**
 * @version 1.0
 * @Author: Lukas Jonsson (Verdox)
 * @date 09.07.2021 00:28
 */
public class ServerPingOfflineEvent extends ServerPingEvent {
    public ServerPingOfflineEvent(@NotNull ServerType serverType, @NotNull String serverName, @NotNull String serverAddress, int serverPort) {
        super(serverType, serverName, serverAddress, serverPort);
    }
}
