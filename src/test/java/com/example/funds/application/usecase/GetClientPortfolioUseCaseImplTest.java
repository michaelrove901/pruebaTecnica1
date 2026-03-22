package com.example.funds.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.example.funds.application.dto.query.GetClientPortfolioQuery;
import com.example.funds.application.dto.response.ClientPortfolioResponse;
import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Money;
import com.example.funds.domain.model.Role;
import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.model.SubscriptionStatus;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.domain.port.out.SubscriptionRepositoryPort;

@ExtendWith(MockitoExtension.class)
class GetClientPortfolioUseCaseImplTest {

    @Mock
    private ClientRepositoryPort clientRepositoryPort;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;

    @InjectMocks
    private GetClientPortfolioUseCaseImpl useCase;

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
                Money.of(425000),
                null
        );
    }

    @Test
    void shouldReturnBalanceAndActiveSubscriptionsOrderedByOpenedAtDesc() {
        Subscription older = new Subscription(
                "sub-1",
                "client-1",
                "fund-1",
                Money.of(75000),
                SubscriptionStatus.ACTIVE,
                OffsetDateTime.now().minusDays(2),
                null
        );
        Subscription newer = new Subscription(
                "sub-2",
                "client-1",
                "fund-2",
                Money.of(50000),
                SubscriptionStatus.ACTIVE,
                OffsetDateTime.now().minusHours(2),
                null
        );
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(subscriptionRepositoryPort.findByClientIdAndStatus("client-1", SubscriptionStatus.ACTIVE))
                .thenReturn(List.of(older, newer));

        ClientPortfolioResponse response = useCase.execute(new GetClientPortfolioQuery("client-1"));

        assertThat(response.clientId()).isEqualTo("client-1");
        assertThat(response.balance()).isEqualTo(425000L);
        assertThat(response.activeSubscriptions()).hasSize(2);
        assertThat(response.activeSubscriptions().get(0).subscriptionId()).isEqualTo("sub-2");
        assertThat(response.activeSubscriptions().get(1).subscriptionId()).isEqualTo("sub-1");
    }
}
