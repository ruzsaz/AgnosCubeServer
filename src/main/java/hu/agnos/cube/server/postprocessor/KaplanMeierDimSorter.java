package hu.agnos.cube.server.postprocessor;

import java.io.Serializable;
import java.util.Comparator;

import hu.agnos.cube.meta.resultDto.ResultElement;

/**
 * Comparator for sorting dimension-value data rows before Kaplan-Meier calculations. The desired order of elements is
 * lexicographical sorting by the dimension coordinate values, while the Kaplan-Meier dimension is the last in the
 * dimensions' order.
 */
public class KaplanMeierDimSorter implements Comparator<ResultElement>, Serializable {

    private final int index;

    /**
     * Initializes the comparator by setting the Kaplan-Meier dimension.
     *
     * @param index Index of the Kaplan-Meier dimension in the array of dimensions
     */
    public KaplanMeierDimSorter(int index) {
        this.index = index;
    }

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

    protected boolean isSameWithoutIndex(ResultElement element1, ResultElement element2) {
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
