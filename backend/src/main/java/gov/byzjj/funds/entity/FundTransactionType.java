package gov.byzjj.funds.entity;

/**
 * 维修资金流水类型。
 * 枚举代码用于程序判断和前端过滤，中文标签用于界面展示。
 */
public enum FundTransactionType {

    /** 交存 */
    DEPOSIT("交存"),

    /** 使用 */
    USAGE("使用"),

    /** 结息 */
    INTEREST("结息");

    private final String label;

    FundTransactionType(String label) {
        this.label = label;
    }

    /**
     * 获取中文标签。
     *
     * @return 中文标签
     */
    public String getLabel() {
        return label;
    }
}
