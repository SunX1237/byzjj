package gov.byzjj.funds.repository;

import gov.byzjj.funds.entity.FundBuilding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 维修资金楼幢数据访问。
 */
public interface FundBuildingRepository extends JpaRepository<FundBuilding, Long> {

    /**
     * 按主键升序查询全部楼幢。
     *
     * @return 楼幢列表
     */
    List<FundBuilding> findAllByOrderByIdAsc();

    /**
     * 查询指定项目下的楼幢。
     *
     * @param projectId 项目 ID
     * @return 楼幢列表
     */
    List<FundBuilding> findByProjectIdOrderByIdAsc(Long projectId);
}
