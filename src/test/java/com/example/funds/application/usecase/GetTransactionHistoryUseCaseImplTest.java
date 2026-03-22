package com.example.funds.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.funds.application.dto.query.GetTransactionHistoryQuery;
import com.example.funds.application.dto.response.TransactionResponse;
import com.example.funds.domain.exception.ClientNotFoundException;
import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Money;
import com.example.funds.domain.model.NotificationPreference;
import com.example.funds.domain.model.Role;
import com.example.funds.domain.model.Transaction;
import com.example.funds.domain.model.TransactionType;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.domain.port.out.TransactionRepositoryPort;

@ExtendWith(MockitoExtension.class)
class GetTransactionHistoryUseCaseImplTest {

    @Mock
    private ClientRepositoryPort clientRepositoryPort;
    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @InjectMocks
    private GetTransactionHistoryUseCaseImpl useCase;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client(
                "client-1",
                "Juan Perez",
                "juan@example.com",
                "+573001112233",
                "hashed-password",
                Set.of(Role.CLIENT),
                Money.of(500000),
                null
        );
    }

    @Test
    void shouldReturnTransactionHistoryOrderedByDateDesc() {
        Transaction older = new Transaction(
                "tx-1", "client-1", "fund-1", "sub-1", TransactionType.SUBSCRIPTION,
                Money.of(75000), "Subscription completed", NotificationPreference.EMAIL, OffsetDateTime.now().minusDays(1)
        );
        Transaction newer = new Transaction(
                "tx-2", "client-1", "fund-2", "sub-2", TransactionType.CANCELLATION,
                Money.of(50000), "Cancellation completed", null, OffsetDateTime.now()
        );
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(transactionRepositoryPort.findByClientId("client-1")).thenReturn(List.of(older, newer));

        List<TransactionResponse> response = useCase.execute(new GetTransactionHistoryQuery("client-1", null));

        assertThat(response).hasSize(2);
        assertThat(response.get(0).transactionId()).isEqualTo("tx-2");
        assertThat(response.get(1).transactionId()).isEqualTo("tx-1");
    }

    @Test
    void shouldReturnTransactionHistoryFilteredByType() {
        Transaction subscription = new Transaction(
                "tx-1", "client-1", "fund-1", "sub-1", TransactionType.SUBSCRIPTION,
                Money.of(75000), "Subscription completed", NotificationPreference.SMS, OffsetDateTime.now()
        );
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(transactionRepositoryPort.findByClientIdAndType("client-1", TransactionType.SUBSCRIPTION))
                .thenReturn(List.of(subscription));

        List<TransactionResponse> response = useCase.execute(
                new GetTransactionHistoryQuery("client-1", TransactionType.SUBSCRIPTION)
        );

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().type()).isEqualTo(TransactionType.SUBSCRIPTION);
        verify(transactionRepositoryPort).findByClientIdAndType("client-1", TransactionType.SUBSCRIPTION);
    }

    @Test
    void shouldRejectTransactionHistoryQueryWhenClientDoesNotExist() {
        when(clientRepositoryPort.findById("missing-client")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GetTransactionHistoryQuery("missing-client", null)))
                .isInstanceOf(ClientNotFoundException.class)
                .hasMessage("Client not found: missing-client");
    }
}
