/* Static Code

/* CanNm.h static file */
#include "CanNm_PBcfg.h"

extern void CanNm_Init ( const CanNm_ConfigType * const config );

/* CanNm_Types.h static file */
typedef struct
{
    /* struct member types */
} CanNm_ConfigType;


/* CanNm_Init.c static file */
void CanNm_Init ( const CanNm_ConfigType * const config )
{
#if(CANNM_DEV_ERROR_DETECT == STD_ON)
    /* Report Errors */
#endif

}

/* Generated Code */

/* .h generated file */
#define   CANNM_DEV_ERROR_DETECT  (STD_ON) 

extern const CanNm_ConfigType CanNmConfig;


/* .c generated file */
const CanNm_ConfigType CanNmConfig =
{
    /* Initialization based on configurations */
};

/* main.c */

#include "CanNm.h"

int main()
{
    CanNm_Init (&CanNmConfig);
    /* Test Cases */
}
