package com.example.funds.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.example.funds.domain.model.Fund;

public interface FundRepositoryPort {

    Optional<Fund> findById(String fundId);

    List<Fund> findAllActive();
}
