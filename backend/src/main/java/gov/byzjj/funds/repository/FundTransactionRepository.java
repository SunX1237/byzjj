package gov.byzjj.funds.repository;

import gov.byzjj.funds.entity.FundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 维修资金流水数据访问。
 */
public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {

    /**
     * 按发生日期升序查询单户流水。
     *
     * @param houseId 房屋 ID
     * @return 流水列表
     */
    List<FundTransaction> findByHouseIdOrderByOccurredAtAscIdAsc(Long houseId);

    /**
     * 按发生日期升序查询多户流水。
     *
     * @param houseIds 房屋 ID 集合，不能为空
     * @return 流水列表
     */
    List<FundTransaction> findByHouseIdInOrderByOccurredAtAscIdAsc(Collection<Long> houseIds);
}
