/*
 * Copyright (C) 2016 Jim Darby.
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, If not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Cheerlights;

import java.io.IOException;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * This class handles the messages used to broadcast cheerlights data around a
 * network.
 * 
 * @author Jim Darby
 */
public class Message
{
    public Message (String s) throws IOException
    {
        // Save the text
        text = s;
        
        // Find the colour
        int colour = -1;
        
        for (String word : s.split (" "))
        {
            colour = Colours.lookup (word.toLowerCase ());
            
            if (colour >= 0)
                break;
        }
        
        if (colour < 0)
            throw new IOException ("No known colour in message");
        
        rgb = colour;
        
        // Enode the string and its length
        final byte[] coded  = s.getBytes ("UTF-8");
        
        int lenlen = 1;
        int left = coded.length;
        
        while (left > 0x7f)
        {
            lenlen += 1;
            left >>= 7;
        }
        
        // Now build the blob
        blob = new byte[4 + lenlen + coded.length];
        
        blob[0] = CHEERS;
        blob[1] = (byte) (rgb >> 16);
        blob[2] = (byte) (rgb >> 8);
        blob[3] = (byte) rgb;
        
        int upto = 4;
        
        for (int i = 0; i < lenlen; ++i)
            blob[upto++] = (byte) (((coded.length >> (7 * i)) & 0x7f) | ((i != lenlen - 1) ? 0x80 : 0));
        
        // Finally add the string to the end
        System.arraycopy (coded, 0, blob, upto, coded.length);
    }
    
    /**
     * Construct a cheerlights message from a byte array.
     * @param data The byte array.
     * @throws IOException In case of error.
     */
    public Message (byte data[]) throws IOException
    {
        // Check some sort of sanity...
        if (data.length < 5)
            throw new IOException ("Cheerligths message too small");
        
        // Check op-code
        if (data[0] != CHEERS)
            throw new IOException ("Cheerlights message not cheerlights!");
        
        // Parse out RGB
        rgb = ((data[1] & 0xff) << 16) | ((data[2] & 0xff) << 8) | (data[3] & 0xff);
        
        // Peel off the length
        int length = 0;
        int shift = 0;
        int upto = 4;
        
        for (;;)
        {
            if (upto >= data.length)
                throw new IOException ("Malformed cheerlights message (length)");
            
            length += (data[upto] & 0x7f) << shift;
            
            if ((data[upto] & 0x80) == 0)
                break;
            
            upto += 1;
            shift += 7;
        }
        
        // Check the rest of the data's length
        if (data.length != upto + 1 + length)
            throw new IOException ("Malformed cheerlights message (text: " + data.length + " != " + (upto + 1 + length) + ")");
        
        // Now build the String.
        text = new String (data, upto + 1, length, "UTF-8");
        blob = data;
    }
    
    /**
     * Get the RGB value. Stored as 0xrrggbb.
     * @return The RBG value.
     */
    public int getRGB ()
    {
        return rgb;
    }
    
    /**
     * Get the text value.
     * @return The text.
     */
    public String getText ()
    {
        return text;
    }
    
    /**
     * Return the binary blob.
     * @return The blob.
     */
    public byte[] getBlob ()
    {
        return blob;
    }
    
    /**
     * Convert the item to a string.
     * 
     * @return A diagnostic dump of the contents of the object.
     */
    @Override
    public String toString ()
    {
        String result = "Text: " + text + " RGB " + Integer.toHexString (rgb) + " blob";
        
        
        for (int i = 0; i < blob.length; ++i)
            result += " " + Integer.toHexString(blob[i]);
        
        return result;
    }
    
    public static void main (String args[]) throws IOException
    {
        final MulticastSocket socket = new MulticastSocket ();
	final InetAddress address = InetAddress.getByName ("224.1.1.1");
        final short port = 5123;
        
        socket.setTimeToLive (3);
        
        for (String arg : args)
        {
            Message m = new Message (arg);
            System.out.println (arg + " ->");
            System.out.println ("  " + m);
            Message n = new Message (m.getBlob ());
            System.out.println ("  " + n);
            
            try
            {                
                final byte[] buffer = m.getBlob ();
                final DatagramPacket packet = new DatagramPacket (buffer, buffer.length, address, port);
                
                socket.send (packet);
            }
            
            catch (IOException e)
            {
                System.err.println ();
                //System.err.println ("Failed to parse " + e.getLocalizedMessage() + ": " + status.getText ());
                System.err.println ();
                
                return;
            }
        }
    }
    
    /** The code for this message. */
    private final static int CHEERS = 1;
    
    /** Where we store the text. */
    private final String text;
    /** Where we store the RGB value */
    private final int rgb;
    /** Where we store the blob. */
    private final byte blob[];
}
