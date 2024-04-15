/*====================================================================================================================*\
    Include headers
\*====================================================================================================================*/
/* [SWS_CanNm_00326] */
#include "CanNM.h"

/*====================================================================================================================*\
    Object Initialization
\*====================================================================================================================*/

CanNmGlobalConfig* pCanNmGlobalConfig = &(CanNmGlobalConfig) { \ 
			.BusLoadReductionEnabled = false, \ 
			.BusSynchronizationEnabled = false, \ 
			.ComControlEnabled = false, \ 
			.ComUserDataSupport = false, \ 
			.CoordinatorSyncSupport = false, \ 
			.DevErrorDetect = false, \ 
			.GlobalPnSupport = false, \ 
			.ImmediateRestartEnabled = false, \ 
			.ImmediateTxconfEnabled = false, \ 
			.PassiveModeEnabled = false, \ 
			.PduRxIndicationEnabled = false, \ 
			.RemoteSleepIndEnabled = false, \ 
			.StateChangeIndEnabled = false, \ 
			.UserDataEnabled = false, \ 
			.VersionInfoApi = false, \ 
			.MainFunctionPeriod = 0.0 };

