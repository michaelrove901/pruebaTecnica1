package com.example.funds.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.funds.application.dto.command.CancelFundSubscriptionCommand;
import com.example.funds.application.dto.response.SubscriptionResponse;
import com.example.funds.domain.exception.SubscriptionAlreadyCancelledException;
import com.example.funds.domain.exception.SubscriptionNotFoundException;
import com.example.funds.domain.exception.UnauthorizedAccessException;
import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Money;
import com.example.funds.domain.model.Role;
import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.model.SubscriptionStatus;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.domain.port.out.SubscriptionRepositoryPort;
import com.example.funds.domain.port.out.TransactionRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CancelFundSubscriptionUseCaseImplTest {

    @Mock
    private ClientRepositoryPort clientRepositoryPort;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;

    @InjectMocks
    private CancelFundSubscriptionUseCaseImpl useCase;

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
    void shouldCancelSubscriptionSuccessfullyAndReturnBalance() {
        Subscription subscription = Subscription.create("sub-1", "client-1", "fund-1", Money.of(75000));
        CancelFundSubscriptionCommand command = new CancelFundSubscriptionCommand("client-1", "sub-1");
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(subscriptionRepositoryPort.findById("sub-1")).thenReturn(Optional.of(subscription));

        SubscriptionResponse response = useCase.execute(command);

        assertThat(response.subscriptionId()).isEqualTo("sub-1");
        assertThat(response.status()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(response.currentBalance()).isEqualTo(500000L);
        assertThat(response.cancelledAt()).isNotNull();
        verify(clientRepositoryPort).save(client);
        verify(subscriptionRepositoryPort).save(subscription);
        verify(transactionRepositoryPort).save(any());
    }

    @Test
    void shouldRejectCancellationWhenSubscriptionDoesNotExist() {
        CancelFundSubscriptionCommand command = new CancelFundSubscriptionCommand("client-1", "missing-sub");
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(subscriptionRepositoryPort.findById("missing-sub")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessage("Subscription not found: missing-sub");
    }

    @Test
    void shouldRejectCancellationWhenSubscriptionIsAlreadyCancelled() {
        Subscription cancelledSubscription = new Subscription(
                "sub-1",
                "client-1",
                "fund-1",
                Money.of(75000),
                SubscriptionStatus.CANCELLED,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusHours(1)
        );
        CancelFundSubscriptionCommand command = new CancelFundSubscriptionCommand("client-1", "sub-1");
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(subscriptionRepositoryPort.findById("sub-1")).thenReturn(Optional.of(cancelledSubscription));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SubscriptionAlreadyCancelledException.class)
                .hasMessage("Subscription is already cancelled: sub-1");
    }

    @Test
    void shouldRejectCancellationWhenSubscriptionBelongsToAnotherClient() {
        Subscription subscription = Subscription.create("sub-1", "other-client", "fund-1", Money.of(75000));
        CancelFundSubscriptionCommand command = new CancelFundSubscriptionCommand("client-1", "sub-1");
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(subscriptionRepositoryPort.findById("sub-1")).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage("You are not authorized to access this resource");
    }
}
