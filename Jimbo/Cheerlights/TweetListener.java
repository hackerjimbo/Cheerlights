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

import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.IOException;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.SocketException;

import twitter4j.DirectMessage;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserStreamListener;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.UserList;

/**
 * Listen for Cheerlights tweets and send out messages to a given IP and port.
 * 
 * @author jim
 */
public class TweetListener {
    private static final Logger LOG = Logger.getLogger ("TweetListener");
    
    /**
     * @param args the command line arguments
     * @throws twitter4j.TwitterException
     * @throws java.io.IOException
     */
    public static void main (String[] args) throws TwitterException, IOException
    {
        // Set up simpler logging to stdout
        Jimbo.Logging.Logging.useStdout();
        
        LOG.log (Level.INFO, "Starting twitter listener");
        
        Twitter twitter = new TwitterFactory().getInstance();
        StatusListener listener = new listener ("224.1.1.1", (short) 5123);
        FilterQuery fq = new FilterQuery();        

        String keywords[] = {"#cheerlights"};

        fq.track(keywords);        
        
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener (listener);
        twitterStream.filter (fq);     
        
        LOG.log (Level.INFO, "Up and running....");
    }

    /**
     * Class to listen to tweets and inform users.
     */
    private static class listener implements UserStreamListener
    {
        public listener (String host, short port) throws SocketException, UnknownHostException, IOException
        {
            socket = new MulticastSocket ();
	    address = InetAddress.getByName (host);
            this.port = port;
            
            socket.setTimeToLive (3);
        }
        
