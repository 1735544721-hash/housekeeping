package org.example.springboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.mapper.ServiceOrderMapper;
import org.example.springboot.mapper.OrderRefundMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 数据迁移控制器
 */
@Tag(name = "数据迁移接口")
@RestController
@RequestMapping("/migration")
public class DataMigrationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataMigrationController.class);

    @Resource
    private JdbcTemplate jdbcTemplate;
    
    @Resource
    private ServiceOrderMapper orderMapper;
    
    @Resource
    private OrderRefundMapper refundMapper;

    @Operation(summary = "迁移订单ID到UUID")
    @PostMapping("/order-id-to-uuid")
    public Result<?> migrateOrderIdToUuid() {
        try {
            LOGGER.info("开始迁移订单ID到UUID...");
            
            // 1. 创建临时字段用于存储旧ID
            try {
                jdbcTemplate.execute("ALTER TABLE service_order ADD COLUMN old_id BIGINT");
                LOGGER.info("添加临时字段 old_id");
            } catch (Exception e) {
                LOGGER.warn("临时字段 old_id 可能已存在");
            }
            
            // 2. 查询所有订单
            String selectSql = "SELECT id FROM service_order WHERE old_id IS NULL ORDER BY id";
            List<Map<String, Object>> orders = jdbcTemplate.queryForList(selectSql);
            
            if (orders.isEmpty()) {
                LOGGER.info("没有需要迁移的订单");
                return Result.success("没有需要迁移的订单");
            }
            
            LOGGER.info("找到 {} 个订单需要迁移", orders.size());
            
            int successCount = 0;
            int failCount = 0;
            
            // 3. 遍历订单，生成UUID并更新
            for (Map<String, Object> order : orders) {
                // 获取旧ID（可能是String或Number）
                Object idObj = order.get("id");
                String oldId;
                Long oldIdNum;
                
                if (idObj instanceof Number) {
                    oldIdNum = ((Number) idObj).longValue();
                    oldId = String.valueOf(oldIdNum);
                } else {
                    oldId = idObj.toString();
                    try {
                        oldIdNum = Long.parseLong(oldId);
                    } catch (NumberFormatException e) {
                        // 如果已经是UUID格式，跳过
                        LOGGER.info("订单 {} 已经是UUID格式，跳过", oldId);
                        continue;
                    }
                }
                
                String newId = UUID.randomUUID().toString().replace("-", "");
                
                try {
                    // 更新订单表
                    String updateOrderSql = "UPDATE service_order SET old_id = ?, id = ? WHERE id = ? AND old_id IS NULL";
                    int orderRows = jdbcTemplate.update(updateOrderSql, oldIdNum, newId, oldId);
                    
                    // 更新退款表
                    String updateRefundSql = "UPDATE order_refund SET order_id = ? WHERE order_id = ?";
                    jdbcTemplate.update(updateRefundSql, newId, oldId);
                    
                    // 更新评价表
                    String updateReviewSql = "UPDATE service_review SET order_id = ? WHERE order_id = ?";
                    jdbcTemplate.update(updateReviewSql, newId, oldId);
                    
                    if (orderRows > 0) {
                        successCount++;
                        if (successCount % 100 == 0) {
                            LOGGER.info("已迁移 {} 个订单...", successCount);
                        }
                    }
                } catch (Exception e) {
                    failCount++;
                    LOGGER.error("迁移订单 {} 失败: {}", oldId, e.getMessage());
                }
            }
            
            LOGGER.info("订单ID迁移完成: 成功 {}, 失败 {}", successCount, failCount);
            
            // 4. 验证迁移结果
            String verifySql = "SELECT COUNT(*) FROM service_order WHERE old_id IS NULL";
            Integer remainingCount = jdbcTemplate.queryForObject(verifySql, Integer.class);
            
            String result = String.format("迁移完成！成功: %d, 失败: %d, 剩余未迁移: %d", 
                                        successCount, failCount, remainingCount);
            
            return Result.success(result);
            
        } catch (Exception e) {
            LOGGER.error("迁移订单ID失败", e);
            return Result.error("迁移失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "清理迁移临时数据")
    @PostMapping("/cleanup")
    public Result<?> cleanupMigrationData() {
        try {
            LOGGER.info("开始清理迁移临时数据...");
            
            // 删除临时字段
            try {
                jdbcTemplate.execute("ALTER TABLE service_order DROP COLUMN old_id");
                LOGGER.info("删除临时字段 old_id");
            } catch (Exception e) {
                LOGGER.error("删除临时字段失败", e);
            }
            
            return Result.success("清理完成");
        } catch (Exception e) {
            LOGGER.error("清理失败", e);
            return Result.error("清理失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "查看迁移状态")
    @GetMapping("/status")
    public Result<?> getMigrationStatus() {
        try {
            // 检查是否有old_id字段
            boolean hasOldId = false;
            try {
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_order WHERE old_id IS NOT NULL", Integer.class);
                hasOldId = true;
            } catch (Exception e) {
                hasOldId = false;
            }
            
            // 统计订单数量
            Integer totalOrders = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM service_order", Integer.class);
            
            // 统计UUID格式的订单（长度为32的字符串ID）
            String uuidCountSql = "SELECT COUNT(*) FROM service_order WHERE LENGTH(id) = 32";
            Integer uuidOrders = jdbcTemplate.queryForObject(uuidCountSql, Integer.class);
            
            String status = String.format(
                "总订单数: %d, UUID格式订单数: %d, 临时字段存在: %s",
                totalOrders, uuidOrders, hasOldId ? "是" : "否"
            );
            
            return Result.success(status);
        } catch (Exception e) {
            LOGGER.error("查询迁移状态失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
    
    @Operation(summary = "修改订单表ID类型为VARCHAR")
    @PostMapping("/alter-table")
    public Result<?> alterTableIdType() {
        try {
            LOGGER.info("开始修改订单表ID类型...");
            
            // 步骤1: 先删除主键约束
            try {
                LOGGER.info("删除主键约束...");
                jdbcTemplate.execute("ALTER TABLE service_order DROP PRIMARY KEY");
            } catch (Exception e) {
                LOGGER.warn("删除主键失败（可能不存在）: {}", e.getMessage());
            }
            
            // 步骤2: 修改列类型（去掉AUTO_INCREMENT）
            LOGGER.info("修改ID列类型...");
            jdbcTemplate.execute("ALTER TABLE service_order MODIFY COLUMN id VARCHAR(32) NOT NULL");
            
            // 步骤3: 重新添加主键约束
            LOGGER.info("重新添加主键约束...");
            jdbcTemplate.execute("ALTER TABLE service_order ADD PRIMARY KEY (id)");
            LOGGER.info("订单表ID类型修改完成");
            
            // 修改退款表order_id类型
            LOGGER.info("修改退款表order_id类型...");
            jdbcTemplate.execute("ALTER TABLE order_refund MODIFY COLUMN order_id VARCHAR(32)");
            LOGGER.info("退款表order_id类型修改完成");
            
            // 修改评价表order_id类型
            LOGGER.info("修改评价表order_id类型...");
            jdbcTemplate.execute("ALTER TABLE service_review MODIFY COLUMN order_id VARCHAR(32)");
            LOGGER.info("评价表order_id类型修改完成");
            
            return Result.success("表结构修改完成！订单表ID已从BIGINT改为VARCHAR(32)");
        } catch (Exception e) {
            LOGGER.error("修改表结构失败", e);
            return Result.error("修改失败: " + e.getMessage());
        }
    }
}

