package com.atguigu.gmall.order.feign;


import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gmall-wms")
public interface GmallWmsClient extends GmallWmsApi {
}
