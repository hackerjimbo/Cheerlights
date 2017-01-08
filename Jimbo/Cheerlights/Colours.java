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

import java.util.HashMap;

/**
 * This class looks up the RGB values of a colour from its name.
 * 
 * @author Jim Darby
 */
class Colours
{
    /**
     * This method looks up the RGB values of a colour from its name. The first
     * matching colour name in the argument is returned or -1 if none.
     * 
     * @param name A string to search for a colour name.
     * @return The RGB value as 0xrrggbb or -1.
     */
    public static int lookup (String name)
    {
	if (map == null)
	    init ();

	Integer result = map.get (name.toLowerCase ());

	return (result == null) ? -1 : result;
    }

    /**
     * Set everything up.
     */
    private synchronized static void init ()
    {
	// Now we're synchronised, check again
	if (map != null)
	    return;

	// Create it
	map = new HashMap <> ();

	// From the cheerlights API documentation
	map.put ("red",       0xFF0000);
	map.put ("green",     0x008000);
	map.put ("blue",      0x0000FF);
	map.put ("cyan",      0x00FFFF);
	map.put ("white",     0xFFFFFF);
	map.put ("oldlace",   0xFDF5E6);
	map.put ("warmwhite", 0xFDF5E6);
	map.put ("purple",    0x800080);
	map.put ("magenta",   0xFF00FF);
	map.put ("yellow",    0xFFFF00);
	map.put ("orange",    0xFFA500);
	map.put ("pink",      0xFFC0CB);
    }

    /** Where we hold the colour mappings. */
    private static HashMap <String, Integer> map = null;
    
    public static void main (String args[])
    {
	for (String s : args)
	{
	    System.out.println ("Processing " + s);

	    final String[] parts = s.split (" ");

	    for (String c : parts)
	    {
		final int v = lookup (c);
	    
		System.out.println (c + " -> " + v);
	    }
	}
    }
}
