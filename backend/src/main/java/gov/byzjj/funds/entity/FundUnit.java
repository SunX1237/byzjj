package gov.byzjj.funds.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 维修资金单元。
 */
@Entity
@Table(name = "fund_unit", indexes = @Index(name = "idx_unit_building", columnList = "building_id"))
public class FundUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "building_id", nullable = false)
    private Long buildingId;

    @Column(nullable = false, length = 50)
    private String name;

    protected FundUnit() {
    }

    /**
     * 创建单元。
     *
     * @param buildingId 所属楼幢 ID
     * @param name       单元名称
     */
    public FundUnit(Long buildingId, String name) {
        this.buildingId = buildingId;
        this.name = name;
    }

    /**
     * 获取单元主键。
     *
     * @return 单元 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取所属楼幢 ID。
     *
     * @return 楼幢 ID
     */
    public Long getBuildingId() {
        return buildingId;
    }

    /**
     * 获取单元名称。
     *
     * @return 单元名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置单元名称。
     *
     * @param name 单元名称
     */
    public void setName(String name) {
        this.name = name;
    }
}
