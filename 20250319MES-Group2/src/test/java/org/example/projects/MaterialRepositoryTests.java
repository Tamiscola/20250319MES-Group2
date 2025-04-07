package org.example.projects;

import org.example.projects.domain.Material;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.example.projects.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
public class MaterialRepositoryTests {

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    public void testCreateMaterials() {
        // Create sample materials
        Material wafer = Material.builder()
                .mName("Silicon Wafer")
                .mCategory("Base Material")
                .mProcess(ProcessType.WAFER_PREPARATION)
                .mQuantity(1000)
                .mPrice(50.0)
                .mStatus(Status.NORMAL)
                .build();

        Material photoresist = Material.builder()
                .mName("Photoresist")
                .mCategory("Chemical")
                .mProcess(ProcessType.PHOTOLITHOGRAPHY)
                .mQuantity(500)
                .mPrice(20.0)
                .mStatus(Status.NORMAL)
                .build();

        Material etchingGas = Material.builder()
                .mName("Etching Gas")
                .mCategory("Chemical")
                .mProcess(ProcessType.ETCHING)
                .mQuantity(300)
                .mPrice(15.0)
                .mStatus(Status.NORMAL)
                .build();

        Material dopingMaterial = Material.builder()
                .mName("Doping Material")
                .mCategory("Chemical")
                .mProcess(ProcessType.DOPING)
                .mQuantity(200)
                .mPrice(25.0)
                .mStatus(Status.NORMAL)
                .build();

        Material annealingGas = Material.builder()
                .mName("Annealing Gas")
                .mCategory("Chemical")
                .mProcess(ProcessType.ANNEALING)
                .mQuantity(150)
                .mPrice(30.0)
                .mStatus(Status.NORMAL)
                .build();

        // Save materials to the database
        materialRepository.save(wafer);
        materialRepository.save(photoresist);
        materialRepository.save(etchingGas);
        materialRepository.save(dopingMaterial);
        materialRepository.save(annealingGas);

        // Verify that materials were saved correctly
        List<Material> allMaterials = materialRepository.findAll();
        assertThat(allMaterials).hasSize(5);

        System.out.println("Saved materials:");
        allMaterials.forEach(material -> System.out.println(material));
    }

    @Test
    public void testFindByProcessType() {
        // Fetch materials for a specific process type
        List<Material> waferPreparationMaterials = materialRepository.findByProcessType(ProcessType.WAFER_PREPARATION);

        // Verify the results
        assertThat(waferPreparationMaterials).isNotEmpty();
        assertThat(waferPreparationMaterials.get(0).getMName()).isEqualTo("Silicon Wafer");

        System.out.println("Materials for WAFER_PREPARATION:");
        waferPreparationMaterials.forEach(material -> System.out.println(material));
    }
}
