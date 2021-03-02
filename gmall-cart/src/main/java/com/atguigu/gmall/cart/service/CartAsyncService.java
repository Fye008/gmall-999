package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.CartEntity;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateCart(CartEntity cartEntity, String userId, String skuId) {
        cartMapper.update(cartEntity, new UpdateWrapper<CartEntity>().eq("user_id", userId).eq("sku_id", skuId));
    }

    @Async
    public void insert(String userId,CartEntity cartEntity) {

        int i = 1 / 0;

        cartMapper.insert(cartEntity);
    }

    @Async
    public void deleteCart(String userKey) {
        cartMapper.delete(new UpdateWrapper<CartEntity>().eq("user_id", userKey));

    }

    @Async
    public void deleteCartByUidAndSkuId(String userId, String skuId) {
        cartMapper.delete(new UpdateWrapper<CartEntity>().eq("user_id", userId).eq("sku_id", skuId));

    }
}
