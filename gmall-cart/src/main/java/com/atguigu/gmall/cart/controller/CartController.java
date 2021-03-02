package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.entity.CartEntity;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {


    @Autowired
    private CartService cartService;


    @GetMapping
    public String addCart(@RequestParam("skuId") Long skuId, @RequestParam("count") Integer count) {
        //添加到购物车。然后重定向到添加成功页面
        cartService.add(skuId, count);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + skuId;
    }

    @GetMapping("addCart.html")
    public String addCart(@RequestParam("skuId") Long skuId, Model model) {
        System.out.println("添加成功.........");
        CartEntity cart = cartService.queryCartBySkuId(skuId);
        model.addAttribute("cart", cart);
        return "addCart";
    }


    @GetMapping("cart.html")
    public String queryCart(Model model) {
        List<CartEntity> carts = cartService.queryCart();
        model.addAttribute("carts",carts);
        return "cart";
    }


    @PostMapping("/updateNum")
    @ResponseBody
    public ResponseVo updateNum(@RequestBody CartEntity cartEntity){

        Long skuId = cartEntity.getSkuId();
        Integer count = cartEntity.getCount();
        cartService.updateNum(skuId,count);
        return ResponseVo.ok();
    }

    @PostMapping("/deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam Long skuId){
        cartService.deleteCart(skuId);
        return ResponseVo.ok();
    }


    /**
     * 下面的方法都是只操作mysql数据库的
     */
    @PostMapping("/deleteUserAllCart")
    @ResponseBody
    public ResponseVo deleteUserAllCart(@RequestParam String userId){
        cartService.deleteUserAllCart(userId);
        return ResponseVo.ok();
    }

    @PostMapping("/insertUserCart")
    @ResponseBody
    public ResponseVo insertUserCart(@RequestBody CartEntity cartEntity){

        cartService.insertUserCart(cartEntity);
        return ResponseVo.ok();
    }


    @GetMapping()
    public ResponseVo queryCheckCart(){

        return ResponseVo.ok();
    }




}
