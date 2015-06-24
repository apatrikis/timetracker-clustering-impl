/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.cluster;

import com.prodyna.pac.timetracker.cluster.jms.SenderBean;
import com.prodyna.pac.timetracker.jms.IncommingMessage;
import com.prodyna.pac.timetracker.jms.OutgoingMessage;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Asynchronous;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for sending and receiving a Message within a {@code Glassfish Cluster}.
 * This class represents the {@code Facade}.
 *
 * @author apatrikis
 */
@Startup
@Singleton
public class ClusterConnector {

    private static final Logger log = LoggerFactory.getLogger(ClusterConnector.class);

    /**
     * Self identification for sendending and receiving {@code JMS Messages}: a
     * message which is send from this server does not need to be handeled when
     * received through messaging.
     */
    private static final String uuid = UUID.randomUUID().toString();

    @Inject
    private SenderBean messageSender;

    @Inject
    @IncommingMessage
    private Event<ClusterMessage> clusterMessageEvent;

    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
    }

    @PreDestroy
    @SuppressWarnings("unused")
    private void cleanup() {
    }

    /**
     * Broadcast a message to the cluster. The receiver will process the
     * message.
     *
     * @param message The message to broadcast.
     */
    @Asynchronous
    public void brodcastMessage(@Observes @OutgoingMessage ClusterMessage message) {
        boolean sameServer = uuid.equals(message.getSender());
        if (sameServer == false) {
            message.setSender(uuid);
            messageSender.sendJMSMessage(message);
        } else {
            StringBuilder stack = new StringBuilder(4000);
            String newLine = System.getProperty("line.separator");

            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement traceElement : stackTrace) {
                stack.append("\tat ").append(traceElement.toString()).append(newLine);
            }

            log.warn("Avoided to resend message {} into cluster. Check stacktrace to identify processing issue: {}",
                    message.toString(), stack.toString());
        }
    }

    /**
     * Fire an event so that an receiver may process the message. This happens
     * only in case the message was not send from this server instance.
     *
     * @param message The message to be handeled by the event receiver.
     */
    public void consumeMessage(ClusterMessage message) {
        boolean sameServer = uuid.equals(message.getSender());
        if (sameServer == false) {
            clusterMessageEvent.fire(message);
        }
    }
}
