package gov.byzjj.funds.repository;

import gov.byzjj.funds.entity.FundUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 维修资金单元数据访问。
 */
public interface FundUnitRepository extends JpaRepository<FundUnit, Long> {

    /**
     * 按主键升序查询全部单元。
     *
     * @return 单元列表
     */
    List<FundUnit> findAllByOrderByIdAsc();

    /**
     * 查询指定楼幢下的单元。
     *
     * @param buildingId 楼幢 ID
     * @return 单元列表
     */
    List<FundUnit> findByBuildingIdOrderByIdAsc(Long buildingId);

    /**
     * 查询多个楼幢下的单元。
     *
     * @param buildingIds 楼幢 ID 集合，不能为空
     * @return 单元列表
     */
    List<FundUnit> findByBuildingIdInOrderByIdAsc(Collection<Long> buildingIds);
}
