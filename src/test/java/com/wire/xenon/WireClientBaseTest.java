package com.wire.xenon;

import com.wire.xenon.backend.models.NewBot;
import com.wire.xenon.crypto.mls.CryptoMlsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

public class WireClientBaseTest {
    private WireClientBase wireClientBase;
    private WireAPI mockApi;
    private CryptoMlsClient mockCryptoMlsClient;
    private NewBot mockState;

    @BeforeEach
    public void setUp() {
        mockApi = mock(WireAPI.class);
        mockCryptoMlsClient = mock(CryptoMlsClient.class);
        mockState = mock(NewBot.class);
        wireClientBase = new WireClientBase(mockApi, null, mockCryptoMlsClient, mockState);
    }

    @Test
    public void checkAndReplenishKeyPackages_replenishesWhenBelowThreshold() {
        when(mockCryptoMlsClient.validKeyPackageCount()).thenReturn(WireClientBase.KEY_PACKAGES_LOWER_THRESHOLD - 5L);

        wireClientBase.checkAndReplenishKeyPackages();

        verify(mockCryptoMlsClient, times(1)).generateKeyPackages(WireClientBase.KEY_PACKAGES_REPLENISH_AMOUNT);
    }

    @Test
    public void checkAndReplenishKeyPackages_doesNotReplenishWhenAboveThreshold() {
        when(mockCryptoMlsClient.validKeyPackageCount()).thenReturn(WireClientBase.KEY_PACKAGES_LOWER_THRESHOLD + 5L);

        wireClientBase.checkAndReplenishKeyPackages();

        verify(mockCryptoMlsClient, never()).generateKeyPackages(anyInt());
    }

    @Test
    public void processWelcomeMessage_callsCheckAndReplenishKeyPackages() {
        String welcomeMessage = "welcomeMessage";
        byte[] expectedResponse = new byte[]{1, 2, 3};

        when(mockCryptoMlsClient.processWelcomeMessage(welcomeMessage)).thenReturn(expectedResponse);
        when(mockCryptoMlsClient.validKeyPackageCount()).thenReturn(WireClientBase.KEY_PACKAGES_LOWER_THRESHOLD + 5L);

        byte[] response = wireClientBase.processWelcomeMessage(welcomeMessage);

        verify(mockCryptoMlsClient, times(1)).processWelcomeMessage(welcomeMessage);
        assertArrayEquals(expectedResponse, response);
    }
}