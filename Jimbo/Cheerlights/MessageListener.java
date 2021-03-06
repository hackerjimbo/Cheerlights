/*
 * Copyright (C) 2016, 2017 Jim Darby.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Cheerlights;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A class to listen for multicast packets containing colour update 
 * information.
 * 
 * @author Jim Darby
 */
public class MessageListener implements Runnable
{
    private static final Logger LOG = Logger.getLogger ("MessageListener");

    /**
     * Create a multicast message listener. It doesn't start running until
     * the go method is called.
     * 
     * @param target The target to feed colour updates into
     */
    public MessageListener (CheerListener target)
    {
        LOG.info ("MessageListener created");
	this.target = target;
    }
    
    /**
     * Start running the process. This is a non-blocking call that actually
     * starts a background thread so you don't need to worry about it!
     */
    public void go ()
    {
        Thread t = new Thread (this, "Multicast Listener");
        
        t.start ();
        LOG.info ("MessageListener started");
    }
    
    /**
     * Listen for and process incoming multicast messages. This is a
     * blocking call so if that's not what you want call go instead and it'll
     * set up a thread to deal with it.
     */
    @Override
    public void run ()
    {
        try
        {
            final MulticastSocket socket = new MulticastSocket (5123);
            final InetAddress group = InetAddress.getByName ("224.1.1.1");

            socket.joinGroup (group);

            final byte[] buffer = new byte[1024];
            final DatagramPacket packet = new DatagramPacket (buffer, buffer.length);

            LOG.log (Level.INFO, "And we're off....");

            while (true)
            {
                socket.receive (packet);

                final InetAddress rx_addr = packet.getAddress ();
                final int rx_port = packet.getPort ();            
                final byte[] data = new byte[packet.getLength()];

                System.arraycopy(packet.getData(), 0, data, 0, packet.getLength ());

                final Message m = new Message (data);

                LOG.log (Level.INFO, "{0}:{1}: {2}", new Object[] {rx_addr.getCanonicalHostName (), rx_port, m.getText ()});
                target.update (m.getRGB ());
            }
        }
        
        catch (UnknownHostException e)
        {
            LOG.log (Level.WARNING, "MessageListener failed: UnknownHostException {0}", e.getLocalizedMessage ());
        }
        
        catch (IOException  e)
        {
            LOG.log (Level.WARNING, "MessageListener failed: IOException {0}", e.getLocalizedMessage ());
        }
    }

    /** The target we want to update. */
    private final CheerListener target;
}
