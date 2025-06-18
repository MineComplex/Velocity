package com.velocitypowered.proxy.tablist.rewriting;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.tablist.KeyedVelocityTabList;

import java.util.Optional;
import java.util.UUID;

public class RewritingKeyedVelocityTabList extends KeyedVelocityTabList implements RewritingTabList {

    public RewritingKeyedVelocityTabList(ConnectedPlayer player, ProxyServer proxyServer) {
        super(player, proxyServer);
    }

    @Override
    public void addEntry(TabListEntry entry) {
        super.addEntry(this.rewriteEntry(entry));
    }

    @Override
    public Optional<TabListEntry> getEntry(UUID uuid) {
        return super.getEntry(this.rewriteUuid(uuid));
    }

    @Override
    public boolean containsEntry(UUID uuid) {
        return super.containsEntry(this.rewriteUuid(uuid));
    }

    @Override
    public Optional<TabListEntry> removeEntry(UUID uuid) {
        return super.removeEntry(this.rewriteUuid(uuid));
    }
}
