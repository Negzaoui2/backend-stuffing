package com.negzaoui.stuffing.service;

import com.negzaoui.stuffing.dto.admin.CreateDepartementRequest;
import com.negzaoui.stuffing.dto.admin.DepartementDto;
import com.negzaoui.stuffing.entity.Departement;
import com.negzaoui.stuffing.repository.DepartementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartementService {

    private final DepartementRepository departementRepository;

    @Transactional(readOnly = true)
    public List<DepartementDto> getAllDepartements() {
        return departementRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartementDto getDepartementById(Long id) {
        Departement dept = departementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable (id=" + id + ")"));
        return toDto(dept);
    }

    @Transactional
    public DepartementDto createDepartement(CreateDepartementRequest request) {
        if (departementRepository.existsByName(request.getName().trim())) {
            throw new IllegalStateException("Un département avec ce nom existe déjà : " + request.getName());
        }

        Departement dept = Departement.builder()
                .name(request.getName().trim())
                .build();
        dept = departementRepository.save(dept);
        return toDto(dept);
    }

    @Transactional
    public DepartementDto updateDepartement(Long id, CreateDepartementRequest request) {
        Departement dept = departementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable (id=" + id + ")"));

        // Vérifier unicité du nom (sauf pour lui-même)
        departementRepository.findByName(request.getName().trim())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Un département avec ce nom existe déjà : " + request.getName());
                });

        dept.setName(request.getName().trim());
        dept = departementRepository.save(dept);
        return toDto(dept);
    }

    @Transactional
    public void deleteDepartement(Long id) {
        Departement dept = departementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Département introuvable (id=" + id + ")"));

        if (dept.getEmployees() != null && !dept.getEmployees().isEmpty()) {
            throw new IllegalStateException(
                    "Impossible de supprimer ce département : " + dept.getEmployees().size() + " collaborateur(s) y sont rattachés");
        }

        departementRepository.delete(dept);
    }

    private DepartementDto toDto(Departement dept) {
        int count = dept.getEmployees() != null ? dept.getEmployees().size() : 0;
        return DepartementDto.builder()
                .id(dept.getId())
                .name(dept.getName())
                .employeeCount(count)
                .build();
    }
}

