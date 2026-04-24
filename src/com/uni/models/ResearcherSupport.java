package com.uni.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared implementation helpers for {@link Researcher}. Keeps Teacher,
 * Student, ResearcherEmployee, and ResearcherDecorator from duplicating
 * h-index and print logic.
 */
final class ResearcherSupport {

    private ResearcherSupport() {}

    /** Standard h-index: largest h such that h papers have ≥ h citations each. */
    static double hIndex(List<ResearchPaper> papers) {
        if (papers == null || papers.isEmpty()) return 0;
        List<ResearchPaper> sorted = new ArrayList<>(papers);
        sorted.sort((a, b) -> Integer.compare(b.getCitations(), a.getCitations()));
        int h = 0;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getCitations() >= i + 1) h = i + 1;
            else break;
        }
        return h;
    }

    static void printPapers(Researcher who,
                            List<ResearchPaper> papers,
                            Comparator<ResearchPaper> comparator) {
        System.out.println("--- Papers of " + who.getResearcherName()
                + " (" + papers.size() + ") ---");
        if (papers.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        List<ResearchPaper> sorted = new ArrayList<>(papers);
        if (comparator != null) sorted.sort(comparator);
        int i = 1;
        for (ResearchPaper p : sorted) {
            System.out.println("  " + i++ + ". " + p);
        }
    }
}
