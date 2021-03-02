package com.atguigu.gmall.cart.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 *
 * @author xiaofangfang
 * @email xainfangfang@atguigu.com
 * @date 2021-02-26 18:34:05
 */
@Data
@TableName("cart_info")
public class CartEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@TableId
	private Long id;
	/**
	 * 用户id或者userKey
	 */
	private String userId;
	/**
	 * skuId
	 */
	private Long skuId;
	/**
	 * 选中状态
	 */
	@TableField("`check`")
	private Integer check;
	/**
	 * 标题
	 */
	private String title;
	/**
	 * 默认图片
	 */
	private String defaultImage;
	/**
	 * 加入购物车时价格
	 */
	private BigDecimal price;
	/**
	 * 数量
	 */
	private Integer count;
	/**
	 * 是否有货
	 */
	private Boolean store;
	/**
	 * 销售属性（json格式）
	 */
	private String saleAttrs;
	/**
	 * 营销信息（json格式）
	 */
	private String sales;


	@TableField(exist = false)
	private BigDecimal currentPrice;
}
