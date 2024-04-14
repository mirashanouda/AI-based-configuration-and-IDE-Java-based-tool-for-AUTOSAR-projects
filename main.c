#include <assert.h> // For asserting conditions
#include "CanNM.h"
#include "dynamic.h"

int main()
{
    CanNm_Init(pCanNmGlobalConfig);
    // assert(CanNm_Internal.State == CANNM_INIT);
}