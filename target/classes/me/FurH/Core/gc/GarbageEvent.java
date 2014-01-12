package me.FurH.Core.gc;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class GarbageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    public GarbageEvent() {
        super(true);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}