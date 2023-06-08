package edu.touro.cs.mcon364;

import com.gargoylesoftware.htmlunit.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.sql.SQLException;
import java.util.concurrent.ConcurrentSkipListSet;
//import java.util.logging.Logger;


/**
 * Class that will be used to scrape the web for emails.
 * Starting at https://www.touro.edu/
 * It will store all the links visited and emails collected
 * using the custom Stack data structure that does not allow duplicate values
 *
 * @author Daniel Crespin
 */

public class InformationControl {


    private WebClient client;
    private static ConcurrentSkipListSet<EmailData> emailSet = new ConcurrentSkipListSet<>();
    private static final Logger LOGGER = LogManager.getLogger(InformationControl.class);
    private SQLDatabaseConnection dataBase;
    private static NoDuplicateQueue<String> customQueueForLinks = new NoDuplicateQueue<>();



    public InformationControl() {




        try {
            dataBase = SQLDatabaseConnection.getInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            dataBase.clearDataFromTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }



        //log = Logger.getLogger("WebScraper Logger");





    }









    public void addEmailsToDataBase() {
        if (dataBase.insertEmails(emailSet)) {
            LOGGER.warn("Inserted " + emailSet.size() + " emails");
        }


    }


    public boolean addedToLinkQueue(String absoluteHref) {
        return(customQueueForLinks.enqueue(absoluteHref));
    }

    public String getTopUrl() {
        return customQueueForLinks.dequeue();
    }

    public boolean addedEmail(EmailData email) {
        return emailSet.add(email);
    }


    public int numOfEmailsCollected() {
        return emailSet.size();
    }
}
