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

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;

public interface CheerListener
{
    /**
     * Update the colour of a CheerListener.
     * 
     * @param colour The colour.
     * @throws IOException In case of error.
     */
    public void update (int colour) throws IOException;
    
    /**
     * Allow a CheerListen to add command line options.
     * 
     * @param opts The options to add to.
     */
    default public void add_options (Options opts)
    {
        // We don't add any options
    }
    
    /**
     * Allow a CheerListen to read command line options.
     * 
     * @param command The command line.
     */
    default public void handle_args (CommandLine command)
    {
        // We don't look at any options
    }
}
