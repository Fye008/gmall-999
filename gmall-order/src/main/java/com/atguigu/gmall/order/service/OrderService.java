package com.atguigu.gmall.order.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.order.feign.GmallPmsClient;
import com.atguigu.gmall.order.feign.GmallSmsClient;
import com.atguigu.gmall.order.feign.GmallUmsClient;
import com.atguigu.gmall.order.feign.GmallWmsClient;
import com.atguigu.gmall.order.pojo.OrderConfirmVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private GmallPmsClient gmallPmsApi;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GmallUmsClient gmallUmsClient;


    public void query(Long userId) {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        //查询用户的收货地址
        List<UserAddressEntity> addressEntityList = gmallUmsClient.queryAddressByUserId(userId).getData();
        orderConfirmVo.setAddresses(addressEntityList);


        //查询该用户勾选的商品（购物车结算页面）

        //查询出sku的销售属性

        //查询出sku的营销信息

        //查询出sku的库存信息


        //查询用户的信息 比如积分(京豆)信息
        UserEntity userEntity = gmallUmsClient.queryUserById(userId).getData();
       // orderConfirmVo.setBounds();

    }


}
