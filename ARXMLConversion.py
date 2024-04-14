#!/usr/bin/env python
from lxml import etree
from collections import defaultdict
import sys
import os 

# Global Varialbles
cont_cnt = defaultdict(int)

# Parsing the ARXML
def get_container(main_cont, my_name):
  # Getting the params and save them in a map
  param_mp = {} # the parameter and its value
  params = main_cont.findall('./autosar:PARAMETER-VALUES', namespaces=namespaces)
  if (len(params) > 0):
    params = params[0]
    for param in params.findall('./autosar:ECUC-NUMERICAL-PARAM-VALUE', namespaces=namespaces):
      k = param.find('./autosar:DEFINITION-REF', namespaces=namespaces).text
      k = k.split('/')[-1]
      v = param.find('./autosar:VALUE', namespaces=namespaces).text
      param_mp[k] = v

  # Getting the subcontainers
  cont_mp = {}
  conts = main_cont.findall('./autosar:SUB-CONTAINERS', namespaces=namespaces)
  if (len(conts) > 0):
    conts = conts[0]
    for cont in conts.findall('./autosar:ECUC-CONTAINER-VALUE', namespaces=namespaces):
      k = cont.find('./autosar:SHORT-NAME', namespaces=namespaces).text
      k = k.split('_', 1)[0]
      cont_cnt[k] += 1
      cont_name = k + "_" + str(cont_cnt[k])
      if k not in cont_mp:
            cont_mp[k] = []
      cont_mp[k].append(cont_name)
      get_container(cont, cont_name)

  # Printing the instantiation of the object
  f = 0
  struct_name = my_name.split('_', 1)[0]
  if struct_name == "CanNmGlobalConfig":
    output_string = f"CanNmGlobalConfig* p{struct_name} = &({struct_name}) {{ \ \n"  # to make only the global config as a pointer
  else:
    output_string = f"#define {my_name} &({struct_name}) {{ \ \n"
  # Params
  for key, value in param_mp.items():
    if f == 0:
      f = 1
    else:
      output_string += ", \ \n"
    if type(value) == str: # To convert Ture and False to true and false
      value = value.lower()
    output_string += "\t\t\t." + key[5:] + " = " + value
  
  # Containers
  for key, values in cont_mp.items():
    if f == 0:
      f = 1
    else:
      output_string += ", \\"  # Subsequent items start with comma
    output_string += "\n\t\t\t.c_" + key + " = {" # the c_ stands for a container
    fp = 0
    for value in values:
      if fp == 0:
        fp = 1
      else:
        output_string += ", "        
      output_string += value
        
    output_string += " } "

  output_string += " }"

  if struct_name == "CanNmGlobalConfig":
    output_string += ";"  # to make only the global config as a pointer
    
  output_string += "\n\n"

  with open("Generated_Code/Initialization.h", "a") as file:
      file.write(output_string)
      

# Writing the header
def init():
  text = '''/*====================================================================================================================*\\
    Include headers
\\*====================================================================================================================*/
/* [SWS_CanNm_00326] */
#include "CanNM.h"

/*====================================================================================================================*\\
    Object Initialization
\\*====================================================================================================================*/

'''

# Open the file in write mode. If the file does not exist, it will be created.
  if not os.path.exists('Generated_Code'): 
    os.mkdir('Generated_Code') 
  with open("Generated_Code/Initialization.h", "w") as file:
      file.write(text)


# The main fucntion
file_path = sys.argv[1]
print(file_path)
# Parse the ARXML file
tree = etree.parse(file_path)
root = tree.getroot()

# Define the namespaces used in the XML document
namespaces = {'autosar': 'http://autosar.org/schema/r4.0'}

# Navigate to each ELEMENTS element
all_containers = root.findall('.//autosar:CONTAINERS', namespaces=namespaces)
init()
for elements in all_containers:
    ecuc_container_values = elements.findall('./autosar:ECUC-CONTAINER-VALUE', namespaces=namespaces)
    for ecuc_container in ecuc_container_values:
      k = ecuc_container.find('./autosar:SHORT-NAME', namespaces=namespaces).text
      cont_cnt[k] += 1
      cont_name = k + "_" + str(cont_cnt[k])
      get_container(ecuc_container, cont_name)