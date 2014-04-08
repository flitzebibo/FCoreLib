package me.FurH.Core.internals;

import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.packets.PacketManager;
import me.FurH.Core.packets.objects.PacketCustomPayload;
import me.FurH.Core.reflection.ReflectionUtils;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitEntityPlayer extends IEntityPlayer {
    
    @Override
    public void setInboundQueue() throws CoreException {
        
        Queue newSyncPackets = new ConcurrentLinkedQueue() {

            private static final long serialVersionUID = 7299839519835756010L;

            @Override
            public boolean add(Object packet) {
                
                try {
                    
                    int id = InternalManager.getPacketId(packet);

                    if (id == 250) {

                        PacketManager.callAsyncCustomPayload(player, new PacketCustomPayload(packet));

                    } else
                    if (id == 204) {

                        PacketManager.callAsyncClientSettings(player);

                    }
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return super.add(packet);
            }
        };
        
        Queue syncPackets = (Queue) ReflectionUtils.getPrivateField(networkManager, "inboundQueue");
        newSyncPackets.addAll(syncPackets);
        ReflectionUtils.setFinalField(networkManager, "inboundQueue", newSyncPackets);
        
    }

    @Override
    public void setOutboundQueue() throws CoreException {

        List newhighPriorityQueue = Collections.synchronizedList(new PriorityQueue());
        List newlowPriorityQueue = Collections.synchronizedList(new PriorityQueue());

        List highPriorityQueue = (List) ReflectionUtils.getPrivateField(networkManager, "highPriorityQueue");
        List lowPriorityQueue = (List) ReflectionUtils.getPrivateField(networkManager, "lowPriorityQueue");

        if (highPriorityQueue != null) {
            newhighPriorityQueue.addAll(highPriorityQueue);
            highPriorityQueue.clear();
        }

        if (lowPriorityQueue != null) {
            newlowPriorityQueue.addAll(lowPriorityQueue);
            lowPriorityQueue.clear();
        }

        ReflectionUtils.setFinalField(networkManager, "highPriorityQueue", newhighPriorityQueue);
        ReflectionUtils.setFinalField(networkManager, "lowPriorityQueue", newlowPriorityQueue);
        
    }
}