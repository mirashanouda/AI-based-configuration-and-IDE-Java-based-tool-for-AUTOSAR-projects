#include <CanNM.h>
#include <assert.h> // For asserting conditions
#include "dynamic.h"

int main()
{
    CanNm_Init(CanNmGlobalConfig_1);
    assert(CanNm_Internal.State == CANNM_STATE_UNINIT);
}