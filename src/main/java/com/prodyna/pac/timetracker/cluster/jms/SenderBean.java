/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.cluster.jms;

import com.prodyna.pac.timetracker.cluster.ClusterMessage;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.jms.Topic;
import org.slf4j.Logger;

/**
 * Send a {@code JMS Message} to a {@code Queue} and/or {@code Topic}.
 *
 * @see ReceiverTopicBean
 * @author apatrikis
 */
@Stateless
public class SenderBean {

    @Inject
    private Logger log;

    @Resource(mappedName = "jms/ttTopicMessageForUser")
    private Topic ttTopic;

    @Inject
    @JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
    private JMSContext context;

    public SenderBean() {
    }

    /**
     * Send a {@code JMS Message} to the {@code jms/ttTopicMessageForUser}
     * topic.
     *
     * @param message The message to send.
     */
    public void sendJMSMessage(ClusterMessage message) {
        try {
            log.debug("|=> Sending message: " + message.toString());
            context.createProducer().send(ttTopic, message);
        } catch (JMSRuntimeException jmsre) {
            log.error("Error while sending message to the cluster via JMS", jmsre);
        }
    }
}
