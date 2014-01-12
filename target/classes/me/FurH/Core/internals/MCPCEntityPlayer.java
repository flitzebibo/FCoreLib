package me.FurH.Core.internals;

import java.lang.reflect.Field;
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
@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class MCPCEntityPlayer extends IEntityPlayer {

    @Override
    public void setInboundQueue() throws CoreException {

        Queue newSyncPackets = new ConcurrentLinkedQueue() {

            private static final long serialVersionUID = 7299839519835756010L;

            @Override
            public boolean add(Object packet) {
                
                try {
                    
                    String name = packet.getClass().getSimpleName();
                    int id = InternalManager.getPacketId(name);

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
        
        for (Field field : networkManager.getClass().getFields()) {
            if (field.getType().equals(Queue.class)) {

                Queue syncPackets = (Queue) ReflectionUtils.getPrivateField(networkManager, field.getName());
                newSyncPackets.addAll(syncPackets);
                ReflectionUtils.setFinalField(networkManager, field.getName(), newSyncPackets);

            }
        }
    }

    @Override
    public void setOutboundQueue() throws CoreException {

        for (Field field : networkManager.getClass().getFields()) {
            if (field.getType().equals(List.class)) {     

                List newhighPriorityQueue = Collections.synchronizedList(new PriorityQueue());
                List highPriorityQueue = (List) ReflectionUtils.getPrivateField(networkManager, field.getName());

                if (highPriorityQueue != null) {
                    newhighPriorityQueue.addAll(highPriorityQueue);
                    highPriorityQueue.clear();
                }

                ReflectionUtils.setFinalField(networkManager, field.getName(), newhighPriorityQueue);
                
            }
        }
    }
}