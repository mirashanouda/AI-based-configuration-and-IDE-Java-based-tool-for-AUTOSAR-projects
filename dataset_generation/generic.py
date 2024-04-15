import os
import sys

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

cwd = go_back(cwd)
cwd += '/dataset_generation'
sys.path.insert(0, cwd)
import base_code

yaml = ""
english = ""
dataset_name = "try_dataset.jsonl"

beginning_english = ""
beginning_yaml = ""
beginning = "{\"text\": \"<s> [INST] "

beginning_english += "Create a CanNm module with a GlobalConfig container. "
beginning_yaml += " [/INST] "
beginning_yaml += "\\nModule:"
beginning_yaml += "\\n\\tName: CanNm"
beginning_yaml += "\\n\\tContainer:\\n"
beginning_yaml += base_code.bslashts(2) + "- Name: CanNmGlobalConfig\\n"

files_multiplier = 1

# ------------------------------ Case 1 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 1000 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, 1, 1, 1)
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_exact_parameters(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 0, 1)
    yaml += listt[0]
    english += listt[1]
    
    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmRxPdu"]), 3, max_rand, 0, 1, 1)
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_exact_parameters(int(global_vars.container_dict["CanNmRxPdu"]), 4, 1, 1)
    yaml += listt[0]
    english += listt[1]
       
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# -------------------------------------------------------------------- 
    

# ------------------------------ Case 2 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 2000 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, 1, 1, 1)
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_random_parameters(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 0, 5)
    yaml += listt[0]
    english += listt[1]
    
    is_first = 0
    if len(listt[1]) == 0:
      is_first = 1

    list2 = base_code.create_random_parameters(int(global_vars.container_dict["CanNmRxPdu"]), 4, 1, 5)

    in_each = 1
    if len(list2[1]) == 0:
      in_each = 0
    
    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmRxPdu"]), 3, max_rand, is_first, 1, in_each)
    yaml += listt[0]
    english += listt[1]

    yaml += list2[0]
    english += list2[1]

    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# -------------------------------------------------------------------- 


# ------------------------------ Case 3 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 500 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    list2 = base_code.create_random_parameters(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 1, 1)

    in_each = 1
    if len(list2[1]) == 0:
      in_each = 0

    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, 1, 1, in_each)
    yaml += listt[0]
    english += listt[1]

    yaml += list2[0]
    english += list2[1]
       
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# -------------------------------------------------------------------- 
    


# ------------------------------ Case 4 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 500 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    list2 = base_code.create_random_parameters(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 1, 5)
    in_each = 1
    if len(list2[1]) == 0:
      in_each = 0

    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, 1, 1, in_each)
    yaml += listt[0]
    english += listt[1]

    yaml += list2[0]
    english += list2[1]

       
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# --------------------------------------------------------------------
    


# ------------------------------ Case 5 ------------------------------
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 500 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, 1, 1, 1)
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_random_parameters(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 0, 1)
    yaml += listt[0]
    english += listt[1]

    is_first = 0
    if len(listt[1]) == 0:
      is_first = 1

    list2 = base_code.create_random_parameters(int(global_vars.container_dict["CanNmTxPdu"]), 4, 1, 1)
    in_each = 1
    if len(list2[1]) == 0:
      in_each = 0

    listt = base_code.create_single_subcontainer(int(global_vars.container_dict["CanNmTxPdu"]), 3, is_first, 1, in_each)
    yaml += listt[0]
    english += listt[1]

    yaml += list2[0]
    english += list2[1]
    
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# --------------------------------------------------------------------


# ------------------------------ Case 6 ------------------------------
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 500 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, 1, 1, 1)
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_random_parameters(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 0, 1)
    yaml += listt[0]
    english += listt[1]

    is_first = 0
    if len(listt[1]) == 0:
      is_first = 1

    list2 = base_code.create_random_parameters(int(global_vars.container_dict["CanNmUserDataTxPdu"]), 4, 1, 1)
    in_each = 1
    if len(list2[1]) == 0:
      in_each = 0

    listt = base_code.create_single_subcontainer(int(global_vars.container_dict["CanNmUserDataTxPdu"]), 3, is_first, 1, in_each)
    yaml += listt[0]
    english += listt[1]

    yaml += list2[0]
    english += list2[1]
       
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# -------------------------------------------------------------------- 
    



