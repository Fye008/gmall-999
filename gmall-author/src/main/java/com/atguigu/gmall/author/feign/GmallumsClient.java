package com.atguigu.gmall.author.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("gmall-ums")
public interface GmallumsClient extends GmallUmsApi {
}
