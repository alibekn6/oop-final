package com.uni.comparators;

import com.uni.models.ResearchPaper;

import java.io.Serializable;
import java.util.Comparator;

/** Sorts papers by length (page count) — longest first. */
public class PaperLengthComparator
        implements Comparator<ResearchPaper>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ResearchPaper a, ResearchPaper b) {
        return Integer.compare(b.getPages(), a.getPages());
    }
}
