package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.CollectionUtils;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public void register(UserEntity userEntity, String code) {

        //TODO 判断验证码是否正确,从redis中取出验证码

        //TODO 查询用户是否已存在

        String salt = StringUtils.substring(UUID.randomUUID().toString(), 6);
        //将盐值设置给用户
        userEntity.setSalt(salt);

        //对密码进行加盐 然后加密
        String password = DigestUtils.md5Hex(userEntity.getPassword() + salt);
        userEntity.setPassword(password);


        // 4.新增用户
        userEntity.setLevelId(1l);
        userEntity.setNickname(userEntity.getUsername());
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());

        this.save(userEntity);

        //TODO 新增成功后，删除redis中验证码


    }

    @Override
    public void code(String phone) {

        //todo 验证手机号是否合法


        //调用阿里云发送短信


    }

    @Override
    public UserEntity queryUser(String loginName, String password) {

        //先根据登录账号，查询用户的盐
        List<UserEntity> userEntityList = this.list(new QueryWrapper<UserEntity>()
                .eq("username", loginName).or().eq("phone", loginName).or().eq("email", loginName));

        if (CollectionUtils.isEmpty(userEntityList)) {
            return null;
        }

        //对密码进行加盐加密，然后和数据库中密码比较
        for (UserEntity userEntity : userEntityList) {
            password = DigestUtils.md5Hex(password + userEntity.getSalt());

            if (StringUtils.equals(password, userEntity.getPassword())) {
                return userEntity;
            }
        }

        return null;
    }

    @Override
    public Boolean checkData(String data, Integer type) {

        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();

        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
        }

        //查询结果为0，说明数据库不存在，该用户可以注册
        return this.count(wrapper)==0;
    }

}
