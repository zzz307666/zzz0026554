package com.hhjt.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hhjt.entity.PointsExchange;
import com.hhjt.entity.PointsGift;
import com.hhjt.entity.Student;
import com.hhjt.entity.User;
import com.hhjt.mapper.StudentMapper;
import com.hhjt.service.PointsExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 积分兑换控制器（学生端）
 */
@Slf4j
@Controller
@RequestMapping("/student/exchange")
public class StudentExchangeController {
    
    @Autowired
    private PointsExchangeService pointsExchangeService;
    
    @Autowired
    private StudentMapper studentMapper;
    
    /**
     * 积分商城页面
     */
    @GetMapping("/shop")
    public String exchangeShop(Model model) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        // 查询学生信息
        Student student = studentMapper.selectByUserId(currentUser.getId());
        if (student == null) {
            throw new RuntimeException("学生信息不存在");
        }
        
        // 获取所有上架的奖品
        List<PointsGift> gifts = pointsExchangeService.getAvailableGifts();
        
        model.addAttribute("student", student);
        model.addAttribute("gifts", gifts);
        
        return "student/exchange_shop";
    }
    
    /**
     * 我的兑换记录
     */
    @GetMapping("/my-orders")
    public String myOrders(Model model,
                          @RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "10") Integer size) {
        // 获取当前用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        
        // 查询学生信息
        Student student = studentMapper.selectByUserId(currentUser.getId());
        if (student == null) {
            throw new RuntimeException("学生信息不存在");
        }
        
        // 查询兑换记录
        IPage<PointsExchange> exchangePage = pointsExchangeService.getStudentExchanges(page, size, student.getId());
        
        model.addAttribute("page", exchangePage);
        
        return "student/my_exchanges";
    }
    
    /**
     * 兑换奖品
     */
    @PostMapping("/do-exchange")
    @ResponseBody
    public Map<String, Object> doExchange(@RequestBody Map<String, Long> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long giftId = params.get("giftId");
            
            // 获取当前用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            
            // 查询学生信息
            Student student = studentMapper.selectByUserId(currentUser.getId());
            if (student == null) {
                result.put("success", false);
                result.put("message", "学生信息不存在");
                return result;
            }
            
            boolean success = pointsExchangeService.exchangeGift(student.getId(), giftId);
            result.put("success", success);
            result.put("message", success ? "兑换成功！请前往领取" : "兑换失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "兑换失败：" + e.getMessage());
            log.error("兑换奖品异常", e);
        }
        return result;
    }
    
    /**
     * 确认领取
     */
    @PostMapping("/confirm-receive/{id}")
    @ResponseBody
    public Map<String, Object> confirmReceive(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = pointsExchangeService.confirmReceive(id);
            result.put("success", success);
            result.put("message", success ? "领取成功" : "领取失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "领取失败：" + e.getMessage());
            log.error("确认领取异常", e);
        }
        return result;
    }
}
