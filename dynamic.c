/*====================================================================================================================*\
    Include headers
\*====================================================================================================================*/
/* [SWS_CanNm_00326] */
#include "CanNm.h"

/*====================================================================================================================*\
    Object Initialization
\*====================================================================================================================*/

CanNmRxPdu CanNmRxPdu_1 = { .RxPduId = 0 };
CanNmTxPdu CanNmTxPdu_1 = { .TxConfirmationPduId = 0 };
CanNmUserDataTxPdu CanNmUserDataTxPdu_1 = { .TxUserDataPduId = 0 };
CanNmChannelConfigType CanNmChannelConfig_1 = { .ImmediateNmTransmissions = 0, .NodeId = 0, .BusLoadReductionActive = False, .CarWakeUpRxEnabled = False, .NodeDetectionEnabled = False, .NodeIdEnabled = False, .RepeatMsgIndEnabled = False, .MsgCycleOffset = 0, .MsgCycleTime = 0, .MsgReducedTime = 0, .RepeatMessageTime = 0, .TimeoutTime = 0, .WaitBusSleepTime = 0 };
CanNmPnFilterMaskByte CanNmPnFilterMaskByte_1 = { .PnFilterMaskByteIndex = 0, .PnFilterMaskByteValue = 0 };
CanNmPnFilterMaskByte CanNmPnFilterMaskByte_2 = { .PnFilterMaskByteIndex = 0, .PnFilterMaskByteValue = 0 };
CanNmPnInfo CanNmPnInfo_1 = { .PnInfoLength = 1, .PnInfoOffset = 1 };
CanNmGlobalConfigType CanNmGlobalConfig_1 = { .BusLoadReductionEnabled = False, .BusSynchronizationEnabled = False, .ComControlEnabled = False, .ComUserDataSupport = False, .CoordinatorSyncSupport = False, .DevErrorDetect = False, .GlobalPnSupport = False, .ImmediateRestartEnabled = False, .ImmediateTxconfEnabled = False, .PassiveModeEnabled = False, .PduRxIndicationEnabled = False, .RemoteSleepIndEnabled = False, .StateChangeIndEnabled = False, .UserDataEnabled = False, .VersionInfoApi = False, .MainFunctionPeriod = 0 };
