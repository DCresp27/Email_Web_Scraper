package edu.touro.cs.mcon364;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scrape implements Runnable{

    //private static WebClient client = new WebClient();
    private static Pattern emailPattern = Pattern.compile("[A-Za-z0-9._%+-]{1,15}@[A-Za-z0-9.-]+\\\\.(?!png$|jpg$)[A-Za-z]{3,}"); //I added a condition that if the string is too long, it won't match since most corner-case errors
    private static final Logger LOGGER = LogManager.getLogger(Scrape.class);
    private static AtomicInteger pagesScraped = new AtomicInteger(0);
    private static AtomicInteger duplicatesFound = new AtomicInteger(0);
    private static AtomicInteger errorsFound = new AtomicInteger(0);
    private static ThreadPoolExecutor executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
    private final InformationControl infoController;
    private boolean shutDownCalled = false;
    private String url;
    private String pageContent;
    private Elements links = null;
    private EmailData email;
    private long currentTimeMillis;





    public Scrape(String url, InformationControl webScraper) {
        this.url = url;
        this.infoController = webScraper;


    }



    @Override
    public void run() {




        LOGGER.info(url);
        try {
            Document document = Jsoup.connect(url).get();
            links = document.select("a[href]");
            pageContent = document.html();
            pagesScraped.incrementAndGet();
            for (Element link : links) {
                String href = link.attr("abs:href");
                if (isNonWebpageLink(href)) {
                    // Skip email link
                    continue;
                }
                LOGGER.info("Found link: " + href);
                synchronized(LOGGER){
                    if(infoController.addedToLinkQueue(href)){
                        executorService.submit(new Scrape(infoController.getTopUrl(), infoController));
                    }

                }


            }


//        catch (FailingHttpStatusCodeException e) {
//            if (e.getStatusCode() == 999) {
//                errorsFound.incrementAndGet();
//                //TODO Log the error
//                return; //break out of lambda ie skip link
//            } else {
//                // Handling other HTTP status codes
//                System.out.println("Encountered HTTP status code: " + e.getStatusCode());
//                // Perform generic actions for other HTTP status codes
//                //TODO Log the error
//            }


        } catch (IOException e) {
            e.printStackTrace();


        }


        Matcher matcher = emailPattern.matcher(pageContent);
        boolean emailFound = false;
        while (matcher.find()) {
            email = new EmailData();
            String emailString = matcher.group();
            String normalizedEmail = emailString.toLowerCase();

            email.setEmail(normalizedEmail);



            synchronized (infoController) {
                if (infoController.addedEmail(email)) {
                    email.setHrefSource(url);
                    currentTimeMillis = System.currentTimeMillis();
                    email.setTimeSaved(new java.sql.Timestamp(currentTimeMillis));
                    LOGGER.info(" Found email: " + email.getEmail() + " Email #:" + infoController.numOfEmailsCollected());
                    if (infoController.numOfEmailsCollected() >= 10_000 && !shutDownCalled) {
                        executorService.shutdown();  //Graceful shutdown of threads. Let threads finish, but does not kill it mid-process
                        LOGGER.info("Thread pool shut down");
                        shutDownCalled = true;
                        //We do this bc shutting down threads mid-process is dangerous and may cause things to break.
                        try {
                            // Wait a while for existing tasks to terminate
                            if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {//Threads are still running, so we move to more drastic measures
                                executorService.shutdownNow(); // Cancel currently executing tasks, kills things mid-process.
                                LOGGER.info("Thread pool shut down aggressively");
                                // Wait a while for tasks to respond to being cancelled
                                if (!executorService.awaitTermination(20, TimeUnit.SECONDS)) {
                                    LOGGER.error("Thread pool did not terminate"); // This shouldn't happen. Despite out best attempts at shutdown, it did not shutdown
                                }
                            }


                        } catch (InterruptedException ie) {
                            // (Re-)Cancel if current thread also interrupted
                            executorService.shutdownNow();
                            // Preserve interrupt status
                            Thread.currentThread().interrupt();
                        }
                        finally {
                            infoController.addEmailsToDataBase();
                        }


                    }

                } else {
                    LOGGER.warn(" Duplicate email: " + email.getEmail());
                    duplicatesFound.incrementAndGet();
                }
                emailFound = true;
            }

        }
        if (!emailFound) {
            LOGGER.warn("No email found at page: " + url);
        }



    }

    private boolean isNonWebpageLink(String link) {
        return link.startsWith("mailto:") || link.startsWith("file://") || link.startsWith("tel:") || link.startsWith("sms:");
    }
}
