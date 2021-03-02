package com.atguigu.gmall.order.feign;


import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gmall-ms")
public interface GmallSmsClient extends GmallSmsApi {
}
