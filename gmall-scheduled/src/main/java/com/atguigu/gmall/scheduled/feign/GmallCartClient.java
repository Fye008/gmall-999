package com.atguigu.gmall.scheduled.feign;

import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gmall-cart")
public interface GmallCartClient extends GmallCartApi {
}
