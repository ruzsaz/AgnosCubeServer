package hu.agnos.cube.server.postprocessor;

import java.util.Comparator;

import hu.agnos.cube.meta.resultDto.ResultElement;

public class KaplanMeierDimSorter implements Comparator<ResultElement> {

    private final int index;

    public KaplanMeierDimSorter(int index) {
        System.out.println("KM: " + index);
        this.index = index;
    }

    // TODO: tesztet írni
    @Override
    public int compare(ResultElement element1, ResultElement element2) {
        int length = element1.header().length;

        for (int i = 0; i < length; i++) {
            if (i != index) {
                int compared = element1.header()[i].knownId().compareTo(element2.header()[i].knownId());
                if (compared != 0) {
                    return compared;
                }
            }
        }
        return element1.header()[index].knownId().compareTo(element2.header()[index].knownId());
    }

    boolean isSameWithoutIndex(ResultElement element1, ResultElement element2) {
        if (element1 == null || element2 == null) {
            return false;
        }
        int length = element1.header().length;
        for (int i = 0; i < length; i++) {
            if (i != index) {
                int compared = element1.header()[i].knownId().compareTo(element2.header()[i].knownId());
                if (compared != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
