package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;


    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }




    //订单管理
    @Transactional
    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId());
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }



    /**
     * 后台查询订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number,String beginTime,String endTime){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件  动态sql  字符串使用StringUtils.isNotEmpty这个方法来判断
        //这里使用了范围查询的动态SQL，这里是重点！！！
        queryWrapper.like(number!=null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);

        ordersService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    @PutMapping
    public R<String> orderStatusChange(@RequestBody Map<String,String> map){

        String id = map.get("id");
        Long orderId = Long.parseLong(id);
        Integer status = Integer.parseInt(map.get("status"));

        if(orderId == null || status==null){
            return R.error("传入信息不合法");
        }
        Orders orders = ordersService.getById(orderId);
        orders.setStatus(status);
        ordersService.updateById(orders);

        return R.success("订单状态修改成功");

    }


}
