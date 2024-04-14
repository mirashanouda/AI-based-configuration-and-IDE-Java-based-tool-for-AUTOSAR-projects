#ifndef CANNM_H
#define CANNM_H

/**===================================================================================================================*\
  Implementation of Can Network Managment Module (Static Code)
\*====================================================================================================================*/

/*====================================================================================================================*\
	System Headers
\*====================================================================================================================*/
#include "stdint.h"
#include "stdbool.h"
#include <stddef.h>

/*====================================================================================================================*\
    Include headers [SWS_CanNm_00245]
\*====================================================================================================================*/
// TODO: Check SWS for correct included headers
/* [SWS_CanNm_00309] */
#include "NmStack_Types.h"

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
typedef struct {
	uint8_t *SduDataPtr;			// payload
	uint16_t SduLength;				// length of SDU
} PduInfoType;

typedef uint8_t NetworkHandleType;


/*====================================================================================================================*\
    Containers Structs:
\*====================================================================================================================*/
typedef enum {
	CANNM_PDU_BYTE_0 = 0x00,
	CANNM_PDU_BYTE_1 = 0x01,
	CANNM_PDU_OFF = 0xFF
} CanNmPduBytePositionEnum;	//[SWS_CanNm_00074][SWS_CanNm_00075]

typedef struct {
	uint16_t	 RxPduId;
	PduInfoType* RxPduRef;
} CanNmRxPdu;

typedef struct {
	uint16_t	 TxConfirmationPduId;
	PduInfoType* TxPduRef;
} CanNmTxPdu;

typedef struct {
	uint16_t	 TxUserDataPduId;
	PduInfoType* TxUserDataPduRef;
} CanNmUserDataTxPdu;

typedef struct {
	uint8_t PnFilterMaskByteIndex;
	uint8_t PnFilterMaskByteValue;
} CanNmPnFilterMaskByte;

typedef struct {
	// Containers:
	CanNmUserDataTxPdu*		    c_CanNmUserDataTxPdu;
	CanNmRxPdu*			    	c_CanNmRxPdu[CANNM_RXPDU_MAX_COUNT];
	CanNmTxPdu*			    	c_CanNmTxPdu;

	// Parameters:
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
	CanNmPduBytePositionEnum	PduCbvPosition;
	CanNmPduBytePositionEnum	PduNidPosition;
	bool						PnEnabled;
	bool						PnEraCalcEnabled;
	bool						PnHandleMultipleNetworkRequests;
	float						RemoteSleepIndTime;
	float						RepeatMessageTime;
	bool						RepeatMsgIndEnabled;
	float						TimeoutTime;
	float						WaitBusSleepTime;
	NetworkHandleType			ComMNetworkHandleRef;
	PduInfoType					PnEraRxNSduRef;
} CanNmChannelConfig;

typedef struct {
	// Containers:
	const CanNmPnFilterMaskByte* 	c_CanNmPnFilterMaskByte;
	
	// Parameters:
	const uint8_t 					PnInfoLength;
	const uint8_t 					PnInfoOffset;
} CanNmPnInfo;

/** @brief CanNmGlobalConfig [SWS_CanNm_00447]
 * 
 * This type shall contain at least all parameters that are post-build able according to chapter 10.
 */
typedef struct {
	// Containers:
	CanNmChannelConfig*	c_CanNmChannelConfig[CANNM_CHANNEL_COUNT];
	CanNmPnInfo*		c_CanNmPnInfo;

	// Parameters:
	bool				BusLoadReductionEnabled;
	bool				BusSynchronizationEnabled;
	bool				ComControlEnabled;
	bool				ComUserDataSupport;
	bool				CoordinatorSyncSupport;
	bool				DevErrorDetect;
	bool				GlobalPnSupport;
	bool				ImmediateRestartEnabled;
	bool				ImmediateTxconfEnabled;				//[SWS_CanNm_00071]
	float				MainFunctionPeriod;
	bool				PassiveModeEnabled;
	bool				PduRxIndicationEnabled;
	bool				PnEiraCalcEnabled;
	float				PnResetTime;
	bool				RemoteSleepIndEnabled;
	bool				StateChangeIndEnabled;
	bool				UserDataEnabled;
	bool				VersionInfoApi;
	PduInfoType*		PnEiraRxNSduRef;
} CanNmGlobalConfig;


/*====================================================================================================================*\
    Local types
\*====================================================================================================================*/
typedef void (*CanNmTimerCallback)(void* Timer, const uint8_t channel);

