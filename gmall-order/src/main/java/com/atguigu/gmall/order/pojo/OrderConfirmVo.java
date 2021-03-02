package com.atguigu.gmall.order.pojo;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.util.List;

@Data
public class OrderConfirmVo {

    private List<UserAddressEntity> addresses;

    private List<OrderItemVo> orderItems;

    private Integer bounds;

    private String orderToken;

}
