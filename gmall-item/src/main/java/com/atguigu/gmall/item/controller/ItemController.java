package com.atguigu.gmall.item.controller;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ThreadPoolExecutor;


@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("{skuId}.html")
    public String queryItemBySkuId(@PathVariable Long skuId, Model model){
        ItemVo itemVo = itemService.queryItemBySkuId(skuId);

        model.addAttribute("itemVo",itemVo);
        threadPoolExecutor.execute(() ->itemService.generateHtml(skuId));
        return "item";
    }

}
