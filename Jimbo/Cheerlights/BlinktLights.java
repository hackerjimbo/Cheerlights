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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.net.UnknownHostException;

import Jimbo.Boards.com.pimoroni.Blinkt;

/**
 *
 * @author Jim Darby
 */
public class BlinktLights implements CheerListener
{
    private static final Logger LOG = Logger.getLogger ("BlinktLights");
    
    public BlinktLights ()
    {
        LOG.log (Level.INFO, "Blinkt cheer lights started");
        
        blinkt = new Blinkt ();
       
        for (int i = 0; i < data.length; ++i)
            data[i] = 0;
    }
    
    @Override
    public void update (int colour) throws IOException
    {
        LOG.log (Level.INFO, "Update new colour {0}", Integer.toHexString(colour));
        
        int[] next = new int [data.length];
        
        for (int i = 1; i < data.length; ++i)
            next[i] = data[i-1];
        
        next[0] = colour;
        
        for (int step = 1; step <= 100; ++step)
        {
            final int left = 100 - step;
            
            for (int i = 0; i < data.length; ++i)
            {
                final int r = (step * ((next[i] >> 16) & 0xff) + left * ((data[i] >> 16) & 0xff)) / 100;
                final int g = (step * ((next[i] >>  8) & 0xff) + left * ((data[i] >>  8) & 0xff)) / 100;
                final int b = (step * ((next[i]      ) & 0xff) + left * ((data[i]      ) & 0xff)) / 100;
                
                blinkt.set (i, r, g, b, 8);
            }
            
            blinkt.show();
            
            try
            {
                Thread.sleep (10);
            }
            
            catch (InterruptedException e)
            {
                LOG.log (Level.WARNING, "Interrupted sleep?");
            }
        }
        
        data = next;
    }
    
    public static void main (String args[]) throws IOException, UnknownHostException, InterruptedException
    {
        // Set up simpler logging to stdout
        Jimbo.Logging.Logging.useStdout ();
        
        final CheerListener target = new BlinktLights ();
        
        MessageListener.run (target);
    }
    
    final Blinkt blinkt;
    int[] data = new int[Blinkt.WIDTH];
}
