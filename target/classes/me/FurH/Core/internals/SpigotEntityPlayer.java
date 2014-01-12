package me.FurH.Core.internals;

import java.util.ArrayDeque;
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
public class SpigotEntityPlayer extends IEntityPlayer {

    private final Queue syncPackets = new ConcurrentLinkedQueue() {
        
        private static final long serialVersionUID = -2989730822625217316L;
        
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

    private final Queue queue = new ArrayDeque(64) {

        private static final long serialVersionUID = -8040655607817764920L;

        @Override
        public boolean add(Object packet) {
            packet = handlePacket(packet);

            if (packet == null) {
                return false;
            }

            return super.add(packet);
        }
    };

    @Override
    public void setInboundQueue() throws CoreException {
        Queue old = (Queue) ReflectionUtils.getPrivateField(networkManager, "syncPackets");
        syncPackets.addAll(old);
        ReflectionUtils.setFinalField(networkManager, "syncPackets", syncPackets);
    }

    @Override
    public void setOutboundQueue() throws CoreException {
        
        Object writer = null;

        try {
            writer = ReflectionUtils.getPrivateField(this.networkManager, "writer");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Queue old = null;

        try {
            old = (ArrayDeque) ReflectionUtils.getPrivateField(writer, "queue");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        queue.addAll(old);

        ReflectionUtils.setFinalField(writer, "queue", queue);
    }
}