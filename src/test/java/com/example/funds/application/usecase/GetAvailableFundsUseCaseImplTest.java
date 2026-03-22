package com.example.funds.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.funds.application.dto.response.FundResponse;
import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.FundCategory;
import com.example.funds.domain.model.Money;
import com.example.funds.domain.port.out.FundRepositoryPort;

@ExtendWith(MockitoExtension.class)
class GetAvailableFundsUseCaseImplTest {

    @Mock
    private FundRepositoryPort fundRepositoryPort;

    @InjectMocks
    private GetAvailableFundsUseCaseImpl useCase;

    @Test
    void shouldListOnlyActiveFundsReturnedByRepository() {
        when(fundRepositoryPort.findAllActive()).thenReturn(List.of(
                new Fund("fund-1", "DEUDAPRIVADA", FundCategory.FIC, Money.of(50000), true),
                new Fund("fund-2", "FPV_BTG_PACTUAL_DINAMICA", FundCategory.FPV, Money.of(100000), true)
        ));

        List<FundResponse> response = useCase.execute();

        assertThat(response).hasSize(2);
        assertThat(response).extracting(FundResponse::name)
                .containsExactly("DEUDAPRIVADA", "FPV_BTG_PACTUAL_DINAMICA");
    }
}
