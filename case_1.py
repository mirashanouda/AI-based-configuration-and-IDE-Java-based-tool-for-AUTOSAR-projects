import xml.etree.ElementTree as ET
from lxml import etree
import random
import et_init
import tagfile
import parameter
import container
import items
import parsing
import base_code
  

# This part is going to be a little bit hard-coded, but it's okay
yaml = ""
english = ""

with open("semifinal_dataset.jsonl", "a") as file:
  num_files = 1000 # number of input/output pairs to generate
  max_rand = 45 # maximum number of subcontainers inside containers
  max_containers = 1000 # maximum number of containers
   
  for _ in range(num_files):
    # Beginning a file:
    beginning = "{\"text\": \"<s>[INST] "
    
    english = ""
    english += "Create a CanNm module with a GlobalConfig container. "
    yaml = "[/INST] "
    yaml += "\\nModule:"
    yaml += "\\n\\tName: CanNm"
    yaml += "\\n\\tContainer:\\n"
    yaml += base_code.bslashts(2) + "Name: CanNmGlobalConfig\\n"
    
    will_create = random.randint(0, 1)
    if int(will_create) == 1:
      param = base_code.gen_rand_parameter(int(base_code.container_dict["CanNmGlobalConfig"]))
      english += "In the CanNmGlobalConfig container, set the value of " + param[0] + " to " + param[1] + ". "
      yaml += base_code.bslashts(2) + "Parameter:\\n"
      yaml += base_code.bslashts(3) + "Name: " + param[0] + "\\n"
      yaml += base_code.bslashts(3) + "Value: " + param[1] + "\\n"
      english += "Also, " # to prepare for the creation of CanNmChannelConfig containers
    else:
      english += "In the CanNmGlobalConfig container, " # to prepare for the creation of CanNmChannelConfig containers    

    
    # --------------------------------------------------------------
    def create_subcontainer(container_idx, depth):
      global yaml
      global english
      yaml += base_code.bslashts(depth) + "Container:\\n"
      yaml += base_code.bslashts(depth + 1) + "Name: " + parsing.container_def[container_idx].name + "\\n"
      num = random.randint(1, max_rand)
      english += "create "
      english += str(num)
      english += " "
      english += parsing.container_def[container_idx].name
      english += " containers. "
      yaml += base_code.bslashts(depth + 1) + "Multiplicity: " + str(num) + "\\n"
      return
    # --------------------------------------------------------------
    

    # --------------------------------------------------------------
    def create_parameter(container_idx, depth, have_subcontainers):
      global yaml
      global english
      will_create = random.randint(0, 1)
      if int(will_create) == 1:
        param = base_code.gen_rand_parameter(container_idx)
        english += "In each, set the value of " + param[0] + " to " + param[1] + ". "
        yaml += base_code.bslashts(depth) + "Parameter:\\n"
        yaml += base_code.bslashts(depth + 1) + "Name: " + param[0] + "\\n"
        yaml += base_code.bslashts(depth + 1) + "Value: " + param[1] + "\\n"
        if have_subcontainers == 1:
          english += "Also, " # to prepare for the creation of CanNmChannelConfig containers
      else:
        if have_subcontainers == 1:
          english += "In the CanNmGlobalConfig container, " # to prepare for the creation of CanNmChannelConfig containers    
      return
    # --------------------------------------------------------------

    create_subcontainer(int(base_code.container_dict["CanNmChannelConfig"]), 2)
    
    create_parameter(int(base_code.container_dict["CanNmChannelConfig"]), 3, 1)
        
    create_subcontainer(int(base_code.container_dict["CanNmRxPdu"]), 3)
    
    create_parameter(int(base_code.container_dict["CanNmRxPdu"]), 4, 0)
       
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")