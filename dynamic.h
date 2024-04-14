/*====================================================================================================================*\
    Include headers
\*====================================================================================================================*/
/* [SWS_CanNm_00326] */
#include "CanNM.h"

/*====================================================================================================================*\
    Object Initialization
\*====================================================================================================================*/

#define CanNmRxPdu_1 &(CanNmRxPdu) { \
	.RxPduId = 0 \
}

#define CanNmTxPdu_1 &(CanNmTxPdu) { \
	.TxConfirmationPduId = 0 \
}

#define CanNmUserDataTxPdu_1 &(CanNmUserDataTxPdu) { \
	.TxUserDataPduId = 0 \
}

#define CanNmChannelConfig_1 &(CanNmChannelConfig) {  \
	.ImmediateNmTransmissions = 0, \
	.NodeId = 0, \
	.BusLoadReductionActive = false, \
	.CarWakeUpRxEnabled = false, \
	.NodeDetectionEnabled = false, \ 
	.NodeIdEnabled = false, \
	.RepeatMsgIndEnabled = false, \ 
	.MsgCycleOffset = 0, \
	.MsgCycleTime = 0, \
	.MsgReducedTime = 0, \ 
	.RepeatMessageTime = 0, \ 
	.TimeoutTime = 0, \
	.WaitBusSleepTime = 0, \ 
	.c_CanNmRxPdu = CanNmRxPdu_1, \ 
	.c_CanNmTxPdu = CanNmTxPdu_1, \
	.c_CanNmUserDataTxPdu = CanNmUserDataTxPdu_1 \
}

#define CanNmPnFilterMaskByte_1 &(CanNmPnFilterMaskByte) { \
	.PnFilterMaskByteIndex = 0, \
	.PnFilterMaskByteValue = 0 \
}

#define CanNmPnFilterMaskByte_2 &(CanNmPnFilterMaskByte) { \
	.PnFilterMaskByteIndex = 0, \
	.PnFilterMaskByteValue = 0 \
}

#define CanNmPnInfo_1 &(CanNmPnInfo) { \
	.PnInfoLength = 1, \ 
	.PnInfoOffset = 1, \
	.c_CanNmPnFilterMaskByte = {CanNmPnFilterMaskByte_1, CanNmPnFilterMaskByte_2 } \
}

CanNmGlobalConfig* pCanNmGlobalConfig = &(CanNmGlobalConfig) { 
			.BusLoadReductionEnabled = false, 
			.BusSynchronizationEnabled = false, 
			.ComControlEnabled = false, 
			.ComUserDataSupport = false, 
			.CoordinatorSyncSupport = false, 
			.DevErrorDetect = false, 
			.GlobalPnSupport = false, 
			.ImmediateRestartEnabled = false, 
			.ImmediateTxConfEnabled = false, 
			.PassiveModeEnabled = false, 
			.PduRxIndicationEnabled = false, 
			.RemoteSleepIndEnabled = false, 
			.StateChangeIndEnabled = false, 
			.UserDataEnabled = false, 
			.VersionInfoApi = false,
			.MainFunctionPeriod = 0, 
			.c_CanNmChannelConfig = {CanNmChannelConfig_1 } , 
			.c_CanNmPnInfo = {CanNmPnInfo_1 }  
        };
