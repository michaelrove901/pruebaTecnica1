package com.example.funds.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.example.funds.application.dto.response.FundResponse;
import com.example.funds.application.mapper.DomainMapper;
import com.example.funds.domain.port.in.GetAvailableFundsUseCase;
import com.example.funds.domain.port.out.FundRepositoryPort;

@Service
@Validated
public class GetAvailableFundsUseCaseImpl implements GetAvailableFundsUseCase {

    private final FundRepositoryPort fundRepositoryPort;

    public GetAvailableFundsUseCaseImpl(FundRepositoryPort fundRepositoryPort) {
        this.fundRepositoryPort = fundRepositoryPort;
    }

    @Override
    public List<FundResponse> execute() {
        return fundRepositoryPort.findAllActive()
                .stream()
                .map(DomainMapper::toResponse)
                .toList();
    }
}
