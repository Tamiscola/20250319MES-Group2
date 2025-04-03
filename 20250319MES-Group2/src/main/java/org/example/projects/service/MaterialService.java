package org.example.projects.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.projects.domain.Material;
import org.example.projects.domain.enums.ProcessType;
import org.example.projects.domain.enums.Status;
import org.example.projects.dto.MaterialDTO;
import org.example.projects.repository.MaterialRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class MaterialService {
   @Autowired
   private MaterialRepository materialRepository;

   @Autowired
   private ModelMapper modelMapper;

   public Page<MaterialDTO> getAllMaterials(Pageable pageable){
       Page<Material> MaterialPage = materialRepository.findAll(pageable);
       return MaterialPage.map(material->{
           MaterialDTO dto = MaterialDTO.fromEntity(material);
           return dto;
       });
   }

   public Material getMaterialById(Long id){
       return materialRepository.findById(id).orElse(null);
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

}
