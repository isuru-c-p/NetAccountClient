package nz.ac.auckland.netlogin;

public enum NetLoginPlan {

    UNKNOWN(0, "Unknown"),
    STAFF(1, "Staff"),
    SPONSORED(2, "Sponsored"),
    UNDERGRADUATE(3, "Undergraduate"),
    EXCEEDED_ALLOWANCE(4, "Exceeded Allowance"),
    NO_ACCESS(5, "No Access"),
    POSTGRADUATE(6, "Postgraduate");

    private int key;
    private String description;

    NetLoginPlan(int key, String description) {
        this.key = key;
        this.description = description;
    }

    public int getKey() {
        return key;
    }

    public String toString() {
        return description;
    }

    public static NetLoginPlan lookupPlan(int planKey) {
        for(NetLoginPlan plan : values()) {
            if (plan.key == planKey) return plan;
        }
        return UNKNOWN;
    }

    public static NetLoginPlan lookupPlanFromFlags(int planFlags) {
		int planKey = (planFlags & 0x0F000000) >> 24;
        return lookupPlan(planKey);
    }

}
