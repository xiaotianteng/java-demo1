package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;



@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /*
    * 员工登录
    * */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes()); // md5处理

        // 根据用户名查库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);


        //表中没有记录，则登录失败
        if(emp == null){
            return R.error("登录失败");
        }

        // 表中有记录，比对密码
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        // 表中有账号密码，但查员工状态是否被禁用
        if(emp.getStatus() == 0){
            return R.error("账号被禁用");
        }

        // 登录成功，将员工id存入Session 并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /*
    * 员工登出
    * */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }


    /*
    * 新增员工
    * */

    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request){
        log.info("新增员工，员工信息：{}",employee.toString());
        // 设置新员工的初始密码123456，需要进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
/*
        employee.setCreateTime(LocalDateTime.now());// 设置创建记录的时间
        employee.setUpdateTime(LocalDateTime.now()); // 修改记录的时间
*/

        // 获取当前用户登录的id
        Long empId = (Long) request.getSession().getAttribute("employee");
/*        employee.setCreateUser(empId);  // 设置创建记录的用户
        employee.setUpdateUser(empId);  // 设置修改记录的用户*/

        employeeService.save(employee);
        return R.success("新增员工成功！");
    }


    /*
    *员工信息分页查询
    * */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page:{},pageSize:{},name:{}",page,pageSize,name);

        // 构造分页构造器
        Page pageInfo = new Page(page, pageSize);

        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        // StringUtils.isNotEmpty(name)相当于if（），true进入，f不进入
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);

        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    /*
    * 根据id修改员工信息
    * */
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        // 获取当前用户登录的id
        Long empId = (Long) request.getSession().getAttribute("employee");
/*
        employee.setUpdateTime(LocalDateTime.now());  // 修改时间
        employee.setUpdateUser(empId);  // 设置修改记录的用户
*/

        employeeService.updateById(employee); // 执行修改
        return R.success("员工信息修改成功！");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息：id:{}",id);
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }

        return R.error("没有查询到对应员工信息");
    }


}
