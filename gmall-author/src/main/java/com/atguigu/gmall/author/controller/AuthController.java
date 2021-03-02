package com.atguigu.gmall.author.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.author.service.AuthService;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;


    @GetMapping("toLogin.html")
    public String toLogin(@RequestParam(value = "returnUrl", defaultValue = "http://item.gmall.com/1.html") String returnUrl, Model model) {

        model.addAttribute("returnUrl", returnUrl);
        return "login";
    }


    @PostMapping("/login")
    public String login(@RequestParam("returnUrl") String returnUrl,
                        @RequestParam("loginName") String loginName,
                        @RequestParam("password") String password,
                        HttpServletRequest request,
                        HttpServletResponse response) {


        authService.checkUser(loginName, password, request, response);

        return "redirect:" + returnUrl;
    }



}
