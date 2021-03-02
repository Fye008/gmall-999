package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.entity.CartEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.*;

public interface GmallCartApi {

    @PostMapping("/deleteUserAllCart")
    @ResponseBody
    public ResponseVo deleteUserAllCart(@RequestParam String userId);


    @PostMapping("/insertUserCart")
    @ResponseBody
    public ResponseVo insertUserCart(@RequestBody CartEntity cartEntity);

}
