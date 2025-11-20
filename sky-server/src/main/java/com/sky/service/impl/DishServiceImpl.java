package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品,并选择口味
     * @param dishDTO
     */
    @Override
    @Transactional // TODO:事务
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.insert(dish);// TODO:后续实现插入

        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            // 将所有口味绑定到一个菜品
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // TODO:批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        // 1.判断是否可以删除：起售状态
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                // 当前菜品正在起售，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 2.判断是否可以删除：套餐关联
        List<Long> setmealIdByDishId = setmealMapper.getSetmealIdByDishId(ids);
        if(setmealIdByDishId != null && setmealIdByDishId.size() > 0){
            // 当前菜品被套餐关联，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 3.删除菜品
        for(Long id:ids){
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish = dishMapper.getById(id);
        List<DishFlavor> dishflavor = dishFlavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishflavor);
        return dishVO;
    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {
        log.info("修改菜品，接收到的数据：{}", dishDTO);
        
        // 验证id是否存在
        if(dishDTO.getId() == null){
            throw new RuntimeException("菜品id不能为空");
        }
        
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        log.info("修改菜品，转换后的dish对象：{}", dish);
        
        // 1.修改菜品信息
        dishMapper.update(dish);
        // 2.删除原有口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        // 3.添加新的口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            log.info("修改菜品，准备插入的口味数据：{}", flavors);
            // 批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .status(status)
                .id(id)
                .build();
        dishMapper.update(dish);

        // 如果商品停售，相应的套餐也停售
        if(status == StatusConstant.DISABLE){
            List<Long> dishIds =new ArrayList<>();
            dishIds.add(id);
            List<Long> setmealIdByDishIds = setmealMapper.getSetmealIdByDishId(dishIds);
            if(setmealIdByDishIds != null && setmealIdByDishIds.size() > 0){
                for (Long setmealId : setmealIdByDishIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    @Override
    public List<DishVO> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }
}
