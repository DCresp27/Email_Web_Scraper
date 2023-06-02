package edu.touro.cs.mcon364;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * This class that represents the data of an email.
 * Data includes the email itself, the time found and the url in which it was found
 * This class includes a custom HashCode and equals method in order to allow it to function in a Set DS
 * so that the set will not contain any doubles of any email address
 *
 */

public class EmailData implements Comparable<EmailData>{


    private String email;
    private String hrefSource;
    private Timestamp timeSaved;


    public EmailData(){


    }


    public String getEmail() {
        return email;
    }

    public String getHrefSource() {
        return hrefSource;
    }

    public Timestamp getTimeSaved() {
        return timeSaved;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHrefSource(String hrefSource) {
        this.hrefSource = hrefSource;
    }

    public void setTimeSaved(Timestamp timeSaved) {
        this.timeSaved = timeSaved;
    }

    /**
     *
     * Equals and HashCode is determined solely by the email address
     */


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailData emailData = (EmailData) o;
        return email.equals(emailData.email);
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(email);
        return hash;
    }

    @Override
    public int compareTo(EmailData o) {
        return this.email.compareTo(o.email);
    }
}
