package gov.byzjj.funds.service;

import gov.byzjj.common.BusinessException;
import gov.byzjj.funds.dto.FundQueryResponse;
import gov.byzjj.funds.dto.FundTreeResponse;
import gov.byzjj.funds.entity.FundBuilding;
import gov.byzjj.funds.entity.FundHouse;
import gov.byzjj.funds.entity.FundProject;
import gov.byzjj.funds.entity.FundTransaction;
import gov.byzjj.funds.entity.FundTransactionType;
import gov.byzjj.funds.entity.FundUnit;
import gov.byzjj.funds.repository.FundBuildingRepository;
import gov.byzjj.funds.repository.FundHouseRepository;
import gov.byzjj.funds.repository.FundProjectRepository;
import gov.byzjj.funds.repository.FundTransactionRepository;
import gov.byzjj.funds.repository.FundUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 维修资金查询服务。
 * 负责层级树、按项目/楼幢/单元/房屋聚合资金数据，以及计算每户流水余额。
 */
@Service
public class FundQueryService {

    private static final String SCOPE_PROJECT = "PROJECT";
    private static final String SCOPE_BUILDING = "BUILDING";
    private static final String SCOPE_UNIT = "UNIT";
    private static final String SCOPE_HOUSE = "HOUSE";

    private final FundProjectRepository projectRepository;
    private final FundBuildingRepository buildingRepository;
    private final FundUnitRepository unitRepository;
    private final FundHouseRepository houseRepository;
    private final FundTransactionRepository transactionRepository;

