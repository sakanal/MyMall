package com.sakanal.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sakanal.common.validator.ListValue;
import com.sakanal.common.validator.group.AddGroup;
import com.sakanal.common.validator.group.UpdateGroup;
import com.sakanal.common.validator.group.UpdateShowStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 *
 * @author sakanal
 * @email sakanal9527@gmail.com
 * @date 2022-12-21 12:40:44
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@NotNull(message = "修改必须指定品牌id", groups = {UpdateGroup.class,UpdateShowStatusGroup.class})
	@Null(message = "新增不能指定id", groups = {AddGroup.class})
	@TableId
	private Long brandId;

	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
	private String name;

	/**
	 * 品牌logo地址
	 */
	@NotBlank(groups = {AddGroup.class})
	@URL(message = "logo必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;

	/**
	 * 介绍
	 */
	private String descript;

	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class,UpdateShowStatusGroup.class})
	@ListValue(values = {0,1},groups = {AddGroup.class,UpdateGroup.class, UpdateShowStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "排序必须大于等于0", groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}

