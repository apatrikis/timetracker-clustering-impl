/*
 * PRODYNA PAC 2015 - Time Tracker
 * Anastasios Patrikis
 */
package com.prodyna.pac.timetracker.cluster.jms;

import com.prodyna.pac.timetracker.cluster.ClusterConnector;
import com.prodyna.pac.timetracker.cluster.ClusterMessage;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.slf4j.Logger;

/**
 * Receive a {@code JMS Message} from the {@code jms/ttTopicMessageForUser}
 * {@code Topic}. This is a {@code MDB}, so the invocation will happen as soon
 * as there is a message to consume.
 *
 * @see SenderBean
 *
 * @author apatrikis
 */
@Named
@MessageDriven(mappedName = "jms/ttTopicMessageForUser", activationConfig = {
    @ActivationConfigProperty(propertyName = "clientID", propertyValue = "${com.sun.aas.instanceName}")})
public class ReceiverTopicBean implements MessageListener {

    @Inject
    private Logger log;

    @Inject
    private ClusterConnector cluster;

    /**
     * Recive a {@code Topic} from {@code jms/ttTopicMessageForUser} and forward
     * it to consumers.
     *
     * @param message The received message
     */
    @Override
    public void onMessage(Message message) {
        try {
            ClusterMessage body = message.getBody(ClusterMessage.class);
            log.debug("=>| Received JMS message body: {}", body.toString());
            cluster.consumeMessage(body);
        } catch (JMSException jmse) {
            log.error("Error while receiving message via JMS", jmse);
        }
    }
}
