package edu.touro.cs.mcon364;

import java.util.*;
import java.util.logging.Logger;

/**
 * This class will primarily use the Stack Data Structure in order to store information for my WebScraper
 * It will also utilize a HashSet to store unique values as I do not want to store email or links twice
 *
 * @param <T>: Realistically for this implementation, this will be a String representing a link
 * @author Daniel Crespin
 */

public class NoDuplicateQueue<T> {


    Queue<T> bs = new LinkedList<>();
    Set<T> set = new HashSet<>();
    Logger logger = Logger.getLogger("DataStructure Logger");


    public boolean enqueue(T element) {
        if (set.add(element)) {
            bs.offer(element);
            //logger.info("Stored: " + element + " in " + this.getClass());
            return true;
        }
        //logger.warning("Not added " + element);

        return false;
    }

    public T dequeue() {
        //Removes from stack while staying in the set, thus ensures that it cannot be added again
        return bs.poll();

    }

    public boolean empty() {
        return bs.isEmpty();
    }

    public int size() {
        return bs.size();
    }
}
