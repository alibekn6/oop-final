package com.uni.models;

import com.uni.enums.CitationFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Academic paper authored by one or more {@link Researcher}s. Comparable
 * by date (newest first) by default; comparators for citations and pages
 * live in com.uni.comparators.
 */
public class ResearchPaper
        implements Serializable, Comparable<ResearchPaper> {

    private static final long serialVersionUID = 1L;

    private String title;
    private final List<Researcher> authors;
    private String journal;
    private int pages;
    private int citations;
    private final Date datePublished;
    private String doi;
    private String paperAbstract;

    public ResearchPaper(String title,
                         List<Researcher> authors,
                         String journal,
                         int pages,
                         int citations,
                         Date datePublished,
                         String doi,
                         String paperAbstract) {
        this.title = title;
        this.authors = new ArrayList<>(authors);
        this.journal = journal;
        this.pages = pages;
        this.citations = citations;
        this.datePublished = datePublished;
        this.doi = doi;
        this.paperAbstract = paperAbstract;
    }

    public String getTitle()                  { return title; }
    public List<Researcher> getAuthors()      { return Collections.unmodifiableList(authors); }
    public String getJournal()                { return journal; }
    public int getPages()                     { return pages; }
    public int getCitations()                 { return citations; }
    public Date getDatePublished()            { return datePublished; }
    public String getDoi()                    { return doi; }
    public String getPaperAbstract()          { return paperAbstract; }

    public void setTitle(String title)              { this.title = title; }
    public void setJournal(String journal)          { this.journal = journal; }
    public void setPages(int pages)                 { this.pages = pages; }
    public void setCitations(int citations)         { this.citations = citations; }
    public void setDoi(String doi)                  { this.doi = doi; }
    public void setPaperAbstract(String text)       { this.paperAbstract = text; }

    public void incrementCitations() { citations++; }

    /** Renders the citation in the requested format. */
    public String getCitation(CitationFormat format) {
        String authorList = authors.stream()
                .map(Researcher::getResearcherName)
                .collect(Collectors.joining(", "));
        if (format == CitationFormat.BIBTEX) {
            return "@article{" + safeKey() + ",\n"
                    + "  author = {" + authorList + "},\n"
                    + "  title = {" + title + "},\n"
                    + "  journal = {" + journal + "},\n"
                    + "  pages = {" + pages + "},\n"
                    + "  year = {" + yearString() + "},\n"
                    + "  doi = {" + doi + "}\n"
                    + "}";
        }
        return authorList + ". \"" + title + "\". "
                + journal + ", "
                + pages + " p., "
                + (datePublished == null ? "n.d." : datePublished)
                + (doi != null && !doi.isEmpty() ? ". DOI: " + doi : "")
                + ".";
    }

    private String safeKey() {
        String first = authors.isEmpty()
                ? "anon"
                : authors.get(0).getResearcherName().split(" ")[0];
        return first.toLowerCase() + yearString();
    }

    private String yearString() {
        if (datePublished == null) return "n.d.";
        Calendar cal = Calendar.getInstance();
        cal.setTime(datePublished);
        return Integer.toString(cal.get(Calendar.YEAR));
    }

    /** Default order: newest paper first. */
    @Override
    public int compareTo(ResearchPaper other) {
        if (other == null || other.datePublished == null) return -1;
        if (this.datePublished == null) return 1;
        return other.datePublished.compareTo(this.datePublished);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResearchPaper)) return false;
        ResearchPaper that = (ResearchPaper) o;
        return Objects.equals(doi, that.doi) && Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doi, title);
    }

    @Override
    public String toString() {
        return "\"" + title + "\" (" + journal + ", "
                + pages + "p, " + citations + " citations)";
    }
}
