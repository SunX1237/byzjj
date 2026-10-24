package gov.byzjj.funds.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 维修资金流水。
 * 金额始终为正数，方向由流水类型决定：交存/结息增加余额，使用减少余额。
 */
@Entity
@Table(name = "fund_transaction", indexes = {
        @Index(name = "idx_tx_house_date", columnList = "house_id, occurred_at")
})
public class FundTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 20)
    private FundTransactionType txType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDate occurredAt;

    @Column(length = 200)
    private String remark;

    protected FundTransaction() {
    }

    /**
     * 创建资金流水。
     *
     * @param houseId    所属房屋 ID
     * @param txType     流水类型
     * @param amount     金额（正数）
     * @param occurredAt 发生日期
     * @param remark     业务说明
     */
    public FundTransaction(Long houseId, FundTransactionType txType, BigDecimal amount,
                           LocalDate occurredAt, String remark) {
        this.houseId = houseId;
        this.txType = txType;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.remark = remark;
    }

    /**
     * 获取流水主键。
     *
     * @return 流水 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取房屋 ID。
     *
     * @return 房屋 ID
     */
    public Long getHouseId() {
        return houseId;
    }

    /**
     * 获取流水类型。
     *
     * @return 流水类型
     */
    public FundTransactionType getTxType() {
        return txType;
    }

    /**
     * 获取金额。
     *
     * @return 金额
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 获取发生日期。
     *
     * @return 发生日期
     */
    public LocalDate getOccurredAt() {
        return occurredAt;
    }

    /**
     * 获取业务说明。
     *
     * @return 业务说明
     */
    public String getRemark() {
        return remark;
    }
}