# ------------------------------ Case 11 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 1000 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    list2 = base_code.create_random_parameters(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 1, 5)

    in_each = 1
    if len(list2[1]) == 0:
      in_each = 0

    listt = base_code.create_single_subcontainer(int(global_vars.container_dict["CanNmChannelConfig"]), 2, 1, 1, in_each)
    yaml += listt[0]
    english += listt[1]

    yaml += list2[0]
    english += list2[1]
       
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# --------------------------------------------------------------------


# ------------------------------ Case 8 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 1000 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, 1, 1, 1)
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_random_parameters(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 0, 1)
    yaml += listt[0]
    english += listt[1]
    
    is_first = 0
    if len(listt[1]) == 0:
      is_first = 1

    num_sets = random.randint(1, 4)
    counter = 0
    for _ in range(num_sets):
      if counter > 0:
        is_first = 0
      listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmRxPdu"]), 3, max_rand, is_first, 0, 0)
      yaml += listt[0]
      english += listt[1]
       
      listt = base_code.create_exact_parameters_parenthesis(int(global_vars.container_dict["CanNmRxPdu"]), 4, 1)
      yaml += listt[0]
      english += listt[1]
      counter += 1

    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# --------------------------------------------------------------------


# ------------------------------ Case 12 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 2000 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    num_setss = random.randint(1, 2)

    for i in range(num_setss):
      is_first = 1
      if i > 0:
        is_first = 0
      is_last = 0
      if i == num_setss - 1:
        is_last = 1
      
      listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, is_first, is_last, 0)
      yaml += listt[0]
      english += listt[1]

      english += "("

      listt = base_code.create_random_parameters_no_parenthesis(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 2)
      yaml += listt[0]
      english += listt[1]
      
      num_sets = random.randint(1, 4)
      for i in range(num_sets):
        is_first = 1
        if i != 0 or len(listt[1]) != 0:
          is_first = 0

        listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmRxPdu"]), 3, max_rand, is_first, 0, 0)
        yaml += listt[0]
        english += listt[1]
          
        listt = base_code.create_random_parameters_parenthesis(int(global_vars.container_dict["CanNmRxPdu"]), 4, 5)
        yaml += listt[0]
        english += listt[1]

    english += ")"

    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# -------------------------------------------------------------------- 
    

# ------------------------------ Case 9 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 1000 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]
    
    num_sets = random.randint(1, 4)
    for i in range(num_sets):
      is_first = 0
      if i == 0:
        is_first = 1
      
      listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmChannelConfig"]), 2, max_rand, is_first, 0, 0)
      yaml += listt[0]
      english += listt[1]

      
      listt = base_code.create_exact_parameters_parenthesis(int(global_vars.container_dict["CanNmChannelConfig"]), 3, 1)
      yaml += listt[0]
      english += listt[1]

    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# --------------------------------------------------------------------
    



# ------------------------------ Case 10 ------------------------------ 
with open(dataset_name, "a") as file:
  num_files = files_multiplier * 2000 # number of input/output pairs to generate
  max_rand = 25 # maximum number of subcontainers inside containers
  for _ in range(num_files):

    english = ""
    yaml = ""

    english += beginning_english
    yaml += beginning_yaml

    listt = base_code.generate_global_parameter()
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmPnInfo"]), 2, max_rand, 1, 1, 1)
    yaml += listt[0]
    english += listt[1]

    listt = base_code.create_random_parameters(int(global_vars.container_dict["CanNmPnInfo"]), 3, 0, 1)
    yaml += listt[0]
    english += listt[1]
    
    is_first = 0
    if len(listt[1]) == 0:
      is_first = 1

    num_sets = random.randint(1, 4)
    counter = 0
    for _ in range(num_sets):
      if counter > 0:
        is_first = 0
      listt = base_code.create_subcontainers(int(global_vars.container_dict["CanNmPnFilterMaskByte"]), 3, max_rand, is_first, 0, 0)
      yaml += listt[0]
      english += listt[1]
       
      listt = base_code.create_exact_parameters_parenthesis(int(global_vars.container_dict["CanNmPnFilterMaskByte"]), 4, 1)
      yaml += listt[0]
      english += listt[1]
      counter += 1

    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")
# --------------------------------------------------------------------
