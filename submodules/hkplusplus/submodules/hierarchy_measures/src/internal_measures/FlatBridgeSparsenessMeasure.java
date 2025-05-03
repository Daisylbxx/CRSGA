//package internal_measures;
//
//import basic_hierarchy.implementation.BasicNode;
//import basic_hierarchy.interfaces.Hierarchy;
//import basic_hierarchy.interfaces.Instance;
//import basic_hierarchy.interfaces.Node;
//import common.CommonQualityMeasure;
//import interfaces.DistanceMeasure;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//public class FlatBridgeSparsenessMeasure extends CommonQualityMeasure {
//    private DistanceMeasure dist;
//
//    private FlatBridgeSparsenessMeasure() {}
//
//    public FlatBridgeSparsenessMeasure(DistanceMeasure dist) {
//        this.dist = dist;
//    }
//
//    // 用于返回簇索引和对应的最大类内距离
//    private static class ClusterSparsenessInfo {
//        int clusterIndex;
//        double maxWithinClusterDistance;
//
//        public ClusterSparsenessInfo(int clusterIndex, double maxWithinClusterDistance) {
//            this.clusterIndex = clusterIndex;
//            this.maxWithinClusterDistance = maxWithinClusterDistance;
//        }
//    }
//
//    private ClusterSparsenessInfo findSparsestCluster(Hierarchy h) {
//        Node[] nodes = h.getGroups();
//        int sparsestClusterIndex = -1;
//        double maxWithinClusterDist = -1;
//
//        for (int i = 0; i < nodes.length; i++) {
//            List<Instance> instances = nodes[i].getNodeInstances();
//            if (instances.size() < 2) continue;
//
//            double localMaxDist = 0;
//            for (int j = 0; j < instances.size(); j++) {
//                for (int k = j + 1; k < instances.size(); k++) {
//                    double d = dist.getDistance(instances.get(j), instances.get(k));
//                    if (d > localMaxDist) {
//                        localMaxDist = d;
//                    }
//                }
//            }
//
//            if (localMaxDist > maxWithinClusterDist) {
//                maxWithinClusterDist = localMaxDist;
//                sparsestClusterIndex = i;
//            }
//        }
//
//        return (sparsestClusterIndex == -1) ? null : new ClusterSparsenessInfo(sparsestClusterIndex, maxWithinClusterDist);
//    }
//
//    @Override
//    public double getMeasure(Hierarchy h) {
//        Node[] nodes = h.getGroups();
//        if (nodes.length < 2) return Double.NaN;
//
//        ClusterSparsenessInfo sparsestInfo = findSparsestCluster(h);
//        if (sparsestInfo == null || sparsestInfo.maxWithinClusterDistance == 0) return Double.NaN;
//
//        List<Instance> sparsestInstances = nodes[sparsestInfo.clusterIndex].getNodeInstances();
//

//        double minBridgeDist = Double.MAX_VALUE;
//        for (int i = 0; i < nodes.length; i++) {
//            if (i == sparsestInfo.clusterIndex || nodes[i].getNodeInstances().isEmpty()) continue;
//
//            List<Instance> otherInstances = nodes[i].getNodeInstances();
//            for (Instance s : sparsestInstances) {
//                for (Instance o : otherInstances) {
//                    double d = dist.getDistance(s, o);
//                    if (d < minBridgeDist) {
//                        minBridgeDist = d;
//                    }
//                }
//            }
//        }
//
//        if (minBridgeDist == Double.MAX_VALUE) {
//            return Double.NaN;
//        }
////
//        return  sparsestInfo.maxWithinClusterDistance/minBridgeDist;
//    }
//
//    @Override
//    public double getDesiredValue() {
//        return 1e6;
//    }
//
//    @Override
//    public double getNotDesiredValue() {
//        return 0;
//    }
//
//    @Override
//    public boolean isFirstMeasureBetterThanSecond(double firstMeasure, double secondMeasure) {
//        return firstMeasure < secondMeasure;
//    }
//
//    @Override
//    public boolean shouldMeasureBeMaximised() {
//        return true;
//    }
//
//    @Override
//    public String getName() {
//        return "FlatBridgeSparseness";
//    }
//}


package internal_measures;

import basic_hierarchy.implementation.BasicNode;
import basic_hierarchy.interfaces.Hierarchy;
import basic_hierarchy.interfaces.Instance;
import basic_hierarchy.interfaces.Node;
import common.CommonQualityMeasure;
import interfaces.DistanceMeasure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlatBridgeSparsenessMeasure extends CommonQualityMeasure {
    private DistanceMeasure dist;

    private FlatBridgeSparsenessMeasure() {}

    public FlatBridgeSparsenessMeasure(DistanceMeasure dist) {
        this.dist = dist;
    }

    @Override
    public double getMeasure(Hierarchy h) {
        double maxWithinNodeDistance = (-1)*Double.MAX_VALUE;
        double minNodesCentersDistance = Double.MAX_VALUE;

        Node[] nodes = h.getGroups();

        Instance[] oldRepr = new Instance[nodes.length];
        for(int n = 0; n < nodes.length; n++) {
            oldRepr[n] = ((BasicNode)nodes[n]).recalculateCentroid(false);
        }

        for(int n1 = 0; n1 < nodes.length; n1++)
        {
            ArrayList<Instance> n1Instances = new ArrayList<>(nodes[n1].getNodeInstances());
            if(!nodes[n1].getNodeInstances().isEmpty()) {
                for (int i1 = 0; i1 < n1Instances.size(); i1++) {
                    for (int i2 = i1; i2 < n1Instances.size(); i2++) {
                        double distance = dist.getDistance(n1Instances.get(i1), n1Instances.get(i2));//TODO: random index access is slow on linked lists
                        maxWithinNodeDistance = Math.max(distance, maxWithinNodeDistance);
                    }
                }

                for (int n2 = n1 + 1; n2 < nodes.length; n2++) {
                    if (!nodes[n2].getNodeInstances().isEmpty()){
                        double distance =
                                dist.getDistance(nodes[n1].getNodeRepresentation(), nodes[n2].getNodeRepresentation());
                        minNodesCentersDistance = Math.min(distance, minNodesCentersDistance);
                    }
                }
            }
        }

        for(int n = 0; n < nodes.length; n++) {
            ((BasicNode)nodes[n]).setRepresentation(oldRepr[n]);
        }

        if(maxWithinNodeDistance == (-1)*Double.MAX_VALUE) {
            System.err.println("FlatWithinBetweenIndex.getMeasure maxWithinNodeDistance didn't change! It is probably " +
                    "because every cluster contain at maximum 1 instance. Returning NaN.");
            return Double.NaN;
        }
        if(minNodesCentersDistance == Double.MAX_VALUE) {
            System.err.println("FlatWithinBetweenIndex.getMeasure minNodesCentersDistance haven't changed, so there " +
                    "should be something wrong with the input hierarchy (maybe there are empty clusters or clusters " +
                    "with single element?). Returning NaN. ");
            return Double.NaN;
        }
        return maxWithinNodeDistance/minNodesCentersDistance;
    }

    @Override
    public double getDesiredValue() {
        return 0;
    }

    @Override
    public double getNotDesiredValue() {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean isFirstMeasureBetterThanSecond(double firstMeasure, double secondMeasure) {
        return firstMeasure < secondMeasure;
    }

    @Override
    public boolean shouldMeasureBeMaximised() {
        return false;
    }

    @Override
    public String getName() {
        return "FBSM";
    }

}
