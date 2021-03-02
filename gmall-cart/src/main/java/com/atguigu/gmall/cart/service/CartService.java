package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.CartEntity;
import com.atguigu.gmall.cart.exception.MyException;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartAsyncService cartAsyncService;


    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    private static final String KEY_PREFIX = "cart:info:";

    private static final String PRICE_PREFIX = "cart:price:";


    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    public void add(Long skuId, Integer count) {

        //TODO 查询商品是否存在
        CartEntity cartEntity = new CartEntity();

        //如果用户登陆了，userId  用户没有登录 userKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserId();
        if (StringUtils.isBlank(userId)) {
            userId = userInfo.getUserKey();
        }
        //判断该用户购物车中是否有此商品
        Map<Object, Object> hashCart = redisTemplate.opsForHash().entries(KEY_PREFIX + userId);
        if (hashCart.containsKey(skuId.toString())) {
            //如果购物车中包含此商品
            String cartJson = (String) hashCart.get(skuId.toString());
            cartEntity = JSON.parseObject(cartJson, CartEntity.class);

            cartEntity.setCount(cartEntity.getCount() + count);
            //在mysql中更新数量
            //  cartMapper.update(cartEntity, new UpdateWrapper<CartEntity>().eq("user_id", userId).eq("sku_id", skuId));
            cartAsyncService.updateCart(cartEntity, userId, skuId.toString());
        } else {
            //该用户购物车中没有此商品，新增

            //设置sku相关属性
            SkuEntity skuEntity = pmsClient.querySkuById(skuId).getData();
            if (skuEntity == null) {
                return;
            }
            cartEntity.setTitle(skuEntity.getTitle());
            cartEntity.setDefaultImage(skuEntity.getDefaultImage());
            cartEntity.setPrice(skuEntity.getPrice());


            //设置是否有货
            List<WareSkuEntity> wareSkuEntityList = wmsClient.queryWareSkuBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(wareSkuEntityList)) {
                cartEntity.setStore(wareSkuEntityList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }


            //销售属性
            List<SkuAttrValueEntity> skuAttrValueEntities = pmsClient.querySkuAttrValueBySkuId(skuId).getData();
            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                cartEntity.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));
            }
            //TODO 营销属性

            //将商品新增到数据库中
            cartEntity.setSkuId(skuId);
            cartEntity.setCheck(1);
            cartEntity.setUserId(userId);
            cartEntity.setCount(count);
            //cartMapper.insert(cartEntity);
            cartAsyncService.insert(userId,cartEntity);
            //加入价格缓存
            redisTemplate.opsForValue().set(PRICE_PREFIX + skuId,cartEntity.getPrice().toString());

        }


        redisTemplate.opsForHash().put(KEY_PREFIX + userId, skuId.toString(), JSON.toJSONString(cartEntity));

    }


    public CartEntity queryCartBySkuId(Long skuId) {

        //第一步还是需要获取userId或者userKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserId();
        if (StringUtils.isBlank(userId)) {
            userId = userInfo.getUserKey();
        }

        Map<Object, Object> map = redisTemplate.opsForHash().entries(KEY_PREFIX + userId);
        if (map.containsKey(skuId.toString())) {
            String cartJson = map.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, CartEntity.class);
        }

        throw new MyException("用户购物车中没有此商品");

    }

    //查询用户购物车
    public List<CartEntity> queryCart() {
        //先查询userKey的购物车
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        Map<Object, Object> map = redisTemplate.opsForHash().entries(KEY_PREFIX + userKey);
        List<CartEntity> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(map)) {
            Collection<Object> cartValues = map.values();
            //未登录的购物车
            unLoginCarts = cartValues.stream().map((cart) -> JSON.parseObject(cart.toString(), CartEntity.class)).collect(Collectors.toList());
        }

        //然后再查询userId的购物车，如果userId未登录 直接返回
        String userId = userInfo.getUserId();
        if (StringUtils.isBlank(userId)) {
            return unLoginCarts;
        }
        //查询userId的购物车
        Map<Object, Object> loginCartMap = redisTemplate.opsForHash().entries(KEY_PREFIX + userId);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            //将未登录的购物车合并到登录的购物车
            unLoginCarts.forEach(unLoginCart -> {
                //如果登录购物车中 包含 该商品 则更新商品数量
                String unLoginCartSkuId = unLoginCart.getSkuId().toString();
                if (loginCartMap.containsKey(unLoginCartSkuId)) {
                    String loginCartJson = loginCartMap.get(unLoginCartSkuId).toString();
                    CartEntity loginCartEntity = JSON.parseObject(loginCartJson, CartEntity.class);
                    Integer loginCartEntityCount = loginCartEntity.getCount();
                    //更新该商品的数量
                    loginCartEntity.setCount(loginCartEntityCount + unLoginCart.getCount());
                    //更新mysql中该商品的数量
                    cartAsyncService.updateCart(loginCartEntity, userId, loginCartEntity.getSkuId().toString());
                    //往redis中添加商品
                    redisTemplate.opsForHash().put(KEY_PREFIX + userId, loginCartEntity.getSkuId().toString(), JSON.toJSONString(loginCartEntity));
                } else {
                    //如果登录购物车中 不包含 该商品 则新增商品到登录购物车
                    unLoginCart.setUserId(userId);
                    cartAsyncService.insert(userId,unLoginCart);
                    //往redis中添加商品
                    redisTemplate.opsForHash().put(KEY_PREFIX + userId, unLoginCart.getSkuId().toString(), JSON.toJSONString(unLoginCart));
                }
            });
        }
        //最后要删除userKey的购物车
        redisTemplate.delete(KEY_PREFIX + userKey);
        cartAsyncService.deleteCart(userKey);

        // 从redis中查询 用户购物车
        Collection<Object> values = loginCartMap.values();
        if (!CollectionUtils.isEmpty(values)) {
            return values.stream().map(cartJson -> {
                CartEntity cartEntity = JSON.parseObject(cartJson.toString(), CartEntity.class);

                String currentPrice = redisTemplate.opsForValue().get(PRICE_PREFIX + cartEntity.getSkuId());
                cartEntity.setCurrentPrice(new BigDecimal(currentPrice));
                return cartEntity;
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 更新商品数量
     *
     * @param skuId
     * @param count
     */
    public void updateNum(Long skuId, Integer count) {
        //第一步拿到userKey 或者userId
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserId();
        if (StringUtils.isBlank(userId)) {
            userId = userInfo.getUserKey();
        }
        //更新这个sku的数量
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (!hashOps.hasKey(skuId.toString())) {
            throw new MyException("违规操作，购物车中没有此商品...");
        }
        String skuJson = hashOps.get(skuId.toString()).toString();
        CartEntity cartEntity = JSON.parseObject(skuJson, CartEntity.class);
        cartEntity.setCount(count);

        hashOps.put(skuId.toString(), JSON.toJSONString(cartEntity));
        //存到数据库中
        cartAsyncService.updateCart(cartEntity, userId, skuId.toString());

    }

    /**
     * 删除购物车
     *
     * @param skuId
     */
    public void deleteCart(Long skuId) {
        //第一步拿到userKey 或者userId
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserId();
        if (StringUtils.isBlank(userId)) {
            userId = userInfo.getUserKey();
        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (!hashOps.hasKey(skuId.toString())) {
            throw new MyException("违规操作，用户购物车中没有此商品...");
        }

        //删除redis中的数据
        hashOps.delete(skuId.toString());
        //异步删除mysql中数据
        cartAsyncService.deleteCartByUidAndSkuId(userId, skuId.toString());
    }


    public void deleteUserAllCart(String userId) {
        cartMapper.delete(new UpdateWrapper<CartEntity>().eq("user_id",userId));

    }

    public void insertUserCart(CartEntity cartEntity) {
        cartMapper.insert(cartEntity);
    }
}
