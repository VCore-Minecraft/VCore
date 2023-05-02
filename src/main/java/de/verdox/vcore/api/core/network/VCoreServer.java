package de.verdox.vcore.api.core.network;

import de.verdox.vpipeline.api.network.RemoteParticipant;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public record VCoreServer(@NotNull DataReference<RemoteParticipant> remoteParticipantReference) {
    void setServerAddress(String address) {
        remoteParticipantReference
                .loadOrCreate()
                .thenApply(lock -> lock.performWriteOperation(remoteParticipant -> remoteParticipant.saveCachedData("internal.serverAddress", address)));
    }

    public boolean isServer(VCoreServer vCoreServer) {
        return remoteParticipantReference.uuid().equals(vCoreServer.remoteParticipantReference.uuid());
    }

    void setServerPort(int port) {
        remoteParticipantReference
                .loadOrCreate()
                .thenApply(lock -> lock.performWriteOperation(remoteParticipant -> remoteParticipant.saveCachedData("internal.serverPort", port)));
    }

    public CompletableFuture<String> getName() {
        var future = new CompletableFuture<String>();
        remoteParticipantReference
                .loadOrCreate()
                .thenApply(lock -> lock.getter(RemoteParticipant::getIdentifier))
                .thenApply(future::complete);
        return future;
    }

    public CompletableFuture<Integer> getPort() {
        var future = new CompletableFuture<Integer>();
        remoteParticipantReference
                .loadOrCreate()
                .thenApply(lock -> lock.performReadOperation(remoteParticipant -> future.complete(remoteParticipant.getData(Integer.class, "internal.serverPort"))));
        return future;
    }

    public CompletableFuture<String> getAddress() {
        var future = new CompletableFuture<String>();
        remoteParticipantReference
                .loadOrCreate()
                .thenApply(lock -> lock.performReadOperation(remoteParticipant -> future.complete(remoteParticipant.getData(String.class, "internal.serverAddress"))));
        return future;
    }
}
