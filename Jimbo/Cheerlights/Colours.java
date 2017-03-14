/*
 * Copyright (C) 2016-2017 Jim Darby.
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
	Integer result = MAP.get (name.toLowerCase ());

	return (result == null) ? -1 : result;
    }
    
    /** Where we hold the colour mappings. */
    private static final HashMap <String, Integer> MAP;
    
    static
    {
	MAP = new HashMap <> ();

	// From the cheerlights API documentation
	MAP.put ("red",       0xFF0000);
	MAP.put ("green",     0x008000);
	MAP.put ("blue",      0x0000FF);
	MAP.put ("cyan",      0x00FFFF);
	MAP.put ("white",     0xFFFFFF);
	MAP.put ("oldlace",   0xFDF5E6);
	MAP.put ("warmwhite", 0xFDF5E6);
	MAP.put ("purple",    0x800080);
	MAP.put ("magenta",   0xFF00FF);
	MAP.put ("yellow",    0xFFFF00);
	MAP.put ("orange",    0xFFA500);
	MAP.put ("pink",      0xFFC0CB);
    }

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
