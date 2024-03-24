#ifndef CANNM_H
#define CANNM_H

/**===================================================================================================================*\
  Implementation of Can Network Managment Module (Static Code)
\*====================================================================================================================*/

/*====================================================================================================================*\
    Include headers [SWS_CanNm_00245]
\*====================================================================================================================*/
// TODO: Check SWS for correct included headers
/* [SWS_CanNm_00309] */
#include "NmStack_Types.h"

//#include "CanNm_Cfg.h

/*====================================================================================================================*\
    Local macros
\*====================================================================================================================*/
//TODO: this can be generated to match the number of channel configs
#ifndef CANNM_CHANNEL_COUNT
#define CANNM_CHANNEL_COUNT 1
#endif

#ifndef CANNM_RXPDU_MAX_COUNT
#define CANNM_RXPDU_MAX_COUNT 128
#endif


/*====================================================================================================================*\
    Included Types from ComStack_Types.h [SWS_CanNm_00305]
\*====================================================================================================================*/
typedef uint16_t PduIdType;
typedef uint16_t PduLengthType;
typedef struct {
	uint8_t *SduDataPtr;			// payload
	PduLengthType SduLength;	// length of SDU
} PduInfoType;

typedef uint8_t NetworkHandleType;


/*====================================================================================================================*\
    Included Types from Std_Types.h [SWS_CanNm_00305]
\*====================================================================================================================*/
typedef uint8_t Std_ReturnType;


/*====================================================================================================================*\
    Containers Structs:
\*====================================================================================================================*/
typedef enum {
	CANNM_PDU_BYTE_0 = 0x00,
	CANNM_PDU_BYTE_1 = 0x01,
	CANNM_PDU_OFF = 0xFF
} CanNmPduBytePositionType;	//[SWS_CanNm_00074][SWS_CanNm_00075]

typedef struct {
	PduIdType	 RxPduId;
	PduInfoType* RxPduRef;
} CanNmRxPdu;

typedef struct {
	PduIdType	 TxConfirmationPduId;
	PduInfoType* TxPduRef;
} CanNmTxPdu;

typedef struct {
	PduIdType	 TxUserDataPduId;
	PduInfoType* TxUserDataPduRef;
} CanNmUserDataTxPdu;

typedef struct {
	uint8_t PnFilterMaskByteIndex;
	uint8_t PnFilterMaskByteValue;
} CanNmPnFilterMaskByte;

typedef struct {
	bool						ActiveWakeupBitEnabled;
	bool						AllNmMessagesKeepAwake;
	bool 					BusLoadReductionActive;
	uint8_t						CarWakeUpBitPosition;
	bool						CarWakeUpFilterEnabled;
	uint8_t						CarWakeUpFilterNodeId;
	bool						CarWakeUpRxEnabled;
	float						ImmediateNmCycleTime;
	uint8_t						ImmediateNmTransmissions;
	float						MsgCycleOffset;
	float						MsgCycleTime;
	float						MsgReducedTime;
	float						MsgTimeoutTime;
	bool						NodeDetectionEnabled;
	uint8_t						NodeId;
	bool						NodeIdEnabled;
	CanNmPduBytePositionType	PduCbvPosition;
	CanNmPduBytePositionType	PduNidPosition;
	bool						PnEnabled;
	bool						PnEraCalcEnabled;
	bool						PnHandleMultipleNetworkRequests;
	float						RemoteSleepIndTime;
	float						RepeatMessageTime;
	bool						RepeatMsgIndEnabled;
	CanNmRxPdu*			    	RxPdu[CANNM_RXPDU_MAX_COUNT];
	float						TimeoutTime;
	CanNmTxPdu*			    	TxPdu;
	CanNmUserDataTxPdu*		    UserDataTxPdu;
	float						WaitBusSleepTime;
	NetworkHandleType			ComMNetworkHandleRef;
	PduInfoType					PnEraRxNSduRef;
} CanNmChannelType;

typedef struct {
	const uint8_t 					PnInfoLength;
	const uint8_t 					PnInfoOffset;
	const CanNmPnFilterMaskByte* 	PnFilterMaskByte;
} CanNmPnInfo;

