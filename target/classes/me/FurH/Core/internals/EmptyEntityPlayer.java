package me.FurH.Core.internals;

import me.FurH.Core.exceptions.CoreException;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class EmptyEntityPlayer extends IEntityPlayer {

    @Override
    public void setInboundQueue() throws CoreException {
        ; // do nothing
    }

    @Override
    public void setOutboundQueue() throws CoreException {
        ; // do nothing
    }
}
