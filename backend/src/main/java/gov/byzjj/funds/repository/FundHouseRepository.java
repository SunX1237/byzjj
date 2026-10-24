package gov.byzjj.funds.repository;

import gov.byzjj.funds.entity.FundHouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 维修资金房屋数据访问。
 */
public interface FundHouseRepository extends JpaRepository<FundHouse, Long> {

    /**
     * 按主键升序查询全部房屋。
     *
     * @return 房屋列表
     */
    List<FundHouse> findAllByOrderByIdAsc();

    /**
     * 查询指定单元下的房屋。
     *
     * @param unitId 单元 ID
     * @return 房屋列表
     */
    List<FundHouse> findByUnitIdOrderByIdAsc(Long unitId);

    /**
     * 查询多个单元下的房屋。
     *
     * @param unitIds 单元 ID 集合，不能为空
     * @return 房屋列表
     */
    List<FundHouse> findByUnitIdInOrderByIdAsc(Collection<Long> unitIds);
}
