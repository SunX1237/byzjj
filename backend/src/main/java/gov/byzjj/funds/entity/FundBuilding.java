package gov.byzjj.funds.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * 维修资金楼幢。
 */
@Entity
@Table(name = "fund_building", indexes = @Index(name = "idx_building_project", columnList = "project_id"))
public class FundBuilding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 50)
    private String name;

    protected FundBuilding() {
    }

    /**
     * 创建楼幢。
     *
     * @param projectId 所属项目 ID
     * @param name      楼幢名称
     */
    public FundBuilding(Long projectId, String name) {
        this.projectId = projectId;
        this.name = name;
    }

    /**
     * 获取楼幢主键。
     *
     * @return 楼幢 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取所属项目 ID。
     *
     * @return 项目 ID
     */
    public Long getProjectId() {
        return projectId;
    }

    /**
     * 获取楼幢名称。
     *
     * @return 楼幢名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置楼幢名称。
     *
     * @param name 楼幢名称
     */
    public void setName(String name) {
        this.name = name;
    }
}
