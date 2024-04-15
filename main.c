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
    printf("Test passed\n");
    CanNm_ConfigPtr->GlobalPnSupport = false;
    CanNm_ConfirmPnAvailability(networkHandle, &CanNm_Internal, CanNm_ConfigPtr);
    assert(CanNm_Internal.Channels[0].NmPduFilterAlgorithm == true);
    printf("Test passed\n");
}

int main()
{
    Test_CanNm_ConfirmPnAvailability();
}