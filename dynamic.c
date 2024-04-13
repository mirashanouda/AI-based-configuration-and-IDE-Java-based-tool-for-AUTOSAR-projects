/*====================================================================================================================*\
    Include headers
\*====================================================================================================================*/
/* [SWS_CanNm_00326] */
#include "CanNm.h"

/*====================================================================================================================*\
    Object Initialization
\*====================================================================================================================*/

struct CanNmRxPdu* CanNmRxPdu_1 = &(struct CanNmRxPdu) { 
			.RxPduId = 0 }
struct CanNmTxPdu* CanNmTxPdu_1 = &(struct CanNmTxPdu) { 
			.TxConfirmationPduId = 0 }
struct CanNmUserDataTxPdu* CanNmUserDataTxPdu_1 = &(struct CanNmUserDataTxPdu) { 
			.TxUserDataPduId = 0 }
struct CanNmChannelConfig* CanNmChannelConfig_1 = &(struct CanNmChannelConfig) { 
			.ImmediateNmTransmissions = 0, 
			.NodeId = 0, 
			.BusLoadReductionActive = False, 
			.CarWakeUpRxEnabled = False, 
			.NodeDetectionEnabled = False, 
			.NodeIdEnabled = False, 
			.RepeatMsgIndEnabled = False, 
			.MsgCycleOffset = 0, 
			.MsgCycleTime = 0, 
			.MsgReducedTime = 0, 
			.RepeatMessageTime = 0, 
			.TimeoutTime = 0, 
			.WaitBusSleepTime = 0, 
			.CanNmRxPdu = {CanNmRxPdu_1 } , 
			.CanNmTxPdu = {CanNmTxPdu_1 } , 
			.CanNmUserDataTxPdu = {CanNmUserDataTxPdu_1 }  }
struct CanNmPnFilterMaskByte* CanNmPnFilterMaskByte_1 = &(struct CanNmPnFilterMaskByte) { 
			.PnFilterMaskByteIndex = 0, 
			.PnFilterMaskByteValue = 0 }
struct CanNmPnFilterMaskByte* CanNmPnFilterMaskByte_2 = &(struct CanNmPnFilterMaskByte) { 
			.PnFilterMaskByteIndex = 0, 
			.PnFilterMaskByteValue = 0 }
struct CanNmPnInfo* CanNmPnInfo_1 = &(struct CanNmPnInfo) { 
			.PnInfoLength = 1, 
			.PnInfoOffset = 1, 
			.CanNmPnFilterMaskByte = {CanNmPnFilterMaskByte_1, CanNmPnFilterMaskByte_2 }  }
struct CanNmGlobalConfig* CanNmGlobalConfig_1 = &(struct CanNmGlobalConfig) { 
			.BusLoadReductionEnabled = False, 
			.BusSynchronizationEnabled = False, 
			.ComControlEnabled = False, 
			.ComUserDataSupport = False, 
			.CoordinatorSyncSupport = False, 
			.DevErrorDetect = False, 
			.GlobalPnSupport = False, 
			.ImmediateRestartEnabled = False, 
			.ImmediateTxconfEnabled = False, 
			.PassiveModeEnabled = False, 
			.PduRxIndicationEnabled = False, 
			.RemoteSleepIndEnabled = False, 
			.StateChangeIndEnabled = False, 
			.UserDataEnabled = False, 
			.VersionInfoApi = False, 
			.MainFunctionPeriod = 0, 
			.CanNmChannelConfig = {CanNmChannelConfig_1 } , 
			.CanNmPnInfo = {CanNmPnInfo_1 }  }
