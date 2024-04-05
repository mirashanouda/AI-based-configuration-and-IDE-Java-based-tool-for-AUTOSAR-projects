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
    System headers
\*====================================================================================================================*/
#include <iostream>


/*====================================================================================================================*\
	Local macros
\*====================================================================================================================*/
#define E_OK        0x00U
#define E_NOT_OK    0x01U


/*====================================================================================================================*\
    Global variables
\*====================================================================================================================*/
CanNm_InternalType CanNm_Internal = {
		.InitStatus = CANNM_UNINIT
};

/*====================================================================================================================*\
    Local variables (static)
\*====================================================================================================================*/
static const CanNmGlobalConfigType* CanNm_ConfigPtr;

/*====================================================================================================================*\
    Local functions declarations
\*====================================================================================================================*/
/* Timer functions */
static inline void CanNm_Internal_TimerStart( CanNm_Timer* Timer, uint32 timeoutValue );
static inline void CanNm_Internal_TimerResume( CanNm_Timer* Timer );
static inline void CanNm_Internal_TimerStop( CanNm_Timer* Timer );
static inline void CanNm_Internal_TimerReset( CanNm_Timer* Timer, uint32 timeoutValue );
static inline void CanNm_Internal_TimerTick( CanNm_Timer* Timer, const uint8_t channel, const float32 period );

static inline void CanNm_Internal_TimersInit( uint8_t channel );
static inline void CanNm_Internal_TimeoutTimerExpiredCallback( void* Timer, const uint8_t channel );
static inline void CanNm_Internal_MessageCycleTimerExpiredCallback( void* Timer, const uint8_t channel );
static inline void CanNm_Internal_RepeatMessageTimerExpiredCallback( void* Timer, const uint8_t channel );
static inline void CanNm_Internal_WaitBusSleepTimerExpiredCallback( void* Timer, const uint8_t channel );
static inline void CanNm_Internal_RemoteSleepIndTimerExpiredCallback( void* Timer, const uint8_t channel );

