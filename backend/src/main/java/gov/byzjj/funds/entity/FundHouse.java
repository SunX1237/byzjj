package gov.byzjj.funds.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * 维修资金房屋。
 */
@Entity
@Table(name = "fund_house", indexes = @Index(name = "idx_house_unit", columnList = "unit_id"))
public class FundHouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "room_no", nullable = false, length = 30)
    private String roomNo;

    @Column(name = "owner_name", nullable = false, length = 50)
    private String ownerName;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal area;

    protected FundHouse() {
    }

    /**
     * 创建房屋。
     *
     * @param unitId    所属单元 ID
     * @param roomNo    房号
     * @param ownerName 业主姓名
     * @param area      建筑面积
     */
    public FundHouse(Long unitId, String roomNo, String ownerName, BigDecimal area) {
        this.unitId = unitId;
        this.roomNo = roomNo;
        this.ownerName = ownerName;
        this.area = area;
    }

    /**
     * 获取房屋主键。
     *
     * @return 房屋 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取所属单元 ID。
     *
     * @return 单元 ID
     */
    public Long getUnitId() {
        return unitId;
    }

    /**
     * 获取房号。
     *
     * @return 房号
     */
    public String getRoomNo() {
        return roomNo;
    }

    /**
     * 获取业主姓名。
     *
     * @return 业主姓名
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * 获取建筑面积。
     *
     * @return 建筑面积
     */
    public BigDecimal getArea() {
        return area;
    }
}
