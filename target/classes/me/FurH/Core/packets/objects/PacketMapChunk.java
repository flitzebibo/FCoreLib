package me.FurH.Core.packets.objects;

/**
 *
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class PacketMapChunk implements ICorePacket {

    private Object handle;

    /**
     * Creates a new Packet51MapChunk Handler
     *
     * @param packet the original Packet51MapChunk
     */
    public PacketMapChunk(Object packet) {
        this.handle = packet;
    }

    @Override
    public int getPacketId() {
        return 51;
    }

    @Override
    public Object getHandle() {
        return this.handle;
    }
    
    /**
     * Set the Packet51MapChunk object
     *
     * @param handle the new Packet51MapChunk object
     * @return this
     */
    public PacketMapChunk setHandle(Object handle) {
        this.handle = handle; return this;
    }
    
    @Override
    public String getPacketName() {
        return this.handle.getClass().getSimpleName();
    }
}