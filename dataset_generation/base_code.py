import sys
import os

def go_back(directory):
	return os.path.abspath(os.path.join(directory, '..'))

cwd = os.getcwd()

from lxml import etree
import random

cwd = go_back(cwd)
cwd += '/global_dependencies'
sys.path.insert(0, cwd)
import items
import global_vars

cwd = go_back(cwd)
cwd += '/parsing'
sys.path.insert(0, cwd)
import parsing

# -------------------------------------------------------------------------
def bslashts(num):
  res = ""
  for i in range(num):
    res += "\\t"
  return res
# -------------------------------------------------------------------------

enumer_value = ['CANNM_PDU_BYTE_0', 'CANNM_PDU_BYTE_1', 'CANMN_PDU_OFF']
# -------------------------------------------------------------------------
def gen_parameter(container_idx, parameter_idx): # Returns a pair (param_name, param_value) given the parameter index
  param_name = ""
  param_value = ""

  param = global_vars.container_parameters[container_idx][parameter_idx]
  
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
  selected = int(random.randint(0, len(global_vars.container_parameters[container_idx]) - 1))
  return gen_parameter(container_idx, selected)
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def gen_rand_parameters(container_idx, num_params):
  # Returns a list of pairs (param_name, param_value)
  num_params = int(min(num_params, len(global_vars.container_parameters[container_idx])))
  whole = []
  for i in range(len(global_vars.container_parameters[container_idx])):
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
global_vars.container_dict = {} # container_dict[container_name] = container_index
for i in range(7):
  global_vars.container_dict.update({str(global_vars.container_def[i].name): i})
# -------------------------------------------------------------------------