/* State Machine functions */
static inline void CanNm_Internal_BusSleep_to_BusSleep( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_BusSleep_to_RepeatMessage( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_RepeatMessage_to_RepeatMessage( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_RepeatMessage_to_ReadySleep( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_RepeatMessage_to_NormalOperation( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_NormalOperation_to_RepeatMessage( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_NormalOperation_to_NormalOperation( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_NormalOperation_to_ReadySleep( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_ReadySleep_to_NormalOperation( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_ReadySleep_to_RepeatMessage( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_ReadySleep_to_PrepareBusSleep( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_PrepareBusSleep_to_RepeatMessage( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_PrepareBusSleep_to_BusSleep( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_NetworkMode_to_NetworkMode( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );

/* Additional functions */
static inline uint8_t CanNm_Internal_TxDisable( CanNm_Internal_ChannelType* ChannelInternal );
static inline uint8_t CanNm_Internal_TxEnable( CanNm_Internal_ChannelType* ChannelInternal );
static inline uint8_t CanNm_Internal_TransmitMessage( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline void CanNm_Internal_SetPduCbvBit( const CanNmChannelConfigType* ChannelConf, const uint8_t PduCbvBitPosition );
static inline void CanNm_Internal_ClearPduCbvBit( const CanNmChannelConfigType* ChannelConf, const uint8_t PduCbvBitPosition );
static inline void CanNm_Internal_ClearPduCbv( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal );
static inline uint8_t CanNm_Internal_GetUserDataOffset( const CanNmChannelConfigType* ChannelConf );
static inline uint8_t* CanNm_Internal_GetUserDataPtr( const CanNmChannelConfigType* ChannelConf, uint8_t* MessageSduPtr );
static inline uint8_t CanNm_Internal_GetUserDataLength( const CanNmChannelConfigType* ChannelConf );

/* Mocked Functions */
void CanIf_Transmit(uint16_t TxPduId, const PduInfoType* PduInfoPtr) { printf("Transmitting PDU with ID: %u\n", TxPduId); }
void Nm_NetworkMode(uint8_t channel) { printf("Network Mode on channel: %u\n", channel); }
void Nm_StateChangeNotification(uint8_t channel, Nm_StateType oldState, Nm_StateType newState) { printf("State changed from %u to %u on channel: %u\n", oldState, newState, channel); }
void Nm_RemoteSleepCancellation(uint8_t channel) { printf("Remote Sleep Cancellation on channel: %u\n", channel); }
void PduR_CanNmRxIndication(uint8_t channel, const PduInfoType* PduInfoPtr) { printf("PduR_CanNmRxIndication on channel: %u\n", channel); }

/*====================================================================================================================*\
    Global functions code
\*====================================================================================================================*/

/** @brief CanNm_Init [SWS_CanNm_00208]
 * 
 * Initialize the CanNm module passed as a constant pointer to the function to be assigned to the global CanNm_ConfigPtr.
 * There are some default values to be initialized from the SWS and they are labeled by a comment in the code.
 * ! TODO: Some are not documented in the SWS such as ChannelInternal.
 */
void CanNm_Init(const CanNmGlobalConfigType* canNmConfigPtr)
{
    CanNm_ConfigPtr = canNmConfigPtr;	//[SWS_CanNm_00060]

	for (uint8_t channel = 0; channel < CANNM_CHANNEL_COUNT; channel++) {
		const CanNmChannelConfigType* ChannelConfig = CanNm_ConfigPtr->ChannelConfig[channel];
		CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[channel];

		ChannelInternal->Channel = channel;
		ChannelInternal->Mode = NM_MODE_BUS_SLEEP;														//[SWS_CanNm_00144]
		ChannelInternal->State = NM_STATE_BUS_SLEEP;													//[SWS_CanNm_00141][SWS_CanNm_00094]
		ChannelInternal->Requested = FALSE;																//[SWS_CanNm_00143] // Released means that it's not requested.
		// ChannelInternal->TxEnabled = FALSE;
		// ChannelInternal->RxLastPdu = NO_PDU_RECEIVED;
		ChannelInternal->ImmediateNmTransmissions = 0;
		ChannelInternal->BusLoadReduction = FALSE;														//[SWS_CanNm_00023]
		// ChannelInternal->RemoteSleepInd = FALSE;
		// ChannelInternal->RemoteSleepIndEnabled = CanNm_ConfigPtr->RemoteSleepIndEnabled;
		ChannelInternal->NmPduFilterAlgorithm = FALSE;													//[SWS_CanNm_00403]
		
		//! TESTING: setting the PduNidPosition to enum other than CANNM_PDU_OFF and check the ID.
		if (ChannelConfig->NodeIdEnabled && ChannelConfig->PduNidPosition != CANNM_PDU_OFF) {
			ChannelConfig->TxPdu->TxPduRef->SduDataPtr[ChannelConfig->PduNidPosition] = ChannelConfig->NodeId;//[SWS_CanNm_00013]
		}

		//! TESTING: setting the PduCbvPosition to enum other than CANNM_PDU_OFF and check the control bit vector CBV.
		if (ChannelConfig->PduCbvPosition != CANNM_PDU_OFF) {
			ChannelConfig->TxPdu->TxPduRef->SduDataPtr[ChannelConfig->PduCbvPosition] = 0x00;				//[SWS_CanNm_00085]
		}										

		//! TESTING: check the destUserData given the offset and the length of the payload.
		uint8_t* destUserData = CanNm_Internal_GetUserDataPtr(ChannelConfig, ChannelConfig->UserDataTxPdu->TxUserDataPduRef->SduDataPtr); // We pass the channel config and the payload of the PDU
		uint8_t userDataLength = CanNm_Internal_GetUserDataLength(ChannelConfig);
		memset(destUserData, 0xFF, userDataLength);														//[SWS_CanNm_00025]

		// CanNm_Internal_TimersInit(channel);																//[SWS_CanNm_00061][SWS_CanNm_00033]
	}
	CanNm_Internal.InitStatus = CANNM_INIT;
}

/** @brief CanNm_DeInit [SWS_CanNm_91002]
 * 
 * De-initializes the CanNm module.
 */
void CanNm_DeInit(void)
{
    for (uint8_t channel = 0; channel < CANNM_CHANNEL_COUNT; channel++) {
		CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[channel];
		
		//! TESTING: set the channel state to value other than the bus sleep state.
		if (ChannelInternal->State != NM_STATE_BUS_SLEEP) {
			std::cout << "Channel internal state is not equal to NM_STATE_BUS_SLEEP\n";
			return;
		}
		// CanNm_Internal_TimersInit(channel);
		ChannelInternal->State = NM_STATE_UNINIT;
	}
	CanNm_Internal.InitStatus = CANNM_UNINIT;
}

/** @brief CanNm_PassiveStartUp [SWS_CanNm_00211]
 * 
 * Passive startup of the AUTOSAR CAN NM. It triggers the transition from Bus-Sleep Mode
 *  or Prepare Bus Sleep Mode to the Network Mode in Repeat Message State.
 */
uint8_t CanNm_PassiveStartUp(NetworkHandleType nmChannelHandle)
{
	const CanNmChannelConfigType* ChannelConfig = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];
	uint8_t status = E_OK;

	if (CanNm_ConfigPtr->PassiveModeEnabled && ChannelInternal->Mode != NM_MODE_NETWORK) {				//[SWS_CanNm_00161]
        CanNm_Internal_BusSleep_to_RepeatMessage(ChannelConfig, ChannelInternal);							//[SWS_CanNm_00128][SWS_CanNm_00314][SWS_CanNm_00315]
		status = E_OK;
	} else {
		status = E_NOT_OK;																				//[SWS_CanNm_00147]
	}
	return status;
}




/** @brief CanNm_NetworkRelease [SWS_CanNm_00214]
 * 
 * Release the network, since ECU doesn't have to communicate on the bus.
 */
uint8_t CanNm_NetworkRelease(NetworkHandleType nmChannelHandle)
{
	const CanNmChannelConfigType* ChannelConfig = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	ChannelInternal->Requested = FALSE;	//[SWS_CanNm_00105]

	if (ChannelInternal->Mode == NM_MODE_NETWORK) {
		if (ChannelInternal->State == NM_STATE_NORMAL_OPERATION) {
			CanNm_Internal_NormalOperation_to_ReadySleep(ChannelConf, ChannelInternal);			//[SWS_CanNm_00118]
		}
	}
	return E_OK;
}

/** @brief CanNm_DisableCommunication [SWS_CanNm_00215]
 * 
 * Disable the NM PDU transmission ability due to a ISO14229 Communication Control (28hex) service.
 */
uint8_t CanNm_DisableCommunication(NetworkHandleType nmChannelHandle)
{
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (ChannelInternal->Mode == NM_MODE_NETWORK && !(CanNm_ConfigPtr->PassiveModeEnabled)) {	//[SWS_CanNm_00170]
		return CanNm_Internal_TxDisable(ChannelInternal);
	} else {																					//[SWS_CanNm_00172][SWS_CanNm_00298]
		return E_NOT_OK;
	}
}

/** @brief CanNm_EnableCommunication [SWS_CanNm_00216]
 * 
 * Enable the NM PDU transmission ability due to a ISO14229 Communication Control (28hex) service.
 */
uint8_t CanNm_EnableCommunication(NetworkHandleType nmChannelHandle)
{
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (ChannelInternal->Mode == NM_MODE_NETWORK && !(CanNm_ConfigPtr->PassiveModeEnabled)) {	//[SWS_CanNm_00170]
		if (ChannelInternal->MessageCycleTimer.State == CANNM_TIMER_STOPPED) {					//[SWS_CanNm_00176]
			return CanNm_Internal_TxEnable(ChannelInternal);
		} else {																				//[SWS_CanNm_00177]
			return E_NOT_OK;
		}
	} else {																					//[SWS_CanNm_00295][SWS_CanNm_00297]
		return E_NOT_OK;
	}
}
/** @brief CanNm_SetUserData [SWS_CanNm_00217]
 * 
 * Set user data for NM PDUs transmitted next on the bus.
 */
uint8_t CanNm_SetUserData(NetworkHandleType nmChannelHandle, const uint8_t* nmUserDataPtr)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (CanNm_ConfigPtr->UserDataEnabled && !CanNm_ConfigPtr->ComUserDataSupport) {				//[SWS_CanNm_00158][SWS_CanNm_00327]
		uint8_t* destUserData = CanNm_Internal_GetUserDataPtr(ChannelConf, ChannelConf->UserDataTxPdu->TxUserDataPduRef->SduDataPtr);
		uint8_t userDataLength = CanNm_Internal_GetUserDataLength(ChannelConf);
		memcpy(destUserData, nmUserDataPtr, userDataLength);									//[SWS_CanNm_00159]	
		return E_OK;
	}
	else {
		return E_NOT_OK;
	}
}
/** @brief CanNm_GetUserData [SWS_CanNm_00218]
 * 
 * Get user data out of the most recently received NM PDU.
 */
uint8_t CanNm_GetUserData(NetworkHandleType nmChannelHandle, uint8_t* nmUserDataPtr)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (CanNm_ConfigPtr->UserDataEnabled && ChannelInternal->RxLastPdu != NO_PDU_RECEIVED) {	//[SWS_CanNm_00158]
		uint8_t* srcUserData = CanNm_Internal_GetUserDataPtr(ChannelConf, ChannelConf->RxPdu[ChannelInternal->RxLastPdu]->RxPduRef->SduDataPtr);
		uint8_t userDataLength = CanNm_Internal_GetUserDataLength(ChannelConf);
		memcpy(nmUserDataPtr, srcUserData, userDataLength);										//[SWS_CanNm_00160]
		return E_OK;
	} else {
		return E_NOT_OK;
	}
}
/** @brief CanNm_Transmit [SWS_CanNm_00331]
 * 
 * Requests transmission of a PDU.
 */
uint8_t CanNm_Transmit(PduIdType TxPduId, const PduInfoType* PduInfoPtr)
{
	if (CanNm_ConfigPtr->ComUserDataSupport || CanNm_ConfigPtr->GlobalPnSupport) {				//[SWS_CanNm_00330]
		 CanIf_Transmit(TxPduId, PduInfoPtr);
		 return E_OK;
	} else {
		return E_NOT_OK;
	}
}
/** @brief CanNm_GetNodeIdentifier [SWS_CanNm_00219]
 * 
 * Get node identifier out of the most recently received NM PDU.
 */
uint8_t CanNm_GetNodeIdentifier(NetworkHandleType nmChannelHandle, uint8_t*nmNodeIdPtr)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (ChannelConf->PduNidPosition != CANNM_PDU_OFF) {
		if (ChannelInternal->RxLastPdu != NO_PDU_RECEIVED) {
			uint8_t *pduNidPtr = ChannelConf->RxPdu[ChannelInternal->RxLastPdu]->RxPduRef->SduDataPtr;//[SWS_CanNm_00132]
			pduNidPtr += ChannelConf->PduNidPosition;
			*nmNodeIdPtr = *pduNidPtr;
			return E_OK;
		} else {
			return E_NOT_OK;
		}
	} else {
		return E_NOT_OK;
	}
}
/** @brief CanNm_GetNodeIdentifier [SWS_CanNm_00220]
 * 
 * Get node identifier configured for the local node.
 */
uint8_t CanNm_GetLocalNodeIdentifier(NetworkHandleType nmChannelHandle, uint8_t* nmNodeIdPtr)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];

	*nmNodeIdPtr = ChannelConf->NodeId;	//[SWS_CanNm_00133]
	return E_OK;
}
/** @brief CanNm_RepeatMessageRequest [SWS_CanNm_00221]
 * 
 * Set Repeat Message Request Bit for NM PDUs transmitted next on the bus.
 */
uint8_t CanNm_RepeatMessageRequest(NetworkHandleType nmChannelHandle)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (ChannelConf->PduCbvPosition != CANNM_PDU_OFF) {
		if (ChannelInternal->State == NM_STATE_READY_SLEEP) {
			if (ChannelConf->NodeDetectionEnabled) {								//[SWS_CanNm_00112]
				CanNm_Internal_SetPduCbvBit(ChannelConf, REPEAT_MESSAGE_REQUEST);	//[SWS_CanNm_00113]
				CanNm_Internal_ReadySleep_to_RepeatMessage(ChannelConf, ChannelInternal);
				return E_OK;
			} else {
				return E_NOT_OK;
			}
		} else if (ChannelInternal->State == NM_STATE_NORMAL_OPERATION) {
			if (ChannelConf->NodeDetectionEnabled) {								//[SWS_CanNm_00120]
				CanNm_Internal_SetPduCbvBit(ChannelConf, REPEAT_MESSAGE_REQUEST);	//[SWS_CanNm_00121]
				CanNm_Internal_NormalOperation_to_RepeatMessage(ChannelConf, ChannelInternal);
				return E_OK;
			} else {
				return E_NOT_OK;
			}
		} else {
			return E_NOT_OK;
		}
	} else {
		return E_NOT_OK;
	}
}

/** @brief CanNm_GetPduData [SWS_CanNm_00222]
 * 
 * Get the whole PDU data out of the most recently received NM PDU.
 */
uint8_t CanNm_GetPduData(NetworkHandleType nmChannelHandle, uint8_t* nmPduDataPtr)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (ChannelConf->NodeDetectionEnabled || CanNm_ConfigPtr->UserDataEnabled || ChannelConf->NodeIdEnabled) {	//[SWS_CanNm_00138]
		if (ChannelInternal->RxLastPdu != NO_PDU_RECEIVED) {
			memcpy(nmPduDataPtr, ChannelConf->RxPdu[0]->RxPduRef->SduDataPtr, ChannelConf->RxPdu[0]->RxPduRef->SduLength);
			return E_OK;
		} else {
			return E_NOT_OK;
		}

	} else {
		return E_NOT_OK;
	}
}

/** @brief CanNm_GetState [SWS_CanNm_00223]
 * 
 * Returns the state and the mode of the network management.
 */
uint8_t CanNm_GetState(NetworkHandleType nmChannelHandle, Nm_StateType* nmStatePtr, Nm_ModeType* nmModePtr)
{
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	*nmStatePtr = ChannelInternal->State;												//[SWS_CanNm_00091]
	*nmModePtr = ChannelInternal->Mode;
	return E_OK;    
}

/** @brief CanNm_RequestBusSynchronization [SWS_CanNm_00226]
 * 
 * Request bus synchronization.
 */
uint8_t CanNm_RequestBusSynchronization(NetworkHandleType nmChannelHandle)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

    if (!CanNm_ConfigPtr->PassiveModeEnabled) {											//[SWS_CanNm_00130]
		if (ChannelInternal->Mode == NM_MODE_NETWORK && ChannelInternal->TxEnabled) {	//[SWS_CanNm_00181][SWS_CanNm_00187]
			CanNm_Internal_TransmitMessage(ChannelConf, ChannelInternal);
			return E_OK;			
		} else {
			return E_NOT_OK;
		}
	} else {
		return E_NOT_OK;
	}
}

/** @brief CanNm_CheckRemoteSleepIndication [SWS_CanNm_00227]
 * 
 * Check if remote sleep indication takes place or not.
 */
uint8_t CanNm_CheckRemoteSleepIndication(NetworkHandleType nmChannelHandle, bool* nmRemoteSleepIndPtr)
{
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (ChannelInternal->State != NM_STATE_BUS_SLEEP && ChannelInternal->State != NM_STATE_PREPARE_BUS_SLEEP 
		&& ChannelInternal->State != NM_STATE_REPEAT_MESSAGE) {							//[SWS_CanNm_00154]
		*nmRemoteSleepIndPtr = ChannelInternal->RemoteSleepInd;
		return E_OK;
	} else {
		return E_NOT_OK;
	}
}

/** @brief CanNm_SetSleepReadyBit [SWS_CanNm_00338]
 * 
 * Set the NM Coordinator Sleep Ready bit in the Control Bit Vector
 */
uint8_t CanNm_SetSleepReadyBit(NetworkHandleType nmChannelHandle,bool nmSleepReadyBit)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[nmChannelHandle];
    CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

	if (ChannelConf->PduCbvPosition != CANNM_PDU_OFF && CanNm_ConfigPtr->CoordinationSyncSupport) {	//[SWS_CanNm_00342]
		CanNm_Internal_SetPduCbvBit(ChannelConf, NM_COORDINATOR_SLEEP_READY_BIT);
		CanNm_Internal_TransmitMessage(ChannelConf, ChannelInternal);
		return E_OK;
	} else {
		return E_NOT_OK;
	}
}

/** @brief CanNm_TxConfirmation [SWS_CanNm_00228]
 * 
 * The lower layer communication interface module confirms the transmission of a PDU, or the failure to transmit a PDU.
 */
void CanNm_TxConfirmation(PduIdType TxPduId, uint8_t result)
{
	const CanNmChannelConfigType* ChannelConfig = CanNm_ConfigPtr->ChannelConfig[TxPduId];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[TxPduId];

	if (result == E_OK) {
		CanNm_Internal_TimerStart(&ChannelInternal->TimeoutTimer, ChannelConfig->TimeoutTime);				//[SWS_CanNm_00099]
	}
	if (CanNm_ConfigPtr->ComUserDataSupport) {
		PduR_CanNmRxIndication(TxPduId, ChannelConfig->TxPdu->TxPduRef); 						//[SWS_CanNm_00329]
	}
}


/** @brief CanNm_ConfirmPnAvailability [SWS_CanNm_00344]
 * 
 * Enables the PN filter functionality on the indicated NM channel.
 * Availability: The API is only available if CanNmGlobalPnSupport is TRUE.
 */
void CanNm_ConfirmPnAvailability(NetworkHandleType nmChannelHandle)
{
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[nmChannelHandle];

    if (CanNm_ConfigPtr->GlobalPnSupport) {
		ChannelInternal->NmPduFilterAlgorithm = TRUE;
	}
}

/** @brief CanNm_TriggerTransmit [SWS_CanNm_91001]
 * 
 * Within this API, the upper layer module (called module) shall check whether the 
 * available data fits into the buffer size reported by PduInfoPtr->SduLength.
 * If it fits, it shall copy its data into the buffer provided by PduInfoPtr->SduDataPtr 
 * and update the length of the actual copied data in PduInfoPtr->SduLength. 
 * If not, it returns E_NOT_OK without changing PduInfoPtr.
 */
uint8_t CanNm_TriggerTransmit(PduIdType TxPduId, PduInfoType* PduInfoPtr)
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[TxPduId];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[TxPduId];

	if (ChannelConf->TxPdu->TxPduRef->SduLength <= PduInfoPtr->SduLength) {
		memcpy(PduInfoPtr->SduDataPtr, ChannelConf->TxPdu->TxPduRef->SduDataPtr, ChannelConf->TxPdu->TxPduRef->SduLength);	//[SWS_CanNm_00351]
		PduInfoPtr->SduLength = ChannelConf->TxPdu->TxPduRef->SduLength;
		return E_OK;
	} else {
		return E_NOT_OK;
	}
}

/************************/
/* Timer functions */
/************************/
/*
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
*/
static inline void CanNm_Internal_TimerStart( CanNm_Timer* Timer, uint32 timeoutValue )
{
	Timer->State = CANNM_TIMER_STARTED;
	Timer->TimeLeft = timeoutValue;	//[SWS_CanNm_00206]
}

static inline void CanNm_Internal_TimerResume( CanNm_Timer* Timer )
{
	Timer->State = CANNM_TIMER_STARTED;
}

static inline void CanNm_Internal_TimerStop( CanNm_Timer* Timer )
{
	Timer->State = CANNM_TIMER_STOPPED;
}

static inline void CanNm_Internal_TimerReset( CanNm_Timer* Timer, uint32 timeoutValue )
{
	Timer->State = CANNM_TIMER_STOPPED;
	Timer->TimeLeft = timeoutValue;
}

/***************************/
/* State machine functions */
/***************************/
static inline void CanNm_Internal_NormalOperation_to_ReadySleep( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal )
{
	ChannelInternal->Mode = NM_MODE_NETWORK;
	ChannelInternal->State = NM_STATE_READY_SLEEP;
	ChannelInternal->TxEnabled = FALSE;																//[SWS_CanNm_00108]
	if (CanNm_ConfigPtr->StateChangeIndEnabled) {
		Nm_StateChangeNotification(ChannelInternal->Channel, NM_STATE_NORMAL_OPERATION, NM_STATE_READY_SLEEP);
	}
}

/************************/
/* Additional functions */
/************************/
static inline uint8_t CanNm_Internal_GetUserDataOffset( const CanNmChannelConfigType* ChannelConfig )
{
	uint8_t userDataPos = 0;
	userDataPos += (ChannelConfig->PduNidPosition == CANNM_PDU_OFF) ? 0 : 1;
	userDataPos += (ChannelConfig->PduCbvPosition == CANNM_PDU_OFF) ? 0 : 1;
	return userDataPos;
}

static inline uint8_t* CanNm_Internal_GetUserDataPtr( const CanNmChannelConfigType* ChannelConfig, uint8_t* MessageSduPtr )
{
	uint8_t userDataOffset = CanNm_Internal_GetUserDataOffset(ChannelConfig);
	return &MessageSduPtr[userDataOffset];
}

static inline uint8_t CanNm_Internal_GetUserDataLength( const CanNmChannelConfigType* ChannelConfig )
{
	uint8_t userDataOffset = CanNm_Internal_GetUserDataOffset(ChannelConfig);
	return ChannelConfig->UserDataTxPdu->TxUserDataPduRef->SduLength - userDataOffset;
}

static inline void CanNm_Internal_BusSleep_to_RepeatMessage( const CanNmChannelConfigType* ChannelConfig, CanNm_Internal_ChannelType* ChannelInternal )
{
	ChannelInternal->Mode = NM_MODE_NETWORK;
	ChannelInternal->State = NM_STATE_REPEAT_MESSAGE;
	ChannelInternal->BusLoadReduction = FALSE;															//[SWS_CanNm_00156]
	CanNm_Internal_TimerStart(&ChannelInternal->TimeoutTimer, ChannelConfig->TimeoutTime);				//[SWS_CanNm_00096]
	CanNm_Internal_TimerStart(&ChannelInternal->RepeatMessageTimer, ChannelConfig->RepeatMessageTime);	//[SWS_CanNm_00102]
	CanNm_Internal_TimerStart(&ChannelInternal->MessageCycleTimer, ChannelConfig->MsgCycleOffset);		//[SWS_CanNm_00100]
	
	//! Fake functions. Can be tested using mock testing.
	Nm_NetworkMode(ChannelInternal->Channel);															//[SWS_CanNm_00097]
	if (CanNm_ConfigPtr->StateChangeIndEnabled) {
		Nm_StateChangeNotification(ChannelInternal->Channel, NM_STATE_BUS_SLEEP, NM_STATE_REPEAT_MESSAGE); //[SWS_CanNm_00166]
	}
}

static inline void CanNm_Internal_SetPduCbvBit( const CanNmChannelConfigType* ChannelConfig, const uint8_t PduCbvBitPosition )
{
	ChannelConfig->TxPdu->TxPduRef->SduDataPtr[ChannelConfig->PduCbvPosition] |= (1 << PduCbvBitPosition);
}

static inline void CanNm_Internal_MessageCycleTimerExpiredCallback( void* Timer, const uint8_t channel )
{
	const CanNmChannelConfigType* ChannelConfig = CanNm_ConfigPtr->ChannelConfig[channel];
	CanNm_Internal_ChannelType* ChannelInternal = &CanNm_Internal.Channels[channel];
	uint8_t txStatus = E_OK;
	static uint8_t lastTxStatus;

	if ((ChannelInternal->State == NM_STATE_REPEAT_MESSAGE) || (ChannelInternal->State == NM_STATE_NORMAL_OPERATION)) {
		txStatus = CanNm_Internal_TransmitMessage(ChannelConfig, ChannelInternal);					//[SWS_CanNm_00032][SWS_CanNm_00087]
		if (ChannelInternal->ImmediateTransmissions) {
			if (txStatus == E_NOT_OK) {
				if (lastTxStatus == E_NOT_OK) {
					ChannelInternal->ImmediateTransmissions = 0;
					CanNm_Internal_TimerStart((CanNm_Timer*)Timer, ChannelConfig->MsgCycleTime);		//[SWS_CanNm_00335]
				} else {
					CanNm_Internal_TimerStart((CanNm_Timer*)Timer, 1);								//[SWS_CanNm_00335]
				}
			} else {
				CanNm_Internal_TimerStart((CanNm_Timer*)Timer, ChannelConfig->ImmediateNmCycleTime);	//[SWS_CanNm_00334]
				ChannelInternal->ImmediateTransmissions--;
			}
		} else {
			CanNm_Internal_TimerStart((CanNm_Timer*)Timer, ChannelConfig->MsgCycleTime);				//[SWS_CanNm_00040]
		}
	}
	lastTxStatus = txStatus;
}

static inline uint8_t CanNm_Internal_TransmitMessage( const CanNmChannelConfigType* ChannelConfig, CanNm_Internal_ChannelType* ChannelInternal )
{
	if (ChannelInternal->TxEnabled) {
		CanIf_Transmit(ChannelConfig->TxPdu->TxConfirmationPduId, ChannelConfig->TxPdu->TxPduRef);	//[SWS_CanNm_00032]
		return E_NOT_OK;
	} else {
		return E_OK;
	}
}

static inline uint8_t CanNm_Internal_TxDisable( CanNm_Internal_ChannelType* ChannelInternal )
{
	ChannelInternal->TxEnabled = FALSE;
	if (CanNm_ConfigPtr->RemoteSleepIndEnabled) {
		ChannelInternal->RemoteSleepIndEnabled = FALSE;												//[SWS_CanNm_00175]
		CanNm_Internal_TimerStop(&ChannelInternal->RemoteSleepIndTimer);
	}								
	CanNm_Internal_TimerStop(&ChannelInternal->MessageCycleTimer);									//[SWS_CanNm_00051][SWS_CanNm_00173]
	CanNm_Internal_TimerStop(&ChannelInternal->TimeoutTimer);										//[SWS_CanNm_00174]
	return E_OK;
}

static inline uint8_t CanNm_Internal_TxEnable( CanNm_Internal_ChannelType* ChannelInternal )
{
	const CanNmChannelConfigType* ChannelConf = CanNm_ConfigPtr->ChannelConfig[ChannelInternal->Channel];

	if (!CanNm_ConfigPtr->PassiveModeEnabled) {
		ChannelInternal->TxEnabled = TRUE;															//[SWS_CanNm_00237]
		if (CanNm_ConfigPtr->RemoteSleepIndEnabled) {
			ChannelInternal->RemoteSleepIndEnabled = TRUE;											//[SWS_CanNm_00180]
			CanNm_Internal_TimerStart(&ChannelInternal->RemoteSleepIndTimer, ChannelConf->RemoteSleepIndTime);
		}											
		CanNm_Internal_TimerStart(&ChannelInternal->MessageCycleTimer, 1);							//[SWS_CanNm_00178]
		return E_OK;
	} else {
		return E_NOT_OK;
	}
}

static inline void CanNm_Internal_ReadySleep_to_RepeatMessage( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal )
{
	ChannelInternal->Mode = NM_MODE_NETWORK;
	ChannelInternal->State = NM_STATE_REPEAT_MESSAGE;
	if (!CanNm_ConfigPtr->PassiveModeEnabled) {
		ChannelInternal->TxEnabled = TRUE;
	}
	ChannelInternal->BusLoadReduction = FALSE;														//[SWS_CanNm_00156]
	CanNm_Internal_TimerStart(&ChannelInternal->RepeatMessageTimer, ChannelConf->RepeatMessageTime);//[SWS_CanNm_00102]
	CanNm_Internal_TimerStart(&ChannelInternal->MessageCycleTimer, ChannelConf->MsgCycleOffset);	//[SWS_CanNm_00100]
	if (ChannelInternal->RemoteSleepInd) {
		ChannelInternal->RemoteSleepInd = FALSE;
		Nm_RemoteSleepCancellation(ChannelInternal->Channel);										//[SWS_CanNm_00151]
	}
	if (CanNm_ConfigPtr->StateChangeIndEnabled) {
		Nm_StateChangeNotification(ChannelInternal->Channel, NM_STATE_READY_SLEEP, NM_STATE_REPEAT_MESSAGE);
	}
}
static inline void CanNm_Internal_NormalOperation_to_RepeatMessage( const CanNmChannelConfigType* ChannelConf, CanNm_Internal_ChannelType* ChannelInternal )
{
	ChannelInternal->Mode = NM_MODE_NETWORK;
	ChannelInternal->State = NM_STATE_REPEAT_MESSAGE;
	ChannelInternal->BusLoadReduction = FALSE;														//[SWS_CanNm_00156]
	CanNm_Internal_TimerStart(&ChannelInternal->RepeatMessageTimer, ChannelConf->RepeatMessageTime);//[SWS_CanNm_00102]
	CanNm_Internal_TimerStart(&ChannelInternal->MessageCycleTimer, ChannelConf->MsgCycleOffset);	//[SWS_CanNm_00100]
	if (ChannelInternal->RemoteSleepInd) {
		ChannelInternal->RemoteSleepInd = FALSE;
		Nm_RemoteSleepCancellation(ChannelInternal->Channel);										//[SWS_CanNm_00151]
	}
	if (CanNm_ConfigPtr->StateChangeIndEnabled) {
		Nm_StateChangeNotification(ChannelInternal->Channel, NM_STATE_NORMAL_OPERATION, NM_STATE_REPEAT_MESSAGE);
	}
}