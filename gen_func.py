import xml.etree.ElementTree as ET
from lxml import etree
import random
import items
import parsing
import base_code


def create_subcontainers(container_idx, depth, max_rand, is_first, is_last, in_each):
  yaml_extra = ""
  english_extra = ""
  yaml_extra += base_code.bslashts(depth) + "- Container:\\n"
  yaml_extra += base_code.bslashts(depth + 1) + "- Name: " + parsing.container_def[container_idx].name + "\\n"
  num = random.randint(1, max_rand)
  if is_first == 0:
    english_extra += " and"
  english_extra += " create "
  english_extra += str(num)
  english_extra += " "
  english_extra += parsing.container_def[container_idx].name
  english_extra += " containers"
  if is_last == 1:
    english_extra += ". "
  if in_each == 1:
    english_extra += "In each,"
  yaml_extra += base_code.bslashts(depth + 1) + "- Multiplicity: " + str(num) + "\\n"
  return [yaml_extra, english_extra]



def create_single_subcontainer(container_idx, depth, is_first, is_last, in_each):
    yaml_extra = ""
    english_extra = ""
    yaml_extra += base_code.bslashts(depth) + "- Container:\\n"
    yaml_extra += base_code.bslashts(depth + 1) + "- Name: " + parsing.container_def[container_idx].name + "\\n"
    yaml_extra += base_code.bslashts(depth + 1) + "- Multiplicity: 1\\n"
    if is_first == 0:
        english_extra += " and"
    english_extra += " create a "
    english_extra += parsing.container_def[container_idx].name
    english_extra += " container"
    if is_last == 1:
        english_extra += ". "
    if in_each == 1:
      english_extra += "In each,"
    return [yaml_extra, english_extra]




def write_parameter(depth, gen_param, is_first, is_last):
  yaml_extra = ""
  english_extra = ""
  if is_first == 0:
    english_extra += " and"
  english_extra += " set the value of " + gen_param[0] + " to " + gen_param[1]
  
  if is_last == 1:
    english_extra += ". "
  
  yaml_extra += base_code.bslashts(depth) + "- Parameter:\\n"
  yaml_extra += base_code.bslashts(depth + 1) + "Name: " + gen_param[0] + "\\n"
  yaml_extra += base_code.bslashts(depth + 1) + "Value: " + gen_param[1] + "\\n"
  return [yaml_extra, english_extra]



def create_random_parameters(container_idx, depth, is_lastt, max_params):
  yaml_extra = ""
  english_extra = ""
  num_params = random.randint(0, max_params)
  generated_params = base_code.gen_rand_parameters(container_idx, num_params)
  
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



def create_exact_parameters(container_idx, depth, is_lastt, num_params):
  yaml_extra = ""
  english_extra = ""
  generated_params = base_code.gen_rand_parameters(container_idx, num_params)
  
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

def generate_global_parameter():
  will_create = random.randint(0, 1)
  yaml_extra = ""
  english_extra = ""
  if int(will_create) == 1:
    param = base_code.gen_rand_parameter(int(base_code.container_dict["CanNmGlobalConfig"]))
    english_extra += "In the CanNmGlobalConfig container, set the value of " + param[0] + " to " + param[1] + ". "
    yaml_extra += base_code.bslashts(2) + "- Parameter:\\n"
    yaml_extra += base_code.bslashts(3) + "Name: " + param[0] + "\\n"
    yaml_extra += base_code.bslashts(3) + "Value: " + param[1] + "\\n"
    english_extra += "Also," # to prepare for the creation of CanNmChannelConfig containers
  else:
    english_extra += "In the CanNmGlobalConfig container," # to prepare for the creation of CanNmChannelConfig containers
  return [yaml_extra, english_extra]


def create_exact_parameters_parenthesis(container_idx, depth, num_params):
  yaml_extra = ""
  english_extra = ""
  generated_params = base_code.gen_rand_parameters(container_idx, num_params)
  
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

def create_random_parameters_parenthesis(container_idx, depth, num_params):
  yaml_extra = ""
  english_extra = ""
  num = random.randint(1, num_params)
  generated_params = base_code.gen_rand_parameters(container_idx, num)
  
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

def create_random_parameters_no_parenthesis(container_idx, depth, num_params):
  yaml_extra = ""
  english_extra = ""
  num = random.randint(1, num_params)
  generated_params = base_code.gen_rand_parameters(container_idx, num)
  
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