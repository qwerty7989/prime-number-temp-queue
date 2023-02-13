/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package primenumberserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 *
 * @author sarun
 */
public class TextListener implements MessageListener {
    private MessageProducer replyProducer;
    private Session session;
    
    public TextListener(Session session) {
        this.session = session;
        try {
            replyProducer = session.createProducer(null);
        } catch (JMSException ex) {
            Logger.getLogger(TextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void onMessage(Message message) {
        TextMessage msg = null;

        try {
            if (message instanceof TextMessage) {
                msg = (TextMessage) message;
                System.out.println("Reading message: " + msg.getText() + " with JMS-ID: " + msg.getJMSCorrelationID());
            } else {
                System.err.println("Message is not a TextMessage");
            }
            // set up the reply message 
            String numberStr = (String) msg.getText();
            String[] numberRange = numberStr.split(",");
            int leftRange = Integer.parseInt(numberRange[0]);
            int rightRange = Integer.parseInt(numberRange[1]);
            int primeCount = 0;
            for (int i = leftRange + 1; i < rightRange; i++) {
                if (isPrime(i)) {
                    primeCount++;
                }
            }
            
            String responseStr = "number of primes between " + leftRange + " and " + rightRange + " is " + primeCount; 
            TextMessage response = session.createTextMessage(responseStr); 
            response.setJMSReplyTo(message.getJMSReplyTo());
            response.setJMSCorrelationID(message.getJMSCorrelationID());
            System.out.println("sending message " + response.getText());
            replyProducer.send(response.getJMSReplyTo(), response);
        } catch (JMSException e) {
            System.err.println("JMSException in onMessage(): " + e.toString());
        } catch (Throwable t) {
            System.err.println("Exception in onMessage():" + t.getMessage());
        }
        
    }
    
    private boolean isPrime(int n) {
        int i;
        for (i = 2; i*i <= n; i++) {
            if ((n % i) == 0) {
                return false;
            }
        }
        return true;
    }
}
