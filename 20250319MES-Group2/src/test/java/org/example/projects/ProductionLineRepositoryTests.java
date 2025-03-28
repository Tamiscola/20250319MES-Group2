package org.example.projects;

import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Priority;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.example.projects.repository.ProductionLineRepository;
import org.example.projects.repository.ProductionPlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Random;
import java.util.stream.IntStream;

@SpringBootTest
@Log4j2
public class ProductionLineRepositoryTests {

    @Autowired
    private ProductionLineRepository productionLineRepository;

    @Test
    public void testProductionLineInsert() {
        Random random = new Random();
        IntStream.rangeClosed(1, 5).forEach(i -> {
            ProductionLine line = ProductionLine.builder()
                    .productionLineStatus(Status.NORMAL)
                    .productionLineName("Production Line " + i)
                    .location("Location " + i)
                    .manager("Manager " + i)
                    .capacity(random.nextInt(500) + 500)  // Capacity between 500 and 1000 per hour
                    .equipment("Equipment Set " + i)
                    .achievedQty(0)
                    .todayQty(0)
                    .processType(ProcessType.WAFER_PREPARATION)  // Starting process
                    .progress(0.0)
                    .regDate(LocalDate.now())
                    .build();

            ProductionLine result = productionLineRepository.save(line);
            log.info("Production Line Code: " + result.getProductionLineCode());
        });
    }

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    @Test
    public void testPlanInsert() {
        Random random = new Random();
        String[] productNames = {"Product A", "Product B", "Product C"};
        String[] managers = {"Manager 1", "Manager 2", "Manager 3"};

        IntStream.rangeClosed(1, 20).forEach(i -> {
            ProductionPlan plan = ProductionPlan.builder()
                    .productName(productNames[i % 3])
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(random.nextInt(30) + 1))
                    .targetQty(random.nextInt(1000) + 100)
                    .manager(managers[random.nextInt(3)])
                    .priority(Priority.values()[random.nextInt(Priority.values().length)])
                    .planStatus(PlanStatus.STANDBY)
                    .fileUrl("http://example.com/file" + i + ".pdf")
                    .build();

            ProductionPlan result = productionPlanRepository.save(plan);
            log.info("Saved Production Plan - ID: " + result.getPlanId());
        });
    }
}