    /**
     * 构造查询服务。
     *
     * @param projectRepository     项目仓储
     * @param buildingRepository    楼幢仓储
     * @param unitRepository        单元仓储
     * @param houseRepository       房屋仓储
     * @param transactionRepository 流水仓储
     */
    public FundQueryService(FundProjectRepository projectRepository,
                            FundBuildingRepository buildingRepository,
                            FundUnitRepository unitRepository,
                            FundHouseRepository houseRepository,
                            FundTransactionRepository transactionRepository) {
        this.projectRepository = projectRepository;
        this.buildingRepository = buildingRepository;
        this.unitRepository = unitRepository;
        this.houseRepository = houseRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * 获取项目、楼幢、单元、房屋的完整层级树。
     *
     * @return 层级树响应
     */
    @Transactional(readOnly = true)
    public FundTreeResponse getTree() {
        List<FundProject> projects = projectRepository.findAllByOrderByIdAsc();
        List<FundBuilding> buildings = buildingRepository.findAllByOrderByIdAsc();
        List<FundUnit> units = unitRepository.findAllByOrderByIdAsc();
        List<FundHouse> houses = houseRepository.findAllByOrderByIdAsc();

        Map<Long, List<FundBuilding>> buildingsByProject = buildings.stream()
                .collect(Collectors.groupingBy(FundBuilding::getProjectId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<FundUnit>> unitsByBuilding = units.stream()
                .collect(Collectors.groupingBy(FundUnit::getBuildingId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, List<FundHouse>> housesByUnit = houses.stream()
                .collect(Collectors.groupingBy(FundHouse::getUnitId, LinkedHashMap::new, Collectors.toList()));

        List<FundTreeResponse.ProjectNode> projectNodes = projects.stream()
                .map(project -> new FundTreeResponse.ProjectNode(
                        project.getId(),
                        project.getName(),
                        buildingsByProject.getOrDefault(project.getId(), List.of()).stream()
                                .map(building -> new FundTreeResponse.BuildingNode(
                                        building.getId(),
                                        building.getName(),
                                        unitsByBuilding.getOrDefault(building.getId(), List.of()).stream()
                                                .map(unit -> new FundTreeResponse.UnitNode(
                                                        unit.getId(),
                                                        unit.getName(),
                                                        housesByUnit.getOrDefault(unit.getId(), List.of()).stream()
                                                                .map(house -> new FundTreeResponse.HouseNode(
                                                                        house.getId(),
                                                                        house.getRoomNo(),
                                                                        house.getOwnerName()))
                                                                .toList()))
                                                .toList()))
                                .toList()))
                .toList();

        return new FundTreeResponse(projectNodes);
    }

    /**
     * 按项目、楼幢、单元或房屋查询资金汇总与明细。
     * 传入的最深层级决定查询范围，祖先层级用于校验归属。
     *
     * @param projectId 项目 ID，必填
     * @param buildingId 楼幢 ID，可选
     * @param unitId     单元 ID，可选
     * @param houseId    房屋 ID，可选
     * @return 查询结果
     */
    @Transactional(readOnly = true)
    public FundQueryResponse query(Long projectId, Long buildingId, Long unitId, Long houseId) {
        validateParams(projectId, buildingId, unitId, houseId);

        FundProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(404, "项目不存在"));

        FundBuilding building = null;
        if (buildingId != null) {
            building = buildingRepository.findById(buildingId)
                    .orElseThrow(() -> new BusinessException(404, "楼幢不存在"));
            if (!building.getProjectId().equals(projectId)) {
                throw new BusinessException(400, "楼幢不属于所选项目");
            }
        }

        FundUnit unit = null;
        if (unitId != null) {
            unit = unitRepository.findById(unitId)
                    .orElseThrow(() -> new BusinessException(404, "单元不存在"));
            if (!unit.getBuildingId().equals(buildingId)) {
                throw new BusinessException(400, "单元不属于所选楼幢");
            }
        }

        FundHouse house = null;
        if (houseId != null) {
            house = houseRepository.findById(houseId)
                    .orElseThrow(() -> new BusinessException(404, "房屋不存在"));
            if (!house.getUnitId().equals(unitId)) {
                throw new BusinessException(400, "房屋不属于所选单元");
            }
        }

        String scope;
        String scopeName;
        List<FundHouse> scopedHouses;

        if (house != null) {
            scope = SCOPE_HOUSE;
            scopeName = joinNames(project.getName(), building.getName(), unit.getName(), house.getRoomNo());
            scopedHouses = List.of(house);
        } else if (unit != null) {
            scope = SCOPE_UNIT;
            scopeName = joinNames(project.getName(), building.getName(), unit.getName());
            scopedHouses = houseRepository.findByUnitIdOrderByIdAsc(unitId);
        } else if (building != null) {
            scope = SCOPE_BUILDING;
            scopeName = joinNames(project.getName(), building.getName());
            List<FundUnit> units = unitRepository.findByBuildingIdOrderByIdAsc(buildingId);
            List<Long> unitIds = units.stream().map(FundUnit::getId).toList();
            scopedHouses = unitIds.isEmpty() ? List.of() : houseRepository.findByUnitIdInOrderByIdAsc(unitIds);
        } else {
            scope = SCOPE_PROJECT;
            scopeName = project.getName();
            List<FundBuilding> buildings = buildingRepository.findByProjectIdOrderByIdAsc(projectId);
            List<Long> buildingIds = buildings.stream().map(FundBuilding::getId).toList();
            List<FundUnit> units = buildingIds.isEmpty()
                    ? List.of()
                    : unitRepository.findByBuildingIdInOrderByIdAsc(buildingIds);
            List<Long> unitIds = units.stream().map(FundUnit::getId).toList();
            scopedHouses = unitIds.isEmpty() ? List.of() : houseRepository.findByUnitIdInOrderByIdAsc(unitIds);
        }

        List<Long> houseIds = scopedHouses.stream().map(FundHouse::getId).toList();
        List<FundTransaction> transactions = houseIds.isEmpty()
                ? List.of()
                : transactionRepository.findByHouseIdInOrderByOccurredAtAscIdAsc(houseIds);
        Map<Long, List<FundTransaction>> transactionsByHouse = transactions.stream()
                .collect(Collectors.groupingBy(FundTransaction::getHouseId, LinkedHashMap::new, Collectors.toList()));

        Map<Long, FundUnit> unitMap = unitRepository.findAllByOrderByIdAsc().stream()
                .collect(Collectors.toMap(FundUnit::getId, Function.identity()));
        Map<Long, FundBuilding> buildingMap = buildingRepository.findAllByOrderByIdAsc().stream()
                .collect(Collectors.toMap(FundBuilding::getId, Function.identity()));

        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalUsage = BigDecimal.ZERO;
        BigDecimal totalInterest = BigDecimal.ZERO;
        List<FundQueryResponse.HouseSummary> houseSummaries = new ArrayList<>();

        if (!SCOPE_HOUSE.equals(scope)) {
            for (FundHouse scopedHouse : scopedHouses) {
                FundUnit houseUnit = unitMap.get(scopedHouse.getUnitId());
                FundBuilding houseBuilding = houseUnit == null ? null : buildingMap.get(houseUnit.getBuildingId());
                FundQueryResponse.FundSummary houseSummary = summarize(
                        transactionsByHouse.getOrDefault(scopedHouse.getId(), List.of()));

                totalDeposit = totalDeposit.add(houseSummary.depositTotal());
                totalUsage = totalUsage.add(houseSummary.usageTotal());
                totalInterest = totalInterest.add(houseSummary.interestTotal());

                houseSummaries.add(new FundQueryResponse.HouseSummary(
                        scopedHouse.getId(),
                        houseBuilding == null ? null : houseBuilding.getId(),
                        houseBuilding == null ? "未知楼幢" : houseBuilding.getName(),
                        houseUnit == null ? null : houseUnit.getId(),
                        houseUnit == null ? "未知单元" : houseUnit.getName(),
                        scopedHouse.getRoomNo(),
                        scopedHouse.getOwnerName(),
                        scopedHouse.getArea(),
                        houseSummary.depositTotal(),
                        houseSummary.usageTotal(),
                        houseSummary.interestTotal(),
                        houseSummary.balance()
                ));
            }
        } else {
            FundQueryResponse.FundSummary houseSummary = summarize(
                    transactionsByHouse.getOrDefault(house.getId(), List.of()));
            totalDeposit = houseSummary.depositTotal();
            totalUsage = houseSummary.usageTotal();
            totalInterest = houseSummary.interestTotal();
        }

        BigDecimal totalBalance = totalDeposit.add(totalInterest).subtract(totalUsage);
        FundQueryResponse.FundSummary summary = new FundQueryResponse.FundSummary(
                scale(totalDeposit), scale(totalUsage), scale(totalInterest), scale(totalBalance));

        FundQueryResponse.HouseDetail detail = null;
        List<FundQueryResponse.TransactionItem> items = List.of();
        if (SCOPE_HOUSE.equals(scope)) {
            detail = new FundQueryResponse.HouseDetail(
                    project.getName(),
                    building.getName(),
                    unit.getName(),
                    house.getRoomNo(),
                    house.getOwnerName(),
                    house.getArea()
            );
            items = buildTransactionItems(transactionsByHouse.getOrDefault(house.getId(), List.of()));
        }

        return new FundQueryResponse(scope, scopeName, summary, houseSummaries, detail, items);
    }

    /**
     * 校验查询参数层级关系。
     *
     * @param projectId  项目 ID
     * @param buildingId 楼幢 ID
     * @param unitId     单元 ID
     * @param houseId    房屋 ID
     */
    private void validateParams(Long projectId, Long buildingId, Long unitId, Long houseId) {
        if (projectId == null) {
            throw new BusinessException(400, "请选择项目");
        }
        if (unitId != null && buildingId == null) {
            throw new BusinessException(400, "请先选择楼幢");
        }
        if (houseId != null && (unitId == null || buildingId == null)) {
            throw new BusinessException(400, "请先选择楼幢和单元");
        }
    }

    /**
     * 统计一组流水的交存、使用、结息和余额。
     *
     * @param transactions 流水列表
     * @return 资金汇总
     */
    private FundQueryResponse.FundSummary summarize(List<FundTransaction> transactions) {
        BigDecimal deposit = BigDecimal.ZERO;
        BigDecimal usage = BigDecimal.ZERO;
        BigDecimal interest = BigDecimal.ZERO;
        for (FundTransaction transaction : transactions) {
            switch (transaction.getTxType()) {
                case DEPOSIT -> deposit = deposit.add(transaction.getAmount());
                case USAGE -> usage = usage.add(transaction.getAmount());
                case INTEREST -> interest = interest.add(transaction.getAmount());
            }
        }
        BigDecimal balance = deposit.add(interest).subtract(usage);
        return new FundQueryResponse.FundSummary(
                scale(deposit), scale(usage), scale(interest), scale(balance));
    }

    /**
     * 构建倒序流水明细，并计算每笔流水后的余额。
     *
     * @param transactions 按日期升序的流水
     * @return 倒序流水明细
     */
    private List<FundQueryResponse.TransactionItem> buildTransactionItems(List<FundTransaction> transactions) {
        BigDecimal running = BigDecimal.ZERO;
        List<FundQueryResponse.TransactionItem> items = new ArrayList<>();
        for (FundTransaction transaction : transactions) {
            BigDecimal signed = transaction.getTxType() == FundTransactionType.USAGE
                    ? transaction.getAmount().negate()
                    : transaction.getAmount();
            running = running.add(signed);
            items.add(new FundQueryResponse.TransactionItem(
                    transaction.getId(),
                    transaction.getTxType().name(),
                    transaction.getTxType().getLabel(),
                    scale(transaction.getAmount()),
                    transaction.getOccurredAt(),
                    transaction.getRemark(),
                    scale(running)
            ));
        }
        Collections.reverse(items);
        return items;
    }

    /**
     * 金额统一保留两位小数。
     *
     * @param value 金额
     * @return 两位小数金额
     */
    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 拼接范围名称。
     *
     * @param names 名称片段
     * @return 使用「 · 」连接的范围名称
     */
    private String joinNames(String... names) {
        return String.join(" · ", names);
    }
}
