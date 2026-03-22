package com.example.funds.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.funds.application.dto.command.SubscribeToFundCommand;
import com.example.funds.application.dto.response.SubscriptionResponse;
import com.example.funds.domain.exception.ActiveSubscriptionAlreadyExistsException;
import com.example.funds.domain.exception.FundInactiveException;
import com.example.funds.domain.exception.FundNotFoundException;
import com.example.funds.domain.exception.InsufficientBalanceException;
import com.example.funds.domain.model.Client;
import com.example.funds.domain.model.Fund;
import com.example.funds.domain.model.FundCategory;
import com.example.funds.domain.model.Money;
import com.example.funds.domain.model.NotificationPreference;
import com.example.funds.domain.model.Role;
import com.example.funds.domain.model.Subscription;
import com.example.funds.domain.port.out.ClientRepositoryPort;
import com.example.funds.domain.port.out.FundRepositoryPort;
import com.example.funds.domain.port.out.NotificationPort;
import com.example.funds.domain.port.out.SubscriptionRepositoryPort;
import com.example.funds.domain.port.out.TransactionRepositoryPort;

@ExtendWith(MockitoExtension.class)
class SubscribeToFundUseCaseImplTest {

    @Mock
    private ClientRepositoryPort clientRepositoryPort;
    @Mock
    private FundRepositoryPort fundRepositoryPort;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepositoryPort;
    @Mock
    private TransactionRepositoryPort transactionRepositoryPort;
    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private SubscribeToFundUseCaseImpl useCase;

    private Client client;
    private Fund activeFund;

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
        activeFund = new Fund("fund-1", "FPV_BTG_PACTUAL_RECAUDADORA", FundCategory.FPV, Money.of(75000), true);
    }

    @Test
    void shouldSubscribeSuccessfully() {
        SubscribeToFundCommand command = new SubscribeToFundCommand("client-1", "fund-1", NotificationPreference.EMAIL);
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(fundRepositoryPort.findById("fund-1")).thenReturn(Optional.of(activeFund));
        when(subscriptionRepositoryPort.findActiveByClientIdAndFundId("client-1", "fund-1")).thenReturn(Optional.empty());

        SubscriptionResponse response = useCase.execute(command);

        assertThat(response.clientId()).isEqualTo("client-1");
        assertThat(response.fundId()).isEqualTo("fund-1");
        assertThat(response.amount()).isEqualTo(75000L);
        assertThat(response.currentBalance()).isEqualTo(425000L);
        assertThat(response.transactionId()).isNotBlank();
        assertThat(response.notificationPreference()).isEqualTo(NotificationPreference.EMAIL);
        verify(clientRepositoryPort).save(client);
        verify(subscriptionRepositoryPort).save(any());
        verify(transactionRepositoryPort).save(any());
        verify(notificationPort).sendSubscriptionNotification(any(), any(), any());
    }

    @Test
    void shouldRejectSubscriptionWhenBalanceIsInsufficient() {
        Client lowBalanceClient = new Client(
                "client-1",
                "Juan Perez",
                "juan@example.com",
                "+573001112233",
                "hashed-password",
                Set.of(Role.CLIENT),
                Money.of(50000),
                null
        );
        SubscribeToFundCommand command = new SubscribeToFundCommand("client-1", "fund-1", NotificationPreference.EMAIL);
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(lowBalanceClient));
        when(fundRepositoryPort.findById("fund-1")).thenReturn(Optional.of(activeFund));
        when(subscriptionRepositoryPort.findActiveByClientIdAndFundId("client-1", "fund-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("No tiene saldo disponible para vincularse al fondo FPV_BTG_PACTUAL_RECAUDADORA");

        verify(clientRepositoryPort, never()).save(any());
        verify(subscriptionRepositoryPort, never()).save(any());
        verify(transactionRepositoryPort, never()).save(any());
        verify(notificationPort, never()).sendSubscriptionNotification(any(), any(), any());
    }

    @Test
    void shouldRejectSubscriptionWhenFundDoesNotExist() {
        SubscribeToFundCommand command = new SubscribeToFundCommand("client-1", "missing-fund", NotificationPreference.EMAIL);
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(fundRepositoryPort.findById("missing-fund")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(FundNotFoundException.class)
                .hasMessage("Fund not found: missing-fund");
    }

    @Test
    void shouldRejectSubscriptionWhenFundIsInactive() {
        Fund inactiveFund = new Fund("fund-1", "FPV_BTG_PACTUAL_RECAUDADORA", FundCategory.FPV, Money.of(75000), false);
        SubscribeToFundCommand command = new SubscribeToFundCommand("client-1", "fund-1", NotificationPreference.SMS);
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(fundRepositoryPort.findById("fund-1")).thenReturn(Optional.of(inactiveFund));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(FundInactiveException.class)
                .hasMessage("Fund is not active: fund-1");
    }

    @Test
    void shouldRejectSubscriptionWhenActiveSubscriptionAlreadyExists() {
        SubscribeToFundCommand command = new SubscribeToFundCommand("client-1", "fund-1", NotificationPreference.EMAIL);
        Subscription existingSubscription = Subscription.create("sub-1", "client-1", "fund-1", Money.of(75000));
        when(clientRepositoryPort.findById("client-1")).thenReturn(Optional.of(client));
        when(fundRepositoryPort.findById("fund-1")).thenReturn(Optional.of(activeFund));
        when(subscriptionRepositoryPort.findActiveByClientIdAndFundId("client-1", "fund-1"))
                .thenReturn(Optional.of(existingSubscription));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ActiveSubscriptionAlreadyExistsException.class)
                .hasMessage("Client client-1 already has an active subscription for fund fund-1");
    }
}
