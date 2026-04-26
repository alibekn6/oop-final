package com.uni.comparators;

import com.uni.models.ResearchPaper;

import java.io.Serializable;
import java.util.Comparator;

/** Sorts papers by citation count — most cited first. */
public class CitationsComparator
        implements Comparator<ResearchPaper>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ResearchPaper a, ResearchPaper b) {
        return Integer.compare(b.getCitations(), a.getCitations());
    }
}
