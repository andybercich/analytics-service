package org.example.Service.Interfaces;

import org.example.Model.DtoAndRecords.*;

import java.io.File;
import java.util.List;

public interface ChartService {

    File createTopProductsChart(
            List<TopProductDTO> data
    );


    File createRevenueProfitChart(
            List<WeeklyRevenueDTO> revenue,
            List<WeeklyProfitDTO> profit
    );

    File createTopProfitProductsChart(
            List<ProductProfitDTO> data
    );




    File createStockMovementChart(
            List<StockMovementSummaryDTO> data
    );

     File createOrdersByDayChart(List<DailyOrdersDTO> data);

}
