package com.itheima.reggie.controller;

/*
* 菜品管理
* */

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /*
     *新增菜品
     * */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("新增菜品：{}",dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功！！");
    }



    /*
    * 菜品信息分页查询
    * */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){

        // 封装分页对象
        Page<Dish> dishInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        // 条件构造器对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Dish::getName,name);// 模糊查询
        queryWrapper.orderByDesc(Dish::getUpdateTime);// 排序

        // 执行分页查询
        dishService.page(dishInfo,queryWrapper);

        List<Dish> records = dishInfo.getRecords();
        // 对象拷贝
        BeanUtils.copyProperties(dishInfo,dishDtoPage,"records");

        List<DishDto> list = records.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId(); // 分类id
            Category category = categoryService.getById(categoryId);// 根据id查询分类对象
            if(category != null){
                String categoryName = category.getName(); // 分类名称
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }


    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功！");
    }


    /**
     * 菜品状态
     * @param condition
     * @param ids
     * @return
     */
    @PostMapping("/status/{condition}")
    public R<String> status(@PathVariable int condition,@RequestParam Long[] ids){

        for (int i = 0; i < ids.length; i++) {
            Dish dish = new Dish();
            dish.setId(ids[i]);
            dish.setStatus(condition);
            dishService.updateById(dish);
        }

/*        Dish dish = new Dish();
        dish.setId(ids);
        dish.setStatus(condition);
        dishService.updateById(dish);*/
        return R.success("修改菜品状态成功！");
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        dishService.deleteWithFlavor(ids);
        return R.success("删除菜品成功！");
    }

    /**
     * 根据条件查询菜品数据
     * @param dish
     * @return
     */

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        // 查询状态为：1的菜品（查询起售菜品）
        queryWrapper.eq(Dish::getStatus,1);

        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();

            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            Long dishId = item.getId();

            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);

            dishDto.setFlavors(dishFlavorList);

            return dishDto;


        }).collect(Collectors.toList());



        return R.success(dishDtoList);
    }

   /*
    @GetMapping("/list")
     public R<List<Dish>> list(Dish dish){
        // 构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        // 查询状态为：1的菜品（查询起售菜品）
        queryWrapper.eq(Dish::getStatus,1);

        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
     }*/
}
