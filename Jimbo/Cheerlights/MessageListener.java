/*
 * Copyright (C) 2016 Jim Darby.
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
import java.net.SocketException;


/**
 *
 * @author pi
 */
public class MessageListener
{
    private static final Logger LOG = Logger.getLogger ("MessageListener");
    
    public static void run (CheerListener target) throws UnknownHostException, IOException, InterruptedException
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
}
