package com.atguigu.gmall.item.controller;



import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;


    @Autowired
    private GmallPmsClient gmallPmsClient;

    @GetMapping("{skuId}.html")
    public String queryItemBySkuId(@PathVariable Long skuId, Model model){
        ItemVo itemVo = itemService.queryItemBySkuId(skuId);

        model.addAttribute("itemVo",itemVo);

        return "item";
    }



}
