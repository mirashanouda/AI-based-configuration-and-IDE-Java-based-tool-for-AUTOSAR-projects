/** ==================================================================================================================*\
  Implementation of Can Network Managment Module
\*====================================================================================================================*/

/*====================================================================================================================*\
    Include headers
\*====================================================================================================================*/
#include "CanNm.h"

/* [SWS_CanNm_00308] */
#include "Det.h"

/* [SWS_CanNm_00309] */
#include "NmStack_Types.h"



/*====================================================================================================================*\
    Local types
\*====================================================================================================================*/
typedef void (*CanNmTimerCallback)(void* Timer, const uint8_t_t channel);

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

typedef struct {
	uint8_t						Channel;
	Nm_ModeType					Mode;					//[SWS_CanNm_00092]
	Nm_StateType				State;					//[SWS_CanNm_00089]
	bool						Requested;
	bool						TxEnabled;
	int8_t						RxLastPdu;
	CanNm_Timer					TimeoutTimer;			//NM-Timeout Timer, Tx Timeout Timer
	CanNm_Timer					MessageCycleTimer;
	CanNm_Timer					RepeatMessageTimer;
	CanNm_Timer					WaitBusSleepTimer;
	CanNm_Timer					RemoteSleepIndTimer;
	uint8_t						ImmediateTransmissions;
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
    Global variables
\*====================================================================================================================*/
CanNm_InternalType CanNm_Internal = {
		.InitStatus = CANNM_UNINIT
};

/*====================================================================================================================*\
    Local variables (static)
\*====================================================================================================================*/
static const CanNmConfigType* CanNm_ConfigPtr;

/*====================================================================================================================*\
    Local functions declarations
\*====================================================================================================================*/
/* Timer functions */
static inline void CanNm_Internal_TimersInit( uint8_t channel );

/* Additional functions */
static inline void CanNm_Internal_ClearPduCbv( const CanNm_ChannelType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline uint8_t* CanNm_Internal_GetUserDataPtr( const CanNm_ChannelType* ChannelConf, uint8_t* MessageSduPtr );
static inline uint8_t CanNm_Internal_GetUserDataLength( const CanNm_ChannelType* ChannelConf );

/*====================================================================================================================*\
    Global functions code
\*====================================================================================================================*/

/** @brief CanNm_Init [SWS_CanNm_00208]
 * 
 * Initialize the CanNm module.
 */
void CanNm_Init(const CanNmConfigType* cannmConfigPtr)
{
    CanNm_ConfigPtr = cannmConfigPtr;	//[SWS_CanNm_00060]

	for (uint8_t channel = 0; channel < CANNM_CHANNEL_COUNT; channel++) {
		const CanNm_ChannelType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[channel];
		CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[channel];

		ChannelInternal->Channel = channel;
		ChannelInternal->Mode = NM_MODE_BUS_SLEEP;														//[SWS_CanNm_00144]
		ChannelInternal->State = NM_STATE_BUS_SLEEP;													//[SWS_CanNm_00141][SWS_CanNm_00094]
		ChannelInternal->Requested = FALSE;																//[SWS_CanNm_00143]
		ChannelInternal->TxEnabled = FALSE;
		ChannelInternal->RxLastPdu = NO_PDU_RECEIVED;
		ChannelInternal->ImmediateTransmissions = 0;
		ChannelInternal->BusLoadReduction = FALSE;														//[SWS_CanNm_00023]
		ChannelInternal->RemoteSleepInd = FALSE;
		ChannelInternal->RemoteSleepIndEnabled = CanNm_ConfigPtr->RemoteSleepIndEnabled;
		ChannelInternal->NmPduFilterAlgorithm = FALSE;

		if (ChannelConf->NodeIdEnabled && ChannelConf->PduNidPosition != CANNM_PDU_OFF) {
			ChannelConf->TxPdu->TxPduRef->SduDataPtr[ChannelConf->PduNidPosition] = ChannelConf->NodeId;//[SWS_CanNm_00013]
		}

		CanNm_Internal_ClearPduCbv(ChannelConf, ChannelInternal);										//[SWS_CanNm_00085]

		uint8_t* destUserData = CanNm_Internal_GetUserDataPtr(ChannelConf, ChannelConf->UserDataTxPdu->TxUserDataPduRef->SduDataPtr);
		uint8_t userDataLength = CanNm_Internal_GetUserDataLength(ChannelConf);
		memset(destUserData, 0xFF, userDataLength);														//[SWS_CanNm_00025]

		CanNm_Internal_TimersInit(channel);																//[SWS_CanNm_00061][SWS_CanNm_00033]
	}
	CanNm_Internal.InitStatus = CANNM_INIT;
}


/************************/
/* Timer functions */
/************************/
static inline void CanNm_Internal_TimersInit( uint8_t channel )
{
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[channel];

	ChannelInternal->TimeoutTimer.Channel = channel;
	ChannelInternal->TimeoutTimer.ExpiredCallback = CanNm_Internal_TimeoutTimerExpiredCallback;
	ChannelInternal->TimeoutTimer.State = CANNM_TIMER_STOPPED;
	ChannelInternal->TimeoutTimer.TimeLeft = 0;

	ChannelInternal->MessageCycleTimer.Channel = channel;
	ChannelInternal->MessageCycleTimer.ExpiredCallback = CanNm_Internal_MessageCycleTimerExpiredCallback;
	ChannelInternal->MessageCycleTimer.State = CANNM_TIMER_STOPPED;
	ChannelInternal->MessageCycleTimer.TimeLeft = 0;

	ChannelInternal->RepeatMessageTimer.Channel = channel;
	ChannelInternal->RepeatMessageTimer.ExpiredCallback = CanNm_Internal_RepeatMessageTimerExpiredCallback;
	ChannelInternal->RepeatMessageTimer.State = CANNM_TIMER_STOPPED;
	ChannelInternal->RepeatMessageTimer.TimeLeft = 0;

	ChannelInternal->WaitBusSleepTimer.Channel = channel;
	ChannelInternal->WaitBusSleepTimer.ExpiredCallback = CanNm_Internal_WaitBusSleepTimerExpiredCallback;
	ChannelInternal->WaitBusSleepTimer.State = CANNM_TIMER_STOPPED;
	ChannelInternal->WaitBusSleepTimer.TimeLeft = 0;

	ChannelInternal->RemoteSleepIndTimer.Channel = channel;
	ChannelInternal->RemoteSleepIndTimer.ExpiredCallback = CanNm_Internal_RemoteSleepIndTimerExpiredCallback;
	ChannelInternal->RemoteSleepIndTimer.State = CANNM_TIMER_STOPPED;
	ChannelInternal->RemoteSleepIndTimer.TimeLeft = 0;
}

static inline void CanNm_Internal_ClearPduCbv( const CanNm_ChannelType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal )
{
	if (ChannelConf->PduCbvPosition != CANNM_PDU_OFF) {
		ChannelConf->TxPdu->TxPduRef->SduDataPtr[ChannelConf->PduCbvPosition] = 0x00;
	}
}

static inline uint8_t* CanNm_Internal_GetUserDataPtr( const CanNm_ChannelType* ChannelConf, uint8_t* MessageSduPtr )
{
	uint8_t userDataOffset = CanNm_Internal_GetUserDataOffset(ChannelConf);
	return &MessageSduPtr[userDataOffset];
}


/************************/
/* Additional functions */
/************************/
static inline void CanNm_Internal_ClearPduCbv( const CanNm_ChannelType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal )
{
	if (ChannelConf->PduCbvPosition != CANNM_PDU_OFF) {
		ChannelConf->TxPdu->TxPduRef->SduDataPtr[ChannelConf->PduCbvPosition] = 0x00;
	}
}

static inline uint8_t CanNm_Internal_GetUserDataLength( const CanNm_ChannelType* ChannelConf )
{
	uint8_t userDataOffset = CanNm_Internal_GetUserDataOffset(ChannelConf);
	return ChannelConf->UserDataTxPdu->TxUserDataPduRef->SduLength - userDataOffset;
}