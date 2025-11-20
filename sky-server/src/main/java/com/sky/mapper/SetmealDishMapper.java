package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套餐菜品关系
 */
@Mapper
public interface SetmealDishMapper {
    void insertBatch(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    void deleteBySetmealId(Long setmealid);

    List<SetmealDish> getBySetmealId(Long id);
}
