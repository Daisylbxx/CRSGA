package algorithms.evolutionary_algorithms.selection;

public enum IndividualsPairingMethod {
    DISTANT_IMMEDIATE_NEIGHBOUR_PAIR("dinp"),
    DISTANT_IMMEDIATE_NEIGHBOUR_PAIR_PERCENT("dinpp"),
    CROSS_CLUSTER_ALL_POSSIBLE_PAIRS("ccapp"),
    ALL_POSSIBLE_PAIRS("app"),
    DISTANT_IMMEDIATE_NEIGHBOUR_PAIR_SIMPLIFIED("dinps"),
    N_MOST_DISTANT_IMMEDIATE_NEIGHBOUR_PAIRS("nmdinp"),
    N_CROSS_CLUSTER_ALL_POSSIBLE_PAIRS("nccapp"),
    N_ALL_POSSIBLE_PAIRS("napp"),
    DISTANT_IMMEDIATE_NEIGHBOUR_PAIR_TOPX("dinpt"),
    DISTANT_SPARSE_CLISTER_BRIDGE_PAIRS("dscbp");

    private final String name;
    private IndividualsPairingMethod(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
}
