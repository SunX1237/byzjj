package gov.byzjj.funds;

import gov.byzjj.funds.dto.FundQueryResponse;
import gov.byzjj.funds.dto.FundTreeResponse;
import gov.byzjj.funds.repository.FundBuildingRepository;
import gov.byzjj.funds.repository.FundHouseRepository;
import gov.byzjj.funds.repository.FundProjectRepository;
import gov.byzjj.funds.repository.FundTransactionRepository;
import gov.byzjj.funds.repository.FundUnitRepository;
import gov.byzjj.funds.service.FundQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 维修资金查询集成测试。
 * 验证演示数据规模、各层级聚合、余额计算和接口错误响应。
 */
@SpringBootTest
@AutoConfigureMockMvc
class FundQueryIntegrationTest {

    @Autowired
    private FundQueryService fundQueryService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FundProjectRepository projectRepository;

    @Autowired
    private FundBuildingRepository buildingRepository;

    @Autowired
    private FundUnitRepository unitRepository;

    @Autowired
    private FundHouseRepository houseRepository;

    @Autowired
    private FundTransactionRepository transactionRepository;

    /**
     * 层级树应包含 3 个项目、6 幢、12 单元、48 户。
     */
    @Test
    void treeShouldContainExpectedHierarchy() {
        FundTreeResponse tree = fundQueryService.getTree();

        assertThat(tree.projects()).hasSize(3);
        int buildingCount = tree.projects().stream()
                .mapToInt(project -> project.buildings().size())
                .sum();
        int unitCount = tree.projects().stream()
                .flatMap(project -> project.buildings().stream())
                .mapToInt(building -> building.units().size())
                .sum();
        int houseCount = tree.projects().stream()
                .flatMap(project -> project.buildings().stream())
                .flatMap(building -> building.units().stream())
                .mapToInt(unit -> unit.houses().size())
                .sum();

        assertThat(buildingCount).isEqualTo(6);
        assertThat(unitCount).isEqualTo(12);
        assertThat(houseCount).isEqualTo(48);
    }

    /**
     * 仓储计数应与演示数据规模一致。
     */
    @Test
    void repositoryCountsShouldMatchSeedData() {
        assertThat(projectRepository.count()).isEqualTo(3);
        assertThat(buildingRepository.count()).isEqualTo(6);
        assertThat(unitRepository.count()).isEqualTo(12);
        assertThat(houseRepository.count()).isEqualTo(48);
        assertThat(transactionRepository.count()).isEqualTo(624);
    }

    /**
     * 项目层级应返回 16 户汇总，且总额满足余额等式。
     */
    @Test
    void projectQueryShouldAggregateAllHouses() {
        Long projectId = projectRepository.findAllByOrderByIdAsc().get(0).getId();

        FundQueryResponse result = fundQueryService.query(projectId, null, null, null);

        assertThat(result.scope()).isEqualTo("PROJECT");
        assertThat(result.houses()).hasSize(16);
        assertThat(result.house()).isNull();
        assertThat(result.transactions()).isEmpty();
        assertSummaryBalanced(result.summary());
    }

    /**
     * 楼幢与单元层级应逐级收窄房屋范围。
     */
    @Test
    void buildingAndUnitQueriesShouldNarrowResults() {
        FundTreeResponse.ProjectNode project = fundQueryService.getTree().projects().get(0);
        FundTreeResponse.BuildingNode building = project.buildings().get(0);
        FundTreeResponse.UnitNode unit = building.units().get(0);

        FundQueryResponse buildingResult = fundQueryService.query(
                project.id(), building.id(), null, null);
        FundQueryResponse unitResult = fundQueryService.query(
                project.id(), building.id(), unit.id(), null);

        assertThat(buildingResult.scope()).isEqualTo("BUILDING");
        assertThat(buildingResult.houses()).hasSize(8);
        assertThat(unitResult.scope()).isEqualTo("UNIT");
        assertThat(unitResult.houses()).hasSize(4);
    }

    /**
     * 房屋层级应返回 13 条流水，包含三种类型，最新余额等于汇总余额。
     */
    @Test
    void houseQueryShouldReturnTransactionsAndBalancedSummary() {
        FundTreeResponse.ProjectNode project = fundQueryService.getTree().projects().get(0);
        FundTreeResponse.BuildingNode building = project.buildings().get(0);
        FundTreeResponse.UnitNode unit = building.units().get(0);
        FundTreeResponse.HouseNode house = unit.houses().get(0);

        FundQueryResponse result = fundQueryService.query(
                project.id(), building.id(), unit.id(), house.id());

        assertThat(result.scope()).isEqualTo("HOUSE");
        assertThat(result.house()).isNotNull();
        assertThat(result.house().ownerName()).isNotBlank();
        assertThat(result.transactions()).hasSize(13);
        assertThat(result.transactions())
                .extracting(FundQueryResponse.TransactionItem::type)
                .contains("DEPOSIT", "USAGE", "INTEREST");
        assertThat(result.transactions())
                .allMatch(item -> item.amount().compareTo(BigDecimal.ZERO) > 0);
        assertThat(result.transactions().get(0).balanceAfter())
                .isEqualByComparingTo(result.summary().balance());
        assertSummaryBalanced(result.summary());
    }

    /**
     * 缺少项目参数应返回 400。
     *
     * @throws Exception MockMvc 调用异常
     */
    @Test
    void missingProjectShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/funds/query"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 项目不存在应返回 404。
     *
     * @throws Exception MockMvc 调用异常
     */
    @Test
    void unknownProjectShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/funds/query").param("projectId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    /**
     * 楼幢不属于所选项目应返回 400。
     *
     * @throws Exception MockMvc 调用异常
     */
    @Test
    void mismatchedHierarchyShouldReturn400() throws Exception {
        FundTreeResponse.ProjectNode firstProject = fundQueryService.getTree().projects().get(0);
        FundTreeResponse.ProjectNode secondProject = fundQueryService.getTree().projects().get(1);
        Long otherBuildingId = secondProject.buildings().get(0).id();

        mockMvc.perform(get("/api/funds/query")
                        .param("projectId", firstProject.id().toString())
                        .param("buildingId", otherBuildingId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 校验余额等式：交存 + 结息 - 使用 = 余额。
     *
     * @param summary 资金汇总
     */
    private void assertSummaryBalanced(FundQueryResponse.FundSummary summary) {
        BigDecimal expected = summary.depositTotal()
                .add(summary.interestTotal())
                .subtract(summary.usageTotal());
        assertThat(summary.balance()).isEqualByComparingTo(expected);
    }
}
