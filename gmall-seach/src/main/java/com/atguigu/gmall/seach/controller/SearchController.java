package com.atguigu.gmall.seach.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.seach.pojo.SearchParamVo;
import com.atguigu.gmall.seach.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    @ResponseBody
    public ResponseVo<Object> search(SearchParamVo searchParamVo) {
        searchService.search(searchParamVo);
        return ResponseVo.ok();
    }


}
