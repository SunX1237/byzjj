package gov.byzjj.funds.repository;

import gov.byzjj.funds.entity.FundProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 维修资金项目数据访问。
 */
public interface FundProjectRepository extends JpaRepository<FundProject, Long> {

    /**
     * 按主键升序查询全部项目。
     *
     * @return 项目列表
     */
    List<FundProject> findAllByOrderByIdAsc();
}
