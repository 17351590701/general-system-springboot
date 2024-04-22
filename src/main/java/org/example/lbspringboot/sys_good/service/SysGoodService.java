package org.example.lbspringboot.sys_good.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.lbspringboot.sys_good.entity.SysGood;

/**
 * @author zyr
 * @date 2024/4/22 下午1:40
 * @Description
 */
public interface SysGoodService extends IService<SysGood> {
    //新增
    void saveGood(SysGood sysGood);
    //编辑
    void editGood(SysGood sysGood);
    //删除
    void deleteGood(Long goodId);

}
