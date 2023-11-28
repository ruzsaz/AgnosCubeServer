package hu.agnos.cube.server.postprocessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hu.agnos.cube.Cube;
import hu.agnos.cube.extraCalculation.PostCalculation;
import hu.agnos.cube.meta.resultDto.ResultElement;
import hu.agnos.cube.meta.resultDto.ResultSet;

public class KaplanMeier {

    public static void process(ResultSet resultSet, List<Integer> extraCalculatedIndices, int dimIndex) {
        int num = extraCalculatedIndices.size();

        if (resultSet.actualDrill()[dimIndex].isShowResultAsDimValue()) {
            List<ResultElement> responseList = resultSet.response();
            KaplanMeierDimSorter sorter = new KaplanMeierDimSorter(dimIndex);
            responseList.sort(sorter);

            ResultElement previous = null;
            for (ResultElement re : responseList) {
                if (!KaplanMeier.isNew(previous, re, sorter)) {
                    for (int i = 0; i < num; i++) {
                        re.measureValues()[extraCalculatedIndices.get(i)] = previous.measureValues()[extraCalculatedIndices.get(i)] * re.measureValues()[extraCalculatedIndices.get(i)];
                    }
                }
                previous = re;
            }
        } else {
            List<ResultElement> responseList = resultSet.response();
            KaplanMeierDimSorter sorter = new KaplanMeierDimSorter(dimIndex);
            responseList.sort(sorter);


            ResultElement previous = null;
            double[] accumulator = new double[num];
            for (ResultElement re : responseList) {
                if (KaplanMeier.isNew(previous, re, sorter)) {
                    Arrays.fill(accumulator, 1.0);
                } else {
                    previous.header()[dimIndex] = null;
                }
                for (int i = 0; i < num; i++) {
                    accumulator[i] = accumulator[i] * re.measureValues()[extraCalculatedIndices.get(i)];
                    re.measureValues()[extraCalculatedIndices.get(i)] = accumulator[i];
                }
                previous = re;
            }

            responseList.removeIf(r -> r.header()[dimIndex] == null);
        }
    }

    private static boolean isNew(ResultElement prev, ResultElement curr, KaplanMeierDimSorter sorter) {
        return !sorter.isSameWithoutIndex(prev, curr);
    }

    /**
     * Determines the indexes of extra calculated indicators within a Cube. (The extra calculation requirements is
     * included in the report only.) If the denominator is hidden, it is omitted.
     *
     * @param cube The cube
     * @return List of indices in the cube to extra-calculate
     */
    public static List<Integer> getKaplanMeierIndicatorIndices(Cube cube) {
        List<Integer> result = new ArrayList<>(1);
        List<PostCalculation> postCalculations = cube.getPostCalculations();
        for (PostCalculation p : postCalculations) {
            if (p.type().equalsIgnoreCase("KaplanMeier")) {
                result.add(Arrays.asList(cube.getMeasureHeader()).indexOf(p.measure().getName()));
            }
        }
        return result;
    }

}
