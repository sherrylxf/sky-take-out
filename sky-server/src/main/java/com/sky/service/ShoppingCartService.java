package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShoppingCartService {

    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showshoppingCart();

    void cleanShoppingCart();

    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
