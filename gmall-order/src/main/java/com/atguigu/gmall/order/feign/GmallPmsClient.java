package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gmall-pms")
public interface GmallPmsClient  extends GmallPmsApi {
}
