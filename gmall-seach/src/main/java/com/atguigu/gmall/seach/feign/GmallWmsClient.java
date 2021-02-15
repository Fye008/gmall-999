package com.atguigu.gmall.seach.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@FeignClient("gmall-wms")
@Component
public interface GmallWmsClient extends GmallWmsApi {
}
