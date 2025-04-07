package org.example.projects.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Material;
import org.example.projects.domain.MaterialConsumption;
import org.example.projects.domain.ProductionData;
import org.example.projects.domain.ProductionPlan;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.example.projects.dto.MaterialDTO;
import org.example.projects.repository.MaterialConsumptionRepository;
import org.example.projects.repository.MaterialRepository;
import org.example.projects.repository.ProductionDataRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class MaterialService {
    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private MaterialConsumptionRepository materialConsumptionRepository;

    @Autowired
    private ProductionDataRepository productionDataRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Page<MaterialDTO> getAllMaterials(Pageable pageable){
        Page<Material> MaterialPage = materialRepository.findAll(pageable);
        return MaterialPage.map(material->{
            MaterialDTO dto = MaterialDTO.fromEntity(material);
            return dto;
        });
    }

    @Transactional
    public void createMaterial(MaterialDTO dto) throws IOException{

        Material material = Material.builder()
                .mName(dto.getMName())
                .mCategory(dto.getMCategory())
                .mProcess(dto.getMProcess())
                .mQuantity(dto.getMQuantity())
                .mPrice(dto.getMPrice())
                .mStatus(dto.getMStatus())
                .build();

        materialRepository.save(material);

    }

    @Transactional
    public void modifyMaterial(MaterialDTO dto) throws IOException{
        if (dto.getMId() == null) {
            throw new IllegalArgumentException("Material ID cannot be null");
        }
        Material material = materialRepository.findById(dto.getMId())
                .orElseThrow(() -> new RuntimeException("Material not found"));
        material.setMName(dto.getMName());
        material.setMCategory(dto.getMCategory());
        material.setMProcess(dto.getMProcess());
        material.setMQuantity(dto.getMQuantity());
        material.setMPrice(dto.getMPrice());
        material.setMStatus(dto.getMStatus());

        materialRepository.save(material);

    }

    @Transactional
    public void deleteMaterial(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Material not found with id:" + id));

        materialRepository.delete(material);
    }

    public Page<MaterialDTO> searchMaterials(String mProcess, String mStatus, Pageable pageable){
//       ProcessType mProcessEnum = mProcess.isEmpty() ? null : ProcessType.valueOf(mProcess);
//       Status mStatusEnum = mStatus.isEmpty() ? null : Status.valueOf(mStatus);
        ProcessType processEnum = (mProcess != null && !mProcess.isEmpty()) ? ProcessType.valueOf(mProcess) : null;
        Status statusEnum = (mStatus != null && !mStatus.isEmpty()) ? Status.valueOf(mStatus) : null;

        Page<Material> result;

        if(processEnum != null && statusEnum != null){
            result = materialRepository.findByMProcessAndMStatus(processEnum, statusEnum, pageable);
        } else if (processEnum != null){
            result = materialRepository.findByMProcess(processEnum, pageable);
        } else if (statusEnum != null){
            result = materialRepository.findByMStatus(statusEnum, pageable);
        } else {
            result = materialRepository.findAll(pageable);
        }

        return result.map(MaterialDTO::fromEntity);
    }

    /**
     * Calculate required materials for a production plan based on target quantity
     * @param productionPlan The production plan
     * @return Map of ProcessType to List of materials with required quantities
     */
    public Map<ProcessType, List<Material>> calculateRequiredMaterials(ProductionPlan productionPlan) {
        Integer targetQty = productionPlan.getTargetQty();

        // Get all materials grouped by process type
        List<Material> allMaterials = materialRepository.findAll();

        return allMaterials.stream()
                .filter(material -> material.getMStatus() == Status.NORMAL)
                .collect(Collectors.groupingBy(Material::getMProcess));
    }

    /**
     * Consume materials for a specific process in a production plan
     * @param productionData The production data record
     * @param processType The process type where materials are being consumed
     * @return Total cost of consumed materials
     */
    @Transactional
    public double consumeMaterialsForProcess(ProductionData productionData, ProcessType processType) {
        ProductionPlan productionPlan = productionData.getProductionPlan();
        int targetQty = productionPlan.getTargetQty();

        // Find materials needed for this process type
        List<Material> materials = materialRepository.findByProcessType(processType);
        double totalProcessCost = 0.0;

        for (Material material : materials) {
            // Skip if material is defected
            if (material.getMStatus() != Status.NORMAL) {
                continue;
            }

            // Calculate required quantity based on production target
            int requiredQuantity = calculateRequiredQuantity(material, targetQty);

            // Check if we have enough inventory
            if (material.getMQuantity() < requiredQuantity) {
                log.warn("Insufficient material: {} (ID: {}). Required: {}, Available: {}",
                        material.getMName(), material.getMId(), requiredQuantity, material.getMQuantity());
                continue;
            }

            // Reduce material quantity in inventory
            material.setMQuantity(material.getMQuantity() - requiredQuantity);
            materialRepository.save(material);

            // Calculate cost for this consumption
            double consumptionCost = requiredQuantity * material.getMPrice();
            totalProcessCost += consumptionCost;

            // Create consumption record
            MaterialConsumption consumption = MaterialConsumption.builder()
                    .material(material)
                    .productionPlan(productionPlan)
                    .productionData(productionData)
                    .processType(processType)
                    .quantityUsed(requiredQuantity)
                    .consumptionCost(consumptionCost)
                    .consumptionDate(LocalDateTime.now())
                    .build();

            // Add to ProductionData using custom method
            productionData.addMaterialConsumption(consumption);
            materialConsumptionRepository.save(consumption);

            log.info("Consumed {} units of material {} (ID: {}) for production plan {} at process {}. Cost: {}",
                    requiredQuantity, material.getMName(), material.getMId(),
                    productionPlan.getPlanId(), processType, consumptionCost);
        }

        return totalProcessCost;
    }

    /**
     * Calculate required quantity of a material based on production target
     * This is a simplified calculation - you may need to adjust based on your business logic
     */
    private int calculateRequiredQuantity(Material material, int targetQty) {
        // This is a placeholder calculation - adjust according to your material consumption rules
        // For example, some materials might be consumed at a fixed rate per product unit
        return Math.max(1, targetQty / 10); // Simplified example
    }

    /**
     * Update the production data with the total material cost after production is complete
     */
    @Transactional
    public void updateProductionCost(ProductionData productionData) {
        // Calculate total cost from all material consumptions
        Double totalCost = materialConsumptionRepository.calculateTotalCostForProductionData(productionData);

        if (totalCost == null) {
            totalCost = 0.0;
        }

        // Update production data with cost information
        productionData.setTotalMaterialCost(totalCost);

        // Calculate unit cost if actual quantity is greater than zero
        if (productionData.getActualQuantity() > 0) {
            productionData.setUnitCost(totalCost / productionData.getActualQuantity());
        } else {
            productionData.setUnitCost(0.0);
        }

        productionDataRepository.save(productionData);

        log.info("Updated production cost for ID: {}. Total material cost: {}, Unit cost: {}",
                productionData.getProductionResultId(), totalCost, productionData.getUnitCost());
    }

    /**
     * Find a material by ID
     */
    public Material getMaterialById(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with ID: " + materialId));
    }
}