package org.example.projects.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionLine;
import org.example.projects.domain.enums.PlanStatus;
import org.example.projects.domain.enums.Priority;
import org.example.projects.domain.enums.Status;
import org.example.projects.dto.ProductionLineDTO;
import org.example.projects.repository.ProductionLineRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductionLineService {
    @Autowired
    private ProductionLineRepository productionLineRepository;

    @Autowired
    private ModelMapper modelMapper;

    public Page<ProductionLineDTO> getAllLines(Pageable pageable){
        Page<ProductionLine> LinePage = productionLineRepository.findAll(pageable);
        return LinePage.map(line->{
            ProductionLineDTO dto = ProductionLineDTO.fromEntity(line);
            return dto;
        });
    }

    public List<ProductionLine> getAllLinesAsEntity(){
        return productionLineRepository.findAll();
    }

    public ProductionLine getProductionLineByCode(String code) {
        return productionLineRepository.findByProductionLineCode(code).orElse(null);
    }

    @Transactional
    public void createProductionLine(ProductionLineDTO dto) throws IOException{

        ProductionLine line = new ProductionLine();
        line.setProductionLineName(dto.getProductionLineName());
        line.setLocation(dto.getLocation());
        line.setManager(dto.getManager());
        line.setCapacity(dto.getCapacity());
        line.setEquipment(dto.getEquipment());
        line.setTodayQty(dto.getTodayQty());
        line.setAchievedQty(dto.getAchievedQty());
        line.setProductionLineStatus(dto.getProductionLineStatus());

        productionLineRepository.save(line);
    }

    @Transactional
    public void modifyProductionLine(ProductionLineDTO dto) throws IOException{
        ProductionLine line = productionLineRepository.findByProductionLineCode(dto.getProductionLineCode())
                .orElseThrow(() -> new RuntimeException("Production line not found"));

        line.setProductionLineName(dto.getProductionLineName());
        line.setLocation(dto.getLocation());
        line.setManager(dto.getManager());
        line.setCapacity(dto.getCapacity());
        line.setEquipment(dto.getEquipment());
        line.setTodayQty(dto.getTodayQty());
        line.setAchievedQty(dto.getAchievedQty());
        line.setProductionLineStatus(dto.getProductionLineStatus());

        productionLineRepository.save(line);
    }

    @Transactional
    public void deleteProductionLine(String code) {
       ProductionLine line = productionLineRepository.findByProductionLineCode(code)
               .orElseThrow(()->new RuntimeException("Production line not found with code:" + code));

       productionLineRepository.delete(line);
    }

    // 생산라인 검색 (생산 라인 이름, 상태)
    public Page<ProductionLineDTO> searchLines(String productionLineName, String productionLineStatus, Pageable pageable) {
        // status가 비어 있지 않으면 Status enum으로 변환
        Status statusEnum = (productionLineStatus != null && !productionLineStatus.isEmpty()) ? Status.valueOf(productionLineStatus) : null;

        Page<ProductionLine> result;

        // productionLineName과 status가 제공되었을 때 해당 값들로 검색
        if (productionLineName != null && !productionLineName.isEmpty() && statusEnum != null) {
            result = productionLineRepository.findByProductionLineNameAndProductionLineStatus(productionLineName, statusEnum, pageable);
        } else if (productionLineName != null && !productionLineName.isEmpty()) {
            result = productionLineRepository.findByProductionLineName(productionLineName, pageable);
        } else if (statusEnum != null) {
            result = productionLineRepository.findByProductionLineStatus(statusEnum, pageable);
        } else {
            result = productionLineRepository.findAll(pageable);  // 기본적으로 모든 데이터 조회
        }

        return result.map(ProductionLineDTO::fromEntity);
    }




}
