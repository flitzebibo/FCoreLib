package me.FurH.Core.packets.objects;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class PacketMapChunkBulk implements ICorePacket {

    private Object handle;

    /**
     * Creates a new PacketMapChunkBulk implementation using reflection, this class will only be used when there is no nms implementation available.
     *
     * @param packet the original Packet56MapChunkBulk
     */
    public PacketMapChunkBulk(Object packet) {
        this.handle = packet;
    }

    @Override
    public int getPacketId() {
        return 56;
    }

    @Override
    public Object getHandle() {
        return this.handle;
    }

    /**
     * Set the Packet56MapChunkBulk object
     *
     * @param handle the new Packet56MapChunkBulk object
     * @return this
     */
    public PacketMapChunkBulk setHandle(Object handle) {
        this.handle = handle; return this;
    }

    @Override
    public String getPacketName() {
        return this.handle.getClass().getSimpleName();
    }
}