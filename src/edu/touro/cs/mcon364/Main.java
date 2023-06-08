package edu.touro.cs.mcon364;

public class Main {

    public static void main(String[] args) {


        InformationControl controller = new InformationControl();
        Scrape scrape = new Scrape("https://www.touro.edu/", controller);
        scrape.run();


    }
}

