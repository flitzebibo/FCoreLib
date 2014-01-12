package me.FurH.Core.internals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.inventory.InventoryStack;
import me.FurH.Core.packets.PacketManager;
import me.FurH.Core.packets.objects.ICorePacket;
import me.FurH.Core.packets.objects.PacketCustomPayload;
import me.FurH.Core.packets.objects.PacketMapChunk;
import me.FurH.Core.packets.objects.PacketMapChunkBulk;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public abstract class IEntityPlayer {

    private static Class<?> packetCLS;

    public List send_later = new ArrayList();
    public List send_replace = new ArrayList();

    protected boolean inventory_hidden = false;
    protected Object entity;
    protected Player player;
    protected Object playerConnection;
    protected Object networkManager;

    /**
     * Set the Player of this IEntityPlayer object
     * 
     * @param player the player to be set
     * @return the IEntityPlayer for the given Player
     */
    public IEntityPlayer setEntityPlayer(Player player) {

        this.player = player;
        
        Class<?> craftPlayer = null;
        
        try {
            craftPlayer = Class.forName("org.bukkit.craftbukkit."+InternalManager.getServerVersion()+"entity.CraftPlayer");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
        try {
            if (packetCLS == null) {
                packetCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"Packet");
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
        Object converted = InventoryStack.convert(player, craftPlayer);
        Method handle = null;

        try {
            handle = converted.getClass().getMethod("getHandle");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            this.entity = handle.invoke(converted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        try {
            
            this.playerConnection = this.entity.getClass().getField("playerConnection").get(entity);
            this.networkManager = this.playerConnection.getClass().getField("networkManager").get(playerConnection);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return this;
    }

    /**
     * Return the NMS EntityPlayer object
     *
     * @return
     */
    public Object getHandle() {
        return this.entity;
    }
    
    /**
     * Get the Player network ping
     * 
     * @return the player ping in milliseconds
     */
    public int ping() {
        
        try {
            return entity.getClass().getField("ping").getInt(entity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return 0;
    }

    /**
     * Send the EntityPlayer a custom payload
     *
     * @param packet the custom payload
     */
    public void sendCustomPayload(PacketCustomPayload packet) {
        this.sendCorePacket(packet);
    }

    /**
     * Hides the player inventory
     */
    public void hideInventory() {

        try {

            inventory_hidden = false;

            Object stack = InventoryStack.getCraftVersion(new org.bukkit.inventory.ItemStack(Material.AIR, 1));
            Object activeContainer = entity.getClass().getField("activeContainer").get(entity);

            Method method = activeContainer.getClass().getMethod("a");
            List a = (List) method.invoke(activeContainer);

            Class<?> container = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"Container");

            List stacks = new ArrayList();
            for (int j1 = 0; j1 < a.size(); j1++) {
                stacks.add(stack);
            }

            Method hide = entity.getClass().getMethod("a", container, List.class);
            hide.invoke(entity, InventoryStack.convert(activeContainer, container), stacks);

            inventory_hidden = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Revels the player inventory
     */
    public void unHideInventory() {
        inventory_hidden = false;
        player.updateInventory();
    }
    
    /**
     * Get if the player inventory is hidden
     *
     * @return true if the inventory is hidden, false otherwise.
     */
    public boolean isInventoryHidden() {
        return this.inventory_hidden;
    }
    
    /**
     * Set the Player Inboud Queue
     * 
     * @throws CoreException 
     */
    public abstract void setInboundQueue() throws CoreException;

    /**
     * Set the Player Outbound queue
     * 
     * @throws CoreException 
     */
    public abstract void setOutboundQueue() throws CoreException;

    /**
     * Add a ICorePacket to the send queue
     *
     * @param packet
     */
    public void sendCorePacket(ICorePacket packet) {
        this.send_later.add(packet.getHandle());
    }

    public class PriorityQueue extends ArrayList {

        private static final long serialVersionUID = 927895363924203624L;

        @Override
        public boolean add(Object packet) {
            
            if (isInventoryHidden()) {

                int id = InternalManager.getPacketId(packet);

                if (id == 103 || id == 104) {
                    return false;
                }
            }

            return super.add(packet);
        }

        @Override
        public Object remove(int index) {

            Object packet = handlePacket(super.remove(index));

            if (packet == null) {
                try {
                    return newEmptyPacket();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            return packet;
        }
    }
    
    /**
     * Handle the given packet and return the packet to be sent
     *
     * @param packet the original packet
     * @return the packet to be sent
     */
    protected Object handlePacket(Object packet) {
        
        if (!send_later.isEmpty()) {

            Object old = packet;
            packet = send_later.remove(0);

            if (InternalManager.getPacketId(packet) != InternalManager.getPacketId(old)) {
                send_replace.add(packet);
            }

            return packet;
        }

        if (!send_replace.isEmpty()) {

            packet = send_replace.remove(0);
            return packet;

        }

        if (packet != null) {

            try {

                int id = InternalManager.getPacketId(packet);

                if (id == 56) {
                    packet = PacketManager.callAsyncMapChunkBulk(player, new PacketMapChunkBulk(packet)).getHandle();
                } else if (id == 51) {
                    packet = PacketManager.callAsyncMapChunk(player, new PacketMapChunk(packet)).getHandle();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        return packet;
    }
    
    /**
     * Creates a new empty Packet0KeepAlive
     *
     * @return the Packet0KeepAlive object
     * @throws Exception
     */
    public Object newEmptyPacket() throws Exception {
        return Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"Packet0KeepAlive")
                .getConstructor(Integer.TYPE).newInstance(1);
    }
}
