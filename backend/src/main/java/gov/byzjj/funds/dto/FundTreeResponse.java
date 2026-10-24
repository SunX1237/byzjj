package gov.byzjj.funds.dto;

import java.util.List;

/**
 * 维修资金层级树响应。
 * 依次表示项目、楼幢、单元和房屋，供前端级联筛选使用。
 *
 * @param projects 项目节点列表
 */
public record FundTreeResponse(List<ProjectNode> projects) {

    /**
     * 项目节点。
     *
     * @param id        项目 ID
     * @param name      项目名称
     * @param buildings 楼幢列表
     */
    public record ProjectNode(Long id, String name, List<BuildingNode> buildings) {
    }

    /**
     * 楼幢节点。
     *
     * @param id    楼幢 ID
     * @param name  楼幢名称
     * @param units 单元列表
     */
    public record BuildingNode(Long id, String name, List<UnitNode> units) {
    }

    /**
     * 单元节点。
     *
     * @param id     单元 ID
     * @param name   单元名称
     * @param houses 房屋列表
     */
    public record UnitNode(Long id, String name, List<HouseNode> houses) {
    }

    /**
     * 房屋节点。
     *
     * @param id        房屋 ID
     * @param name      房号
     * @param ownerName 业主姓名
     */
    public record HouseNode(Long id, String name, String ownerName) {
    }
}
