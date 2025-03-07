package com.lareb.springProject.AirBnb.service;

import com.lareb.springProject.AirBnb.Strategy.PricingService;
import com.lareb.springProject.AirBnb.entity.Hotel;
import com.lareb.springProject.AirBnb.entity.HotelMinPrice;
import com.lareb.springProject.AirBnb.entity.Inventory;
import com.lareb.springProject.AirBnb.repository.HotelMinPriceRepository;
import com.lareb.springProject.AirBnb.repository.HotelRepository;
import com.lareb.springProject.AirBnb.repository.InventoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingUpdateService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;



    //scheduler to update the inventory and hotelMinPrice table every hour
    //@Scheduled(cron = "*/5 * * * * *")
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updatePrice(){
        int page = 0;
        int batchSize = 100;
        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page, batchSize));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrices);


            page++;
        }
    }
    @Transactional
    public void updateHotelPrices(Hotel hotel){
        log.info("Updating hotel prices for hotel ID : {} ", hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel, startDate,endDate);
        updateInventoryList(inventoryList);
        updateHotelMinPrice(hotel, inventoryList, startDate, endDate);

    }
//to go through all the inventory to find the cheapest room price for this hotel on a particular date in between startDate and endDate
    //and update the hotelMinPrice
    @Transactional
    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate) {
        //compute minimum price per day for a hotel
        Map<LocalDate, BigDecimal> dailyMinPrice = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,//grouping by the date
                        Collectors.mapping(Inventory::getPrice,Collectors.minBy(Comparator.naturalOrder()))
                )).entrySet().stream()//.entrySet().stream() → Converts the Map<LocalDate, Optional<BigDecimal>> to a stream of key-value pairs.
                .collect(Collectors.toMap(Map.Entry::getKey,e -> e.getValue().orElse(BigDecimal.ZERO)));
        //.collect(Collectors.toMap(...)) → Converts it back into a map

        //prepare hotel price entity in bulk(we are preparing this so that we can have a call of the save all method)
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrice.forEach((date, price)-> {
            HotelMinPrice hotPrice = hotelMinPriceRepository.findByHotelAndDate(hotel, date).orElse(new HotelMinPrice(hotel, date));
            hotPrice.setPrice(price);
            hotelPrices.add(hotPrice);
        });
        //save all HotelPrices entities in bulk
        hotelMinPriceRepository.saveAll(hotelPrices);


    }
    @Transactional
    public void updateInventoryList(List<Inventory> inventoryList){
        inventoryList.forEach(inventory -> {
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);

        });
        inventoryRepository.saveAll(inventoryList);
    }

}
