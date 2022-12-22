package io.renren.modules.job.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.common.utils.DateUtils;
import io.renren.modules.sys.entity.SysCaptchaEntity;
import io.renren.modules.sys.service.SysCaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component("captchaTask")
public class CaptchaTask implements ITask {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private SysCaptchaService captchaService;

    @Override
    public void run(String params) {
        logger.debug("captchaTask定时任务正在执行，参数为：{}", params);
        long limit = Long.parseLong(params);
        if (captchaService.count()>0){
            List<SysCaptchaEntity> list = captchaService.list(new QueryWrapper<SysCaptchaEntity>().orderByAsc("expire_time").last("limit "+limit));
            String now = DateUtils.format(new Date(), DateUtils.DATE_TIME_PATTERN);
            List<String> deleteList=new ArrayList<>();
            list.stream()
                    .filter(captcha -> {
                        String date = DateUtils.format(captcha.getExpireTime(), DateUtils.DATE_TIME_PATTERN);
                        return now.compareTo(date) > 0;
                    })
                    .forEach(captcha -> deleteList.add(captcha.getUuid()));

            logger.debug("该次任务需要删除"+deleteList.size()+"条记录");
            boolean result = captchaService.removeByIds(deleteList);
            logger.debug("删除"+(result?"成功":"失败"));
        }else {
            logger.debug("数据库中暂无数据，无需进行定时任务");
        }

    }
}
