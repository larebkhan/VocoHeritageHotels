package com.lareb.springProject.AirBnb.Strategy;

import com.lareb.springProject.AirBnb.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
