package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车条目 Mapper
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