typedef enum {
	CANNM_TIMER_STOPPED,
	CANNM_TIMER_STARTED
} CanNmTimerState;

typedef struct {
	uint8_t						Channel;				
	CanNmTimerCallback 	    	ExpiredCallback;
	CanNmTimerState 			State;
	float						TimeLeft;
} CanNm_Timer;

typedef enum {
	CANNM_INIT,
	CANNM_UNINIT
} CanNm_InitStatusType;

// This struct is used to store the internal state of the CanNm individual channels
// It can't be combined with the CanNmChannelConfig struct because it is not part of the params and containers. 
typedef struct {
	uint8_t						Channel;
	Nm_ModeType					Mode;					//[SWS_CanNm_00092] // Defined in "NmStack_Types.h"
	Nm_StateType				State;					//[SWS_CanNm_00089]
	bool						Requested;
	bool						TxEnabled;
	int8_t						RxLastPdu;
	CanNm_Timer					TimeoutTimer;			//NM-Timeout Timer, Tx Timeout Timer
	CanNm_Timer					MessageCycleTimer;
	CanNm_Timer					RepeatMessageTimer;
	CanNm_Timer					WaitBusSleepTimer;
	CanNm_Timer					RemoteSleepIndTimer;
	uint8_t						ImmediateNmTransmissions;
	bool						BusLoadReduction;		//[SWS_CanNm_00238]
	bool						RemoteSleepInd;
	bool						RemoteSleepIndEnabled;
	bool						NmPduFilterAlgorithm;
} CanNm_Internal_ChannelType;

typedef struct {
	CanNm_InitStatusType 		InitStatus;
	CanNm_Internal_ChannelType	Channels[CANNM_CHANNEL_COUNT];
} CanNm_InternalType;


/*====================================================================================================================*\
    Global inline functions and function macros code
\*====================================================================================================================*/
void CanNm_Init(const CanNmGlobalConfig* canNmConfigPtr); //* Ref
void CanNm_DeInit(void);
uint8_t CanNm_PassiveStartUp(NetworkHandleType nmChannelHandle);
// uint8_t CanNm_NetworkRequest(NetworkHandleType nmChannelHandle); //! [Skipped]
uint8_t CanNm_NetworkRelease(NetworkHandleType nmChannelHandle);
uint8_t CanNm_DisableCommunication(NetworkHandleType nmChannelHandle);
uint8_t CanNm_EnableCommunication(NetworkHandleType nmChannelHandle);
uint8_t CanNm_SetUserData(NetworkHandleType nmChannelHandle, const uint8_t* nmUserDataPtr); //* Ref
uint8_t CanNm_GetUserData(NetworkHandleType nmChannelHandle, uint8_t* nmUserDataPtr); //* Ref
uint8_t CanNm_Transmit(uint16_t TxPduId, const PduInfoType* PduInfoPtr);
// uint8_t CanNm_GetNodeIdentifier(NetworkHandleType nmChannelHandle, uint8_t*nmNodeIdPtr);  //! [Skipped]
uint8_t CanNm_GetLocalNodeIdentifier(NetworkHandleType nmChannelHandle, uint8_t* nmNodeIdPtr);
uint8_t CanNm_RepeatMessageRequest(NetworkHandleType nmChannelHandle);
// uint8_t CanNm_GetPduData(NetworkHandleType nmChannelHandle, uint8_t* nmPduDataPtr); //! [Skipped]
uint8_t CanNm_GetState(NetworkHandleType nmChannelHandle, Nm_StateType* nmStatePtr, Nm_ModeType* nmModePtr);
uint8_t CanNm_RequestBusSynchronization(NetworkHandleType nmChannelHandle);
uint8_t CanNm_CheckRemoteSleepIndication(NetworkHandleType nmChannelHandle, bool* nmRemoteSleepIndPtr);
uint8_t CanNm_SetSleepReadyBit(NetworkHandleType nmChannelHandle,bool nmSleepReadyBit);

void CanNm_TxConfirmation(uint16_t TxPduId, uint8_t result);
// void CanNm_RxIndication(uint16_t RxPduId, const PduInfoType* PduInfoPtr);//! [Skipped]
void CanNm_ConfirmPnAvailability(NetworkHandleType nmChannelHandle);
uint8_t CanNm_TriggerTransmit(uint16_t TxPduId, PduInfoType* PduInfoPtr); //* Ref

#endif /* CANNM_H */
