package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.entity.UserEntity;

import java.util.Map;

/**
 * 用户表
 *
 * @author xiaofangfang
 * @email xainfangfang@atguigu.com
 * @date 2021-01-18 19:16:28
 */
public interface UserService extends IService<UserEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    void register(UserEntity userEntity, String code);

    void code(String phone);

    UserEntity queryUser(String loginName, String password);

    Boolean checkData(String data, Integer type);
}

