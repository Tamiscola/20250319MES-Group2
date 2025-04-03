package org.example.projects.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.ProductionLine;
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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductionLineService {
    @Autowired
    private ProductionLineRepository productionLineRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<String> getAllProductionLineName() {
        // productionLineRepository에서 모든 생산라인을 가져와서 이름만 리스트로 반환
        return productionLineRepository.findAll().stream()
                .map(ProductionLine::getProductionLineName) // 각 ProductionLine 객체에서 이름만 가져오기
                .collect(Collectors.toList());
    }

    public Page<ProductionLineDTO> getAllLines(Pageable pageable){
        Page<ProductionLine> LinePage = productionLineRepository.findAll(pageable);
        return LinePage.map(line->{
            ProductionLineDTO dto = ProductionLineDTO.fromEntity(line);
            return dto;
        });
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

    // 생산라인 검색 (생산 라인 이름, 상태, 날짜)
    public Page<ProductionLineDTO> searchLines(String productionLineName, String productionLineStatus, LocalDate regDate, Pageable pageable) {
        // status가 비어 있지 않으면 Status enum으로 변환
        Status statusEnum = (productionLineStatus != null && !productionLineStatus.isEmpty()) ? Status.valueOf(productionLineStatus) : null;

        Page<ProductionLine> result;

        // productionLineName, status, regDate가 모두 제공된 경우
        if (productionLineName != null && !productionLineName.isEmpty() && statusEnum != null && regDate != null) {
            result = productionLineRepository.findByProductionLineNameAndProductionLineStatusAndRegDate(productionLineName, statusEnum, regDate, pageable);
        }
        // productionLineName과 status가 제공되었을 때 해당 값들로 검색
        else if (productionLineName != null && !productionLineName.isEmpty() && statusEnum != null) {
            result = productionLineRepository.findByProductionLineNameAndProductionLineStatus(productionLineName, statusEnum, pageable);
        }
        // productionLineName과 regDate가 제공된 경우
        else if (productionLineName != null && !productionLineName.isEmpty() && regDate != null) {
            result = productionLineRepository.findByProductionLineNameAndRegDate(productionLineName, regDate, pageable);
        }
        // status와 regDate가 제공된 경우
        else if (statusEnum != null && regDate != null) {
            result = productionLineRepository.findByProductionLineStatusAndRegDate(statusEnum, regDate, pageable);
        }
        // productionLineName만 제공된 경우
        else if (productionLineName != null && !productionLineName.isEmpty()) {
            result = productionLineRepository.findByProductionLineName(productionLineName, pageable);
        }
        // status만 제공된 경우
        else if (statusEnum != null) {
            result = productionLineRepository.findByProductionLineStatus(statusEnum, pageable);
        }
        // regDate만 제공된 경우
        else if (regDate != null) {
            result = productionLineRepository.findByRegDate(regDate, pageable);
        }
        // 모든 조건이 없을 경우
        else {
            result = productionLineRepository.findAll(pageable);  // 기본적으로 모든 데이터 조회
        }

        return result.map(ProductionLineDTO::fromEntity);
    }




}
