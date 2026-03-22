package com.example.funds.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.funds.domain.exception.InsufficientBalanceException;

class ClientTest {

    @Test
    void shouldCreateClientWithInitialBalanceOf500000Cop() {
        Client client = Client.create(
                "client-1",
                "Juan Perez",
                "juan@example.com",
                "+573001112233",
                "hashed-password",
                Set.of(Role.CLIENT)
        );

        assertThat(client.balance().toLong()).isEqualTo(500000L);
    }

    @Test
    void shouldRejectDebitWhenBalanceIsInsufficient() {
        Client client = new Client(
                "client-1",
                "Juan Perez",
                "juan@example.com",
                "+573001112233",
                "hashed-password",
                Set.of(Role.CLIENT),
                Money.of(50000),
                null
        );
        Fund fund = new Fund("fund-1", "FPV_BTG_PACTUAL_ECOPETROL", FundCategory.FPV, Money.of(125000), true);

        assertThatThrownBy(() -> client.debitForFund(fund))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("No tiene saldo disponible para vincularse al fondo FPV_BTG_PACTUAL_ECOPETROL");
    }
}
