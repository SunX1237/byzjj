package gov.byzjj.funds.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 维修资金项目（住宅小区）。
 */
@Entity
@Table(name = "fund_project")
public class FundProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    protected FundProject() {
    }

    /**
     * 创建项目。
     *
     * @param name 项目名称
     */
    public FundProject(String name) {
        this.name = name;
    }

    /**
     * 获取项目主键。
     *
     * @return 项目 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 获取项目名称。
     *
     * @return 项目名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置项目名称。
     *
     * @param name 项目名称
     */
    public void setName(String name) {
        this.name = name;
    }
}
