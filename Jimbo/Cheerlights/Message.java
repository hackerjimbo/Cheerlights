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
import java.io.UnsupportedEncodingException;

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
    /**
     * Build a message from a colour. The original text is also added. Good
     * for when a message is scanned for multiple colours.
     * 
     * @param colour The colour, encoded as 0x00rrggbb.
     * @param text The associated text.
     * 
     * @throws UnsupportedEncodingException In case of String difficulties.
     */
    public Message (int colour, String text) throws UnsupportedEncodingException
    {
        build (colour, text);
    }
    
    /**
     * Build a message from a String. Uses the first recognised colour in
     * the String.
     * 
     * @param s The String.
     * 
     * @throws UnsupportedEncodingException In case of String difficulties.
     * @throws IOException If there is no known colour in the String.
     */
    public Message (String s) throws UnsupportedEncodingException, IOException
    {
        // Save the text
        text = s;
        
        // Find the colour
        int colour = -1;
        
        for (String word : s.split (" "))
        {
            colour = Colours.lookup (word);
            
            if (colour >= 0)
                break;
        }
        
        if (colour < 0)
            throw new IOException ("No known colour in message");
        
        build (colour, s);
    }
    
    /**
     * Build all the data.
     * 
     * @param colour The colour to fill in
     * @param string The String we want to send.
     * 
     * @throws UnsupportedEncodingException In case of String difficulties.
     */
    private void build (int colour, String string) throws UnsupportedEncodingException
    {
        final byte[] coded = string.getBytes ("UTF-8");
        final int length = coded.length;
        final int lenlen;

        if (length <= 0x7f)
            lenlen = 1;
        else if (length <= (0x7f << 7) + 0x7f)
            lenlen = 2;
        else if (length <= (0x7f << 14) + (0x7f << 7) + 0x7f)
            lenlen = 3;
        else if (length <= (0x7f << 21) + (0x7f << 14) + (0x7f << 7) + 0x7f)
            lenlen = 4;
        else
            lenlen = 5;
        
        // Now build the blob
        blob = new byte[4 + lenlen + length];
        
        blob[0] = CHEERS;
        blob[1] = (byte) (colour >> 16);
        blob[2] = (byte) (colour >> 8);
        blob[3] = (byte) colour;
        
        final int upto;
        
        switch (lenlen)
        {
            case 1:
                blob[4] = (byte) length;
                upto = 5;
                break;
                
            case 2:
                blob[4] = (byte) (length >> 7);
                blob[5] = (byte) length;
                upto = 6;
                break;
                
            case 3:
                blob[4] = (byte) (length >> 14);
                blob[5] = (byte) (length >>  7);
                blob[6] = (byte) length;
                upto = 7;
                break;
                
            case 4:
                blob[4] = (byte) (length >> 21);
                blob[5] = (byte) (length >> 14);
                blob[6] = (byte) (length >>  7);
                blob[7] = (byte) length;
                upto = 8;
                break;
        
            case 5:
                blob[4] = (byte) (length >> 28);
                blob[5] = (byte) (length >> 21);
                blob[6] = (byte) (length >> 14);
                blob[7] = (byte) (length >>  7);
                blob[9] = (byte) length;
                upto = 9;
                break;
                
            default:
                throw new java.lang.AssertionError ("Impossible String length");                   
        }
        
        text = string;
        rgb = colour;
        
        // Finally add the string to the end
        System.arraycopy (coded, 0, blob, upto, length);
    }
    
    /**
     * Construct a cheerlights message from a byte array. This would be
     * the data received from the network (typically).
     * 
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
        int upto = 4;
        boolean end = false;
        
        while (!end)
        {
            if (upto >= data.length)
                throw new IOException ("Malformed cheerlights message (length)");
            
            length = (length << 7) + (data[upto] & 0x7f);
            end = (data[upto] & 0x80) == 0;
            
            upto += 1;
        }
        
        // Check the rest of the data's length
        if (data.length != upto + length)
            throw new IOException ("Malformed cheerlights message (text: " + data.length + " != " + (upto + length) + ")");
        
        // Now build the String.
        text = new String (data, upto, length, "UTF-8");
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
    private String text;
    /** Where we store the RGB value */
    private int rgb;
    /** Where we store the blob. */
    private byte blob[];
}
