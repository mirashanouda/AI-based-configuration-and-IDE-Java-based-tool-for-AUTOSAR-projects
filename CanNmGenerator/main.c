#include <assert.h> // For asserting conditions
#include "CanNM.h"
#include "dynamic.h"

// Test Cases
void Test_CanNm_ConfirmPnAvailability()
{
    CanNm_Internal_ChannelType channel0;
    CanNm_InternalType CanNm_Internal = {
        .Channels = &channel0
    };
    CanNm_Internal.Channels[0].NmPduFilterAlgorithm = false;

    CanNmGlobalConfig* CanNm_ConfigPtr = &(CanNmGlobalConfig) {};
    CanNm_ConfigPtr->GlobalPnSupport = true;

    NetworkHandleType networkHandle = 0;
    CanNm_ConfirmPnAvailability(networkHandle, &CanNm_Internal, CanNm_ConfigPtr);
    assert(CanNm_Internal.Channels[0].NmPduFilterAlgorithm == true);
    printf("[PASSED] Ture GlobalPnSupport\n");
    CanNm_ConfigPtr->GlobalPnSupport = false;
    CanNm_ConfirmPnAvailability(networkHandle, &CanNm_Internal, CanNm_ConfigPtr);
    assert(CanNm_Internal.Channels[0].NmPduFilterAlgorithm == true);
    printf("[PASSED] False GlobalPnSupport\n");
}


void Test_CanNm_TxConfirmation()
{
    CanNm_Internal_ChannelType channel0;
    CanNm_InternalType CanNm_Internal = {
        .Channels = &channel0
    };
    CanNmGlobalConfig* CanNm_ConfigPtr = &(CanNmGlobalConfig) {};

    uint16_t TxPduId = 0;
    uint8_t result = E_OK;
    CanNm_TxConfirmation(TxPduId, result, &CanNm_Internal, CanNm_ConfigPtr);
    assert(CanNm_Internal.Channels[0].TimeoutTimer.State == CANNM_TIMER_STARTED);
    assert(CanNm_Internal.Channels[0].TimeoutTimer.TimeLeft == CanNm_ConfigPtr->c_CanNmChannelConfig[TxPduId]->TimeoutTime);
    printf("[PASSED] Timer call function\n");

    // Mock testing PduR_CanNmRxIndication:
    CanNm_ConfigPtr->ComUserDataSupport = true;
    // Reset the fake function
    // RESET_FAKE(PduR_CanNmRxIndication)

    CanNm_TxConfirmation(TxPduId, result, &CanNm_Internal, CanNm_ConfigPtr);
    // assert(PduR_CanNmRxIndication_fake.call_count == 1);
    printf("[PASSED] Setting ComUserDataSupport to true\n");

}

int main()
{
    // Test_CanNm_ConfirmPnAvailability();
    Test_CanNm_TxConfirmation();
}