/** @brief CanNm_ConfigType [SWS_CanNm_00447]
 * 
 * This type shall contain at least all parameters that are post-build able according to chapter 10.
 */
typedef struct {
	bool				BusLoadReductionEnabled;
	bool				BusSynchronizationEnabled;
	CanNmChannelType*	ChannelConfig[CANNM_CHANNEL_COUNT];
	bool				ComControlEnabled;
	bool				ComUserDataSupport;
	bool				CoordinationSyncSupport;
	bool				DevErrorDetect;
	bool				GlobalPnSupport;
	bool				ImmediateRestartEnabled;
	bool				ImmediateTxConfEnabled;				//[SWS_CanNm_00071]
	float				MainFunctionPeriod;
	bool				PassiveModeEnabled;
	bool				PduRxIndicationEnabled;
	bool				PnEiraCalcEnabled;
	CanNmPnInfo*		PnInfo;
	float				PnResetTime;
	bool				RemoteSleepIndEnabled;
	bool				StateChangeIndEnabled;
	bool				UserDataEnabled;
	bool				VersionInfoApi;
	PduInfoType*		PnEiraRxNSduRef;
} CanNm_ConfigType;

/*====================================================================================================================*\
    Global variables export
\*====================================================================================================================*/

/*====================================================================================================================*\
    Global functions declarations
\*====================================================================================================================*/

/*====================================================================================================================*\
    Global inline functions and function macros code
\*====================================================================================================================*/
void CanNm_Init(const CanNm_ConfigType* cannmConfigPtr);
void CanNm_DeInit(void);
Std_ReturnType CanNm_PassiveStartUp(NetworkHandleType nmChannelHandle);
Std_ReturnType CanNm_NetworkRequest(NetworkHandleType nmChannelHandle);
Std_ReturnType CanNm_NetworkRelease(NetworkHandleType nmChannelHandle);
Std_ReturnType CanNm_DisableCommunication(NetworkHandleType nmChannelHandle);
Std_ReturnType CanNm_EnableCommunication(NetworkHandleType nmChannelHandle);
Std_ReturnType CanNm_SetUserData(NetworkHandleType nmChannelHandle, const uint8_t* nmUserDataPtr);
Std_ReturnType CanNm_GetUserData(NetworkHandleType nmChannelHandle, uint8_t* nmUserDataPtr);
Std_ReturnType CanNm_Transmit(PduIdType TxPduId, const PduInfoType* PduInfoPtr);
Std_ReturnType CanNm_GetNodeIdentifier(NetworkHandleType nmChannelHandle, uint8_t*nmNodeIdPtr);
Std_ReturnType CanNm_GetLocalNodeIdentifier(NetworkHandleType nmChannelHandle, uint8_t* nmNodeIdPtr);
Std_ReturnType CanNm_RepeatMessageRequest(NetworkHandleType nmChannelHandle);
Std_ReturnType CanNm_GetPduData(NetworkHandleType nmChannelHandle, uint8_t* nmPduDataPtr);
Std_ReturnType CanNm_GetState(NetworkHandleType nmChannelHandle, Nm_StateType* nmStatePtr, Nm_ModeType* nmModePtr);
void CanNm_GetVersionInfo(Std_VersionInfoType* versioninfo);
Std_ReturnType CanNm_RequestBusSynchronization(NetworkHandleType nmChannelHandle);
Std_ReturnType CanNm_CheckRemoteSleepIndication(NetworkHandleType nmChannelHandle, bool* nmRemoteSleepIndPtr);
Std_ReturnType CanNm_SetSleepReadyBit(NetworkHandleType nmChannelHandle,bool nmSleepReadyBit);

void CanNm_TxConfirmation(PduIdType TxPduId, Std_ReturnType result);
void CanNm_RxIndication(PduIdType RxPduId, const PduInfoType* PduInfoPtr);
void CanNm_ConfirmPnAvailability(NetworkHandleType nmChannelHandle);
Std_ReturnType CanNm_TriggerTransmit(PduIdType TxPduId, PduInfoType* PduInfoPtr);

#endif /* CANNM_H */