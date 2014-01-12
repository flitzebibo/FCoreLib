package me.FurH.Core.packets;

import me.FurH.Core.CorePlugin;
import me.FurH.Core.packets.objects.PacketCustomPayload;
import me.FurH.Core.packets.objects.PacketMapChunk;
import me.FurH.Core.packets.objects.PacketMapChunkBulk;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public abstract class IPacketQueue {
    
    private String owner;

    public IPacketQueue(CorePlugin plugin) {
        this.owner = plugin.getDescription().getName();
    }
    
    /**
     * Receive a Custom Payload (Packet250CustomPayload)
     *
     * @param player the player
     * @param packet the original packet
     * @return true if the packet is ment to be procesed by the server, false otherwise 
     */
    public boolean handleAsyncCustomPayload(Player player, PacketCustomPayload packet) {
        return true;
    }
    
    /**
     * Receive and set a custom Payload (Packet250CustomPayload)
     *
     * @param player the player
     * @param object the packet object
     * @return the modified packet object
     */
    public PacketCustomPayload handleAndSetAsyncCustomPayload(Player player, PacketCustomPayload object) {
        return object;
    }
    
    /**
     * Receive the Client Settings (Packet204LocaleAndViewDistance)
     *
     * @param player the player
     * @return true if the packet is ment to be processe by the server, false otherwise
     */
    public boolean handleAsyncClientSettings(Player player) {
        return true;
    }

    /**
     * Receive a chunk packet (Packet56MapChunkBulk)
     *
     * @param player the player
     * @param object the packet object
     * @return the modified (or not) chunk packet
     */
    public PacketMapChunkBulk handleAsyncMapChunkBulk(Player player, PacketMapChunkBulk object) {
        return object;
    }

    /**
     * Receive a chunk packet (Packet51MapChunk)
     *
     * @param player the player
     * @param object the packet object
     * @return the modified (or not) chunk packet
     */
    public PacketMapChunk handleAsyncMapChunk(Player player, PacketMapChunk object) {
        return object;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 89 * hash + (this.owner != null ? this.owner.hashCode() : 0);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final IPacketQueue other = (IPacketQueue) obj;
        if ((this.owner == null) ? (other.owner != null) : !this.owner.equals(other.owner)) {
            return false;
        }

        return true;
    }
}
