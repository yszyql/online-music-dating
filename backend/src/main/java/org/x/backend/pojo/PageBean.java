package org.x.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 分页查询结果
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageBean<T>{

    private Long total; // 总记录数
    private List<T> items; // 当前页数据

}
