package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController") // 管理端店铺管理
@Api("管理端店铺管理")
@Slf4j
@RequestMapping("/admin/shop")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String SHOP_STATUS = "SHOP_STATUS";// 店铺营业状态

    /**
     * 设置营业状态
     * @param status
     * @return
     */
    @PutMapping("{/status}")
    @ApiOperation("设置营业状态")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置营业状态: {}", status == 1 ? "营业中" : "打烊中");
        // 将状态设置到Redis中
        redisTemplate.opsForValue().set(SHOP_STATUS, status);
        return Result.success();
    }

    /**
     * 获取店铺营业状态
     * @param status
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> getStatus(@PathVariable Integer status) {
        Integer shopstatus = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        log.info("获取营业状态: {}", shopstatus == 1 ? "营业中" : "打烊中");
        return Result.success(shopstatus);
    }
}
