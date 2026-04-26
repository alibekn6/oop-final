package com.uni.comparators;

import com.uni.models.ResearchPaper;

import java.io.Serializable;
import java.util.Comparator;

/** Sorts papers by date published — newest first. */
public class DateComparator
        implements Comparator<ResearchPaper>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(ResearchPaper a, ResearchPaper b) {
        if (a.getDatePublished() == null && b.getDatePublished() == null) return 0;
        if (a.getDatePublished() == null) return 1;
        if (b.getDatePublished() == null) return -1;
        return b.getDatePublished().compareTo(a.getDatePublished());
    }
}
