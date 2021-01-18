package com.atguigu.gmall.oms.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.oms.mapper.OmsRefundInfoMapper;
import com.atguigu.gmall.oms.entity.OmsRefundInfoEntity;
import com.atguigu.gmall.oms.service.OmsRefundInfoService;


@Service("omsRefundInfoService")
public class OmsRefundInfoServiceImpl extends ServiceImpl<OmsRefundInfoMapper, OmsRefundInfoEntity> implements OmsRefundInfoService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<OmsRefundInfoEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<OmsRefundInfoEntity>()
        );

        return new PageResultVo(page);
    }

}