package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 Mapper
 * 乐观锁扣减库存：stock > 0 才允许扣减
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 原子扣减库存（stock > 0 才允许 -1）
     * 返回受影响行数：1 成功，0 失败（库存不足）
     */
    @Update("UPDATE product SET stock = stock - 1, sales = sales + 1, update_time = NOW() " +
            "WHERE id = #{productId} AND stock > 0")
    int decrStock(@Param("productId") Long productId);

    /**
     * 原子恢复库存（取消订单时归还）
     */
    @Update("UPDATE product SET stock = stock + 1, sales = sales - 1, update_time = NOW() " +
            "WHERE id = #{productId}")
    int incrStock(@Param("productId") Long productId);
}
