package com.sky.mapper;

import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 根据菜品id查询套餐id
     * @param ids
     * @return
     */
    List<Long> getSetmealIdByDishId(List<Long> ids);

    /**
     * 根据id查询套餐数据
     * @param setmeal
     */
    void update(Setmeal setmeal);

    /**
     * 插入套餐数据
     * @param setmeal
     */
    void insert(Setmeal setmeal);
}
