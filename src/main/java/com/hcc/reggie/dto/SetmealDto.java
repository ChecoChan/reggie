package com.hcc.reggie.dto;

import com.hcc.reggie.entity.Setmeal;
import com.hcc.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
