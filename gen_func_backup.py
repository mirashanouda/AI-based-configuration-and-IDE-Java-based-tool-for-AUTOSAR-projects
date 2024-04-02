import xml.etree.ElementTree as ET
from lxml import etree
import random
import items
import parsing
import base_code


def create_subcontainers(container_idx, depth, max_rand):
  yaml_extra = ""
  english_extra = ""
  yaml_extra += base_code.bslashts(depth) + "- Container:\\n"
  yaml_extra += base_code.bslashts(depth + 1) + "- Name: " + parsing.container_def[container_idx].name + "\\n"
  num = random.randint(1, max_rand)
  english_extra += "create "
  english_extra += str(num)
  english_extra += " "
  english_extra += parsing.container_def[container_idx].name
  english_extra += " containers. "
  yaml_extra += base_code.bslashts(depth + 1) + "- Multiplicity: " + str(num) + "\\n"
  return [yaml_extra, english_extra]



def create_single_subcontainer(container_idx, depth):
    yaml_extra = ""
    english_extra = ""
    yaml_extra += base_code.bslashts(depth) + "- Container:\\n"
    yaml_extra += base_code.bslashts(depth + 1) + "- Name: " + parsing.container_def[container_idx].name + "\\n"
    yaml_extra += base_code.bslashts(depth + 1) + "- Multiplicity: 1\\n"
    english_extra += "create a "
    english_extra += parsing.container_def[container_idx].name
    english_extra += " container. "
    return [yaml_extra, english_extra]




def write_parameter(depth, gen_param, is_first):
  yaml_extra = ""
  english_extra = ""
  if is_first:
    english_extra += "In each, set the value of " + gen_param[0] + " to " + gen_param[1] + ". "
  else:
    english_extra += "Also, set the value of " + gen_param[0] + " to " + gen_param[1] + ". "
  
  yaml_extra += base_code.bslashts(depth) + "- Parameter:\\n"
  yaml_extra += base_code.bslashts(depth + 1) + "Name: " + gen_param[0] + "\\n"
  yaml_extra += base_code.bslashts(depth + 1) + "Value: " + gen_param[1] + "\\n"
  return [yaml_extra, english_extra]



def create_random_parameters(container_idx, depth, have_subcontainers, max_params):
  yaml_extra = ""
  english_extra = ""
  num_params = random.randint(0, max_params)
  generated_params = base_code.gen_rand_parameters(container_idx, num_params)
  counter = 2
  for gen_param in generated_params:
    counter = max(counter - 1, 0)
    listt = write_parameter(depth, gen_param, counter)
    yaml_extra += listt[0]
    english_extra += listt[1]
  
  if num_params == 0:
    if have_subcontainers == 1:
      english_extra += "In the "
      english_extra += str(parsing.container_def[container_idx].name)
      english_extra += " container, "
  else:
    if have_subcontainers == 1:
      english_extra += "Also, "
  return [yaml_extra, english_extra]



def create_exact_parameters(container_idx, depth, have_subcontainers, num_params):
  yaml_extra = ""
  english_extra = ""
  generated_params = base_code.gen_rand_parameters(container_idx, num_params)
  counter = 2
  for gen_param in generated_params:
    counter = max(counter - 1, 0)
    listt = write_parameter(depth, gen_param, counter)
    yaml_extra += listt[0]
    english_extra += listt[1]
  
  if num_params == 0:
    if have_subcontainers == 1:
      english_extra += "In the "
      english_extra += str(parsing.container_def[container_idx].name)
      english_extra += " container, "
  else:
    if have_subcontainers == 1:
      english_extra += "Also, "
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
    english_extra += "Also, " # to prepare for the creation of CanNmChannelConfig containers
  else:
    english_extra += "In the CanNmGlobalConfig container, " # to prepare for the creation of CanNmChannelConfig containers
  return [yaml_extra, english_extra]