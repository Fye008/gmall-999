package com.atguigu.gmall.seach.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@FeignClient("gmall-pms")
@Component
public interface GmallPmsClient extends GmallPmsApi {
}
