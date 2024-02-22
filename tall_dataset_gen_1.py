import xml.etree.ElementTree as ET
from lxml import etree
import random
import et_init
import tagfile
import parameter
import container
import items
import parsing

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
  

class Set:
  def __init__(self):
    self.num_containers = 1
    self.children = [] # children sets
    self.parse_index = 0
    self.parameters = [] # param items in the set, according to the parsing, but the values changed
    self.par_set = -1


with open("thousand_dataset.jsonl", "a") as file:
  num_files = 1000 # number of input/output pairs to generate
  max_rand = 45 # maximum number of subcontainers inside containers
  max_containers = 1000 # maximum number of containers
   
  for _ in range(num_files):
    beginning = "{\"text\": \"<s>[INST] "
    
    english = ""
    english += "Create a CanNm module with a CanNmGlobalConfig container. In the CanNmGlobalConfig container create "
    yaml = "[/INST] "
    yaml += "\\nModule:\\n\\tName: CanNm\\n\\tContainer:\\n"
    yaml += "\\t\\tName: CanNmGlobalConfig\\n\\t\\tContainer:\\n"
    
    num = random.randint(1, max_rand)
    english += str(num)
    
    file.write(" CanNmChannelConfig containers. In each of these containers, ")
    
    param_name = ""
    param_value = ""
    
    selected = int(random.randint(0, len(parsing.container_parameters[1]) - 1))
    iter = 0
    for param in parsing.container_parameters[1]:
      if str(selected) != str(iter):
        iter += 1
        continue
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
      
      file.write("set the value of " + str(param.def_name) + " to " + str(res_value) + ". [/INST] ")
      param_name = str(param.def_name)
      param_value = str(res_value)
      break
    
    # file.write("\\t\\tName: CanNmGlobalConfig\\n\\t\\tContainer:\\n")
    # file.write("\\t\\t\\tName: CanNmChannelConfig\\n")
    # file.write("\\t\\t\\tMultiplicity: " + str(num) + "\\n")
    # file.write("\\t\\t\\tParameter:\\n")
    # file.write("\\t\\t\\t\\tName: " + str(param_name) + "\\n")
    # file.write("\\t\\t\\t\\tValue: " + str(param_value) + "\\n")
    
    file.write(beginning)
    file.write(english)
    file.write(yaml)
    file.write("</s>\"}\n")