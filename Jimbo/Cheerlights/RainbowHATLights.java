/*
 * Copyright (C) 2017 Jim Darby.
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

import Jimbo.Boards.com.pimoroni.RainbowHAT;
import java.net.UnknownHostException;

/**
 * This class handles Cheerlights on the Pimoroni Rainbow HAT.
 * 
 * @author Jim Darby
 */
    
public class RainbowHATLights implements CheerListener
{
    private static final Logger LOG = Logger.getLogger ("RainbowHATLights");
    
    public RainbowHATLights () throws IOException, InterruptedException
    {
        LOG.log (Level.INFO, "RainbowHAT cheer lights started");
        
        leds = new RainbowHAT ().getLEDs ();
        leds.brightness (3);
        data = new int[leds.WIDTH];
    }
    
    @Override
    public synchronized void update (int colour) throws IOException
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
                
                leds.setPixel (i, 0, r, g, b);
            }
            
            leds.show();
            
            try
            {
                Thread.sleep (100);
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
        
        final CheerListener target = new RainbowHATLights ();
        
        Listener.setup (args, target);
    }
        
    private final RainbowHAT.LEDs leds;
    private int[] data;
}
