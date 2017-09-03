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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Command line parser and generic controller for CheerLights things.
 * 
 * @author Jim Darby
 */
public class Listener
{
    private static final Logger LOG = Logger.getLogger ("Listener");
    
    /**
     * Setup the appropriate inputs to feed into the target. It parses
     * the command line for the available options and creates them as needed.
     * 
     * @param args The command line arguments
     * @param target The CheerListener to feed data into
     * 
     * @return If anything was created.
     */
    public static boolean setup (String args[], CheerListener target)
    {
        // Decode the command line arguments
        Options options = new Options();
        
        options.addOption("b", MQTT_BROKER_KEY, true, "URL of the broker")
                .addOption ("c", MQTT_CLIENT_KEY, true, "Client ID")
                .addOption ("t", MQTT_TOPIC_KEY, true, "Topic to subscribe to")
                .addOption ("m", "multicast", false, "enable multicast listener");
        
        target.add_options (options);

        CommandLineParser parser = new DefaultParser ();
        CommandLine command;
        
        boolean something_worked = false;
        
        try
        {
            // Parse it up
            command = parser.parse (options, args);

	    target.handle_args (command);

            MQTTListener mqtt = null;
            String mqtt_topic = Listener.DEFAULT_MQTT_TOPIC;

            if (command.hasOption (Listener.MQTT_BROKER_KEY))
            {
                if (!command.hasOption (Listener.MQTT_CLIENT_KEY))
                    throw new ParseException ("MQTT without client name");

                if (command.hasOption (Listener.MQTT_TOPIC_KEY))
                    mqtt_topic = command.getOptionValue (Listener.MQTT_TOPIC_KEY);

                try
                {
                    mqtt = new MQTTListener (command.getOptionValue (Listener.MQTT_BROKER_KEY),
                        command.getOptionValue (Listener.MQTT_CLIENT_KEY),
                        mqtt_topic, target);
                    
                    something_worked = true;
                }

                catch (MqttException e)
                {
                    LOG.log (Level.WARNING, "Failed to create MQTT client: {0}", e.getLocalizedMessage ());
                }
            }
            else
            {
                if (command.hasOption (Listener.MQTT_TOPIC_KEY))
                    LOG.warning("MQTT topic supplied but no broker");

                if (command.hasOption (Listener.MQTT_CLIENT_KEY))
                    LOG.warning ("MQTT client name but no broker");
            }
            
            if (command.hasOption ("multicast"))
            {
                MessageListener l = new MessageListener (target);
                l.go ();
                something_worked = true;
            }
        }
        
        catch (ParseException e)
        {
            System.err.println ("Command line arguments failed: " + e.getLocalizedMessage ());
        }
        
        return something_worked;
    }
    
    /** Command line long name for broker. */
    public static final String MQTT_BROKER_KEY = "mqtt-broker";
    /** Command line long name for client name. */
    public static final String MQTT_CLIENT_KEY = "mqtt-client-name"; 
    /** Command line long name for topic. */
    public static final String MQTT_TOPIC_KEY  = "mqtt-topic";
    /** The default name to subscribe to. */
    public static final String DEFAULT_MQTT_TOPIC = "CheerLights";
}
