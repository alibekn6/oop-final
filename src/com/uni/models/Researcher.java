package com.uni.models;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Anyone capable of doing research. Implemented directly by
 * {@link ResearcherEmployee} and {@link ResearcherDecorator}; also by
 * {@link Teacher} and {@link Student} (gated on {@link #isResearcher()}).
 */
public interface Researcher extends Serializable {

    /** Calculates the h-index from the researcher's papers. */
    double calculateHIndex();

    /** Prints the researcher's papers in the order dictated by the comparator. */
    void printPapers(Comparator<ResearchPaper> comparator);

    /** Returns a defensive copy / immutable view of this researcher's papers. */
    List<ResearchPaper> getPapers();

    /** Adds a new paper authored by this researcher. */
    void publishPaper(ResearchPaper paper);

    /** Display name used by reports (e.g. "Top cited researcher: Alice Doe"). */
    String getResearcherName();

    /**
     * Whether this object is currently acting as a researcher. Default true;
     * Teacher/Student override to gate on a per-instance flag (e.g. only
     * professors are always researchers).
     */
    default boolean isResearcher() { return true; }
}
