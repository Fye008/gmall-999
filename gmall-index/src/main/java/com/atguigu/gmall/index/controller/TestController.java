package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.index.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/index")
public class TestController {


    @Autowired
    private TestService testService;

    @GetMapping("/ha")
    public void test() {

        testService.testLock();

    }

    @GetMapping("/ha11")
    public void test2() {
        System.out.println("================");
    }

    @GetMapping("/ha3")
    public void test3() {

        testService.testLock3();

    }

    @GetMapping("/ha4")
    public void test4() {
        //使用lua脚本，让判断和删除具备原子性
        testService.testLock4();

    }

    @GetMapping("/ha5")
    public void test5() {
        //使用可重入锁
        testService.testLock5();
    }



    @GetMapping("/ha6")
    public void test6(){
        testService.testRedisson();

    }


    @GetMapping("/ha7")
    public void test7(){
        testService.testRedisson2();

    }
}
