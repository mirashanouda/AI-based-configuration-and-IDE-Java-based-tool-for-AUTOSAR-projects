import xml.etree.ElementTree as ET
from lxml import etree
import random
import et_init
import tagfile
import parameter
import container
import items
import parsing


# -------------------------------------------------------------------------
et_elements = []
et_subelements = []
AS = et_init.initialize(et_elements)
children = [[] for _ in range(parsing.max_nodes)]

for i in range(7):
    if parsing.par[i] != -1:
        children[parsing.par[i]].append(i)

enumer_value = ['CANNM_PDU_BYTE_0', 'CANNM_PDU_BYTE_1', 'CANMN_PDU_OFF']
def get_default_value(partype):
  if partype == 'INTEGER':
    return 0
  elif partype == 'FLOAT':
    return 0
  elif partype == 'BOOLEAN':
    return "False"
  elif partype == 'ENUMERATION':
    return enumer_value[0]
# -------------------------------------------------------------------------

  
# -------------------------------------------------------------------------
class Set:
  def __init__(self):
    self.num_containers = 1
    self.children = [] # children sets
    self.parse_index = 0
    self.parameters = [] # param items in the set, according to the parsing, but the values changed
    self.par_set = -1
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def bslashts(num):
  res = ""
  for i in range(num):
    res += "\\t"
  return res
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def gen_parameter(container_idx, parameter_idx): # Returns a pair (param_name, param_value) given the parameter index
  param_name = ""
  param_value = ""

  param = parsing.container_parameters[container_idx][parameter_idx]
  
  if str(param.data_type) == 'INTEGER':
      res_value = random.randint(int(param.start_range), int(param.end_range))

  elif str(param.data_type) == 'FLOAT':
      low = int((float(param.start_range) * 1000.0))
      up = 0
      if float(param.end_range) == float('inf'):
        up = 65535
      else:
        up = int((float(param.end_range) * 1000))
      res_value = random.randint(low, up)
      res_value = res_value / 1000.0
      
  elif param.data_type == 'BOOLEAN':
      which = random.randint(0, 1)        
      if which == 1:
        res_value = "True"
      else:
        res_value = "False"

  elif str(param.data_type) == 'ENUMERATION':
      tempidx = random.randint(0, 2)
      res_value = enumer_value[tempidx]
  
  param_name = str(param.def_name)
  param_value = str(res_value)
  return [param_name, param_value]
# -------------------------------------------------------------------------  


# -------------------------------------------------------------------------
def gen_rand_parameter(container_idx): 
  # Returns a pair (param_name, param_value)
  selected = int(random.randint(0, len(parsing.container_parameters[container_idx]) - 1))
  return gen_parameter(container_idx, selected)
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def gen_rand_parameters(container_idx, num_params):
  # Returns a list of pairs (param_name, param_value)
  num_params = int(min(num_params, len(parsing.container_parameters[container_idx])))
  whole = []
  for i in range(len(parsing.container_parameters[container_idx])):
    whole.append(i)
  
  # generating a random permutation
  selected = []
  back_idx = len(whole) - 1
  for _ in range(len(whole)):
    sel_idx = random.randint(0, back_idx)
    selected.append(whole[sel_idx])
    
    # swap(whole[sel_idx], whole[back_idx])
    temp = whole[sel_idx]
    whole[sel_idx] = whole[back_idx]
    whole[back_idx] = temp
    
    back_idx -= 1
    
  res = []
  for i in range(num_params):
    res.append(gen_parameter(container_idx, selected[i]))
  
  return res
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
container_dict = {} # container_dict[container_name] = container_index
for i in range(7):
  container_dict.update({str(parsing.container_def[i].name): i})
# -------------------------------------------------------------------------