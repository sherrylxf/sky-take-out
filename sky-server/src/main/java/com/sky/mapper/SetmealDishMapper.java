package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 套餐菜品关系
 */
@Mapper
public interface SetmealDishMapper {
    void insertBatch(List<SetmealDish> setmealDishes);
}
