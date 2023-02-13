/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package primenumberclient;

import java.util.Scanner;
import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 *
 * @author koony
 */
public class Main {
    @Resource(mappedName = "jms/ConnectionFactory")
    private static ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/TempQueue")
    private static Queue queue;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MessageProducer replyProducer = null;
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        TextMessage message = null;
        TextListener listener = null;
        
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
            Queue tempDest = session.createTemporaryQueue();
            listener = new TextListener();
            MessageConsumer responseConsumer = session.createConsumer(tempDest);
            responseConsumer.setMessageListener(listener);
            
            consumer = session.createConsumer(tempDest);
            consumer.setMessageListener(listener);
            
            MessageProducer producer = session.createProducer(queue);
            
            connection.start();
            
            String ch = "";
            Scanner inp = new Scanner(System.in);
            int messageId = 0;
            while(true) {
                System.out.println("enter two number. (seperated with ,) to exit, press enter");
                ch = inp.nextLine();
                if (ch.equals("\n")) {
                    break;
                }
                
                String[] numberRange = ch.split(",");
                if (numberRange.length != 2) {
                    System.out.println("Please enter only 2 number, please");
                    continue;
                }
                
                if (numberRange[0].contains(" ") || numberRange[1].contains(" ")) {
                    System.out.println("Please enter NUMBERS, please");
                    continue;
                }
                
                String regexNumOnly = "[0-9]+";
                if (!(numberRange[0].matches(regexNumOnly)) || !(numberRange[0].matches(regexNumOnly))) {
                    System.out.println("Please enter NUMBERS, please");
                    continue;
                }
                
                message = session.createTextMessage();
                message.setText(ch);
                message.setJMSReplyTo(tempDest);

                String correlationId = Integer.toString(messageId);
                message.setJMSCorrelationID(correlationId);
                
                System.out.println("sending message: " + message.getText());
                producer.send(message);
                
                messageId++;
            }
            
            
        } catch (JMSException e) {
            System.err.println("Exception occurred: " + e.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                }
            }
        }
     
    }
}