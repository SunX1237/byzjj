package gov.byzjj.funds.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 维修资金查询响应。
 * 聚合层级返回房屋列表，房屋层级返回房屋详情与流水。
 *
 * @param scope        查询层级：PROJECT / BUILDING / UNIT / HOUSE
 * @param scopeName    当前范围名称
 * @param summary      当前范围资金汇总
 * @param houses       房屋汇总列表；房屋层级为空
 * @param house        房屋详情；聚合层级为 null
 * @param transactions 资金流水；聚合层级为空
 */
public record FundQueryResponse(
        String scope,
        String scopeName,
        FundSummary summary,
        List<HouseSummary> houses,
        HouseDetail house,
        List<TransactionItem> transactions
) {

    /**
     * 资金汇总。
     *
     * @param depositTotal  累计交存
     * @param usageTotal    累计使用
     * @param interestTotal 累计结息
     * @param balance       当前余额
     */
    public record FundSummary(BigDecimal depositTotal, BigDecimal usageTotal,
                              BigDecimal interestTotal, BigDecimal balance) {
    }

    /**
     * 房屋汇总行。
     *
     * @param houseId       房屋 ID
     * @param buildingId    楼幢 ID，用于下钻回填
     * @param buildingName  楼幢名称
     * @param unitId        单元 ID，用于下钻回填
     * @param unitName      单元名称
     * @param roomNo        房号
     * @param ownerName     业主姓名
     * @param area          建筑面积
     * @param depositTotal  累计交存
     * @param usageTotal    累计使用
     * @param interestTotal 累计结息
     * @param balance       当前余额
     */
    public record HouseSummary(Long houseId, Long buildingId, String buildingName,
                               Long unitId, String unitName, String roomNo,
                               String ownerName, BigDecimal area,
                               BigDecimal depositTotal, BigDecimal usageTotal,
                               BigDecimal interestTotal, BigDecimal balance) {
    }

    /**
     * 房屋详情。
     *
     * @param projectName  项目名称
     * @param buildingName 楼幢名称
     * @param unitName     单元名称
     * @param roomNo       房号
     * @param ownerName    业主姓名
     * @param area         建筑面积
     */
    public record HouseDetail(String projectName, String buildingName, String unitName,
                              String roomNo, String ownerName, BigDecimal area) {
    }

    /**
     * 资金流水项。
     *
     * @param id           流水 ID
     * @param type         类型代码：DEPOSIT / USAGE / INTEREST
     * @param typeLabel    类型中文标签
     * @param amount       金额（正数）
     * @param occurredAt   发生日期
     * @param remark       业务说明
     * @param balanceAfter 该笔流水后的余额
     */
    public record TransactionItem(Long id, String type, String typeLabel,
                                  BigDecimal amount, LocalDate occurredAt,
                                  String remark, BigDecimal balanceAfter) {
    }
}
