package com.sakanal.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sakanal.common.utils.PageUtils;
import com.sakanal.common.utils.Query;
import com.sakanal.product.dao.BrandDao;
import com.sakanal.product.entity.BrandEntity;
import com.sakanal.product.service.BrandService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String searchKey = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(searchKey)){
            queryWrapper.eq("brand_id", searchKey).or().like("name",searchKey);
        }
        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params),queryWrapper);

        return new PageUtils(page);
    }

}
