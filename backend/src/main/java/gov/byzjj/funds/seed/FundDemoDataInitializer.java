package gov.byzjj.funds.seed;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 维修资金演示数据初始化器。
 * 启动时生成项目、楼幢、单元、房屋及资金流水；使用固定随机种子和相对日期，重启后自动重建。
 */
@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class FundDemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FundDemoDataInitializer.class);

    private static final String[] PROJECT_NAMES = {"阳光花园", "锦绣江南", "翠竹苑"};
    private static final String[] BUILDING_NAMES = {"1号楼", "2号楼"};
    private static final String[] UNIT_NAMES = {"1单元", "2单元"};
    private static final String[] ROOM_NUMBERS = {"101室", "102室", "201室", "202室"};
    private static final String[] SURNAMES = {"张", "王", "李", "赵", "刘", "陈", "杨", "黄", "周", "吴", "徐", "孙"};
    private static final String[] GIVEN_NAMES = {"伟", "芳", "静", "磊"};
    private static final String[] USAGE_REMARKS = {"电梯维修分摊", "屋面防水维修", "消防设施维护", "公共照明维修"};

    /** 年利率 1.5%，按季结息 */
    private static final BigDecimal QUARTERLY_RATE =
            new BigDecimal("0.015").divide(new BigDecimal("4"), 10, RoundingMode.HALF_UP);

    /** 单次使用金额不超过当时余额的 30%，保证余额为正 */
    private static final BigDecimal USAGE_LIMIT_RATE = new BigDecimal("0.30");

    private final FundProjectRepository projectRepository;
    private final FundBuildingRepository buildingRepository;
    private final FundUnitRepository unitRepository;
    private final FundHouseRepository houseRepository;
    private final FundTransactionRepository transactionRepository;

    /**
     * 构造演示数据初始化器。
     *
     * @param projectRepository     项目仓储
     * @param buildingRepository    楼幢仓储
     * @param unitRepository        单元仓储
     * @param houseRepository       房屋仓储
     * @param transactionRepository 流水仓储
     */
    public FundDemoDataInitializer(FundProjectRepository projectRepository,
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
     * 生成演示数据。已有数据时直接跳过，保证幂等。
     *
     * @param args 启动参数
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (projectRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();
        Random random = new Random(20261024L);
        List<FundTransaction> transactions = new ArrayList<>();
        int ownerIndex = 0;

        for (String projectName : PROJECT_NAMES) {
            FundProject project = projectRepository.save(new FundProject(projectName));

            for (String buildingName : BUILDING_NAMES) {
                FundBuilding building = buildingRepository.save(
                        new FundBuilding(project.getId(), buildingName));

                for (String unitName : UNIT_NAMES) {
                    FundUnit unit = unitRepository.save(
                            new FundUnit(building.getId(), unitName));

                    for (String roomNo : ROOM_NUMBERS) {
                        String ownerName = SURNAMES[ownerIndex % SURNAMES.length]
                                + GIVEN_NAMES[ownerIndex / SURNAMES.length];
                        ownerIndex++;

                        BigDecimal area = BigDecimal.valueOf(80 + random.nextInt(51))
                                .setScale(2, RoundingMode.HALF_UP);
                        FundHouse house = houseRepository.save(
                                new FundHouse(unit.getId(), roomNo, ownerName, area));

                        transactions.addAll(createTransactions(house.getId(), today, random));
                    }
                }
            }
        }

        transactionRepository.saveAll(transactions);
        log.info("已生成维修资金演示数据：{} 个项目，{} 户，{} 条流水",
                projectRepository.count(), houseRepository.count(), transactions.size());
    }

    /**
     * 为单户生成固定 13 条流水：3 条交存、8 条结息、2 条使用。
     * 事件按时间顺序生成，结息和使用金额依赖当时余额。
     *
     * @param houseId 房屋 ID
     * @param today   当前日期
     * @param random  随机数生成器
     * @return 该房屋的流水列表
     */
    private List<FundTransaction> createTransactions(Long houseId, LocalDate today, Random random) {
        List<FundTransaction> list = new ArrayList<>();
        BigDecimal balance = BigDecimal.ZERO;

        // 首期交存
        BigDecimal initial = money(4000 + random.nextInt(4001));
        list.add(new FundTransaction(houseId, FundTransactionType.DEPOSIT, initial,
                today.minusMonths(24), "首期交存"));
        balance = balance.add(initial);

        // 季度结息、使用、再结息
        balance = addInterest(list, houseId, balance, today.minusMonths(21));
        balance = addUsage(list, houseId, balance, today.minusMonths(20), random);
        balance = addInterest(list, houseId, balance, today.minusMonths(18));
        balance = addInterest(list, houseId, balance, today.minusMonths(15));

        // 年度续交
        BigDecimal annualDeposit = money(600 + random.nextInt(901));
        list.add(new FundTransaction(houseId, FundTransactionType.DEPOSIT, annualDeposit,
                today.minusMonths(12), "年度续交"));
        balance = balance.add(annualDeposit);

        balance = addInterest(list, houseId, balance, today.minusMonths(11));
        balance = addInterest(list, houseId, balance, today.minusMonths(9));
        balance = addInterest(list, houseId, balance, today.minusMonths(6));
        balance = addUsage(list, houseId, balance, today.minusMonths(5), random);
        balance = addInterest(list, houseId, balance, today.minusMonths(3));

        // 最近一次年度续交与结息
        BigDecimal latestDeposit = money(600 + random.nextInt(901));
        list.add(new FundTransaction(houseId, FundTransactionType.DEPOSIT, latestDeposit,
                today.minusMonths(1), "年度续交"));
        balance = balance.add(latestDeposit);
        addInterest(list, houseId, balance, today.minusDays(2));

        return list;
    }

    /**
     * 按当前余额生成季度结息流水。
     *
     * @param list    流水列表
     * @param houseId 房屋 ID
     * @param balance 当前余额
     * @param date    结息日期
     * @return 结息后的余额
     */
    private BigDecimal addInterest(List<FundTransaction> list, Long houseId,
                                   BigDecimal balance, LocalDate date) {
        BigDecimal interest = balance.multiply(QUARTERLY_RATE).setScale(2, RoundingMode.HALF_UP);
        if (interest.compareTo(BigDecimal.ZERO) <= 0) {
            return balance;
        }
        list.add(new FundTransaction(houseId, FundTransactionType.INTEREST, interest, date, "季度结息"));
        return balance.add(interest);
    }

    /**
     * 生成使用流水，金额不超过当时余额的 30%。
     *
     * @param list    流水列表
     * @param houseId 房屋 ID
     * @param balance 当前余额
     * @param date    使用日期
     * @param random  随机数生成器
     * @return 使用后的余额
     */
    private BigDecimal addUsage(List<FundTransaction> list, Long houseId,
                                BigDecimal balance, LocalDate date, Random random) {
        BigDecimal maxUsage = balance.multiply(USAGE_LIMIT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal requested = money(300 + random.nextInt(801));
        BigDecimal amount = requested.min(maxUsage);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return balance;
        }
        String remark = USAGE_REMARKS[random.nextInt(USAGE_REMARKS.length)];
        list.add(new FundTransaction(houseId, FundTransactionType.USAGE, amount, date, remark));
        return balance.subtract(amount);
    }

    /**
     * 将整数金额转换为两位小数。
     *
     * @param value 整数金额
     * @return BigDecimal 金额
     */
    private BigDecimal money(int value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