        /**
         * Call back routine for when a status arrives.
         * @param status The twitter status message.
         */
        @Override
        public void onStatus(Status status)
        {
            LOG.log (Level.INFO, "{0}@{1}: {2}",
                    new Object[] {userText (status.getUser ()), status.getCreatedAt ().toString(), status.getText ()});

            try
            {
                final String text = status.getText ();
                String words[] = text.split ("[^A-Za-z]+");
                int sent = 0;
                
                for (int i = 0; i < words.length; ++i)
                {
                    final int colour = Colours.lookup (words[i]);
                    
                    if (colour >= 0)
                    {
                        Message m = new Message (colour, text);
                
                        final byte[] buffer = m.getBlob ();
                        final DatagramPacket packet = new DatagramPacket (buffer, buffer.length, address, port);
                
                        socket.send (packet);
                        
                        sent += 1;
                        
                        // Now check we can decode what we just sent.
                        
                        try
                        {
                            Message n = new Message (m.getBlob ());
                        }

                        catch (IOException e)
                        {
                            LOG.log (Level.WARNING, "Failed to parse binary {0}: {1}",
                                    new Object[] {e.getLocalizedMessage(), status.getText ()});
                        }
                    }
                }
                
                if (sent == 0)
                    LOG.log (Level.INFO, "No colour in {0}", text);
            }
            
            catch (IOException e)
            {
                LOG.log (Level.WARNING, "Failed to parse {0}: {1}",
                        new Object[] {e.getLocalizedMessage (), status.getText ()});
                
                return;
            }
        }
        
        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice)
        {
            LOG.log (Level.WARNING, "Delete notice: {0}", statusDeletionNotice);
        }
        
        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses)
        {
            LOG.log (Level.WARNING, "Track limit: {0}", numberOfLimitedStatuses);
        }
        
        @Override
        public void onException(Exception ex)
        {
            ex.printStackTrace();
        }

        @Override
        public void onScrubGeo(long l, long l1)
        {
            LOG.log (Level.WARNING, "onScrubGeo {0} {1}",
                    new Object[] {l, l1});
        }

        @Override
        public void onDeletionNotice(long l, long l1)
        {
            LOG.log (Level.WARNING, "deletion notice: {0} {1}",
                    new Object[] {l, l1});
        }

        @Override
        public void onFriendList(long[] longs)
        {
            LOG.log (Level.WARNING, "Heard about friends {0}", longs.length);
        }

        @Override
        public void onFavorite(User user, User user1, Status status)
        {
            LOG.log (Level.WARNING, "Favourite: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), status});
        }

        @Override
        public void onUnfavorite(User user, User user1, Status status)
        {
            LOG.log (Level.WARNING, "Unfavourite: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), status});
        }

        @Override
        public void onFollow(User user, User user1)
        {
            LOG.log (Level.WARNING, "Follow: {0} {1}",
                    new Object[] {userText (user), userText (user1)});
        }

        @Override
        public void onUnfollow(User user, User user1)
        {
            LOG.log (Level.WARNING, "Unfollow: {0} {1}",
                    new Object[] {userText (user), userText (user1)});
        }

        @Override
        public void onDirectMessage(DirectMessage dm)
        {
            LOG.log (Level.WARNING, "Direct message: {0}", dm);
        }
        
        @Override
        public void onUserListMemberAddition(User user, User user1, UserList ul)
        {
            LOG.log (Level.WARNING, "List addition: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), ul});
        }

        @Override
        public void onUserListMemberDeletion(User user, User user1, UserList ul)
        {
            LOG.log (Level.WARNING, "List deletion: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), ul});
        }

        @Override
        public void onUserListSubscription(User user, User user1, UserList ul)
        {
            LOG.log (Level.WARNING, "List subscription: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), ul});
        }

        @Override
        public void onUserListUnsubscription(User user, User user1, UserList ul)
        {
            LOG.log (Level.WARNING, "List unsubscription: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), ul});
        }

        @Override
        public void onUserListCreation(User user, UserList ul)
        {
            LOG.log (Level.WARNING, "List creation: {0} {1}",
                    new Object[] {userText (user), ul});
        }

        @Override
        public void onUserListUpdate(User user, UserList ul)
        {
            LOG.log (Level.WARNING, "List update: {0} {1}",
                    new Object[] {userText (user), ul});
        }

        @Override
        public void onUserListDeletion(User user, UserList ul)
        {
            LOG.log (Level.WARNING, "List deletion: {0} {1}",
                    new Object[] {userText (user), ul});
        }

        @Override
        public void onUserProfileUpdate(User user)
        {
            LOG.log (Level.WARNING, "Profile update: {0}", userText (user));
        }

        @Override
        public void onUserSuspension(long l)
        {
            LOG.log (Level.WARNING, "Suspendion: {0}", l);
        }

        @Override
        public void onUserDeletion(long l)
        {
            LOG.log (Level.WARNING, "Deletion: {0}", l);
        }

        @Override
        public void onBlock(User user, User user1)
        {
            LOG.log (Level.WARNING, "Block: {0} {1}",
                    new Object[] {userText (user), userText (user1)});
        }

        @Override
        public void onUnblock(User user, User user1)
        {
            LOG.log (Level.WARNING, "Unblock: {0} {1}",
                    new Object[] {userText (user), userText (user1)});
        }

        @Override
        public void onRetweetedRetweet(User user, User user1, Status status)
        {
            LOG.log (Level.WARNING, "Retweet retweet: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), status});
        }

        @Override
        public void onFavoritedRetweet(User user, User user1, Status status)
        {
            LOG.log (Level.WARNING, "Favourite: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), status});
        }

        @Override
        public void onQuotedTweet(User user, User user1, Status status)
        {
            LOG.log (Level.WARNING, "Quoted: {0} {1} {2}",
                    new Object[] {userText (user), userText (user1), status});
        }

        @Override
        public void onStallWarning(StallWarning sw)
        {
            LOG.log (Level.WARNING, "Stallwarning: {0}", sw);
        }
        
        final MulticastSocket socket;
        final InetAddress address;
        final short port;
    }
    
    private static String userText (User u)
    {
        return u.getName() + " (@" + u.getScreenName () + ")";
    }
}
