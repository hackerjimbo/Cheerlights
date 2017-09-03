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
 * License along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 */

package Jimbo.Cheerlights;

import Jimbo.MQTT.MQTTClient;
import Jimbo.MQTT.MQTTMessageRecipient;

import java.io.IOException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import org.eclipse.paho.client.mqttv3.MqttException;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * Class to listen to MQTT CheerLights tweets and process them. In
 * other words it's an adaptor that turns a MQTTMessageRecipient into a
 * CheerListener.
 * 
 * @author Jim Darby
 */
public class MQTTListener implements MQTTMessageRecipient
{
    private static final Logger LOG = Logger.getLogger ("MQTTListener");
    
    /**
     * Create a MQTTListener, listen, parse and then feed the results
     * (if correct) into the target.
     * 
     * @param broker The URI of the MQTT broker
     * @param client Our client name for the broker
     * @param topic The topic name to listen to
     * @param target The CheerListener to feed to results to
     * 
     * @throws ParseException In case of error
     * @throws MqttException In case of error
     */
    public MQTTListener (String broker, String client, String topic, CheerListener target) throws ParseException, MqttException
    {
        MQTTClient listener = new MQTTClient (broker, client, topic, this);
        
        this.target = target;
        
        listener.run ();
    }
    
    /**
     * Receive an MQTT message.
     * 
     * @param topic The topic it's on
     * @param message The message itself
     */
    @Override
    public void receive (String topic, String message)
    {
        try
        {
            JSONObject j = new JSONObject (message);
            
            final Instant instant = Instant.ofEpochMilli (j.getLong ("sent")).truncatedTo (ChronoUnit.SECONDS);
            final LocalDateTime stamp = LocalDateTime.ofInstant (instant, ZONE);
            final String when = stamp.format (DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            
            LOG.log (Level.INFO, "{0} (@{1}) sent {2}: {3}",
                    new Object[] {j.getString ("name"), j.getString ("screen"), when, j.getString ("text")});
            target.update(j.getInt ("colour"));
        }
        
        catch (JSONException | IOException e)
        {
            LOG.log (Level.WARNING, "Unable to parse: \"{0}\": {1}",
                    new Object[]{message, e.getLocalizedMessage ()});
        }
    }
    
    /** The listener we forward the new colour to. */
    private final CheerListener target;
    /** Time time zone we're in. Used to format logging information. */
    private static final ZoneId ZONE = ZoneOffset.systemDefault ();
}
