package com.hhjt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhjt.entity.PointsExchange;
import com.hhjt.entity.PointsGift;
import com.hhjt.service.PointsExchangeService;
import com.hhjt.mapper.PointsGiftMapper;
import com.hhjt.mapper.PointsExchangeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分兑换管理控制器（管理员端）
 */
@Slf4j
@Controller
@RequestMapping("/admin/exchange")
public class AdminExchangeController {
    
    @Autowired
    private PointsExchangeService pointsExchangeService;
    
    @Autowired
    private PointsGiftMapper pointsGiftMapper;
    
    @Autowired
    private PointsExchangeMapper pointsExchangeMapper;
    
    /**
     * 兑换记录管理页面
     */
    @GetMapping("/manage")
    public String exchangeManage(Model model,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size,
                                @RequestParam(required = false) Integer status) {
        IPage<PointsExchange> exchangePage = pointsExchangeService.getAllExchanges(page, size, status);
        
        model.addAttribute("page", exchangePage);
        model.addAttribute("status", status);
        
        return "admin/exchange_manage";
    }
    
    /**
     * 奖品管理页面
     */
    @GetMapping("/gifts")
    public String giftManage(Model model,
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer size) {
        Page<PointsGift> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<PointsGift> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(PointsGift::getSortOrder);
        
        IPage<PointsGift> giftPage = pointsGiftMapper.selectPage(pageInfo, wrapper);
        model.addAttribute("page", giftPage);
        
        return "admin/gift_manage";
    }
    
    /**
     * 添加奖品页面
     */
    @GetMapping("/gift/add")
    public String addGiftPage() {
        return "admin/gift_add";
    }
    
    /**
     * 编辑奖品页面
     */
    @GetMapping("/gift/edit/{id}")
    public String editGiftPage(@PathVariable Long id, Model model) {
        PointsGift gift = pointsGiftMapper.selectById(id);
        if (gift == null) {
            throw new RuntimeException("奖品不存在");
        }
        model.addAttribute("gift", gift);
        return "admin/gift_edit";
    }
    
    /**
     * 保存奖品（新增或更新）
     */
    @PostMapping("/gift/save")
    @ResponseBody
    public Map<String, Object> saveGift(@RequestBody PointsGift gift) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success;
            if (gift.getId() == null) {
                // 新增
                success = pointsGiftMapper.insert(gift) > 0;
            } else {
                // 更新
                success = pointsGiftMapper.updateById(gift) > 0;
            }
            result.put("success", success);
            result.put("message", success ? "保存成功" : "保存失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存失败：" + e.getMessage());
            log.error("保存奖品异常", e);
        }
        return result;
    }
    
    /**
     * 删除奖品
     */
    @PostMapping("/gift/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteGift(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 检查是否有兑换记录
            long count = pointsExchangeMapper.selectCount(
                new LambdaQueryWrapper<PointsExchange>().eq(PointsExchange::getGiftId, id)
            );
            if (count > 0) {
                result.put("success", false);
                result.put("message", "该奖品已有兑换记录，不能删除，建议下架");
                return result;
            }
            
            boolean success = pointsGiftMapper.deleteById(id) > 0;
            result.put("success", success);
            result.put("message", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
            log.error("删除奖品异常", e);
        }
        return result;
    }
    
    /**
     * 切换奖品状态（上架/下架）
     */
    @PostMapping("/gift/toggle-status/{id}")
    @ResponseBody
    public Map<String, Object> toggleGiftStatus(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            PointsGift gift = pointsGiftMapper.selectById(id);
            if (gift == null) {
                result.put("success", false);
                result.put("message", "奖品不存在");
                return result;
            }
            
            gift.setStatus(gift.getStatus() == 1 ? 0 : 1);
            boolean success = pointsGiftMapper.updateById(gift) > 0;
            result.put("success", success);
            result.put("message", success ? "操作成功" : "操作失败");
            result.put("newStatus", gift.getStatus());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
            log.error("切换奖品状态异常", e);
        }
        return result;
    }
}