# -------------------------------------------------------------------------
def create_subcontainers(container_idx, depth, max_rand, is_first, is_last, in_each):
  yaml_extra = ""
  english_extra = ""
  yaml_extra += bslashts(depth) + "- Container:\\n"
  yaml_extra += bslashts(depth + 1) + "- Name: " + global_vars.container_def[container_idx].name + "\\n"
  num = random.randint(1, max_rand)
  if is_first == 0:
    english_extra += " and"
  english_extra += " create "
  english_extra += str(num)
  english_extra += " "
  english_extra += global_vars.container_def[container_idx].name
  english_extra += " containers"
  if is_last == 1:
    english_extra += ". "
  if in_each == 1:
    english_extra += "In each,"
  yaml_extra += bslashts(depth + 1) + "- Multiplicity: " + str(num) + "\\n"
  return [yaml_extra, english_extra]
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def create_single_subcontainer(container_idx, depth, is_first, is_last, in_each):
    yaml_extra = ""
    english_extra = ""
    yaml_extra += bslashts(depth) + "- Container:\\n"
    yaml_extra += bslashts(depth + 1) + "- Name: " + global_vars.container_def[container_idx].name + "\\n"
    yaml_extra += bslashts(depth + 1) + "- Multiplicity: 1\\n"
    if is_first == 0:
        english_extra += " and"
    english_extra += " create a "
    english_extra += global_vars.container_def[container_idx].name
    english_extra += " container"
    if is_last == 1:
        english_extra += ". "
    if in_each == 1:
      english_extra += "In each,"
    return [yaml_extra, english_extra]
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def write_parameter(depth, gen_param, is_first, is_last):
  yaml_extra = ""
  english_extra = ""
  if is_first == 0:
    english_extra += " and"
  english_extra += " set the value of " + gen_param[0] + " to " + gen_param[1]
  
  if is_last == 1:
    english_extra += ". "
  
  yaml_extra += bslashts(depth) + "- Parameter:\\n"
  yaml_extra += bslashts(depth + 1) + "Name: " + gen_param[0] + "\\n"
  yaml_extra += bslashts(depth + 1) + "Value: " + gen_param[1] + "\\n"
  return [yaml_extra, english_extra]
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def create_random_parameters(container_idx, depth, is_lastt, max_params):
  yaml_extra = ""
  english_extra = ""
  num_params = random.randint(0, max_params)
  generated_params = gen_rand_parameters(container_idx, num_params)
  
  counter = 0
  ll = len(generated_params)
  for gen_param in generated_params:

    is_first = 0
    is_last = 0
    if counter == 0:
      is_first = 1
    if counter == ll - 1:
      is_last = is_lastt

    listt = write_parameter(depth, gen_param, is_first, is_last)

    yaml_extra += listt[0]
    english_extra += listt[1]
    counter += 1
  
  return [yaml_extra, english_extra]
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def create_exact_parameters(container_idx, depth, is_lastt, num_params):
  yaml_extra = ""
  english_extra = ""
  generated_params = gen_rand_parameters(container_idx, num_params)
  
  counter = 0
  ll = len(generated_params)
  for gen_param in generated_params:

    is_first = 0
    is_last = 0
    if counter == 0:
      is_first = 1
    if counter == ll - 1:
      is_last = is_lastt

    listt = write_parameter(depth, gen_param, is_first, is_last)

    yaml_extra += listt[0]
    english_extra += listt[1]
    counter += 1
  
  return [yaml_extra, english_extra]
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def generate_global_parameter():
  will_create = random.randint(0, 1)
  yaml_extra = ""
  english_extra = ""
  if int(will_create) == 1:
    param = gen_rand_parameter(int(global_vars.container_dict["CanNmGlobalConfig"]))
    english_extra += "In the CanNmGlobalConfig container, set the value of " + param[0] + " to " + param[1] + ". "
    yaml_extra += bslashts(2) + "- Parameter:\\n"
    yaml_extra += bslashts(3) + "Name: " + param[0] + "\\n"
    yaml_extra += bslashts(3) + "Value: " + param[1] + "\\n"
    english_extra += "Also," # to prepare for the creation of CanNmChannelConfig containers
  else:
    english_extra += "In the CanNmGlobalConfig container," # to prepare for the creation of CanNmChannelConfig containers
  return [yaml_extra, english_extra]
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def create_exact_parameters_parenthesis(container_idx, depth, num_params):
  yaml_extra = ""
  english_extra = ""
  generated_params = gen_rand_parameters(container_idx, num_params)
  
  english_extra += " (in each,"
  counter = 0
  for gen_param in generated_params:

    is_first = 0
    if counter == 0:
      is_first = 1

    listt = write_parameter(depth, gen_param, is_first, 0)

    yaml_extra += listt[0]
    english_extra += listt[1]
    counter += 1
  
  english_extra += ")"
  
  return [yaml_extra, english_extra]
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def create_random_parameters_parenthesis(container_idx, depth, num_params):
  yaml_extra = ""
  english_extra = ""
  num = random.randint(1, num_params)
  generated_params = gen_rand_parameters(container_idx, num)
  
  english_extra += " ("
  counter = 0
  for gen_param in generated_params:

    is_first = 0
    if counter == 0:
      is_first = 1

    listt = write_parameter(depth, gen_param, is_first, 0)

    yaml_extra += listt[0]
    english_extra += listt[1]
    counter += 1
  
  english_extra += ")"
  
  return [yaml_extra, english_extra]
# -------------------------------------------------------------------------


# -------------------------------------------------------------------------
def create_random_parameters_no_parenthesis(container_idx, depth, num_params):
  yaml_extra = ""
  english_extra = ""
  num = random.randint(1, num_params)
  generated_params = gen_rand_parameters(container_idx, num)
  
  counter = 0
  for gen_param in generated_params:

    is_first = 0
    if counter == 0:
      is_first = 1

    listt = write_parameter(depth, gen_param, is_first, 0)

    yaml_extra += listt[0]
    english_extra += listt[1]
    counter += 1
  
  return [yaml_extra, english_extra]
# -------------------------------------------------------------------------