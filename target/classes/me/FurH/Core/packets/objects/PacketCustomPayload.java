package me.FurH.Core.packets.objects;

import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.reflection.ReflectionUtils;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class PacketCustomPayload implements ICorePacket {

    private Object handle;
    
    /**
     * Creates a new Packet250CustomPayload implementation using reflection
     *
     * @param packet the original Packet250CustomPayload
     */
    public PacketCustomPayload(Object packet) {
        
        this.handle = packet;
        
        try {

            this.channel = (String) ReflectionUtils.getPrivateField(packet, "tag");
            this.data = (byte[]) ReflectionUtils.getPrivateField(packet, "data");
            this.length = ReflectionUtils.getPrivateIntField(packet, "length");

        } catch (CoreException ex) {
            ex.printStackTrace();
        }
        
    }
    
    /**
     * Creates a new empty custom payload
     *
     */
    public PacketCustomPayload() { }
    
    private String   channel;
    private byte[]   data;
    private int      length;
    
    /**
     * Set this custom payload channel
     *
     * @param channel the channel to set
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    /**
     * Get this custom payload channel
     *
     * @return the channel
     */
    public String getChannel() {
        return this.channel;
    }

    /**
     * Set this custom payload data
     *
     * @param data the data to set
     */
    public void setData(byte[] data) {
        this.data = data;
    }
    
    /**
     * Get this custom payload data
     *
     * @return the data
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * Set this custom payload length
     *
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }
    
    /**
     * Get this custom payload length
     *
     * @return this custom payload length
     */
    public int getLength() {
        return this.length;
    }

    @Override
    public int getPacketId() {
        return 250;
    }
    
    /**
     * Set the Packet250CustomPayload object
     *
     * @param handle the new Packet250CustomPayload object
     * @return this
     */
    public PacketCustomPayload setHandle(Object handle) {
        this.handle = handle; return this;
    }

    @Override
    public Object getHandle() {
        return this.handle;
    }
    
    @Override
    public String getPacketName() {
        return this.handle.getClass().getSimpleName();
    }
}
