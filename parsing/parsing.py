import sys
import os
  
def go_back(directory):
	return os.path.abspath(os.path.join(directory, '..'))
  
from lxml import etree
cwd = os.getcwd()
cwd = go_back(cwd)
cwd += '/global_dependencies'
sys.path.insert(0, cwd)
import items

max_nodes = 100000
container_def = []
par = [-1] * max_nodes
children = [[] for _ in range(max_nodes)]
container_parameters = [[] for _ in range(max_nodes)]

cwd = go_back(cwd)
cwd += '/parsing'
sys.path.insert(0, cwd)
file_path = 'CanNM_BSWMD.arxml'

tree = etree.parse(file_path)
root = tree.getroot()

def process_container(ecuc_container):
  name = ecuc_container.find('./SHORT-NAME').text
  LM = ecuc_container.find('./LOWER-MULTIPLICITY').text
  UM = ecuc_container.find('./UPPER-MULTIPLICITY-INFINITE')
  if UM is not None:
    UM = float('inf')
  else:
    UM = ecuc_container.find('./UPPER-MULTIPLICITY').text
  UUID = ecuc_container.get('UUID')
  c = items.container_item(name, UUID, LM, UM)
  return c

def process_parameter(ecuc_parameter, typ):
  def_name = ecuc_parameter.find('./SHORT-NAME').text
  data_type = typ
  decl_name = 'TEXTUAL' if typ == 'ENUMERATION' else 'NUMERICAL'

  is_default = ecuc_parameter.find('./DEFAULT-VALUE')
  if is_default is not None:
    is_default = 1
    value = ecuc_parameter.find('./DEFAULT-VALUE').text
  else:
    is_default = 0
    value = -1

  if data_type == 'INTEGER':
    start_range = ecuc_parameter.find('./MIN').text
    end_range = ecuc_parameter.find('./MAX').text
  elif data_type == 'FLOAT':
    start_range = ecuc_parameter.find('./MIN')
    if start_range is not None:
      start_range = start_range.text
    else:
      start_range = -1
    end_range = ecuc_parameter.find('./MAX')
    if end_range is not None:
      end_range = end_range.text
    else:
      start_range = -1
  else:
    start_range = -1
    end_range = -1

  LM = ecuc_parameter.find('./LOWER-MULTIPLICITY').text
  UM = ecuc_parameter.find('./UPPER-MULTIPLICITY-INFINITE')
  if UM is not None:
    UM = float('inf')
  else:
    UM = ecuc_parameter.find('./UPPER-MULTIPLICITY').text
  #print(def_name, value, data_type, decl_name, is_default, start_range, end_range, LM, UM)
  p = items.parameter_item(def_name, value, data_type, decl_name, is_default, start_range, end_range, LM, UM)
  return p

def get_parameters(params, idx):
  for param in params.findall('./ECUC-INTEGER-PARAM-DEF'):
    p = process_parameter(param, 'INTEGER')
    container_parameters[idx].append(p)
  for param in params.findall('./ECUC-FLOAT-PARAM-DEF'):
    p = process_parameter(param, 'FLOAT')
    container_parameters[idx].append(p)
  for param in params.findall('./ECUC-BOOLEAN-PARAM-DEF'):
    p = process_parameter(param, 'BOOLEAN')
    container_parameters[idx].append(p)
  for param in params.findall('./ECUC-ENUMERATION-PARAM-DEF'):
    p = process_parameter(param, 'ENUMERATION')
    container_parameters[idx].append(p)

def dfs (ecuc_container, level, p):
  for ecuc_container in ecuc_container.findall('./ECUC-PARAM-CONF-CONTAINER-DEF'):
    c = process_container(ecuc_container)
    container_def.append(c)
    idx = len(container_def) - 1
    par[idx] = p
    for params in ecuc_container.findall('./PARAMETERS'):
      get_parameters(params, idx)
    for sub_cont in ecuc_container.findall('./SUB-CONTAINERS'):
      dfs(sub_cont, level + 1, idx)

containers = root.findall('./CONTAINERS')[0]
dfs(containers, 0, -1)

container_dict = {} # container_dict[container_name] = container_index
for i in range(7):
  container_dict.update({str(container_def[i].name): i})
