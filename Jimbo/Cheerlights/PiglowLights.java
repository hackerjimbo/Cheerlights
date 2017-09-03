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

import Jimbo.Boards.com.pimoroni.Piglow;

/**
 * This class handles Cheerlights on the Pimoroni PiGlow.
 * 
 * @author Jim Darby
 */
public class PiglowLights implements CheerListener
{
    private static final Logger LOG = Logger.getLogger ("PiglowLights");
    
    public PiglowLights () throws IOException, InterruptedException
    {
        LOG.log (Level.INFO, "Piglow cheer lights started");
        pg = new Piglow ();
    }
    
    @Override
    public synchronized void update (int colour) throws IOException
    {
        LOG.log (Level.INFO, "Update new colour {0}", Integer.toHexString(colour));
        
        int next[][] = new int[data.length][data[0].length];
        
        // Shift up...
        for (int i = 0; i < data.length - 1; ++i)
            System.arraycopy (data[i], 0, next[i + 1], 0, data[0].length);
        
        // Use the ghost of the old value
        for (int i = 0; i < next[0].length; ++i)
            next[0][i] = data[2][i] / 4;
        
        /*System.out.print ("Legs before:");
        
        for (int i = 0; i < next[0].length; ++i)
            System.out.print (" " + next[0][i]);
        
        System.out.println ();*/
        
        final int max = 32;
        
        switch (colour)
        {
            case 0xFF0000: next[0][5] += max; break; // Red
            case 0x008000: next[0][2] += max; break; // Green
            case 0x0000FF: next[0][1] += max; break; // Blue
            case 0x00FFFF: next[0][2] += max / 2; next[0][1] += max / 2; break; // Cyan
            case 0xFFFFFF: next[0][0] += max; break; // White
            case 0xFDF5E6: next[0][0] += max / 2; next[0][5] += max / 2; break; // Warmwhite
            case 0x800080: next[0][5] += max / 4; next[0][1] += max / 4; break; // Purple
            case 0xFF00FF: next[0][5] += max / 2; next[0][1] += max / 2; break; // Magenta
            case 0xFFFF00: next[0][3] += max; break; // Yellow
            case 0xFFA500: next[0][4] += max; break; // Orange
            case 0xFFC0CB: next[0][5] += max / 2; next[0][0] += max / 2; break; // Pink
        }
        
        /*System.out.print ("Legs:");
        
        for (int i = 0; i < next[0].length; ++i)
            System.out.print (" " + next[0][i]);
        
        System.out.println ();*/
        
        final int[][] mix = new int[next.length][next[0].length];
        
        for (int step = 1; step <= 100; ++step)
        {
            final int left = 100 - step;
            
            for (int i = 0; i < data.length; ++i)
                for (int j = 0; j < data[0].length; ++j)
                    mix[i][j] = (left * data[i][j] + step * next[i][j]) / 100;

            
            pg.setLegs (mix);
            pg.update ();
            
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
    
    private final Piglow pg;
    private int data[][] = new int[3][6];
    
    public static void main (String args[]) throws IOException, InterruptedException
    {
        // Set up simpler logging to stdout
        Jimbo.Logging.Logging.useStdout ();
        
        final CheerListener target = new PiglowLights ();
        
        Listener.setup (args, target);
    }
}
